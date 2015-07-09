/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.service;

import edu.dartmouth.AstroSkyCalc;
import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.model.BaseLine;
import fr.jmmc.aspro.model.Beam;
import fr.jmmc.aspro.model.Range;
import fr.jmmc.aspro.model.observability.ObservabilityData;
import fr.jmmc.aspro.model.observability.StarData;
import fr.jmmc.aspro.model.oi.FocalInstrumentMode;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.aspro.model.uvcoverage.UVBaseLineData;
import fr.jmmc.aspro.model.uvcoverage.UVCoverageData;
import fr.jmmc.aspro.model.uvcoverage.UVRangeBaseLineData;
import fr.jmmc.aspro.service.UserModelService.MathMode;
import fr.jmmc.aspro.util.AngleUtils;
import fr.jmmc.aspro.util.TestUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is dedicated to compute the UV tracks for a given target
 * @author bourgesl
 */
public final class UVCoverageService {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(UVCoverageService.class.getName());
    /** flag to slow down the service to detect concurrency problems */
    private final static boolean DEBUG_SLOW_SERVICE = false;
    /** safety limit for the number of sampled HA points = 500 */
    public static final int MAX_HA_POINTS = 500;
    /** 1s precision in HA */
    public static final double HA_PRECISION = 1d / 3600d;

    /* members */

    /* output */
    /** uv coverage data */
    private final UVCoverageData data;

    /* inputs */
    /** observation settings used  (read-only copy of the modifiable observation) */
    private final ObservationSetting observation;
    /** computed Observability Data (read-only) */
    private final ObservabilityData obsData;
    /** target to use */
    private final String targetName;
    /** maximum U or V coordinate in rad-1 (corrected by the minimal wavelength) */
    private double uvMax;
    /** flag to compute the UV support */
    private final boolean doUVSupport;
    /** true to use instrument bias; false to compute only theoretical error */
    private final boolean useInstrumentBias;
    /** flag to add gaussian noise to OIFits data */
    private final boolean doDataNoise;
    /** OIFits supersampling preference */
    private final int supersamplingOIFits;
    /** OIFits MathMode preference */
    private final MathMode mathModeOIFits;
    /** cosinus wrapper used by FastMath.sinAndCos() */
    private final DoubleWrapper cw = new DoubleWrapper();
    /* internal */
    /** Get the current thread to check if the computation is interrupted */
    private final Thread currentThread = Thread.currentThread();
    /** hour angle step (see samplingPeriod) */
    private double haStep;
    /** observation time expressed in hour angle (see acquisitionTime) */
    private double haObsTime;
    /** lower wavelength of the selected instrument (meter) */
    private double instrumentMinWaveLength;
    /** minimal wavelength of the selected instrument mode (meter) */
    private double lambdaMin;
    /** central wavelength of the selected instrument mode (meter) */
    private double lambda;
    /** maximal wavelength of the selected instrument mode (meter) */
    private double lambdaMax;
    /** number of spectral channels (used by OIFits) */
    private int nSpectralChannels;

    /* reused observability data */
    /** sky calc instance */
    private AstroSkyCalc sc = null;
    /** beam list */
    private List<Beam> beams = null;
    /** base line list */
    private List<BaseLine> baseLines = null;
    /** star data */
    private StarData starData = null;

    /**
     * Constructor.
     * Note : This service is statefull so it can not be reused by several calls.
     *
     * @param observation observation settings
     * @param obsData computed observability data
     * @param targetName target name
     * @param uvMax U-V max in meter
     * @param doUVSupport flag to compute the UV support
     * @param useInstrumentBias true to use instrument bias; false to compute only theoretical error
     * @param doDataNoise enable data noise
     * @param supersamplingOIFits OIFits supersampling preference
     * @param mathModeOIFits OIFits MathMode preference
     */
    public UVCoverageService(final ObservationSetting observation, final ObservabilityData obsData, final String targetName,
                             final double uvMax, final boolean doUVSupport, final boolean useInstrumentBias, final boolean doDataNoise,
                             final int supersamplingOIFits, final MathMode mathModeOIFits) {

        this.observation = observation;
        this.obsData = obsData;
        this.targetName = targetName;
        this.uvMax = uvMax;
        this.doUVSupport = doUVSupport;
        this.useInstrumentBias = useInstrumentBias;
        this.doDataNoise = doDataNoise;
        this.supersamplingOIFits = supersamplingOIFits;
        this.mathModeOIFits = mathModeOIFits;

        // create the uv coverage data corresponding to the observation version :
        this.data = new UVCoverageData(observation.getVersion());
    }

    /**
     * Main operation to compute the UV tracks for a given target
     *
     * @return UVCoverageData container
     */
    public UVCoverageData compute() {
        if (logger.isDebugEnabled()) {
            logger.debug("compute: {}", this.observation);
        }

        // Start the computations :
        final long start = System.nanoTime();

        // Get instrument and observability data :
        prepareObservation();

        if (this.starData != null) {
            // Note : for Baseline limits, the starData is null
            // (target observability is not available) :

            // target name :
            this.data.setTargetName(this.targetName);

            // central wave length :
            this.data.setLambda(this.lambda);

            // Is the target visible :
            if (this.starData.getHaElev() > 0d) {
                if (this.doUVSupport) {
                    computeUVSupport();
                }

                computeObservableUV();

                // fast interrupt :
                if (this.currentThread.isInterrupted()) {
                    return null;
                }

                // prepare OIFits computation :
                createOIFits();
            }

            // fast interrupt :
            if (this.currentThread.isInterrupted()) {
                return null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("UV coordinate maximum: {}", this.uvMax);
            }

            // uv Max = max base line / minimum wave length
            this.data.setUvMax(this.uvMax);

            // fast interrupt :
            if (this.currentThread.isInterrupted()) {
                return null;
            }

        } // starData is defined

        // fast interrupt :
        if (this.currentThread.isInterrupted()) {
            return null;
        }

        if (DEBUG_SLOW_SERVICE) {
            TestUtils.busyWait(2000l);

            if (this.currentThread.isInterrupted()) {
                return null;
            }
        }

        logger.info("compute : duration = {} ms.", 1e-6d * (System.nanoTime() - start));

        return this.data;
    }

    /**
     * Compute UV tracks using only rise/set intervals
     */
    private void computeUVSupport() {

        final double haElev = this.starData.getHaElev();

        // 1 minute is fine to get pretty ellipse :
        final double step = 1d / 60d;

        final int nPoints = (int) Math.round(2d * haElev / step) + 1;

        // precessed target declination in rad :
        final double precDEC = FastMath.toRadians(this.starData.getPrecDEC());

        // compute once cos/sin DEC:
        final double cosDec = FastMath.cos(precDEC);
        final double sinDec = FastMath.sin(precDEC);

        final double invLambda = 1d / this.lambda;

        final List<BaseLine> _baseLines = this.baseLines;
        final int sizeBL = _baseLines.size();
        final List<UVBaseLineData> targetUVRiseSet = new ArrayList<UVBaseLineData>(sizeBL);

        UVBaseLineData uvData;
        BaseLine baseLine;
        /* U,V coordinates corrected with central wavelength */
        double[] u;
        double[] v;

        final DoubleWrapper _cw = cw;
        double cosHa, sinHa;

        for (int i = 0, j; i < sizeBL; i++) {
            baseLine = _baseLines.get(i);

            uvData = new UVBaseLineData(baseLine.getName());

            u = new double[nPoints];
            v = new double[nPoints];

            j = 0;

            for (double ha = -haElev; ha <= haElev; ha += step) {

                sinHa = FastMath.sinAndCos(AngleUtils.hours2rad(ha), _cw); // cw holds cosine
                cosHa = _cw.value;

                // Baseline projected vector (m) :
                u[j] = CalcUVW.computeU(baseLine, cosHa, sinHa);
                v[j] = CalcUVW.computeV(cosDec, sinDec, baseLine, cosHa, sinHa);

                // wavelength correction :
                // Spatial frequency (xLambda) :
                u[j] *= invLambda;
                v[j] *= invLambda;

                j++;
            }

            uvData.setNPoints(j);
            uvData.setU(u);
            uvData.setV(v);

            targetUVRiseSet.add(uvData);

            // fast interrupt :
            if (this.currentThread.isInterrupted()) {
                return;
            }
        }

        this.data.setTargetUVRiseSet(targetUVRiseSet);
    }

    /**
     * Compute UV points (observable) inside HA min/max ranges
     */
    private void computeObservableUV() {

        final List<Range> obsRangesHA = this.starData.getObsRangesHA();

        if (logger.isDebugEnabled()) {
            logger.debug("obsRangesHA: {}", obsRangesHA);
        }

        if (obsRangesHA != null) {

            // use observable HA bounds:
            final Double haLower = Range.getMinimum(obsRangesHA);
            final Double haUpper = Range.getMaximum(obsRangesHA);

            if (haLower != null && haUpper != null) {

                final double haMin = haLower.doubleValue();
                final double haMax = haUpper.doubleValue();

                if (logger.isDebugEnabled()) {
                    logger.debug("HA min/Max: {} - {}", haMin, haMax);
                    logger.debug("HA ObsTime: {}", haObsTime);
                }

                final double precRA = this.starData.getPrecRA();

                final double step = this.haStep;

                // estimate the number of HA points :
                final int capacity = (int) Math.round((haMax - haMin) / step) + 1;

                // First pass : find observable HA values :
                // use safety limit to avoid out of memory errors :
                final int haLen = (capacity > MAX_HA_POINTS) ? MAX_HA_POINTS : capacity;
                final double[] haValues = new double[haLen];
                final Date[] dateValues = new Date[haLen];

                Range obsRange;
                int j = 0;
                for (double ha = haMin; ha <= haMax; ha += step) {

                    // check HA start:
                    if ((obsRange = Range.find(obsRangesHA, ha, HA_PRECISION)) != null) {
                        // check HA end:
                        if (obsRange.contains(ha + haObsTime, HA_PRECISION)) {
                            haValues[j] = ha;
                            dateValues[j] = this.sc.toDate(this.sc.convertHAToJD(ha, precRA), this.obsData.isUseLST()); // LST or GMT
                            j++;

                            // check safety limit :
                            if (j >= MAX_HA_POINTS) {
                                addWarning("Too many HA points (" + capacity + "), check your sampling periodicity. Only " + MAX_HA_POINTS + " samples computed");
                                break;
                            }
                        } else if (logger.isDebugEnabled()) {
                            logger.debug("Observation HA range [{}; {}]end exceed observable range: {}", ha, ha + haObsTime, obsRange);
                        }
                    }
                }

                // correct number of HA points :
                final int nPoints = j;

                // check if there is at least one observable HA :
                if (nPoints == 0) {
                    addWarning("Check your HA min/max settings. There is no observable HA");
                    return;
                }

                final double[] HA = new double[nPoints];
                System.arraycopy(haValues, 0, HA, 0, nPoints);

                this.data.setHA(HA);
                this.data.setDates(dateValues);

                // Second pass : extract UV values for HA points :
                // precessed target declination in rad :
                final double precDEC = FastMath.toRadians(this.starData.getPrecDEC());

                // compute once cos/sin DEC:
                final double cosDec = FastMath.cos(precDEC);
                final double sinDec = FastMath.sin(precDEC);

                final double invLambdaMin = 1d / this.lambdaMin;
                final double invLambdaMax = 1d / this.lambdaMax;

                final List<BaseLine> _baseLines = this.baseLines;
                final int sizeBL = _baseLines.size();
                final List<UVRangeBaseLineData> targetUVObservability = new ArrayList<UVRangeBaseLineData>(sizeBL);

                UVRangeBaseLineData uvData;
                BaseLine baseLine;

                /* pure U,V coordinates (m) */
                double[] u;
                double[] v;
                /* U,V coordinates corrected with minimal wavelength */
                double[] uWMin;
                double[] vWMin;
                /* U,V coordinates corrected with maximal wavelength */
                double[] uWMax;
                double[] vWMax;

                final DoubleWrapper _cw = cw;
                double cosHa, sinHa;

                for (int i = 0; i < sizeBL; i++) {
                    baseLine = _baseLines.get(i);

                    uvData = new UVRangeBaseLineData(baseLine);

                    u = new double[nPoints];
                    v = new double[nPoints];
                    uWMin = new double[nPoints];
                    vWMin = new double[nPoints];
                    uWMax = new double[nPoints];
                    vWMax = new double[nPoints];

                    for (j = 0; j < nPoints; j++) {

                        sinHa = FastMath.sinAndCos(AngleUtils.hours2rad(HA[j]), _cw); // cw holds cosine
                        cosHa = _cw.value;

                        // Baseline projected vector (m) :
                        u[j] = CalcUVW.computeU(baseLine, cosHa, sinHa);
                        v[j] = CalcUVW.computeV(cosDec, sinDec, baseLine, cosHa, sinHa);

                        // wavelength correction :
                        // Spatial frequency (rad-1) :
                        uWMin[j] = u[j] * invLambdaMin;
                        vWMin[j] = v[j] * invLambdaMin;

                        uWMax[j] = u[j] * invLambdaMax;
                        vWMax[j] = v[j] * invLambdaMax;
                    }

                    uvData.setNPoints(nPoints);
                    uvData.setU(u);
                    uvData.setV(v);
                    uvData.setUWMin(uWMin);
                    uvData.setVWMin(vWMin);
                    uvData.setUWMax(uWMax);
                    uvData.setVWMax(vWMax);

                    targetUVObservability.add(uvData);

                    // fast interrupt :
                    if (this.currentThread.isInterrupted()) {
                        return;
                    }
                }

                this.data.setTargetUVObservability(targetUVObservability);
            }
        }
    }

    /**
     * Define the baselines, star data and instrument mode's wavelengths
     * @throws IllegalStateException if the instrument mode is undefined
     */
    private void prepareObservation() throws IllegalStateException {
        // Get AstroSkyCalc instance :
        this.sc = this.obsData.getDateCalc();
        // Copy station names :
        this.data.setStationNames(this.obsData.getStationNames());
        // Get beams :
        this.beams = this.obsData.getBeams();
        // Get baselines :
        this.baseLines = this.obsData.getBaseLines();
        // Copy baseLines :
        this.data.setBaseLines(this.obsData.getBaseLines());

        // Get starData for the selected target name :
        this.starData = this.obsData.getStarData(this.targetName);

        if (logger.isDebugEnabled()) {
            logger.debug("starData: {}", this.starData);
        }

        // Get lower wavelength for the selected instrument:
        this.instrumentMinWaveLength = AsproConstants.MICRO_METER
                * this.observation.getInstrumentConfiguration().getInstrumentConfiguration().getFocalInstrument().getWaveLengthMin();

        final FocalInstrumentMode insMode = this.observation.getInstrumentConfiguration().getFocalInstrumentMode();
        if (insMode == null) {
            throw new IllegalStateException("The instrumentMode is empty !");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("instrumentMode: {}", insMode.getName());
        }

        // Get wavelength range for the selected instrument mode :
        this.lambdaMin = AsproConstants.MICRO_METER * insMode.getWaveLengthMin();
        this.lambdaMax = AsproConstants.MICRO_METER * insMode.getWaveLengthMax();
        this.lambda = AsproConstants.MICRO_METER * insMode.getWaveLength();

        // TODO: handle properly spectral channels (rebinning):
        // this.nSpectralChannels = insMode.getEffectiveNumberOfChannels();
        this.nSpectralChannels = insMode.getSpectralChannels();

        if (logger.isDebugEnabled()) {
            logger.debug("lambdaMin: {}", this.lambdaMin);
            logger.debug("lambda: {}", this.lambda);
            logger.debug("lambdaMax: {}", this.lambdaMax);
            logger.debug("nChannels: {}", this.nSpectralChannels);
        }

        // hour angle step in decimal hours :
        this.haStep = this.observation.getInstrumentConfiguration().getSamplingPeriod() / 60d;

        if (logger.isDebugEnabled()) {
            logger.debug("ha step: {}", this.haStep);
        }

        // get acquisition time to ensure sampled HA intervals [HA; HA+obsTime] is within observable range
        haObsTime = observation.getInstrumentConfiguration().getAcquisitionTime().doubleValue() / 3600d;

        // Adjust the user uv Max = max base line / lower wavelength of the selected instrument
        // note : use the lower wave length of the instrument to
        // - make all uv segment visible
        // - avoid to much model computations (when the instrument mode changes)
        this.uvMax /= this.instrumentMinWaveLength;

        if (logger.isDebugEnabled()) {
            logger.debug("Corrected uvMax: {}", this.uvMax);
        }
    }

    /**
     * Create the OIFits structure (array, target, wave lengths and visibilities)
     */
    private void createOIFits() {
        final List<UVRangeBaseLineData> targetUVObservability = this.data.getTargetUVObservability();

        if (targetUVObservability == null) {
            addWarning("OIFits data not available");
        } else {
            // thread safety : TODO: observation can change ... extract observation info in prepare ??

            // get current target :
            final Target target = this.observation.getTarget(this.targetName);

            if (target != null) {
                // Create the OIFitsCreatorService / NoiseService :

                // note: OIFitsCreatorService parameter dependencies:
                // observation {target, instrumentMode {lambdaMin, lambdaMax, nSpectralChannels}}
                // obsData {beams, baseLines, starData, sc (DateCalc)}
                // parameter: supersamplingOIFits, doDataNoise, useInstrumentBias
                // results: computeObservableUV {HA, targetUVObservability} {obsData + observation{haMin/haMax, instrumentMode {lambdaMin, lambdaMax}}}
                // and warning container
                final OIFitsCreatorService oiFitsCreator = new OIFitsCreatorService(this.observation, target,
                        this.beams, this.baseLines, this.lambdaMin, this.lambdaMax, this.nSpectralChannels,
                        this.useInstrumentBias, this.doDataNoise,
                        this.supersamplingOIFits, this.mathModeOIFits,
                        this.data.getHA(), targetUVObservability, this.starData.getPrecRA(), this.sc,
                        this.data.getWarningContainer());

                // TODO: create elsewhere the OIFitsCreatorService:
                this.data.setOiFitsCreator(oiFitsCreator);

                // get noise service to compute noise on model image (if enabled):
                this.data.setNoiseService(oiFitsCreator.getNoiseService());
            }
        }
    }

    /**
     * Add a warning message in the OIFits file
     * @param msg message to add
     */
    private void addWarning(final String msg) {
        this.data.getWarningContainer().addWarningMessage(msg);
    }

    /**
     * Compute UV points (observable) inside HA min/max ranges
     * @param observation observation settings
     * @param obsData computed observability data
     * @param starData star data
     * @param ha hour angle to compute UV points
     * @return list of uv point couples corresponding to the target observability
     * 
     * @throws IllegalStateException if the instrument mode is undefined
     */
    public static List<UVRangeBaseLineData> computeUVPoints(final ObservationSetting observation,
                                                            final ObservabilityData obsData, final StarData starData,
                                                            final double ha) throws IllegalStateException {

        // Compute UV points at given HA:
        final List<Range> obsRangesHA = starData.getObsRangesHA();

        if (logger.isDebugEnabled()) {
            logger.debug("obsRangesHA: {}", obsRangesHA);
        }

        final List<UVRangeBaseLineData> targetUVObservability;
        Range obsRange = null;

        if (obsRangesHA != null && (obsRange = Range.find(obsRangesHA, ha, HA_PRECISION)) != null) {

            // Prepare informations:
            // Get baselines :
            final List<BaseLine> baseLines = obsData.getBaseLines();

            final FocalInstrumentMode insMode = observation.getInstrumentConfiguration().getFocalInstrumentMode();
            if (insMode == null) {
                throw new IllegalStateException("The instrumentMode is empty !");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("instrumentMode: {}", insMode.getName());
            }

            // Get wavelength range for the selected instrument mode :
            final double lambdaMin = AsproConstants.MICRO_METER * insMode.getWaveLengthMin();
            final double lambdaMax = AsproConstants.MICRO_METER * insMode.getWaveLengthMax();

            if (logger.isDebugEnabled()) {
                logger.debug("lambdaMin: {}", lambdaMin);
                logger.debug("lambdaMax: {}", lambdaMax);
            }

            // extract UV values for HA point:
            // precessed target declination in rad :
            final double precDEC = FastMath.toRadians(starData.getPrecDEC());

            // compute once cos/sin DEC:
            final double cosDec = FastMath.cos(precDEC);
            final double sinDec = FastMath.sin(precDEC);

            final double invLambdaMin = 1d / lambdaMin;
            final double invLambdaMax = 1d / lambdaMax;

            final int sizeBL = baseLines.size();
            targetUVObservability = new ArrayList<UVRangeBaseLineData>(sizeBL);

            UVRangeBaseLineData uvData;
            BaseLine baseLine;

            /* pure U,V coordinates (m) */
            double[] u;
            double[] v;
            /* U,V coordinates corrected with minimal wavelength */
            double[] uWMin;
            double[] vWMin;
            /* U,V coordinates corrected with maximal wavelength */
            double[] uWMax;
            double[] vWMax;

            // compute once cos/sin HA:
            final double haRad = AngleUtils.hours2rad(ha);
            final double sinHa = FastMath.sin(haRad);
            final double cosHa = FastMath.cos(haRad);

            for (int i = 0; i < sizeBL; i++) {
                baseLine = baseLines.get(i);

                uvData = new UVRangeBaseLineData(baseLine);

                u = new double[1];
                v = new double[1];
                uWMin = new double[1];
                vWMin = new double[1];
                uWMax = new double[1];
                vWMax = new double[1];

                // Baseline projected vector (m) :
                u[0] = CalcUVW.computeU(baseLine, cosHa, sinHa);
                v[0] = CalcUVW.computeV(cosDec, sinDec, baseLine, cosHa, sinHa);

                // wavelength correction :
                // Spatial frequency (rad-1) :
                uWMin[0] = u[0] * invLambdaMin;
                vWMin[0] = v[0] * invLambdaMin;

                uWMax[0] = u[0] * invLambdaMax;
                vWMax[0] = v[0] * invLambdaMax;

                uvData.setNPoints(1);
                uvData.setU(u);
                uvData.setV(v);
                uvData.setUWMin(uWMin);
                uvData.setVWMin(vWMin);
                uvData.setUWMax(uWMax);
                uvData.setVWMax(vWMax);

                targetUVObservability.add(uvData);
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Observation HA [{}] out of observable range: {}", ha, obsRange);
            }
            targetUVObservability = null;
        }
        return targetUVObservability;
    }
}

/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.service;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.model.oi.AdaptiveOptics;
import fr.jmmc.aspro.model.oi.AtmosphereQuality;
import fr.jmmc.aspro.model.oi.FocalInstrument;
import fr.jmmc.aspro.model.oi.FringeTracker;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.model.oi.SpectralBand;
import fr.jmmc.aspro.model.oi.Station;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.aspro.model.oi.TargetConfiguration;
import fr.jmmc.aspro.model.oi.Telescope;
import fr.jmmc.aspro.model.WarningContainer;
import fr.jmmc.aspro.model.observability.TargetPointInfo;
import fr.jmmc.aspro.model.oi.AdaptiveOpticsSetup;
import fr.jmmc.aspro.model.oi.BiasType;
import fr.jmmc.aspro.model.oi.BiasUnit;
import fr.jmmc.aspro.model.oi.BiasValue;
import fr.jmmc.aspro.model.oi.FocalInstrumentMode;
import fr.jmmc.aspro.model.oi.FocalInstrumentSetup;
import fr.jmmc.aspro.model.oi.ObservationSequence;
import fr.jmmc.aspro.model.oi.SpectralSetup;
import fr.jmmc.aspro.model.oi.SpectralSetupColumn;
import fr.jmmc.aspro.model.oi.SpectralSetupQuantity;
import fr.jmmc.aspro.model.oi.TargetGroup;
import fr.jmmc.aspro.model.oi.TargetGroupMembers;
import fr.jmmc.aspro.model.oi.TargetInformation;
import fr.jmmc.aspro.model.oi.TargetUserInformations;
import fr.jmmc.aspro.model.util.AtmosphereQualityUtils;
import fr.jmmc.aspro.model.util.SpectralBandUtils;
import fr.jmmc.jmcs.util.StatUtils;
import fr.jmmc.jmal.Band;
import fr.jmmc.jmal.model.VisNoiseService;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.jafama.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the noise modelling of visibility data (error and noise)
 * from the current observation
 *
 * Note : this code is inspired by the Aspro/tasks/lib/noise_lib.f90
 *
 * @author bourgesl
 */
public final class NoiseService implements VisNoiseService {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(NoiseService.class.getName());

    /** enable bound checks on iPoint / iChannel */
    private final static boolean DO_CHECKS = false;

    /** enable tests on vis2 error */
    private final static boolean DO_DUMP_VIS2 = false;

    /** dump atmospheric transmission */
    private final static boolean DO_DUMP_ATM_TRANS = false;

    /** dump strehl values */
    private final static boolean DO_DUMP_STREHL = false;

    /** maximum error on Vis to avoid excessively large errors */
    private final static double MAX_ERR_V = 10.0;
    /** maximum error on Vis2 to avoid excessively large errors */
    private final static double MAX_ERR_V2 = MAX_ERR_V * MAX_ERR_V;

    /** enable bias handling (only for debugging / ETC tests) */
    private final static boolean USE_BIAS = true;

    /* members */
    /** instrument name */
    private String instrumentName = null;

    /* interferometer parameters */
    /** telescope in use */
    private Telescope telescope = null;
    /** Telescope Diameter (m) */
    private double telDiam = Double.NaN;
    /** Number of telescopes interfering as double */
    private double nbTel = Double.NaN;

    /* adaptive optics parameters */
    /** seeing (arc sec) */
    private double seeing = Double.NaN;
    /** coherence time (ms) */
    private double t0 = Double.NaN;
    /** AO setup */
    private AdaptiveOpticsSetup aoSetup = null;
    /** AO Usage limit (mag) */
    private double adaptiveOpticsLimit = Double.NaN;
    /** AO (multiplicative) Instrumental Visibility */
    private double aoInstrumentalVisibility = Double.NaN;

    /** AO band */
    private SpectralBand aoBand;

    /* instrument parameters */
 /* fixed values (spectrally independent) */
    /** Total Acquisition time per observed u,v point (s) */
    private double totalObsTime = Double.NaN;
    /** Detector individual integration time (s) */
    private double dit = Double.NaN;
    /** Detector readout noise */
    private double ron = Double.NaN;
    /** Detector is non-linear above (to avoid saturation/n-on-linearity) */
    private double detectorSaturation = Double.NaN;
    /** Detector quantum efficiency (optional ie 1.0 by default) */
    private double quantumEfficiency = Double.NaN;

    /** flag to use the photometry */
    private boolean usePhotometry = false;
    /** flag to use the strehl correction */
    private boolean useStrehlCorrection = true;

    /* varying values (spectrally dependent) */
    /** (W) instrument band */
    private Band[] insBand;

    /** fraction of flux going into the interferometric channel */
    private double fracFluxInInterferometry = Double.NaN;
    /** fraction of flux going into the photometric channel */
    private double fracFluxInPhotometry = Double.NaN;

    /** ratio photometry exposures per photometric channel (chopping) */
    private double ratioPhotoPerBeam = Double.NaN;
    /** ratio between photometry vs interferometry in time unit per beam  */
    private double ratioPhotoVsInterfero = Double.NaN;

    /** frame ratio (overhead) */
    private double frameRatio = 1.0;

    /** (W) Transmission of interferometer+instrument at observed wavelength (no strehl, no QE) */
    private double[] transmission = null;
    /** (W) Instrumental Visibility [0.0-1.0] */
    private double[] instrumentalVisibility = null;
    /** (W) Number of thermal photon (background) per beam per second (no strehl, no QE) */
    private double[] nbPhotThermal = null;
    /** (W) number of pixels to code all fringes together (interferometric channel) */
    private double[] nbPixInterf = null;
    /** (W) number of pixels to code each photometric channel */
    private double[] nbPixPhoto = null;

    /* instrument &nd calibration bias */
    /** true to use calibration bias; false to compute only theoretical (optional systematic) error */
    private final boolean useCalibrationBias;
    /** true to use instrument or calibration bias; false to compute only theoretical error */
    private boolean useBias;
    /** true to use random bias; false to disable random sampling, only adjust error */
    private boolean useRandomCalBias;
    /** flag to prepare instrumentalVisRelBias / instrumentalVis2RelBias using instrumentalPhotRelBias */
    private boolean prepareVisRelBias = false;
    /** (W) Typical Photometric Bias (relative) */
    private double[] instrumentalPhotRelBias = null;
    /** (W) Typical Vis Bias (relative) */
    private double[] instrumentalVisRelBias = null;
    /** (W) Typical Vis Calibration Bias (absolute) */
    private double[] instrumentalVisCalBias = null;
    /** (W) Typical Vis2 Bias (relative) */
    private double[] instrumentalVis2RelBias = null;
    /** (W) Typical Vis. Phase Bias (rad) */
    private double[] instrumentalVisPhaseBias = null;
    /** (W) Typical Vis. Phase Calibration Bias (rad) */
    private double[] instrumentalVisPhaseCalBias = null;
    /** (W) Typical Phase Closure Bias (rad) */
    private double[] instrumentalT3PhaseBias = null;
    /** (W) Typical Phase Closure Calibration Bias (rad) */
    private double[] instrumentalT3PhaseCalBias = null;

    /* instrument mode parameters */
    /** number of spectral channels */
    private final int nSpectralChannels;
    /** index of reference (central) spectral channel */
    private final int iRefChannel;
    /** spectral channel central wavelength (meters) */
    private final double[] waveLengths;
    /** spectral channel widths */
    private final double[] waveBands;

    /* fringe tracker parameters */
    /** Fringe Tracker is Present */
    private boolean fringeTrackerPresent = false;
    /** Fringe Tracking Mode i.e. not group tracking (FINITO) */
    private boolean fringeTrackingMode = false;
    /** Fringe Tracker (multiplicative) Instrumental Visibility */
    private double fringeTrackerInstrumentalVisibility = Double.NaN;
    /** Fringe Tracker Usage limit (mag) */
    private double fringeTrackerLimit = Double.NaN;
    /** Fringe Tracker Max Integration Time (s) */
    private double fringeTrackerMaxDit = Double.NaN;
    /** Fringe Tracker Max Frame Time (s) */
    private double fringeTrackerMaxFrameTime = Double.NaN;
    /** FT band */
    private SpectralBand ftBand;

    /* object parameters */
    /** (W) Magnitude in Observing Band (mag) */
    private double[] objectMag = null;
    /** Magnitude in Fringe Tracker's Band of FT ref star (mag) */
    private double fringeTrackerMag = Double.NaN;
    /** Magnitude in V Band (for AO performances / for strehl) (mag) */
    private double adaptiveOpticsMag = Double.NaN;
    /** target information for each uv point couples */
    private final TargetPointInfo[] targetPointInfos;
    /** number of uv points */
    private final int nPoints;
    /** index of central uv point */
    private final int iMidPoint;

    /* internal */
    /** container for warning messages */
    private final WarningContainer warningContainer;
    /* time formatter */
    private final DecimalFormat df = new DecimalFormat("##0.##");
    /** flag to indicate that a parameter is invalid in order the code to return errors as NaN values */
    private boolean invalidParameters = false;

    /** (W) total instrumental visibility (with FT if any) */
    private double[] vinst;

    /* cached intermediate constants */
    /** error correction = 1 / SQRT(total frame) */
    private double totFrameCorrection;
    /** error correction = 1 / SQRT(total frame photometry) */
    private double totFrameCorrectionPhot;
    /** noise computation parameters per uv point */
    private NoiseWParams[] params = null;
    /* varying values (spectrally dependent) */
    /** (W) number of thermal photons per telescope in the interferometric channel */
    private double[] nbPhotThermInterf = null;
    /** (W) number of thermal photons per telescope in each photometric channel */
    private double[] nbPhotThermPhoto = null;

    /**
     * Protected constructor
     * @param observation observation settings used (read-only copy of the modifiable observation)
     * @param target target to use
     * @param targetPointInfos target information for each uv point couples
     * @param useCalibrationBias true to use calibration bias; false to compute only theoretical (optional systematic) error
     * @param warningContainer container for warning messages
     * @param waveLengths concrete wavelength values (spectral channel central value) in meters
     * @param waveBands concrete spectral channel bandwidths in meters
     */
    protected NoiseService(final ObservationSetting observation,
                           final Target target,
                           final TargetPointInfo[] targetPointInfos,
                           final boolean useCalibrationBias,
                           final WarningContainer warningContainer,
                           final double[] waveLengths,
                           final double[] waveBands) {

        this.useCalibrationBias = USE_BIAS && useCalibrationBias;
        this.warningContainer = warningContainer;

        // Get spectral channels:
        this.nSpectralChannels = waveLengths.length;
        this.waveLengths = waveLengths;
        this.waveBands = waveBands;

        final FocalInstrumentMode insMode = observation.getInstrumentConfiguration().getFocalInstrumentMode();
        if (insMode == null) {
            throw new IllegalStateException("The instrumentMode is empty !");
        }

        // Fix mid channel for image noising (MATISSE LM has a large hole at 4 microns):
        int midChannel = this.nSpectralChannels / 2;
        if ((nSpectralChannels > 1) && (insMode.getWaveLengthRef() != null)) {
            final double lambdaRef = AsproConstants.MICRO_METER * insMode.getWaveLengthRef().doubleValue();

            for (int i = 0, end = waveLengths.length - 1; i < end; i++) {
                if (Math.abs(waveLengths[i] - lambdaRef) <= Math.abs(waveLengths[i + 1] - lambdaRef)) {
                    midChannel = i;
                    break;
                }
            }
        }
        this.iRefChannel = midChannel;

        this.targetPointInfos = targetPointInfos;
        this.nPoints = this.targetPointInfos.length;
        this.iMidPoint = this.nPoints / 2;

        if (logger.isDebugEnabled()) {
            logger.debug("spectralChannels              : {}", nSpectralChannels);
            logger.debug("iRefChannel                   : {}", iRefChannel);
            logger.debug("waveLength[iRefChannel]       : {}", waveLengths[iRefChannel]);
            logger.debug("waveLengths                   : {}", Arrays.toString(waveLengths));
            logger.debug("waveBands[iRefChannel]        : {}", waveBands[iRefChannel]);
            logger.debug("waveBands                     : {}", Arrays.toString(waveBands));
        }

        // extract parameters in observation and configuration :
        prepareInterferometer(observation, target);
        prepareInstrument(observation);
        prepareFringeTracker(observation, target);
        prepareTarget(observation, target);
        initParameters();
    }

    /**
     * Prepare interferometer and AO parameters (related to telescopes so to each configuration)
     * @param observation observation settings
     * @param target target to use
     */
    private void prepareInterferometer(final ObservationSetting observation, final Target target) {

        final List<Station> stations = observation.getInstrumentConfiguration().getStationList();

        this.nbTel = stations.size();

        // All telescopes have the same diameter for a given baseline :
        this.telescope = stations.get(0).getTelescope();

        this.telDiam = telescope.getDiameter();

        // AO handling
        AdaptiveOptics ao = null;

        final TargetConfiguration targetConf = target.getConfiguration();

        if (targetConf != null && targetConf.getAoSetup() != null) {
            this.aoSetup = telescope.findAOSetup(targetConf.getAoSetup());
            if (this.aoSetup == null) {
                // TODO: support multi-config VLTI UT and AT confs:
                logger.info("Invalid AO[{}] for telescope {}", targetConf.getAoSetup(), telescope.getName());
            } else {
                ao = this.aoSetup.getAdaptiveOptics();
            }
        }
        if (ao == null) {
            if (!telescope.getAdaptiveOptics().isEmpty()) {
                // use default AO for the telescope:
                ao = telescope.getAdaptiveOptics().get(0);
                if (!ao.getSetups().isEmpty()) {
                    this.aoSetup = ao.getSetups().get(0); // FIRST SETUP if present
                    logger.info("Using default AO setup[{}] for telescope {}", aoSetup.getName(), telescope.getName());
                }
            }
        }

        if (ao != null) {
            this.aoBand = ao.getBand();
            this.adaptiveOpticsLimit = (ao.getMagLimit() != null) ? ao.getMagLimit().doubleValue() : Double.NaN;
        } else {
            // by default: compute strehl ratio on V band with only 1 actuator ?
            this.aoBand = SpectralBand.V;
        }

        // Seeing :
        final AtmosphereQuality atmQual = observation.getWhen().getAtmosphereQuality();
        if (atmQual != null) {
            this.seeing = AtmosphereQualityUtils.getSeeing(atmQual);
            this.t0 = AtmosphereQualityUtils.getCoherenceTime(atmQual);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("nbTel                         : {}", nbTel);
            logger.debug("telDiam                       : {}", telDiam);
            logger.debug("aoBand                        : {}", aoBand);
            logger.debug("aoSetup                       : {}", aoSetup);
            logger.debug("seeing                        : {}", seeing);
            logger.debug("t0                            : {}", t0);
        }
    }

    /**
     * Prepare instrument and mode parameters
     * @param observation observation settings
     */
    private void prepareInstrument(final ObservationSetting observation) {

        if (observation.getInstrumentConfiguration().getAcquisitionTime() != null) {
            this.totalObsTime = observation.getInstrumentConfiguration().getAcquisitionTime().doubleValue();
        }

        final FocalInstrument instrument = observation.getInstrumentConfiguration().getInstrumentConfiguration().getFocalInstrument();

        final FocalInstrumentMode insMode = observation.getInstrumentConfiguration().getFocalInstrumentMode();
        final FocalInstrumentSetup insSetup = insMode.getSetupRef();

        // use alias or real instrument name:
        this.instrumentName = instrument.getAliasOrName();

        this.ron = insSetup.getRon();
        this.detectorSaturation = insSetup.getDetectorSaturation();
        this.quantumEfficiency = insSetup.getQuantumEfficiency();

        // DIT:
        this.dit = insSetup.getDit();
        if (insMode.getDit() != null) {
            this.dit = insMode.getDit();
        }

        // fractions:
        this.fracFluxInInterferometry = insSetup.getFracFluxInInterferometry();
        this.fracFluxInPhotometry = insSetup.getFracFluxInPhotometry();

        this.useStrehlCorrection = insSetup.isUseStrehlCorrection();

        // Use Sequence to get both time & beam ratios:
        final ObservationSequence sequence = insSetup.getSequence();

        final double ratioInterfero = sequence.getRatioInterferometry(); // [0..1]

        this.ratioPhotoPerBeam = sequence.getRatioPhotoPerBeam();
        this.ratioPhotoVsInterfero = sequence.getRatioPhotoVsInterfero();

        this.frameRatio = 1.0;
        if (insMode.getFrameTime() != null) {
            this.frameRatio = insMode.getFrameTime() / this.dit;
        }

        final double effectiveFrameTime = frameRatio * this.dit;
        // ratio in time [0..1]
        // note: ratio is not correct if FT is enabled !
        final double ratioTimeInterfero = ratioInterfero / frameRatio;

        if (logger.isDebugEnabled()) {
            logger.debug("ratioInterfero                : {}", ratioInterfero);
            logger.debug("ratioPhotoPerBeam             : {}", ratioPhotoPerBeam);
            logger.debug("frameRatio                    : {}", frameRatio);
            logger.debug("effectiveFrameTime            : {}", effectiveFrameTime);
            logger.debug("efficiency (%)                : {}", (100.0 * ratioTimeInterfero));
            logger.debug("totalObsTime                  : {}", totalObsTime);
            logger.debug("totalOBTime                   : {}", totalObsTime / ratioTimeInterfero);
        }

        if (ratioTimeInterfero < 0.99) {
            final double seqTimeMin = (totalObsTime / ratioTimeInterfero); // s

            addInformation("Min O.B. time: " + df.format(Math.round(seqTimeMin)) + " s ("
                    + df.format(Math.round(seqTimeMin / 60.0)) + " min) on acquisition"
                    + " - Ratio Interferometry: " + df.format(100.0 * ratioTimeInterfero) + " %");
        }

        final boolean insHasInstrumentBias = (!insSetup.getInstrumentBias().isEmpty());

        if (USE_BIAS && (this.useCalibrationBias || insHasInstrumentBias)) {
            this.useBias = true;
            this.useRandomCalBias = true; // default

            this.instrumentalPhotRelBias = new double[nSpectralChannels];
            this.instrumentalVisRelBias = new double[nSpectralChannels];
            this.instrumentalVisCalBias = new double[nSpectralChannels];
            this.instrumentalVis2RelBias = new double[nSpectralChannels];

            this.instrumentalVisPhaseBias = new double[nSpectralChannels];
            this.instrumentalVisPhaseCalBias = new double[nSpectralChannels];

            this.instrumentalT3PhaseBias = new double[nSpectralChannels];
            this.instrumentalT3PhaseCalBias = new double[nSpectralChannels];

            if (insHasInstrumentBias) {
                // New approach to only add calibration bias into error without random sampling:
                this.useRandomCalBias = false;

                // telescope is known:
                final Telescope tel = this.telescope;
                // AtmosphereQuality is known:
                final AtmosphereQuality atmQual = observation.getWhen().getAtmosphereQuality();

                // initialize vectors:
                for (int i = 0; i < nSpectralChannels; i++) {
                    // band: depends on spectral channel
                    final double lambda = this.waveLengths[i] / AsproConstants.MICRO_METER; // microns

                    // Band is known
                    final SpectralBand band = SpectralBandUtils.findBand(findBand(instrumentName, lambda));

                    for (final BiasType type : BiasType.values()) {
                        final BiasValue insBias = insSetup.getInstrumentBias(type, band, atmQual, tel);
                        // only if calibration bias enabled:
                        final BiasValue calBias = (this.useCalibrationBias) ? insSetup.getCalibrationBias(type, band, atmQual, tel) : null;

                        logger.debug("bias type: {} ins bias: {} cal bias: {}", type, insBias, calBias);

                        // note: calibration bias are always absolute (amp / phi)
                        // sum of variance:
                        switch (type) {
                            case VIS:
                                if (insBias != null) {
                                    if (insBias.getUnit() == BiasUnit.REL) {
                                        this.instrumentalVisRelBias[i] = insBias.getValue();
                                    } else {
                                        logger.warn("Absolute Vis instrumental bias not supported !");
                                    }
                                }
                                if (calBias != null) {
                                    if (calBias.getUnit() == BiasUnit.REL) {
                                        logger.warn("Relative Vis calibration bias not supported !");
                                    } else {
                                        this.instrumentalVisCalBias[i] = calBias.getValue();
                                    }
                                }
                                break;
                            case VISPHI:
                                if (insBias != null) {
                                    if (insBias.getUnit() == BiasUnit.REL) {
                                        logger.warn("Relative VisPhi instrumental bias not supported !");
                                    } else {
                                        this.instrumentalVisPhaseBias[i] = insBias.getValue();
                                    }
                                }
                                if (calBias != null) {
                                    if (calBias.getUnit() == BiasUnit.REL) {
                                        logger.warn("Relative VisPhi calibration bias not supported !");
                                    } else {
                                        this.instrumentalVisPhaseCalBias[i] = calBias.getValue();
                                    }
                                }
                                /* Convert Phase bias to radians */
                                this.instrumentalVisPhaseBias[i] = FastMath.toRadians(this.instrumentalVisPhaseBias[i]);
                                this.instrumentalVisPhaseCalBias[i] = FastMath.toRadians(this.instrumentalVisPhaseCalBias[i]);
                                break;
                            case T_3_PHI:
                                if (insBias != null) {
                                    if (insBias.getUnit() == BiasUnit.REL) {
                                        logger.warn("Relative T3Phi instrumental bias not supported !");
                                    } else {
                                        this.instrumentalT3PhaseBias[i] = insBias.getValue();
                                    }
                                }
                                if (calBias != null) {
                                    if (calBias.getUnit() == BiasUnit.REL) {
                                        logger.warn("Relative T3Phi calibration bias not supported !");
                                    } else {
                                        this.instrumentalT3PhaseCalBias[i] = calBias.getValue();
                                    }
                                }
                                /* Convert Phase bias to radians */
                                this.instrumentalT3PhaseBias[i] = FastMath.toRadians(this.instrumentalT3PhaseBias[i]);
                                this.instrumentalT3PhaseCalBias[i] = FastMath.toRadians(this.instrumentalT3PhaseCalBias[i]);
                                break;
                            case PHOT:
                                if (insBias != null) {
                                    if (insBias.getUnit() != BiasUnit.JY) {
                                        logger.warn("Only JY supported for Photometric instrumental bias !");
                                    } else {
                                        this.instrumentalPhotRelBias[i] = insBias.getValue(); // initial: e_phot_jy
                                    }
                                }
                                if (calBias != null) {
                                    logger.warn("Calibration bias not supported for Photometric bias !");
                                }
                                break;
                            default:
                        }
                    } // type
                } // channels

                // instrumentalPhotRelBias is incomplete (e_phot_jy only)
                // instrumentalVis2RelBias is undefined yet
                if (logger.isDebugEnabled()) {
                    logger.debug("instrumentalPhotRelBias:     {}", Arrays.toString(instrumentalPhotRelBias));     // e_phot_jy

                    logger.debug("instrumentalVisRelBias:      {}", Arrays.toString(instrumentalVisRelBias));      // rel
                    logger.debug("instrumentalVisCalBias:      {}", Arrays.toString(instrumentalVisCalBias));      // abs

                    logger.debug("instrumentalVisPhaseBias:    {}", Arrays.toString(instrumentalVisPhaseBias));    // rad
                    logger.debug("instrumentalVisPhaseCalBias: {}", Arrays.toString(instrumentalVisPhaseCalBias)); // rad

                    logger.debug("instrumentalT3PhaseBias:     {}", Arrays.toString(instrumentalT3PhaseBias));     // rad
                    logger.debug("instrumentalT3PhaseCalBias:  {}", Arrays.toString(instrumentalT3PhaseCalBias));  // rad
                }
                /* see next steps in prepareTarget() */
                this.prepareVisRelBias = true;

            } else {
                // former approach: absolute biases

                /* Get Vis bias (percents) */
                final double visBias = 0.01 * insSetup.getInstrumentVisibilityBias();
                Arrays.fill(this.instrumentalVisCalBias, visBias);

                /* Convert Phase bias to radians */
                final double phiBias = FastMath.toRadians(insSetup.getInstrumentPhaseBias());

                Arrays.fill(this.instrumentalVisPhaseCalBias, phiBias);
                Arrays.fill(this.instrumentalT3PhaseCalBias, phiBias);

                if (logger.isDebugEnabled()) {
                    logger.debug("instrumentalVisCalBias:      {}", Arrays.toString(instrumentalVisCalBias));    // abs
                    logger.debug("instrumentalVisPhaseCalBias: {}", Arrays.toString(instrumentalVisPhaseCalBias)); // rad
                    logger.debug("instrumentalT3PhaseBias:     {}", Arrays.toString(instrumentalT3PhaseBias)); // rad
                }
            }
        }
        logger.debug("useBias : {}", this.useBias);
        logger.debug("useRandomCalBias : {}", this.useRandomCalBias);

        // Check wavelength range:
        final double lambdaMin = this.waveLengths[0];
        final double lambdaMax = this.waveLengths[nSpectralChannels - 1];

        // TODO: fix message if FT enabled (no restriction ...)
        if (insMode.isWavelengthRangeRestriction()) {
            addWarning("Detector can not be read completely within 1 DIT: the wavelength range is restricted to "
                    + df.format(insMode.getWaveLengthBandRef()) + " " + SpecialChars.UNIT_MICRO_METER);
        }

        final SpectralSetup table = insMode.getTable();
        int firstIdx = -1;
        int lastIdx = -1;

        if (table != null) {
            // check WLen range:
            final double[] lambda = table.getAndScaleColumn(SpectralSetupQuantity.LAMBDA, AsproConstants.MICRO_METER);
            if (lambda == null) {
                throw new IllegalStateException("Missing lambda column within spectral table !");
            }

            // TODO: fix check ranges
            for (int i = 0; i < lambda.length; i++) {
                if (Math.abs(lambda[i] - lambdaMin) < 1e-15) {
                    firstIdx = i;
                    break;
                }
            }

            for (int i = lambda.length - 1; i >= 0; i--) {
                if (Math.abs(lambda[i] - lambdaMax) < 1e-15) {
                    lastIdx = i + 1; // exclusive
                    break;
                }
            }

            if (firstIdx == -1 || lastIdx == -1) {
                throw new IllegalStateException("Invalid range within spectral table !");
            }
            // check the number of channels is consistent:
            if ((lastIdx - firstIdx) != nSpectralChannels) {
                throw new IllegalStateException("Inconsistent channel count: " + (lastIdx - firstIdx) + " expected : " + nSpectralChannels + " !");
            }

            // Get table data:
            SpectralSetupColumn col;
            // transmission:
            col = table.getColumn(SpectralSetupQuantity.TRANSMISSION, telescope);
            if (col == null) {
                col = table.getColumn(SpectralSetupQuantity.TRANSMISSION);
            }
            if (col != null) {
                this.transmission = Arrays.copyOfRange(col.getValues(), firstIdx, lastIdx);
            }
            // visibility:
            col = table.getColumn(SpectralSetupQuantity.VISIBILITY, telescope);
            if (col == null) {
                col = table.getColumn(SpectralSetupQuantity.VISIBILITY);
            }
            if (col != null) {
                this.instrumentalVisibility = Arrays.copyOfRange(col.getValues(), firstIdx, lastIdx);
            }

            // nb photon thermal per beam per second (background):
            col = table.getColumn(SpectralSetupQuantity.NB_PHOTON_THERMAL, telescope);
            if (col == null) {
                col = table.getColumn(SpectralSetupQuantity.NB_PHOTON_THERMAL);
            }
            if (col != null) {
                this.nbPhotThermal = Arrays.copyOfRange(col.getValues(), firstIdx, lastIdx);
            }

            // number of pixel per channel:
            col = table.getColumn(SpectralSetupQuantity.NB_PIX_INTERF);
            if (col != null) {
                this.nbPixInterf = Arrays.copyOfRange(col.getValues(), firstIdx, lastIdx);
            }
            col = table.getColumn(SpectralSetupQuantity.NB_PIX_PHOTO);
            if (col != null) {
                this.nbPixPhoto = Arrays.copyOfRange(col.getValues(), firstIdx, lastIdx);
            }
        }

        if (this.transmission == null) {
            this.transmission = new double[nSpectralChannels];
            Arrays.fill(this.transmission, insSetup.getTransmission());
        }
        if (this.instrumentalVisibility == null) {
            this.instrumentalVisibility = new double[nSpectralChannels];
            Arrays.fill(this.instrumentalVisibility, insSetup.getInstrumentVisibility());
        }
        if (this.nbPixInterf == null) {
            this.nbPixInterf = new double[nSpectralChannels];
            Arrays.fill(this.nbPixInterf, insSetup.getNbPixInterferometry());
        }
        if (this.nbPixPhoto == null) {
            this.nbPixPhoto = new double[nSpectralChannels];
            Arrays.fill(this.nbPixPhoto, insSetup.getNbPixPhotometry());
        }

        this.usePhotometry = ((fracFluxInPhotometry > 0.0) && (StatUtils.max(nbPixPhoto) > 0.0));

        if (logger.isDebugEnabled()) {
            logger.debug("instrumentName                : {}", instrumentName);
            logger.debug("instrumentSetup               : {}", insSetup.getName());
            logger.debug("totalObsTime                  : {}", totalObsTime);
            logger.debug("dit                           : {}", dit);
            logger.debug("ron                           : {}", ron);
            logger.debug("detectorSaturation            : {}", detectorSaturation);
            logger.debug("quantumEfficiency             : {}", quantumEfficiency);
            logger.debug("fracFluxInInterferometry      : {}", fracFluxInInterferometry);
            logger.debug("fracFluxInPhotometry          : {}", fracFluxInPhotometry);
            logger.debug("usePhotometry                 : {}", usePhotometry);
            logger.debug("useStrehlCorrection           : {}", useStrehlCorrection);
            logger.debug("transmission[iRefChannel]     : {}", transmission[iRefChannel]);
            logger.debug("transmission                  : {}", Arrays.toString(transmission));
            logger.debug("instrumentalVis[iRefChannel]  : {}", instrumentalVisibility[iRefChannel]);
            logger.debug("instrumentalVisibility        : {}", Arrays.toString(instrumentalVisibility));
            if (nbPhotThermal != null) {
                logger.debug("nbPhotThermal[iRefChannel]    : {}", nbPhotThermal[iRefChannel]);
                logger.debug("nbPhotThermal                 : {}", Arrays.toString(nbPhotThermal));
            }
            logger.debug("nbPixInterf[iRefChannel]      : {}", nbPixInterf[iRefChannel]);
            logger.debug("nbPixInterf                   : {}", Arrays.toString(nbPixInterf));
            logger.debug("nbPixPhoto[iRefChannel]       : {}", nbPixPhoto[iRefChannel]);
            logger.debug("nbPixPhoto                    : {}", Arrays.toString(nbPixPhoto));
        }
    }

    /**
     * Prepare fringe tracker parameters
     * @param observation observation settings
     * @param target target to use
     */
    private void prepareFringeTracker(final ObservationSetting observation, final Target target) {
        final TargetConfiguration targetConf = target.getConfiguration();

        if (targetConf != null && targetConf.getFringeTrackerMode() != null) {
            final FocalInstrument instrument = observation.getInstrumentConfiguration().getInstrumentConfiguration().getFocalInstrument();

            final FocalInstrumentMode insMode = observation.getInstrumentConfiguration().getFocalInstrumentMode();

            final FringeTracker ft = instrument.getFringeTracker();
            if (ft != null) {
                final String ftMode = targetConf.getFringeTrackerMode();
                this.fringeTrackerPresent = true;
                // TODO: handle FT modes properly: GroupTrack is hard coded !
                this.fringeTrackingMode = (ftMode != null && !ftMode.startsWith("GroupTrack"));
                this.fringeTrackerInstrumentalVisibility = ft.getInstrumentVisibility();
                this.fringeTrackerLimit = ft.getMagLimit();
                this.fringeTrackerMaxDit = ft.getMaxIntegration();
                this.fringeTrackerMaxFrameTime = this.fringeTrackerMaxDit;
                this.ftBand = ft.getBand();

                // use specific FT DIT defined for this instrument mode:
                if (insMode.getFtDit() != null) {
                    this.fringeTrackerMaxDit = insMode.getFtDit();
                }
                if (insMode.getFtFrameTime() != null) {
                    this.fringeTrackerMaxFrameTime = insMode.getFtFrameTime();
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("fringeTrackerPresent          : {}", fringeTrackerPresent);
        }
        if (fringeTrackerPresent) {
            if (logger.isDebugEnabled()) {
                logger.debug("fringeTrackingMode            : {}", fringeTrackingMode);
                logger.debug("fringeTrackerInstrumentalVisibility : {}", fringeTrackerInstrumentalVisibility);
                logger.debug("fringeTrackerLimit            : {}", fringeTrackerLimit);
                logger.debug("fringeTrackerMaxDit           : {}", fringeTrackerMaxDit);
                logger.debug("fringeTrackerMaxFrameTime     : {}", fringeTrackerMaxFrameTime);
                logger.debug("ftBand                        : {}", ftBand);
            }
        }
    }

    /**
     * Prepare object parameters
     * @param observation observation settings
     * @param target target to use
     */
    private void prepareTarget(final ObservationSetting observation, final Target target) {
        Double flux;

        // Get band from wavelength range:
        final double lambdaMin = waveLengths[0];
        final double lambdaMax = waveLengths[nSpectralChannels - 1];

        // use band range to cover lambdaMin / lambdaMax:
        // JHK or LM or BVR
        final Band bandMin = findBand(instrumentName, lambdaMin / AsproConstants.MICRO_METER); // microns
        final Band bandMax = findBand(instrumentName, lambdaMax / AsproConstants.MICRO_METER); // microns

        if (logger.isDebugEnabled()) {
            logger.debug("lambdaMin                     : {}", lambdaMin);
            logger.debug("bandMin                       : {}", bandMin);
            logger.debug("lambdaMax                     : {}", lambdaMax);
            logger.debug("bandMax                       : {}", bandMax);
        }

        this.insBand = new Band[nSpectralChannels];
        this.objectMag = new double[nSpectralChannels];

        // For science target only:
        final HashSet<SpectralBand> missingMags = new HashSet<SpectralBand>();

        if (bandMin == bandMax) {
            // same band
            Arrays.fill(insBand, bandMin);

            /** instrument band corresponding to target mags */
            final SpectralBand insTargetBand = SpectralBandUtils.findBand(bandMin);

            if (logger.isDebugEnabled()) {
                logger.debug("insTargetBand                 : {}", insTargetBand);
            }

            // If a flux / magnitude is missing => user message
            // and it is impossible to compute any error
            flux = target.getFlux(insTargetBand);

            if (flux == null) {
                missingMags.add(insTargetBand);
            }

            Arrays.fill(objectMag, (flux != null) ? flux.doubleValue() : Double.NaN);
            if (logger.isDebugEnabled()) {
                logger.debug("objectMag                     : {}", flux);
            }
        } else {
            final int nWLen = nSpectralChannels;

            for (int i = 0; i < nWLen; i++) {
                final Band band = findBand(instrumentName, waveLengths[i] / AsproConstants.MICRO_METER); // microns
                insBand[i] = band;

                /** instrument band corresponding to target mags */
                final SpectralBand insTargetBand = SpectralBandUtils.findBand(band);

                if (logger.isDebugEnabled()) {
                    logger.debug("insTargetBand[" + waveLengths[i] + "] : {}", insTargetBand);
                }

                // If a flux / magnitude is missing => user message
                // and it is impossible to compute any error
                flux = target.getFlux(insTargetBand);

                if (flux == null) {
                    missingMags.add(insTargetBand);
                    objectMag[i] = Double.NaN;
                } else {
                    objectMag[i] = flux.doubleValue();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("objectMag                     : {}", flux);
                }
            }
        }

        if (this.prepareVisRelBias) {
            // ObjectMag in spectral channels is known:

            // initialize vectors:
            for (int i = 0; i < nSpectralChannels; i++) {
                final double mag = objectMag[i];
                final double flux_density = insBand[i].magToJy(mag); // Jy

                // relative error on photometry:
                final double rel_e_phot = this.instrumentalPhotRelBias[i] / flux_density;

                // (dV2 / V2) = 2 (dv / v)
                final double rel_e_v2 = 2.0 * this.instrumentalVisRelBias[i];
                final double rel_v2_error = Math.sqrt(rel_e_v2 * rel_e_v2 + 2.0 * rel_e_phot * rel_e_phot); // VIS2 implies 2 photometries
                final double rel_e_v = rel_v2_error / 2.0; // 1/2 error

                if (false) {
                    System.out.println("" + mag + "\t" + flux_density
                            + "\t" + (100.0 * rel_e_phot) + "\t" + (100.0 * rel_v2_error)
                            + "\t" + (1.0 / rel_v2_error) + "\t" + (100.0 * rel_e_v) + "\t" + (1.0 / rel_e_v)
                    );
                }
                this.instrumentalPhotRelBias[i] = rel_e_phot;
                this.instrumentalVisRelBias[i] = rel_e_v;
                this.instrumentalVis2RelBias[i] = rel_v2_error;

            } // channels

            if (logger.isDebugEnabled()) {
                logger.debug("instrumentalPhotRelBias:  {}", Arrays.toString(instrumentalPhotRelBias));  // e_phot_jy
                logger.debug("instrumentalVisRelBias:   {}", Arrays.toString(instrumentalVisRelBias));   // rel
                logger.debug("instrumentalVis2RelBias:  {}", Arrays.toString(instrumentalVis2RelBias));   // rel
            }
        }

        Target ftTarget = null;
        Target aoTarget = null;

        final TargetUserInformations targetUserInfos = observation.getTargetUserInfos();

        if (targetUserInfos != null) {
            // Handle OB targets (AO / FT)
            final TargetInformation targetInfo = targetUserInfos.getOrCreateTargetInformation(target);

            // FT
            ftTarget = getFirstTargetForGroup(targetUserInfos, targetInfo, TargetGroup.GROUP_FT);
            // AO
            aoTarget = getFirstTargetForGroup(targetUserInfos, targetInfo, TargetGroup.GROUP_AO);
        }
        if (ftTarget == null) {
            ftTarget = target;
        }
        if (aoTarget == null) {
            aoTarget = target;
        }

        logger.debug("ftTarget                      : {}", ftTarget);
        logger.debug("aoTarget                      : {}", aoTarget);

        if (fringeTrackerPresent) {
            flux = ftTarget.getFlux(ftBand);

            if (flux == null) {
                if (ftTarget == target) {
                    missingMags.add(ftBand);
                } else {
                    addWarning(AsproConstants.WARN_MISSING_MAGS + " on FT target [" + ftTarget.getName() + "] "
                            + "in band " + ftBand);
                }
                fringeTrackerMag = Double.NaN;
            } else {
                fringeTrackerMag = flux.doubleValue();
                if (ftTarget != target) {
                    addInformation("FT associated to target [" + ftTarget.getName() + "]: "
                            + df.format(fringeTrackerMag) + " mag");
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("fringeTrackerMag              : {}", fringeTrackerMag);
            }
        }

        SpectralBand fluxAOBand = aoBand;
        flux = aoTarget.getFlux(fluxAOBand);

        // handle special case for R band (NAOMI)
        if ((flux == null) && (fluxAOBand == SpectralBand.R)) {
            // use G then V (if no mag)
            fluxAOBand = SpectralBand.G;
            flux = aoTarget.getFlux(fluxAOBand);

            if (flux == null) {
                fluxAOBand = SpectralBand.V;
                flux = aoTarget.getFlux(fluxAOBand);
            }
        }

        if (flux == null) {
            if (aoTarget == target) {
                missingMags.add(aoBand);
            } else {
                addWarning(AsproConstants.WARN_MISSING_MAGS + " on AO target [" + aoTarget.getName() + "] "
                        + "in band " + aoBand);
            }
            adaptiveOpticsMag = Double.NaN;
        } else {
            adaptiveOpticsMag = flux.doubleValue();

            // check AO mag limits:
            if (!Double.isNaN(adaptiveOpticsLimit) && adaptiveOpticsMag > adaptiveOpticsLimit) {
                this.invalidParameters = true;
                addWarning("Observation can not use AO (magnitude limit = " + adaptiveOpticsLimit + ") in " + aoBand + " band");
            } else {
                if (this.aoSetup != null) {
                    addInformation("AO setup: " + aoSetup.getName() + " in " + aoBand + " band ("
                            + fluxAOBand + '=' + df.format(adaptiveOpticsMag) + " mag)");
                }
                if (aoTarget != target) {
                    addInformation("AO associated to target [" + aoTarget.getName() + "] ("
                            + fluxAOBand + '=' + df.format(adaptiveOpticsMag) + " mag)");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("fluxAOBand                    : {}", fluxAOBand);
            logger.debug("adaptiveOpticsMag             : {}", adaptiveOpticsMag);
        }

        if (!missingMags.isEmpty()) {
            // missing magnitude
            this.invalidParameters = true;

            final ArrayList<SpectralBand> mags = new ArrayList<SpectralBand>(missingMags);
            Collections.sort(mags);

            addWarning(AsproConstants.WARN_MISSING_MAGS + " on target [" + target.getName() + "] "
                    + "in following bands: " + mags.toString());
        }
    }

    // TODO: refactor this method (same in ExportOBXml)
    private static Target getFirstTargetForGroup(final TargetUserInformations targetUserInfos,
                                                 final TargetInformation targetInfo,
                                                 final String groupId) {
        final TargetGroup g = targetUserInfos.getGroupById(groupId);
        if (g != null) {
            final TargetGroupMembers tgm = targetInfo.getGroupMembers(g);
            if (tgm != null && !tgm.isEmpty()) {
                return tgm.getTargets().get(0);
            }
        }
        return null;
    }

    /**
     * Initialise other parameters
     */
    void initParameters() {
        // fast return if invalid configuration :
        if (invalidParameters) {
            return;
        }

        final int nObs = nPoints; // = targetPointInfos.length

        // Pass 1: find maximum flux per channel taking into account the target elevation:
        // strehl is spectrally dependent:
        final double[][] strehlPerChannel;

        if (useStrehlCorrection) {
            strehlPerChannel = new double[nObs][];

            if (this.instrumentName.startsWith(AsproConstants.INS_SPICA)) {
                // SPICA case (waiting for CHARA AO):
                final double strehl;
                if (seeing <= 0.7) {
                    strehl = 0.25;
                } else if (seeing <= 1.0) {
                    strehl = 0.15;
                } else {
                    strehl = 0.1;
                }
                addInformation("Strehl (SPICA): " + NumberUtils.format(strehl));

                for (int n = 0; n < nObs; n++) {
                    strehlPerChannel[n] = new double[waveLengths.length];
                    Arrays.fill(strehlPerChannel[n], strehl);
                }
            } else {
                Band band = Band.V;
                int nbSubPupils = 1;
                double ao_td = 1.0;
                double ao_ron = 1.0;
                double ao_qe = 0.9;

                if (aoSetup != null) {
                    band = Band.valueOf(aoBand.name());
                    nbSubPupils = aoSetup.getNumberSubPupils();
                    ao_td = aoSetup.getDit();
                    ao_ron = aoSetup.getRon();
                    ao_qe = aoSetup.getQuantumEfficiency();

                    if (aoSetup.getTransmission() != null) {
                        ao_qe *= aoSetup.getTransmission();
                        aoInstrumentalVisibility = 1.0 - aoSetup.getTransmission();
                        if (logger.isDebugEnabled()) {
                            logger.debug("aoInstrumentalVisibility      : {}", aoInstrumentalVisibility);
                        }
                    }
                }

                for (int n = 0; n < nObs; n++) {
                    final double elevation = targetPointInfos[n].getElevation();

                    strehlPerChannel[n] = Band.strehl(band, adaptiveOpticsMag, waveLengths, telDiam, seeing,
                            nbSubPupils, ao_td, t0, ao_qe, ao_ron, elevation);

                    if (logger.isDebugEnabled()) {
                        logger.debug("elevation                     : {}", elevation);
                        logger.debug("strehlPerChannel[iRefChannel] : {}", strehlPerChannel[n][iRefChannel]);
                        logger.debug("strehlPerChannel              : {}", Arrays.toString(strehlPerChannel[n]));
                    }

                    if (DO_DUMP_STREHL) {
                        System.out.println("strehl table for elevation=" + elevation + " seeing=" + seeing);
                        System.out.println("channel\twaveLength\tstrehl");
                        for (int i = 0; i < waveLengths.length; i++) {
                            System.out.println(i + "\t" + waveLengths[i] + "\t" + strehlPerChannel[n][i]);
                        }
                    }
                }
            }
        } else {
            strehlPerChannel = null;
        }

        final int nWLen = nSpectralChannels;

        // total number of thermal photons per spectral channel:
        final double[] nbTotalPhotTherm;

        if (this.nbPhotThermal == null) {
            nbTotalPhotTherm = null;
        } else {
            nbTotalPhotTherm = new double[nWLen];

            for (int i = 0; i < nWLen; i++) {
                // Per beam Per second:
                nbTotalPhotTherm[i] = this.nbPhotThermal[i] * quantumEfficiency;
            }
        }

        // Target flux per spectral channel per second per m^2:
        final double[] fluxSrcPerChannel = computeTargetFlux();

        // total number of science photons per spectral channel per second per telescope:
        final double[][] nbTotalPhot = new double[nObs][nWLen];

        final double telSurface = Math.PI * FastMath.pow2(0.5 * telDiam);

        // maximum number of (science + thermal) photons per pixel in the interferometric channel and per second:
        double maxNbAllPhotInterfPerPixPerSec = 0.0;
        double nbPhot;

        for (int n = 0; n < nObs; n++) {

            for (int i = 0; i < nWLen; i++) {
                // Per second:
                nbPhot = (telSurface * quantumEfficiency) * transmission[i] * fluxSrcPerChannel[i];

                if (strehlPerChannel != null) {
                    nbPhot *= strehlPerChannel[n][i];
                }

                nbTotalPhot[n][i] = nbPhot;

                if (nbTotalPhotTherm != null) {
                    // add thermal photons per sec:
                    nbPhot += nbTotalPhotTherm[i];
                }

                // Per pixel in the interferometric channel for the spectral channel:
                nbPhot /= nbPixInterf[i];

                if (nbPhot > maxNbAllPhotInterfPerPixPerSec) {
                    maxNbAllPhotInterfPerPixPerSec = nbPhot;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("elevation                     : {}", targetPointInfos[n].getElevation());
                logger.debug("nbTotalPhotPerSec[iRefChannel]: {}", nbTotalPhot[n][iRefChannel]);
                logger.debug("nbTotalPhotPerSec             : {}", Arrays.toString(nbTotalPhot[n]));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("maxNbAllPhotInterfPerPixPerSec: {}", maxNbAllPhotInterfPerPixPerSec);
        }

        // dit used by this observation:
        double obsDit = dit;

        // maximum number of expected photoevents in dit (for all telescopes) per pixel:
        final double maxTotalPhotPerPix = (nbTel * maxNbAllPhotInterfPerPixPerSec) * obsDit;

        // fraction of total interferometric flux in the peak pixel :
        final double peakFluxPix = fracFluxInInterferometry * maxTotalPhotPerPix;

        if (logger.isDebugEnabled()) {
            logger.debug("maxTotalPhotPerPix            : {}", maxTotalPhotPerPix);
            logger.debug("peakfluxPix                   : {}", peakFluxPix);
        }

        final int nbFrameToSaturation;
        if (detectorSaturation < peakFluxPix) {
            // the dit is too long
            obsDit *= detectorSaturation / peakFluxPix;

            addWarning("DIT too long (saturation). Adjusting it to (possibly impossible): " + formatTime(obsDit));

            nbFrameToSaturation = 1;
        } else {
            nbFrameToSaturation = (int) Math.floor(detectorSaturation / peakFluxPix);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("dit (no saturation)           : {}", obsDit);
            logger.debug("nbFrameToSaturation           : {}", nbFrameToSaturation);
        }

        // Adjust instrumental visibility:
        this.vinst = instrumentalVisibility;

        if (!Double.isNaN(aoInstrumentalVisibility)) {
            // correct instrumental visibility due to AO flux loss:
            // TODO: may be depend on the spectral band (H != K != N) ?
            for (int i = 0; i < this.vinst.length; i++) {
                this.vinst[i] *= aoInstrumentalVisibility;
            }
        }

        if (fringeTrackerPresent) {
            if ((fringeTrackerMag <= fringeTrackerLimit) && (nbFrameToSaturation > 1)) {
                // correct instrumental visibility due to FT flux loss:
                // TODO: may be depend on the spectral band (H != K != N) ?
                for (int i = 0; i < this.vinst.length; i++) {
                    this.vinst[i] *= fringeTrackerInstrumentalVisibility;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("vinst[iRefChannel]            : {}", vinst[iRefChannel]);
                    logger.debug("vinst (FT)                    : {}", Arrays.toString(vinst));
                }

                if (fringeTrackingMode) {
                    // FT is asked, can work, and is useful (need to integrate longer)
                    obsDit = Math.min(obsDit * nbFrameToSaturation, totalObsTime);
                    obsDit = Math.min(obsDit, fringeTrackerMaxDit);

                    // Fix frame ratio:
                    this.frameRatio = fringeTrackerMaxFrameTime / fringeTrackerMaxDit;

                    if (logger.isDebugEnabled()) {
                        logger.debug("dit (FT)                      : {}", obsDit);
                        logger.debug("frameRatio (FT)               : {}", frameRatio);
                    }

                    addInformation("Observation can take advantage of FT. Adjusting DIT to: " + formatTime(obsDit));
                } else {
                    addInformation("Observation can take advantage of FT (Group track). DIT set to: " + formatTime(obsDit));
                }
            } else {
                addWarning("Observation can not use FT (magnitude limit or saturation). DIT set to: " + formatTime(obsDit));

                if (logger.isDebugEnabled()) {
                    logger.debug("vinst[iRefChannel]              : {}", vinst[iRefChannel]);
                    logger.debug("vinst (noFT)                    : {}", Arrays.toString(vinst));
                }
            }
        } else {
            addInformation("Observation without FT. DIT set to: " + formatTime(obsDit));
        }

        // total number of frames:
        final double nbFrames = totalObsTime / obsDit;
        final double nbFramesWithOverheads = nbFrames / frameRatio;

        // total frame correction = 1 / SQRT(nFrames):
        this.totFrameCorrection = 1.0 / Math.sqrt(nbFramesWithOverheads);
        this.totFrameCorrectionPhot = 1.0 / Math.sqrt(nbFramesWithOverheads * ratioPhotoVsInterfero);

        if (logger.isDebugEnabled()) {
            logger.debug("nbFrames                      : {}", nbFrames);
            logger.debug("nbFramesWithOverheads         : {}", nbFramesWithOverheads);
            logger.debug("totFrameCorrection            : {}", totFrameCorrection);
            logger.debug("totFrameCorrectionPhot        : {}", totFrameCorrectionPhot);
        }

        // 2nd pass: obsDit is known = integration time (setup)
        this.nbPhotThermInterf = new double[nWLen];
        this.nbPhotThermPhoto = new double[nWLen];

        if (nbTotalPhotTherm != null) {
            for (int i = 0; i < nWLen; i++) {
                // corrected total number of thermal photons using the final observation dit per telescope:
                nbTotalPhotTherm[i] *= obsDit;
                nbPhot = nbTotalPhotTherm[i];

                // number of thermal photons in the interferometric channel (per telescope):
                nbPhotThermInterf[i] = nbPhot * fracFluxInInterferometry;
                // number of thermal photons in each photometric channel (photometric flux):
                nbPhotThermPhoto[i] = nbPhot * fracFluxInPhotometry;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("nbPhotThermInterf[iRefChannel]: {}", nbPhotThermInterf[iRefChannel]);
                logger.debug("nbPhotThermInterf         : {}", Arrays.toString(nbPhotThermInterf));
                logger.debug("nbPhotThermPhoto[iRefChannel]: {}", nbPhotThermPhoto[iRefChannel]);
                logger.debug("nbPhotThermPhoto          : {}", Arrays.toString(nbPhotThermPhoto));
            }
        }

        this.params = new NoiseWParams[nObs];

        for (int n = 0; n < nObs; n++) {

            // give back the two useful values for the noise estimate :
            final NoiseWParams param = new NoiseWParams(nWLen);
            this.params[n] = param;

            final double[] nbPhotInterf = param.nbPhotInterf;
            final double[] nbPhotPhoto = param.nbPhotPhoto;

            for (int i = 0; i < nWLen; i++) {
                // corrected total number of photons using the final observation dit per telescope:
                nbTotalPhot[n][i] *= obsDit;
                nbPhot = nbTotalPhot[n][i];

                // number of photons in the interferometric channel (per telescope):
                nbPhotInterf[i] = nbPhot * fracFluxInInterferometry;
                // number of photons in each photometric channel (photometric flux):
                nbPhotPhoto[i] = nbPhot * fracFluxInPhotometry;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("elevation                     : {}", targetPointInfos[n].getElevation());
                logger.debug("nbTotalPhot[iRefChannel]      : {}", nbTotalPhot[n][iRefChannel]);
                logger.debug("nbTotalPhot                   : {}", Arrays.toString(nbTotalPhot[n]));
                logger.debug("nbPhotonInI[iRefChannel]      : {}", nbPhotInterf[iRefChannel]);
                logger.debug("nbPhotonInI                   : {}", Arrays.toString(nbPhotInterf));
                logger.debug("nbPhotonInP[iRefChannel]      : {}", nbPhotPhoto[iRefChannel]);
                logger.debug("nbPhotonInP                   : {}", Arrays.toString(nbPhotPhoto));
            }

            // Prepare numeric constants for fast error computation:
            prepareVis2Error(n);
            prepareT3PhiError(n);
        }

        if (DO_DUMP_VIS2) {
            if (false && (nWLen < 50)) {
                for (int i = 0; i < nWLen; i++) {
                    dumpVis2Error(i);
                }
            } else {
                dumpVis2Error(0);
                dumpVis2Error(iRefChannel);
                dumpVis2Error(nSpectralChannels - 1);
            }
        }
    }

    private void dumpVis2Error(final int iChannel) {
        logger.info("channel: {} => {} microns", iChannel, waveLengths[iChannel]);
        dumpVis2ErrorSample(iChannel, 1d);
        dumpVis2ErrorSample(iChannel, 0.5d);
        dumpVis2ErrorSample(iChannel, 0.1d);
        dumpVis2ErrorSample(iChannel, 0.01d);
    }

    private void dumpVis2ErrorSample(final int iChannel, final double visAmp) {
        double v2 = visAmp * visAmp;
        double errV2 = computeVis2Error(iMidPoint, iChannel, v2, true);
        double snr = v2 / errV2;

        logger.info("computeVis2Error({}) :{} SNR= {} bias= {}", NumberUtils.trimTo5Digits(v2), errV2, NumberUtils.trimTo3Digits(snr));
    }

    double[] computeTargetFlux() {

        final double[] atmTrans = AtmosphereSpectrumService.getInstance().getTransmission(this.waveLengths, this.waveBands);

        if (logger.isDebugEnabled()) {
            logger.debug("atmTrans[iRefChannel]         : {}", atmTrans[iRefChannel]);
            logger.debug("atmTrans                      : {}", Arrays.toString(atmTrans));
        }

        if (DO_DUMP_ATM_TRANS) {
            System.out.println("AtmTransmission[" + instrumentName + "] [" + this.waveLengths[0] + " - "
                    + this.waveLengths[this.waveLengths.length - 1] + "]:");
            System.out.println("channel\twaveLength\tatm_trans");
            for (int i = 0; i < waveLengths.length; i++) {
                System.out.println(i + "\t" + waveLengths[i] + "\t" + atmTrans[i]);
            }
        }

        final int nWLen = nSpectralChannels;
        // nb photons per surface and per second:
        final double[] fluxSrcPerChannel = new double[nWLen];

        for (int i = 0; i < nWLen; i++) {
            // nb of photons m^-2.s^-1.m^-1 for an object at magnitude 0:
            // note: fzero depends on the spectral band:
            // consider flat profile:
            final double fzero = insBand[i].getNbPhotZero();

            // TODO: source flux may be spectrally dependent i.e. use 1 or several black body profiles ?
            // nb of photons m^-2.s^-1.m^-1 for the target object:
            final double fsrc = fzero * FastMath.pow(10d, -0.4d * objectMag[i]);

            if (logger.isDebugEnabled()) {
                logger.debug("insBand                       : {}", insBand[i]);
                logger.debug("objectMag                     : {}", objectMag[i]);
                logger.debug("fzero                         : {}", fzero);
                logger.debug("fsrc                          : {}", fsrc);
            }

            // nb of photons m^-2.s^-1 for the target object:
            fluxSrcPerChannel[i] = atmTrans[i] * fsrc * waveBands[i];
        }
        if (logger.isDebugEnabled()) {
            logger.debug("fluxSrcPerChannel[iRefChannel]: {}", fluxSrcPerChannel[iRefChannel]);
            logger.debug("fluxSrcPerChannel             : {}", Arrays.toString(fluxSrcPerChannel));
        }

        return fluxSrcPerChannel;
    }

    public boolean isUseBias() {
        return useBias;
    }

    public boolean isUseRandomCalBias() {
        return useRandomCalBias;
    }

    public int getIndexRefChannel() {
        return iRefChannel;
    }

    public int getIndexMidPoint() {
        return iMidPoint;
    }

    /**
     * Return the correlated flux weight of the object (without visibility). 
     * It returns NaN if the flux can not be computed
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @return correlated flux or NaN if the flux can not be computed
     */
    public double getCorrelatedFluxWeight(final int iPoint, final int iChannel) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }
        // correlated flux (include instrumental visibility loss) (1T):
        return this.params[iPoint].nbPhotInterf[iChannel] * vinst[iChannel];
    }

    /**
     * Return the number of photons in each photometric channel (photometric flux)
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @return number of photons in each photometric channel
     */
    public double getNbPhotPhoto(final int iPoint, final int iChannel) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }
        return this.params[iPoint].nbPhotPhoto[iChannel];
    }

    /**
     * Return the error on the number of photons in each photometric channel
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @return error on the number of photons in each photometric channel
     */
    public double getErrorPhotPhoto(final int iPoint, final int iChannel) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }
        return this.params[iPoint].errPhotPhoto[iChannel];
    }

    /**
     * Return the squared correlated flux
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @return squared correlated flux
     */
    public double getSqCorrFlux(final int iPoint, final int iChannel) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }
        return this.params[iPoint].sqCorrFlux[iChannel];
    }

    /**
     * Return the error on squared correlated flux
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @return error on squared correlated flux
     */
    public double getErrorSqCorrFlux(final int iPoint, final int iChannel) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }
        return this.params[iPoint].errSqCorrFlux[iChannel];
    }

    /**
     * Prepare numeric constants for square visibility error
     *
     * Note: this method is statefull and NOT thread safe
     *
     * @param iPoint index of the observable point
     */
    private void prepareVis2Error(final int iPoint) {
        final int nWLen = nSpectralChannels;

        final NoiseWParams param = this.params[iPoint];

        final double[] nbPhotInterf = param.nbPhotInterf;
        final double[] nbPhotPhoto = param.nbPhotPhoto;
        final double[] errPhotPhoto = param.errPhotPhoto;

        final double[] sqErrVis2Phot = param.sqErrVis2Phot;
        final double[] sqCorFluxCoef = param.sqCorFluxCoef;
        final double[] varSqCorFluxCoef = param.varSqCorFluxCoef;
        final double[] varSqCorFluxConst = param.varSqCorFluxConst;

        if (usePhotometry) {
            for (int i = 0; i < nWLen; i++) {
                // error on the photometric flux in photometric channel:
                errPhotPhoto[i] = Math.sqrt(nbPhotPhoto[i]
                        + ratioPhotoPerBeam * (nbPhotThermPhoto[i] + nbPixPhoto[i] * FastMath.pow2(ron)));

                // square error contribution of 2 photometric channels on the square visiblity FiFj:
                sqErrVis2Phot[i] = 2.0 * FastMath.pow2(errPhotPhoto[i] / nbPhotPhoto[i]);

                // repeat OBS measurements to reach totalObsTime minutes (corrected by photo / interfero ratio):
                errPhotPhoto[i] *= totFrameCorrectionPhot;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("sqErrVis2Phot[iRefChannel]    : {}", sqErrVis2Phot[iRefChannel]);
                logger.debug("sqErrVis2Phot                 : {}", Arrays.toString(sqErrVis2Phot));
            }
        } else {
            Arrays.fill(sqErrVis2Phot, 0.0);
        }

        for (int i = 0; i < nWLen; i++) {
            // squared correlated flux (include instrumental visibility loss) for vis2 = 1.0:
            sqCorFluxCoef[i] = FastMath.pow2(nbPhotInterf[i] * vinst[i]);

            // total number of photons for all telescopes:
            final double nbTotPhot = nbTel * (nbPhotInterf[i] + nbPhotThermInterf[i]);

            // variance of the squared correlated flux = sqCorFlux * coef + constant
            varSqCorFluxCoef[i] = 2.0 * (nbTotPhot + nbPixInterf[i] * FastMath.pow2(ron)) + 4.0;

            varSqCorFluxConst[i] = FastMath.pow2(nbTotPhot)
                    + nbTotPhot * (1.0 + 2.0 * nbPixInterf[i] * FastMath.pow2(ron))
                    + (3.0 + nbPixInterf[i]) * nbPixInterf[i] * FastMath.pow(ron, 4.0);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("elevation                     : {}", targetPointInfos[iPoint].getElevation());
            logger.debug("sqCorFluxCoef[iRefChannel]    : {}", sqCorFluxCoef[iRefChannel]);
            logger.debug("sqCorFluxCoef                 : {}", Arrays.toString(sqCorFluxCoef));
            logger.debug("varSqCorFluxCoef[iRefChannel] : {}", varSqCorFluxCoef[iRefChannel]);
            logger.debug("varSqCorFluxCoef              : {}", Arrays.toString(varSqCorFluxCoef));
            logger.debug("varSqCorFluxConst[iRefChannel]: {}", varSqCorFluxConst[iRefChannel]);
            logger.debug("varSqCorFluxConst             : {}", Arrays.toString(varSqCorFluxConst));
        }
    }

    /**
     * Compute error on square visibility. It returns NaN if the error can not be computed
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @param vis2 squared visibility
     * @param usePhot do use the photometric error
     * @return square visiblity error or NaN if the error can not be computed
     */
    public double computeVis2Error(final int iPoint, final int iChannel, final double vis2, final boolean usePhot) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }

        final NoiseWParams param = this.params[iPoint];

        // squared correlated flux (include instrumental visibility loss):
        final double sqCorFlux = param.sqCorFluxCoef[iChannel] * vis2;

        // variance of the squared correlated flux:
        // variance = sqCorFlux * coef + constant
        final double varSqCorFlux = sqCorFlux * param.varSqCorFluxCoef[iChannel] + param.varSqCorFluxConst[iChannel];

        // error of the squared correlated flux:
        param.sqCorrFlux[iChannel] = sqCorFlux;
        param.errSqCorrFlux[iChannel] = Math.sqrt(varSqCorFlux);

        // Uncertainty on square visibility per frame:
        double errVis2;
        if (usePhot && usePhotometry) {
            // repeat OBS measurements to reach totalObsTime minutes (corrected by photo / interfero ratio):
            errVis2 = vis2 * Math.sqrt(
                    (varSqCorFlux / FastMath.pow2(sqCorFlux)) * FastMath.pow2(totFrameCorrection)
                    + param.sqErrVis2Phot[iChannel] * FastMath.pow2(totFrameCorrectionPhot)
            );
        } else {
            // no photometry...
            errVis2 = vis2 * (param.errSqCorrFlux[iChannel] / param.sqCorrFlux[iChannel]);
            // repeat OBS measurements to reach totalObsTime minutes:
            errVis2 *= totFrameCorrection;
        }
        param.errSqCorrFlux[iChannel] *= totFrameCorrection;

        // Limit excessively large errors (very low transmission or strehl):
        errVis2 = Math.min(errVis2, MAX_ERR_V2);

        return errVis2;
    }

    private double computeVisErrorForMatisse(final int iPoint, final int iChannel, final double visAmp) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }

        final NoiseWParams param = this.params[iPoint];

        final double[] nbPhotInterf = param.nbPhotInterf;

        // MATISSE SNR(Fc) = (nI * Vinst * V) / SQRT( ntel * (nI + nth) + npixI * RON^2 )
        // correlated flux (include instrumental visibility loss) for vis = 1.0:
        final double corFlux = nbPhotInterf[iChannel] * vinst[iChannel] * visAmp;

        // total number of photons for all telescopes:
        final double nbTotPhot = nbTel * (nbPhotInterf[iChannel] + nbPhotThermInterf[iChannel]);

        // variance of the correlated flux:
        final double varCorFlux = /* 2.0 * */ (nbTotPhot + nbPixInterf[iChannel] * FastMath.pow2(ron)) + 4.0; // x 1 or x 2 ?

        // Uncertainty on visibility per frame:
        double errVis = Math.sqrt(varCorFlux) / corFlux;
        // repeat OBS measurements to reach totalObsTime minutes:
        errVis *= totFrameCorrection;

        // Limit excessively large errors (very low transmission or strehl):
        errVis = Math.min(errVis, MAX_ERR_V);

        return errVis;
    }

    /**
     * Prepare numeric constants for closure phase error
     *
     * Note: this method is statefull and NOT thread safe
     *
     * @param iPoint index of the observable point
     */
    private void prepareT3PhiError(final int iPoint) {
        final int nWLen = nSpectralChannels;

        final NoiseWParams param = this.params[iPoint];

        final double[] nbPhotInterf = param.nbPhotInterf;

        final double[] t3photCoef = param.t3photCoef;
        final double[] t3photCoef2 = param.t3photCoef2;
        final double[] t3photCoef3 = param.t3photCoef3;

        final double[] t3detConst = param.t3detConst;
        final double[] t3detCoef1 = param.t3detCoef1;
        final double[] t3detCoef2 = param.t3detCoef2;

        for (int i = 0; i < nWLen; i++) {
            final double invNbPhotonInIPerTel = 1.0 / nbPhotInterf[i];

            // photon noise on closure phase
            t3photCoef[i] = invNbPhotonInIPerTel;
            t3photCoef2[i] = FastMath.pow2(invNbPhotonInIPerTel);
            t3photCoef3[i] = FastMath.pow3(invNbPhotonInIPerTel);

            // detector noise on closure phase
            t3detConst[i] = FastMath.pow(invNbPhotonInIPerTel, 6d) * (FastMath.pow(nbPixInterf[i], 3d) * FastMath.pow(ron, 6d)
                    + 3 * FastMath.pow2(nbPixInterf[i]) * FastMath.pow(ron, 6d));

            t3detCoef1[i] = FastMath.pow(invNbPhotonInIPerTel, 4d) * (3d * nbPixInterf[i] * FastMath.pow(ron, 4d)
                    + FastMath.pow2(nbPixInterf[i]) * FastMath.pow(ron, 4d));

            t3detCoef2[i] = FastMath.pow2(invNbPhotonInIPerTel) * nbPixInterf[i] * FastMath.pow2(ron);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("elevation                     : {}", targetPointInfos[iPoint].getElevation());
            logger.debug("t3photCoef                    : {}", Arrays.toString(t3photCoef));
            logger.debug("t3photCoef2                   : {}", Arrays.toString(t3photCoef2));
            logger.debug("t3photCoef3                   : {}", Arrays.toString(t3photCoef3));
            logger.debug("t3detConst                    : {}", Arrays.toString(t3detConst));
            logger.debug("t3detCoef1                    : {}", Arrays.toString(t3detCoef1));
            logger.debug("t3detCoef2                    : {}", Arrays.toString(t3detCoef2));
        }
    }

    /**
     * Compute error on closure phase. It returns NaN if the error can not be computed
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @param visAmp12 visibility amplitude of baseline AB = 12
     * @param visAmp23 visibility amplitude of baseline BC = 23
     * @param visAmp31 visibility amplitude of baseline CA = 31
     * @return error on closure phase in radians or NaN if the error can not be computed
     */
    public double computeT3PhiError(final int iPoint, final int iChannel,
                                    final double visAmp12, final double visAmp23, final double visAmp31) {
        if (check(iPoint, iChannel)) {
            return Double.NaN;
        }

        final NoiseWParams param = this.params[iPoint];
        final double[] t3photCoef = param.t3photCoef;
        final double[] t3photCoef2 = param.t3photCoef2;
        final double[] t3photCoef3 = param.t3photCoef3;

        final double[] t3detConst = param.t3detConst;
        final double[] t3detCoef1 = param.t3detCoef1;
        final double[] t3detCoef2 = param.t3detCoef2;

        // include instrumental visib
        final double v1 = visAmp12 * vinst[iChannel];
        final double v2 = visAmp23 * vinst[iChannel];
        final double v3 = visAmp31 * vinst[iChannel];

        double v123 = v1 * v2 * v3;

        // protect zero divide: why 1e-3 ?
        v123 = Math.max(v123, 1e-3);

        final double v12 = v1 * v2;
        final double v13 = v1 * v3;
        final double v23 = v2 * v3;

        final double sv1 = v1 * v1;
        final double sv2 = v2 * v2;
        final double sv3 = v3 * v3;

        final double sv123 = v123 * v123;
        final double sv12 = v12 * v12;
        final double sv13 = v13 * v13;
        final double sv23 = v23 * v23;

        // photon noise on closure phase
        final double scpphot = (t3photCoef3[iChannel] * (nbTel * nbTel * nbTel - 2d * v123)
                + t3photCoef2[iChannel] * (nbTel * nbTel * (sv1 + sv2 + sv3) - (sv1 * sv1 + sv2 * sv2 + sv3 * sv3 + 2 * (sv12 + sv13 + sv23)))
                + t3photCoef[iChannel] * (nbTel * (sv12 + sv13 + sv23) - 2d * v123 * (sv1 + sv2 + sv3))) / (2d * sv123);
        /*
         final double scpphot = (Math.pow(nbTel / nbPhotonInI, 3d) * (nbTel * nbTel * nbTel - 2d * v123)
         + Math.pow(nbTel / nbPhotonInI, 2d) * (nbTel * nbTel * (sv1 + sv2 + sv3) - (sv1 * sv1 + sv2 * sv2 + sv3 * sv3 + 2 * (sv12 + sv13 + sv23)))
         + (nbTel / nbPhotonInI) * (nbTel * (sv12 + sv13 + sv23) - 2d * v123 * (sv1 + sv2 + sv3))) / (2d * sv123);
         */

        // detector noise on closure phase
        final double scpdet = (t3detConst[iChannel]
                + t3detCoef1[iChannel] * (sv1 + sv2 + sv3)
                + t3detCoef2[iChannel] * (sv12 + sv13 + sv23)) / (2d * sv123);
        /*
         double scpdet = (Math.pow(nbTel / nbPhotonInI, 6d) * (Math.pow(nbPixInterf, 3d) * Math.pow(ron, 6d) + 3 * Math.pow(nbPixInterf, 2d) * Math.pow(ron, 6d))
         + Math.pow(nbTel / nbPhotonInI, 4d) * (sv1 + sv2 + sv3) * (3d * nbPixInterf * Math.pow(ron, 4d) + Math.pow(nbPixInterf, 2d) * Math.pow(ron, 4d))
         + Math.pow(nbTel / nbPhotonInI, 2d) * nbPixInterf * Math.pow(ron, 2d) * (sv12 + sv13 + sv23) ) / (2d * sv123);
         */

        // total noise on closure phase per frame:
        double sclosph = Math.sqrt(scpphot + scpdet);

        // repeat OBS measurements to reach totalObsTime minutes
        sclosph *= totFrameCorrection;

        return sclosph;
    }

    /**
     * Return true if all parameters are valid i.e. returned errors are valid
     * @return true if all parameters are valid
     */
    public boolean isValid() {
        return !this.invalidParameters;
    }

    /**
     * Compute error on complex visibility derived from computeVis2Error(visAmp) by default.
     * It returns Double.NaN if the error can not be computed
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @param visAmp visibility amplitude
     * @param forAmplitude true to compute error for amplitudes (including the photometric error); false to compute error for phases
     * @return complex visiblity error or NaN if the error can not be computed
     */
    public double computeVisComplexErrorValue(final int iPoint, final int iChannel, final double visAmp, final boolean forAmplitude) {
        // visibility amplitude error (gaussian distribution):
        double visAmpErr = computeVisError(iPoint, iChannel, visAmp, forAmplitude);

        // Limit excessively large errors (very low transmission or strehl):
        visAmpErr = Math.min(visAmpErr, MAX_ERR_V);

        // Distribute the error on RE/IM parts for an uniform error distribution :
        // see These Martin Vannier (2003) p 76
        // sigma2(visRe) = 1/2 ( sigma2(visRe) + sigma2(visIm) ) = sigma2(vis) / 2
        // complex visibility error : visErrRe = visErrIm = visAmpErr / SQRT(2) :
        // No, circular normal distribution:
        // visErrRe = visErrIm = visAmpErr
        return visAmpErr;
    }

    /**
     * Compute error on visibility amplitude derived from computeVis2Error(visAmp)
     *
     * @param iPoint index of the observable point
     * @param iChannel index of the channel
     * @param visAmp visibility amplitude
     * @param forAmplitude true to compute error for amplitudes (including the photometric error); false to compute error for phases
     * @return visiblity error
     */
    private double computeVisError(final int iPoint, final int iChannel, final double visAmp, final boolean forAmplitude) {
        if (!forAmplitude) {
            if (AsproConstants.MATCHER_MATISSE.match(instrumentName)) {
                // special case for VISPHI / T3PHI and Correlated flux (VISDATA):
                return computeVisErrorForMatisse(iPoint, iChannel, visAmp);
            }
        }

        // vis2 error with or without photometric error:
        final double errV2 = computeVis2Error(iPoint, iChannel, visAmp * visAmp, forAmplitude); // for phases, do not use photometric error
        if (errV2 >= MAX_ERR_V2) {
            return MAX_ERR_V;
        }
        // dvis = d(vis2) / (2 * vis) :
        // in log scale: (dv / v) = (1/2) (dv2 / v2)
        return errV2 / (2d * visAmp);
    }

    public static double deriveVis2Error(final double cVisError, final double visAmp) {
        if (cVisError >= MAX_ERR_V) {
            return MAX_ERR_V2;
        }
        final double visAmpErr = cVisError; // visErrRe = visErrIm = visAmpErr

        final double errV2 = visAmpErr * (2d * visAmp);
        return errV2;
    }

    private boolean check(final int iPoint, final int iChannel) {
        if (DO_CHECKS) {
            // fast return NaN if invalid configuration :
            if (this.invalidParameters) {
                return true;
            }
            if (iPoint < 0 || iPoint >= nPoints) {
                logger.warn("invalid point index {}, expect [0 to {}]", iPoint, nPoints);
                return true;
            }
            if (iChannel < 0 || iChannel >= nSpectralChannels) {
                logger.warn("invalid channel index {}, expect [0 to {}]", iChannel, nSpectralChannels);
                return true;
            }
        }
        return false;
    }

    public double getVisAmpBias(final int iChannel, final double vamp) {
        return instrumentalVisRelBias[iChannel] * vamp;
    }

    public double getVisAmpCalBias(final int iChannel) {
        return instrumentalVisCalBias[iChannel];
    }

    public double getVisPhiBias(final int iChannel) {
        return instrumentalVisPhaseBias[iChannel]; // absolute in radians
    }

    public double getVisPhiCalBias(final int iChannel) {
        return instrumentalVisPhaseCalBias[iChannel]; // absolute in radians
    }

    public double getVis2Bias(final int iChannel, final double vis2) {
        return instrumentalVis2RelBias[iChannel] * vis2; // absolute
    }

    public double getVis2CalBias(final int iChannel, final double vis2) {
        // upper-limit for V = 1:
        // max [ d(V2) ] = max [ 2V d(V) ] = 2 d(V)
        return 2.0 * instrumentalVisCalBias[iChannel]; // absolute
    }

    public double getT3AmpBias(final int iChannel, final double t3amp) {
        return t3amp * getT3PhiBias(iChannel); // absolute
    }

    public double getT3AmpCalBias(final int iChannel, final double t3amp) {
        return t3amp * getT3PhiCalBias(iChannel); // absolute
    }

    public double getT3PhiBias(final int iChannel) {
        return instrumentalT3PhaseBias[iChannel]; // absolute in radians
    }

    public double getT3PhiCalBias(final int iChannel) {
        return instrumentalT3PhaseCalBias[iChannel]; // absolute in radians
    }

    /* --- VisNoiseService implementation --- */
    /**
     * Return true if this service is enabled
     * @return true if this service is enabled
     */
    @Override
    public boolean isEnabled() {
        return isValid();
    }

    /**
     * Compute error on complex visibility given its amplitude.
     * It returns Double.NaN if the error can not be computed
     *
     * @param visAmp visibility amplitude
     * @param forAmplitude true to compute error for amplitudes (including the photometric error); false to compute error for phases
     * @return complex visiblity error or NaN if the error can not be computed
     */
    @Override
    public double computeVisComplexErrorValue(final double visAmp, final boolean forAmplitude) {
        return computeVisComplexErrorValue(iMidPoint, iRefChannel, visAmp, forAmplitude);
    }

    /* --- utility methods --- */
    /**
     * Find the band corresponding to the given wavelength
     * but always use V band instead of I & R bands
     *
     * @param instrumentName instrument name
     * @param waveLength wave length in microns
     * @return corresponding band
     * @throws IllegalArgumentException if no band found
     */
    public static Band findBand(final String instrumentName, final double waveLength) throws IllegalArgumentException {
        // MATISSE: use specific bands:
        if (AsproConstants.MATCHER_MATISSE.match(instrumentName)) {
            // L <= 4.2
            if (waveLength <= 4.2) {
                return Band.L;
            }
            // N >= 7.0
            if (waveLength >= 7.0) {
                return Band.N;
            }
            // M ] 4.2 - 7.0 [
            return Band.M;
        }

        final Band band = Band.findBand(waveLength);
        // TODO: fix that logic to use all possible bands within the instrument bandwidth
        switch (band) {
            case U:
            // avoid 'band U not supported'
            case B:
            case V:
            case R:
            case I:
                // always use V for VEGA:
                return Band.V;
            case Q:
                // avoid 'band Q not supported'
                return Band.N;
            default:
                return band;
        }
    }

    /**
     * Add a warning message in the OIFits file
     * @param msg message to add
     */
    private void addWarning(final String msg) {
        this.warningContainer.addWarning(msg);
    }

    /**
     * Add an information message in the OIFits file
     * @param msg message to add
     */
    private void addInformation(final String msg) {
        this.warningContainer.addInformation(msg);
    }

    /**
     * Format time value for warning messages
     * @param value time (s)
     * @return formatted value
     */
    private String formatTime(final double value) {
        final String unit;
        final double val;
        if (value >= 1d) {
            val = value;
            unit = " s";
        } else {
            val = value * 1000d;
            unit = " ms";
        }
        return df.format(val) + unit;
    }

    static class NoiseWParams {

        /* varying values (spectrally dependent) */
        /** (W) number of photons in interferometer channel per telescope */
        final double[] nbPhotInterf;
        /** (W) number of photons in each photometric channel (photometric flux) */
        final double[] nbPhotPhoto;
        /** (W) error on the number of photons in each photometric channel (photometric flux) */
        final double[] errPhotPhoto;
        /** (W) square error contribution of 2 photometric channels on the square visiblity FiFj */
        final double[] sqErrVis2Phot;
        /** (W) coefficient used to the squared correlated flux */
        final double[] sqCorFluxCoef;
        /** (W) coefficient used to compute variance of the squared correlated flux */
        final double[] varSqCorFluxCoef;
        /** (W) constant used to compute variance of the squared correlated flux */
        final double[] varSqCorFluxConst;

        /** (W) squared correlated flux */
        final double[] sqCorrFlux;
        /** (W) error on squared correlated flux */
        final double[] errSqCorrFlux;

        /** (W) t3 phi error - coefficient */
        final double[] t3photCoef;
        /** (W) t3 phi error - coefficient^2 */
        final double[] t3photCoef2;
        /** (W) t3 phi error - coefficient^3 */
        final double[] t3photCoef3;
        /** (W) t3 phi detector error - constant */
        final double[] t3detConst;
        /** (W) t3 phi detector error - coefficient 1 */
        final double[] t3detCoef1;
        /** (W) t3 phi detector error - coefficient 2 */
        final double[] t3detCoef2;

        NoiseWParams(final int nWLen) {
            this.nbPhotInterf = init(nWLen);
            this.nbPhotPhoto = init(nWLen);
            this.errPhotPhoto = init(nWLen);
            this.sqErrVis2Phot = init(nWLen);
            this.sqCorFluxCoef = init(nWLen);
            this.varSqCorFluxCoef = init(nWLen);
            this.varSqCorFluxConst = init(nWLen);
            this.sqCorrFlux = init(nWLen);
            this.errSqCorrFlux = init(nWLen);
            this.t3photCoef = init(nWLen);
            this.t3photCoef2 = init(nWLen);
            this.t3photCoef3 = init(nWLen);
            this.t3detConst = init(nWLen);
            this.t3detCoef1 = init(nWLen);
            this.t3detCoef2 = init(nWLen);
        }

        static double[] init(final int len) {
            final double[] array = new double[len];
            Arrays.fill(array, Double.NaN);
            return array;
        }
    }
}

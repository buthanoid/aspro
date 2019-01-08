/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.service;

import fr.jmmc.aspro.Preferences;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import fr.jmmc.aspro.model.oi.UserModel;
import fr.jmmc.jmal.ALX;
import fr.jmmc.jmal.complex.MutableComplex;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.FFTUtils;
import fr.jmmc.jmal.image.FloatArrayCache;
import fr.jmmc.jmal.image.ImageArrayUtils;
import fr.jmmc.jmal.image.ImageUtils;
import fr.jmmc.jmal.image.job.ImageFlipJob;
import fr.jmmc.jmal.image.job.ImageLowerThresholdJob;
import fr.jmmc.jmal.image.job.ImageNormalizeJob;
import fr.jmmc.jmal.image.job.ImageRegionThresholdJob;
import fr.jmmc.jmal.model.ImageMode;
import fr.jmmc.jmal.model.ModelUVMapService;
import fr.jmmc.jmal.model.UVMapData;
import fr.jmmc.jmal.model.VisNoiseService;
import fr.jmmc.jmal.model.function.math.Functions;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.concurrent.InterruptedJobException;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.nom.tam.fits.FitsException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This stateless class generates UV Map Image for the given user model (fits image or cube) and UV area
 * based on FFT computation
 * 
 * Note on FastMath performance:
 * FastMath: use two tables for cos/sin [2049 doubles] i.e. 2 x 2049 x 8 = 32784 bytes
 *
 * @author Laurent BOURGES.
 */
@SuppressWarnings("ArrayHashCode")
public final class UserModelService {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(UserModelService.class.getName());
    /** minimum visiblity threshold (1e-2) for direct fourier transform */
    public static final double MIN_VISIBILITY_DATA = 1e-3d;
    /** ratio to ignore data values i.e. data value < LIMIT_RATIO * dataLowThreshold */
    public static final float LIMIT_RATIO = 1e-1f;
    /** maximum fft size (power of two) */
    public static final int MAX_FFT_SIZE = 128 * 1024;
    /** Two PI constant */
    public static final double TWO_PI = 2d * Math.PI;
    /** 1 nano arcsec in radians (to compare increments) */
    private static final double INC_EPSILON_RAD = Math.toRadians(1e-9 * ALX.ARCSEC_IN_DEGREES);
    /** formatter for frequencies */
    private final static DecimalFormat df = new DecimalFormat("0.00#E0");
    /** number of floats per data point */
    public final static int DATA_1D_POINT_SIZE = 3;
    /** shared InterruptedJobException instance */
    private static final InterruptedJobException ije = new InterruptedJobException("UserModelService.computeUVMap: interrupted");
    /** lock used by localDoubleWrappers */
    private final static Object lockDoubleWrappers = new Object();
    /**
     * ThreadLocal giving DoubleWrapper instances
     */
    private static final ThreadLocal<DoubleWrapper[]> localDoubleWrappers = new ThreadLocal<DoubleWrapper[]>() {
        @Override
        protected DoubleWrapper[] initialValue() {
            final int length = 8; // 4 x (8 Object + 8 double) = 64 (cache line padding)

            // synchronize to allocate arrays and instances together and (hopefully) contiguous:
            synchronized (lockDoubleWrappers) {
                final DoubleWrapper[] array = new DoubleWrapper[length];
                for (int i = 0; i < length; i++) {
                    array[i] = new DoubleWrapper();
                }
                return array;
            }
        }
    };

    /** FastMath mode (fast or quick) */
    public enum MathMode {

        /** default (JDK), slow but very accurate: more than 1e-15 */
        DEFAULT,
        /** fast but very accurate: up to 1e-15 */
        FAST,
        /** faster but not accurate: up to 1e-3 */
        QUICK
    }

    /**
     * Forbidden constructor
     */
    private UserModelService() {
        // no-op
    }

    /**
     * Load the given user model file and prepare ONLY the first image for FFT processing and direct Fourier transform
     * @param userModel user model to load and prepare
     * @throws FitsException if any FITS error occured
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or image has invalid keyword(s) / data
     */
    public static void prepareUserModel(final UserModel userModel) throws FitsException, IOException, IllegalArgumentException {
        final boolean useFastMode = Preferences.getInstance().isFastUserModel();
        prepareUserModel(userModel, useFastMode);
    }

    /**
     * Load the given user model file and prepare ONLY the first image for FFT processing and direct Fourier transform
     * @param userModel user model to load and prepare
     * @param useFastMode true to ignore useless data (faster); false to have highest precision
     * @throws FitsException if any FITS error occured
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or image has invalid keyword(s) / data
     */
    public static void prepareUserModel(final UserModel userModel, final boolean useFastMode) throws FitsException, IOException, IllegalArgumentException {
        // clear previously cached data:
        userModel.setModelDataList(null);

        // throws FitsException or IOException or IllegalArgumentException if the image can not be read properly:
        // note: load only the first valid image HDU (image or cube):
        final FitsImageFile imgFitsFile = FitsImageUtils.load(userModel.getFile(), true);

        if (imgFitsFile.getImageHDUCount() == 0) {
            throw new FitsException("The Fits file '" + userModel.getFile() + "' does not contain any supported Fits image !");
        }

        logger.info("useFastMode: {}", useFastMode);
        logger.info("scaleX:      {}", userModel.getScaleX());
        logger.info("scaleY:      {}", userModel.getScaleY());
        logger.info("rotation:    {}", userModel.getRotation());

        final FitsImageHDU fitsImageHDU = imgFitsFile.getFitsImageHDUs().get(0); // only first HDU

        final List<UserModelData> modelDataList = new ArrayList<UserModelData>(fitsImageHDU.getImageCount());

        IllegalArgumentException firstException = null;
        final StringBuilder preparationReport = new StringBuilder(512);

        final long start = System.nanoTime();

        for (final FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
            // Set User transform (scale & rotation):
            defineUserTransform(userModel, fitsImage);
            try {
                final UserModelData modelData = new UserModelData();

                // note: fits image instance can be modified by image preparation:
                // can throw IllegalArgumentException if image has invalid keyword(s) / data:
                prepareImage(fitsImage, modelData, useFastMode);

                logger.info("Prepared FitsImage: {}", fitsImage.toString());

                modelDataList.add(modelData);
            } catch (IllegalArgumentException iae) {
                if (firstException == null) {
                    firstException = iae;
                }
                preparationReport.append(iae.getMessage()).append('\n');
            }
        }

        logger.info("prepareFitsFile: duration = {} ms.", 1e-6d * (System.nanoTime() - start));

        // exception occured during image preparation(s):
        if (firstException != null) {
            logger.warn("FitsImage preparation has failure(s):\n{}", preparationReport.toString());

            if (modelDataList.isEmpty()) {
                throw firstException;
            }
        }

        // update cached data if no exception occured:
        userModel.setModelDataList(modelDataList);
    }

    private static void defineUserTransform(final UserModel userModel, final FitsImage fitsImage) {
        final double incCol = (userModel.getScaleX() != null) ? userModel.getScaleX() : Double.NaN;
        final double incRow = (userModel.getScaleY() != null) ? userModel.getScaleY() : Double.NaN;
        FitsImageUtils.rescaleImage(fitsImage, incCol, incRow);
        
        if (userModel.getRotation() != null) {
            fitsImage.setRotAngle(userModel.getRotation());
        }
    }

    /**
     * Validate the given user model i.e. both file and image values are valid
     *
     * @param model user model to validate
     * @param uvMaxFreq maximum UV frequency (rad-1)
     *
     * @throws IllegalArgumentException if the file or the image is invalid !
     */
    public static void validateModel(final UserModel model, final double uvMaxFreq) throws IllegalArgumentException {
        if (model == null) {
            throw new IllegalStateException("User model is empty !");
        }
        if (!model.isModelDataReady()) {
            throw new IllegalStateException("Fits image(s) are not prepared !");
        }

        for (final UserModelData modelData : model.getModelDataList()) {
            final FitsImage fitsImage = modelData.getFitsImage();

            // check CRC:
            // note: only possible with one Fits image or one Fits cube (single HDU):
            if (model.getChecksum() != fitsImage.getFitsImageHDU().getChecksum()) {
                throw new IllegalArgumentException("Fits image checksum is incorrect; please verify your file (probably modified) !");
            }
            checkFitsImage(fitsImage, uvMaxFreq);
        }
    }

    /**
     * Check the given fits image with the given corrected UV Max (rad-1)
     * @param fitsImage fits image to check
     * @param uvMaxFreq maximum UV frequency (rad-1)
     *
     * @throws IllegalArgumentException if the fits image is invalid (undefined increments or too small increments)
     * @throws IllegalStateException if the image is invalid (null or not square)
     */
    private static void checkFitsImage(final FitsImage fitsImage, final double uvMaxFreq) {
        if (fitsImage == null) {
            throw new IllegalStateException("Fits image is empty !");
        }

        // Suppose the image is square (see FitsImageUtils.prepareImage):
        if (fitsImage.getNbCols() != fitsImage.getNbRows()) {
            throw new IllegalStateException("Fits image must be a square image !");
        }

        if (!fitsImage.isIncColDefined() || !fitsImage.isIncRowDefined()) {
            throw new IllegalArgumentException("Undefined pixel increments (rad) !");
        }

        // TODO: support different axis increments on row and column axes ?
        if (!NumberUtils.equals(fitsImage.getIncCol(), fitsImage.getIncRow(), INC_EPSILON_RAD)) {
            throw new IllegalArgumentException("Fits image increments along row and column axes must be equals !");
        }
        if (uvMaxFreq > 0.0) {
            final double increment = fitsImage.getIncRow();
            final double maxFreq = getMaxFreq(fitsImage);

            if (maxFreq < uvMaxFreq) {
                final double minIncrement = (maxFreq / uvMaxFreq) * increment;

                throw new IllegalArgumentException("Fits image [" + fitsImage.getFitsImageIdentifier()
                        + "] must have smaller pixel increments [expected "
                        + FitsImage.getAngleAsString(minIncrement, df) + " < "
                        + FitsImage.getAngleAsString(increment, df) + "] to have a maximum frequency [expected "
                        + df.format(uvMaxFreq) + " rad-1 > " + df.format(maxFreq) + " rad-1 ] !");
            }
        }
    }

    public static double getMaxFreq(final FitsImage fitsImage) {
        final double increment = fitsImage.getIncRow();
        return 1.0 / (2.0 * increment);
    }

    /**
     * Compute the UV Map for to the given user model (fits image or cube)
     *
     * @param fitsImage user model as FitsImage
     * @param uvRect expected UV frequency area in rad-1
     * @param mode image mode (amplitude or phase)
     * @param imageSize expected number of pixels for both width and height of the generated image
     * @param colorModel color model to use
     * @param colorScale color scaling method
     * @return UVMapData
     *
     * @throws InterruptedJobException if the current thread is interrupted (cancelled)
     * @throws IllegalArgumentException if the fits image is invalid (not square or too small increments)
     * @throws RuntimeException if any exception occured during the computation
     */
    public static UVMapData computeUVMap(final FitsImage fitsImage,
                                         final Rectangle2D.Double uvRect,
                                         final ImageMode mode,
                                         final int imageSize,
                                         final IndexColorModel colorModel,
                                         final ColorScale colorScale) {
        return computeUVMap(fitsImage, uvRect, mode, imageSize, colorModel, colorScale, null, null, null, null);
    }

    /**
     * Compute the UV Map for to the given user model (fits image or cube)
     *
     * @param fitsImage user model as FitsImage
     * @param uvRect expected UV frequency area in rad-1
     * @param mode image mode (amplitude or phase)
     * @param imageSize expected number of pixels for both width and height of the generated image
     * @param colorModel color model to use
     * @param colorScale color scaling method
     * @param noiseService optional noise service to compute noisy complex visibilities before computing amplitude or phase
     * @return UVMapData
     *
     * @throws InterruptedJobException if the current thread is interrupted (cancelled)
     * @throws IllegalArgumentException if the fits image is invalid (not square or too small increments)
     * @throws RuntimeException if any exception occured during the computation
     */
    public static UVMapData computeUVMap(final FitsImage fitsImage,
                                         final Rectangle2D.Double uvRect,
                                         final ImageMode mode,
                                         final int imageSize,
                                         final IndexColorModel colorModel,
                                         final ColorScale colorScale,
                                         final VisNoiseService noiseService) {
        return computeUVMap(fitsImage, uvRect, mode, imageSize, colorModel, colorScale, noiseService, null, null, null);
    }

    /**
     * Compute the UV Map for to the given user model (fits image or cube)
     *
     * @param fitsImage user model as FitsImage
     * @param uvRect expected UV frequency area in rad-1
     * @param mode image mode (amplitude or phase)
     * @param imageSize expected number of pixels for both width and height of the generated image
     * @param colorModel color model to use
     * @param colorScale color scaling method
     * @param refVisData reference complex visibility data (optional)
     * @param refMin minimum reference value used only for sub images
     * @param refMax maximum reference value used only for sub images
     * @param noiseService optional noise service to compute noisy complex visibilities before computing amplitude or phase
     * @return UVMapData
     *
     * @throws InterruptedJobException if the current thread is interrupted (cancelled)
     * @throws IllegalArgumentException if the fits image is invalid (not square or too small increments)
     * @throws RuntimeException if any exception occured during the computation
     */
    public static UVMapData computeUVMap(final FitsImage fitsImage,
                                         final Rectangle2D.Double uvRect,
                                         final ImageMode mode,
                                         final int imageSize,
                                         final IndexColorModel colorModel,
                                         final ColorScale colorScale,
                                         final VisNoiseService noiseService,
                                         final Float refMin, final Float refMax,
                                         final float[][] refVisData) {

        // Note: do not support sub region (uvRect)
        // Get uvMaxFreq from uv rectangle:
        final double rectUvMaxFreq = Math.max(Math.max(Math.max(Math.abs(uvRect.getX()), Math.abs(uvRect.getY())),
                Math.abs(uvRect.getX() + uvRect.getWidth())),
                Math.abs(uvRect.getY() + uvRect.getHeight()));

        // todo enhance image size to fit sub image!
        logger.debug("UserModelService.computeUVMap: rectUvMaxFreq (rad-1): {}", rectUvMaxFreq);

        // throws exceptions:
        // do not check uv max:
        checkFitsImage(fitsImage, 0.0);

        if (fitsImage == null) {
            return null;
        }

        // Fix uv max according to the image max frequency:
        final double imgMaxFreq = getMaxFreq(fitsImage);

        final double uvMaxFreq = (rectUvMaxFreq < imgMaxFreq) ? rectUvMaxFreq : imgMaxFreq;
        logger.debug("UserModelService.computeUVMap: Fixed uvMaxFreq (rad-1): {}", uvMaxFreq);

        /** Get the current thread to check if the computation is interrupted */
        final Thread currentThread = Thread.currentThread();

        // Start the computations :
        final long start = System.nanoTime();

        final double mapScale;
        final double rotationAngle;

        if (fitsImage.isRotAngleDefined()) {
            rotationAngle = fitsImage.getRotAngle();
            Rectangle2D imgRectRef = new Rectangle2D.Double(0, 0, uvMaxFreq, uvMaxFreq);

            // angle sign is same direction (North -> East):
            final double theta = Math.toRadians(rotationAngle);

            final AffineTransform at = AffineTransform.getRotateInstance(theta, 0, 0);

            if (logger.isDebugEnabled()) {
                logger.debug("uv rect: {}", imgRectRef);
            }

            imgRectRef = ImageUtils.getBoundingBox(at, imgRectRef);

            if (logger.isDebugEnabled()) {
                logger.debug("rotated uv rect: {}", imgRectRef);
            }
            mapScale = imgRectRef.getWidth() / uvMaxFreq;
        } else {
            mapScale = 1.0;
            rotationAngle = 0.0;
        }
        logger.debug("mapScale: {}", mapScale);

        // input image is always squared:
        final int inputSize = fitsImage.getNbRows();
        logger.debug("Image size: {}", inputSize);

        // TODO: handle asymetric increments:
        final double increment = fitsImage.getIncRow();
        logger.debug("Current increment (rad): {}", increment);

        final double scaleFreqPerPix = 1d / (2d * increment);
        logger.debug("Freq scale per Pixel (rad-1): {}", scaleFreqPerPix);

        // UV / maxFreq ratio per FT pixel:
        final double ratioFreqPerPix = uvMaxFreq / scaleFreqPerPix;
        logger.debug("Ratio freq per Pixel: {}", ratioFreqPerPix);

        // find best FFT size:
        final int fftSize = findBestFFTSize(ratioFreqPerPix, imageSize, inputSize);

        // use the next even integer for pixel size & use map scale (larger):
        int dataSize = getOutputSize(mapScale * ratioFreqPerPix, fftSize);

        // make FFT larger (2 pixels more to avoid boundary errors):
        int fftOutputSize = dataSize + 2;

        logger.debug("UV plane FFT size (pixels): {}", fftOutputSize);

        // Fix FFT size due to map scale:
        if (fftSize < fftOutputSize) {
            // use all possible FFT pixels => reduce UV max:
            fftOutputSize = fftSize;
            dataSize = fftOutputSize - 2;
        }

        final int outputSize = (int) Math.ceil(dataSize / mapScale);

        logger.debug("dataSize:   {}", dataSize);
        logger.debug("outputSize: {}", outputSize);

        final double mapUvMaxFreq = (scaleFreqPerPix * outputSize) / fftSize;
        logger.debug("UVMap exact uvMaxFreq (m): {}", mapUvMaxFreq);

        // fast interrupt :
        if (currentThread.isInterrupted()) {
            throw ije;
        }

        // fft data as float [rows][cols] packed:
        float[][] visData = null;
        float[][] imgData = null;

        try {
            if (refVisData == null || fftOutputSize != refVisData.length) {
                // use single precision for FFT performance (image needs not double precision) :

                // 1 - compute FFT
                // TODO: cache the FFT in the Target object or save it to disk (temp) ...
                visData = FFTUtils.computeFFT(inputSize, fitsImage.getData(), fftSize, fftOutputSize);

                // fast interrupt :
                if (currentThread.isInterrupted()) {
                    throw ije;
                }
            } else {
                // use reference complex visibility data:
                visData = refVisData;
            }

            // 2 - Extract the amplitude/phase/square amplitude to get the uv map :
            // data as float [rows][cols]:
            imgData = FFTUtils.convert(fftOutputSize, visData, mode, dataSize, noiseService);

            // fast interrupt :
            if (currentThread.isInterrupted()) {
                throw ije;
            }

            // 3 - Get the image with the given color model and color scale :
            final Rectangle2D.Double uvMapRect = new Rectangle2D.Double();
            uvMapRect.setFrameFromDiagonal(-mapUvMaxFreq, -mapUvMaxFreq, mapUvMaxFreq, mapUvMaxFreq);

            final UVMapData uvMapData = ModelUVMapService.computeImage(uvRect, refMin, refMax, mode, imageSize, colorModel, colorScale,
                    dataSize, visData, imgData, uvMapRect, noiseService, rotationAngle, outputSize);

            logger.info("compute : duration = {} ms.", 1e-6d * (System.nanoTime() - start));

            return uvMapData;

        } catch (RuntimeException re) {
            logger.debug("recycleArrays <= interrupted job:");
            // recycle arrays:
            if (visData != refVisData) {
                FloatArrayCache.recycleArray(visData);
            }
            // rethrow exception:
            throw re;
        } finally {
            // recycle arrays:
            FloatArrayCache.recycleArray(imgData);
        }
    }

    /**
     * Return the best FFT size (power of two) i.e. giving the output size closest than the expected image size
     * @param ratioFreqPerPix Ratio freq per Pixel
     * @param imageSize expected image size in pixels
     * @param inputSize image input size to check minimum FFT size
     * @return best FFT size (power of two)
     */
    private static int findBestFFTSize(final double ratioFreqPerPix, final int imageSize, final int inputSize) {

        final int fftSizeMax = getFFTSize(ratioFreqPerPix, imageSize, inputSize);
        final int fftSizeMin = getFFTSize(ratioFreqPerPix, imageSize / 2, inputSize);

        if (fftSizeMin == fftSizeMax) {
            return fftSizeMin;
        }

        final int outputSizeMax = getOutputSize(ratioFreqPerPix, fftSizeMax);
        final int outputSizeMin = getOutputSize(ratioFreqPerPix, fftSizeMin);

        // keep fftSize that gives outputSize closest to imageSize:
        final int fftSize = (imageSize - outputSizeMin < outputSizeMax - imageSize) ? fftSizeMin : fftSizeMax;

        logger.debug("Best FFT size (pixels): {}", fftSize);

        return fftSize;
    }

    /**
     * Return the FFT size (power of two) for the given ratio and expected image size
     * @param ratio UV / maxFreq ratio
     * @param imageSize expected image size in pixels
     * @param inputSize image input size to check minimum FFT size
     * @return best FFT size (power of two)
     */
    private static int getFFTSize(final double ratio, final int imageSize, final int inputSize) {
        // Correct power of two for FFT:
        int fftSize = FFTUtils.getPowerOfTwo((int) Math.ceil(imageSize / ratio));

        logger.debug("For UV plane size (pixels): {}", imageSize);
        logger.debug("FFT size (pixels): {}", fftSize);

        if (fftSize > MAX_FFT_SIZE) {
            fftSize = MAX_FFT_SIZE;
            logger.info("Max FFT size reached (pixels): {}", fftSize);
        }
        if (fftSize < inputSize) {
            fftSize = FFTUtils.getPowerOfTwo(inputSize);
            logger.info("Min FFT size reached (input pixels): {}", fftSize);
        }
        return fftSize;
    }

    /**
     * Return the output size (even number) for the given ratio and FFT size
     * @param ratioFreqPerPix Ratio freq per Pixel
     * @param fftSize FFT size (power of two)
     * @return best FFT size (power of two)
     */
    private static int getOutputSize(final double ratioFreqPerPix, final int fftSize) {

        final double outputExactSize = fftSize * ratioFreqPerPix;
        logger.debug("UV plane exact size (pixels): {}", outputExactSize);

        // use the next even integer for pixel size:
        int outputSize = (int) Math.ceil(outputExactSize);
        if (outputSize % 2 != 0) {
            outputSize++;
        }

        logger.debug("UV plane size (pixels): {}", outputSize);

        return outputSize;
    }

    /**
     * Compute the spatial coordinates (rad) (relative to the half length) given the number of values and the increment
     * @param length number of values
     * @param increment increment (rad)
     * @return spatial coordinates (rad) (relative to the half length)
     */
    public static double[] computeSpatialCoords(final int length, final double increment) {

        final double[] coords = new double[length];

        final double half = length / 2.0;

        for (int i = 0; i < length; i++) {
            coords[i] = increment * (i - half); //in sky (radians).
        }

        return coords;
    }

    /**
     * Compute the complex visiblity of the given user model for the given Ufreq and Vfreq arrays
     *
     * @param data1D user model data as 1D array
     * @param fromData index of the first data to process
     * @param endData index of the last data to process (exclusive)
     * @param ufreq U frequencies in rad-1 x 2 PI
     * @param vfreq V frequencies in rad-1 x 2 PI
     * @param vis complex visibility array
     * @param from index of the first wavelength to compute
     * @param end index of the last wavelength to compute (exclusive)
     * @param mathMode Math mode to use to compute trigonometric functions
     */
    public static void computeModel(final float[] data1D, final int fromData, final int endData,
                                    final double[] ufreq, final double[] vfreq, final MutableComplex[] vis, final int from, final int end,
                                    final MathMode mathMode) {

        if (data1D != null && ufreq != null && vfreq != null && vis != null) {

            if (ufreq.length != vfreq.length || vis.length < ufreq.length) {
                throw new IllegalStateException("Incorrect array sizes (Ufreq, VFreq, Vis) !");
            }

            // compute complex visiblities using exact fourier transform (slow):
            compute1D(data1D, fromData, endData, ufreq, vfreq, vis, from, end, mathMode);
        }
    }

    /**
     * Compute exact discrete fourier transform / complex visiblity of given user model for the given Ufreq and Vfreq arrays
     * @param data1D user model data as 1D array
     * @param fromData index of the first data to process
     * @param endData index of the last data to process (exclusive)
     * @param ufreq U frequencies in rad-1 x 2 PI
     * @param vfreq V frequencies in rad-1 x 2 PI
     * @param vis complex visibility array
     * @param from index of the first wavelength to compute
     * @param end index of the last wavelength to compute (exclusive)
     * @param mathMode Math mode to use to compute trigonometric functions
     */
    private static void compute1D(final float[] data1D, final int fromData, final int endData,
                                  final double[] ufreq, final double[] vfreq, final MutableComplex[] vis, final int from, final int end,
                                  final MathMode mathMode) {

        /** Get the current thread to check if the computation is interrupted */
        final Thread currentThread = Thread.currentThread();

        final int lenData = endData - DATA_1D_POINT_SIZE;

//    logger.info("from {} to {}", offset, len);

        /*
         * Performance:
         * - QUICK:
         Timer [chunk[600000] - ms] [6]	{num = 6 :	min = 2197.30994,	avg = 2320.72833,	max = 2758.09333,	acc = 13924.37}
         Timer [chunk[450000] - ms] [6]	{num = 6 :	min = 2163.82152,	avg = 2187.24396,	max = 2250.24739,	acc = 13123.46377}
         Timer [chunk[337500] - ms] [6]	{num = 6 :	min = 2149.31863,	avg = 2198.63036,	max = 2327.58621,	acc = 13191.7822}
         Timer [chunk[253125] - ms] [6]	{num = 6 :	min = 2170.0252,	avg = 2179.62133,	max = 2207.19855,	acc = 13077.72801}
         Timer [chunk[189843] - ms] [6]	{num = 6 :	min = 2163.19821,	avg = 2183.49676,	max = 2264.70316,	acc = 13100.98056}
         Timer [chunk[142382] - ms] [6]	{num = 6 :	min = 2152.85887,	avg = 2187.79126,	max = 2248.17353,	acc = 13126.74756}
         Timer [chunk[106786] - ms] [6]	{num = 6 :	min = 2153.87358,	avg = 2167.66898,	max = 2193.36412,	acc = 13006.01392}
         Timer [chunk[80089] - ms] [6]	{num = 6 :	min = 2144.14673,	avg = 2161.44585,	max = 2180.06556,	acc = 12968.67514}
         Timer [chunk[60066] - ms] [6]	{num = 6 :	min = 2154.52333,	avg = 2169.33297,	max = 2206.79534,	acc = 13015.99783}
         Timer [chunk[45049] - ms] [6]	{num = 6 :	min = 2140.7008,	avg = 2146.16914,	max = 2150.53099,	acc = 12877.01488}
         Timer [chunk[33786] - ms] [6]	{num = 6 :	min = 2148.31889,	avg = 2195.75968,	max = 2297.56219,	acc = 13174.55808}
         Timer [chunk[25339] - ms] [6]	{num = 6 :	min = 2135.89435,	avg = 2169.17585,	max = 2243.99312,	acc = 13015.05512}
         Timer [chunk[19004] - ms] [6]	{num = 6 :	min = 2132.60587,	avg = 2157.38175,	max = 2243.52941,	acc = 12944.29052}
         Timer [chunk[14253] - ms] [6]	{num = 6 :	min = 2134.62622,	avg = 2155.90243,	max = 2204.15121,	acc = 12935.41462}
         Timer [chunk[10689] - ms] [6]	{num = 6 :	min = 2133.48513,	avg = 2148.59266,	max = 2170.79936,	acc = 12891.556}
         Timer [chunk[8016] - ms] [6]	{num = 6 :	min = 2132.62025,	avg = 2161.33295,	max = 2205.75479,	acc = 12967.99775}
         Timer [chunk[6012] - ms] [6]	{num = 6 :	min = 2130.40955,	avg = 2139.03898,	max = 2151.91463,	acc = 12834.23392}
         Timer [chunk[4509] - ms] [6]	{num = 6 :	min = 2131.51534,	avg = 2163.09447,	max = 2247.2551,	acc = 12978.56683}
         * 
         * - FAST:
         * 
         Timer [chunk[600000] - ms] [6]	{num = 6 :	min = 6057.139,	avg = 6293.27596,	max = 6642.10127,	acc = 37759.65577}
         Timer [chunk[450000] - ms] [6]	{num = 6 :	min = 6016.35241,	avg = 6147.07046,	max = 6523.63,	acc = 36882.42277}
         Timer [chunk[337500] - ms] [6]	{num = 6 :	min = 6022.16359,	avg = 6065.2004,	max = 6104.91548,	acc = 36391.20241}
         Timer [chunk[253125] - ms] [6]	{num = 6 :	min = 6019.71289,	avg = 6031.6292,	max = 6046.49311,	acc = 36189.77521}
         Timer [chunk[189843] - ms] [6]	{num = 6 :	min = 6022.26495,	avg = 6055.0051,	max = 6112.85134,	acc = 36330.03065}
         Timer [chunk[142382] - ms] [6]	{num = 6 :	min = 6012.305,	avg = 6026.99833,	max = 6054.79236,	acc = 36161.98998}
         Timer [chunk[106786] - ms] [6]	{num = 6 :	min = 5999.2643,	avg = 6091.08765,	max = 6275.80363,	acc = 36546.52591}
         Timer [chunk[80089] - ms] [6]	{num = 6 :	min = 5975.87674,	avg = 5997.58693,	max = 6020.7726,	acc = 35985.52163}
         Timer [chunk[60066] - ms] [6]	{num = 6 :	min = 5997.09171,	avg = 6117.68266,	max = 6541.92285,	acc = 36706.09601}
         Timer [chunk[45049] - ms] [6]	{num = 6 :	min = 5976.74927,	avg = 6043.52729,	max = 6108.33167,	acc = 36261.16378}
         Timer [chunk[33786] - ms] [6]	{num = 6 :	min = 5986.14369,	avg = 6040.79816,	max = 6138.52005,	acc = 36244.78897}
         Timer [chunk[25339] - ms] [6]	{num = 6 :	min = 5985.55532,	avg = 6001.17616,	max = 6016.55492,	acc = 36007.057}
         Timer [chunk[19004] - ms] [6]	{num = 6 :	min = 5970.343,	avg = 5984.99634,	max = 6024.19756,	acc = 35909.97805}
         Timer [chunk[14253] - ms] [6]	{num = 6 :	min = 5969.17255,	avg = 6041.71992,	max = 6222.17572,	acc = 36250.31957}
         Timer [chunk[10689] - ms] [6]	{num = 6 :	min = 5967.60576,	avg = 6018.68233,	max = 6109.34226,	acc = 36112.09402}
         Timer [chunk[8016] - ms] [6]	{num = 6 :	min = 5962.58387,	avg = 6002.71542,	max = 6066.26941,	acc = 36016.29257}
         Timer [chunk[6012] - ms] [6]	{num = 6 :	min = 5954.25996,	avg = 5961.66638,	max = 5980.68242,	acc = 35769.9983}
         Timer [chunk[4509] - ms] [6]	{num = 6 :	min = 5954.44538,	avg = 6009.70111,	max = 6144.77561,	acc = 36058.20671}     * 
         * - DEFAULT (JDK):
         [SwingWorker-pool-1] fr.jmmc.aspro.service.OIFitsCreatorService - computeModelVisibilities: duration = 44020.670999999995 ms.
         */
        double kwCol, kwRow, re, im, flux, z;

        if (mathMode == MathMode.QUICK) {

            // iterate on ufreq / vfreq / vis by wavelength:
            for (int i = from, j; i < end; i++) {
                // divide by model image frequency ?
                kwCol = TWO_PI * ufreq[i];
                kwRow = TWO_PI * vfreq[i];

                // reset:
                re = 0d;
                im = 0d;

                // iterate on data points:
                for (j = fromData; j <= lenData; j += DATA_1D_POINT_SIZE) {
                    flux = data1D[j];
                    z = kwCol * data1D[j + 1] + kwRow * data1D[j + 2];

                    re += flux * FastMath.cosQuick(z);
                    im -= flux * FastMath.sinQuick(z);

                } // data1D

                // update complex instance (mutable):
                vis[i].add(re, im);

                // fast interrupt :
                if (currentThread.isInterrupted()) {
                    return;
                }
            } // vis
        } else if (mathMode == MathMode.FAST) {

            // TODO: use context or thread local
            final DoubleWrapper[] dw = localDoubleWrappers.get();
            final DoubleWrapper cw = dw[0];

            // iterate on ufreq / vfreq / vis by wavelength:
            for (int i = from, j; i < end; i++) {
                // divide by model image frequency ?
                kwCol = TWO_PI * ufreq[i];
                kwRow = TWO_PI * vfreq[i];

                // reset:
                re = 0d;
                im = 0d;

                // iterate on data points:
                for (j = fromData; j <= lenData; j += DATA_1D_POINT_SIZE) {
                    flux = data1D[j];
                    z = kwCol * data1D[j + 1] + kwRow * data1D[j + 2];

                    im -= flux * FastMath.sinAndCos(z, cw); // cw holds cosine
                    re += flux * cw.value;

                } // data1D

                // update complex instance (mutable):
                vis[i].add(re, im);

                // fast interrupt :
                if (currentThread.isInterrupted()) {
                    return;
                }
            } // vis
        } else {

            // iterate on ufreq / vfreq / vis by wavelength:
            for (int i = from, j; i < end; i++) {
                // divide by model image frequency ?
                kwCol = TWO_PI * ufreq[i];
                kwRow = TWO_PI * vfreq[i];

                // reset:
                re = 0d;
                im = 0d;

                // iterate on data points:
                for (j = fromData; j <= lenData; j += DATA_1D_POINT_SIZE) {
                    flux = data1D[j];
                    z = kwCol * data1D[j + 1] + kwRow * data1D[j + 2];

                    // Test without cos/sin:
                    // re += flux * z;
                    // im += flux * z;
                    // use Math (not StrictMath):
                    re += flux * Math.cos(z);
                    im -= flux * Math.sin(z);

                } // data1D

                // update complex instance (mutable):
                vis[i].add(re, im);

                // fast interrupt :
                if (currentThread.isInterrupted()) {
                    return;
                }
            } // vis
        } // math mode
    }

    /**
     * Prepare the given image for FFT (normalize, threshold, pad to next power of two) and direct Fourier transform.
     * Update the given FitsImage by the prepared FitsImage ready for FFT and prepared model data for direct Fourier transform
     * @param fitsImage FitsImage to process
     * @param modelData prepared model data for direct Fourier transform
     * @param useFastMode true to ignore useless data (faster); false to have highest precision
     * @throws IllegalArgumentException if image has invalid keyword(s) / data
     */
    public static void prepareImage(final FitsImage fitsImage, final UserModelData modelData, final boolean useFastMode) throws IllegalArgumentException {

        if (!fitsImage.isDataRangeDefined()) {
            // update boundaries excluding zero values:
            FitsImageUtils.updateDataRangeExcludingZero(fitsImage);
        }

        // in place modifications:
        float[][] data = fitsImage.getData();
        int nbRows = fitsImage.getNbRows();
        int nbCols = fitsImage.getNbCols();

        logger.info("Image size: {} x {}", nbRows, nbCols);

        // 1 - Ignore negative values:
        if (fitsImage.getDataMax() <= 0d) {
            throw new IllegalArgumentException("Fits image [" + fitsImage.getFitsImageIdentifier() + "] has only negative data !");
        }
        if (fitsImage.getDataMin() < 0d) {
            final float threshold = 0f;

            final ImageLowerThresholdJob thresholdJob = new ImageLowerThresholdJob(data, nbCols, nbRows, threshold, 0f);
            logger.info("ImageLowerThresholdJob - threshold = {} (ignore negative values)", threshold);

            thresholdJob.forkAndJoin();

            logger.info("ImageLowerThresholdJob - updateCount: {}", thresholdJob.getUpdateCount());

            // update boundaries excluding zero values:
            FitsImageUtils.updateDataRangeExcludingZero(fitsImage);
        }

        // 2 - Normalize data (total flux):
        if (!NumberUtils.equals(fitsImage.getSum(), 1.0, MIN_VISIBILITY_DATA)) {
            final double normFactor = 1d / fitsImage.getSum();

            final ImageNormalizeJob normJob = new ImageNormalizeJob(data, nbCols, nbRows, normFactor);
            logger.info("ImageNormalizeJob - factor: {}", normFactor);

            normJob.forkAndJoin();

            // update boundaries excluding zero values:
            FitsImageUtils.updateDataRangeExcludingZero(fitsImage);
        }

        // 2.1 - Determine flux threshold to ignore useless values:
        final float thresholdFlux;

        if (useFastMode) {
            final double totalFlux = fitsImage.getSum();
            logger.info("Total flux: {}", totalFlux);

            final int nData = fitsImage.getNData();
            final float[] data1D = sortData(fitsImage);

            // TODO: use preference to define flux threshold:
            double error = MIN_VISIBILITY_DATA;

            final int thIdx = findThresholdIndex(data1D, totalFlux, error);

            if (thIdx != -1) {
                final double thPixRatio = (100.0 * (nData - thIdx)) / nData;

                logger.info("threshold ratio: {} %", NumberUtils.trimTo3Digits(thPixRatio));

                thresholdFlux = data1D[thIdx];

                logger.info("thresholdFlux: {}", thresholdFlux);
            } else {
                thresholdFlux = 0f;
            }
        } else {
            thresholdFlux = 0f;
        }

        // 2.2 - Skip too small data values i.e. lower than thresholdImage ie below 10^-6:
        if (useFastMode) {
            if (thresholdFlux > 0f && fitsImage.getDataMin() < thresholdFlux) {

                final ImageLowerThresholdJob thresholdJob = new ImageLowerThresholdJob(data, nbCols, nbRows, thresholdFlux, 0f);
                logger.info("ImageLowerThresholdJob - threshold: {}", thresholdFlux);

                thresholdJob.forkAndJoin();

                logger.info("ImageLowerThresholdJob - updateCount: {}", thresholdJob.getUpdateCount());

                // update boundaries excluding zero values:
                FitsImageUtils.updateDataRangeExcludingZero(fitsImage);
            }
        }

        // 3 - Locate useful data values inside image:
        final ImageRegionThresholdJob regionJob = new ImageRegionThresholdJob(data, nbCols, nbRows, thresholdFlux);

        logger.info("ImageRegionThresholdJob: thresholdImage: {}", thresholdFlux);
        regionJob.forkAndJoin();

        // 4 - Extract ROI:
        // keep the center of the ROI and keep the image square (width = height = even number):
        int rows1, rows2, cols1, cols2;

        final float halfRows = 0.5f * nbRows;
        final float halfCols = 0.5f * nbCols;

        final float distToCenter;

        // use non zero area:
        rows1 = regionJob.getRowLowerIndex();
        rows2 = regionJob.getRowUpperIndex();
        cols1 = regionJob.getColumnLowerIndex();
        cols2 = regionJob.getColumnUpperIndex();

        logger.info("ImageRegionThresholdJob: row indexes: {} - {}", rows1, rows2);
        logger.info("ImageRegionThresholdJob: col indexes: {} - {}", cols1, cols2);

        final float rowDistToCenter = Math.max(Math.abs(halfRows - rows1), Math.abs(halfRows - rows2));
        final float colDistToCenter = Math.max(Math.abs(halfCols - cols1), Math.abs(halfCols - cols2));

        logger.info("ImageRegionThresholdJob: rowDistToCenter: {}", rowDistToCenter);
        logger.info("ImageRegionThresholdJob: colDistToCenter: {}", colDistToCenter);

        // ensure minimum size to 2 pixels:
        distToCenter = Math.max(1.5f, Math.max(rowDistToCenter, colDistToCenter));
        logger.info("ImageRegionThresholdJob: distToCenter: {}", distToCenter);

        // range check ?
        rows1 = (int) Math.floor(halfRows - distToCenter);
        rows2 = (int) Math.ceil(halfRows + distToCenter);

        // fix width to be an even number:
        if ((rows2 - rows1) % 2 != 0) {
            rows2++;
        }

        logger.info("ImageRegionThresholdJob: even row indexes: {} - {}", rows1, rows2);

        // range check ?
        cols1 = (int) Math.floor(halfCols - distToCenter);
        cols2 = (int) Math.ceil(halfCols + distToCenter);

        // fix width to be an even number:
        if ((cols2 - cols1) % 2 != 0) {
            cols2++;
        }

        logger.info("ImageRegionThresholdJob: even col indexes: {} - {}", cols1, cols2);

        // update fits image:
        // note: this extraction does not check boundary overlapping:
        data = ImageArrayUtils.extract(nbRows, nbCols, data, rows1, cols1, rows2, cols2);

        if (data == null) {
            // outside ranges:
            data = fitsImage.getData();
        } else {
            // update data:
            FitsImageUtils.updateFitsImage(fitsImage, data);

            // update ref pixel:
            fitsImage.setPixRefRow(fitsImage.getPixRefRow() - rows1);
            fitsImage.setPixRefCol(fitsImage.getPixRefCol() - cols1);

            nbRows = fitsImage.getNbRows();
            nbCols = fitsImage.getNbCols();

            logger.info("ROI size = {} x {}", nbRows, nbCols);
        }

        // 5 - Make sure the image is square i.e. padding (width = height = even number):
        final int newSize = Math.max(
                (nbRows % 2 != 0) ? nbRows + 1 : nbRows,
                (nbCols % 2 != 0) ? nbCols + 1 : nbCols);

        if (newSize != nbRows || newSize != nbCols) {
            data = ImageArrayUtils.enlarge(nbRows, nbCols, data, newSize, newSize);

            // update data/dataMin/dataMax:
            FitsImageUtils.updateFitsImage(fitsImage, data, fitsImage.getDataMin(), fitsImage.getDataMax());

            // update ref pixel:
            fitsImage.setPixRefRow(fitsImage.getPixRefRow() + ((newSize - nbRows) / 2));
            fitsImage.setPixRefCol(fitsImage.getPixRefCol() + ((newSize - nbCols) / 2));

            nbRows = fitsImage.getNbRows();
            nbCols = fitsImage.getNbCols();

            logger.info("Square size = {} x {}", nbRows, nbCols);
        }

        // 6 - flip axes to have positive increments (left to right for the column axis and bottom to top for the row axis)
        // note: flip operation requires image size to be an even number
        final double incRow = fitsImage.getSignedIncRow();
        if (incRow < 0d) {
            // flip row axis:
            final ImageFlipJob flipJob = new ImageFlipJob(data, nbCols, nbRows, false);

            flipJob.forkAndJoin();

            logger.info("ImageFlipJob - flipY done");

            fitsImage.setSignedIncRow(-incRow);
        }

        final double incCol = fitsImage.getSignedIncCol();
        if (incCol < 0d) {
            // flip column axis:
            final ImageFlipJob flipJob = new ImageFlipJob(data, nbCols, nbRows, true);

            flipJob.forkAndJoin();

            logger.info("ImageFlipJob - flipX done");

            fitsImage.setSignedIncCol(-incCol);
        }

        // Update FFT ready fits image in model data:
        modelData.setFitsImage(fitsImage);

        // 7 - prepare model data to compute direct Fourier transform:
        prepareModelData(fitsImage, modelData, thresholdFlux);
    }

    /**
     * Flatten 2D image data to 1D array and sort data by ascending order
     *
     * @param fitsImage user model as FitsImage
     * @return sorted model data as 1D array
     */
    public static float[] sortData(final FitsImage fitsImage) {

        /** Get the current thread to check if the computation is interrupted */
        final Thread currentThread = Thread.currentThread();

        final int nbRows = fitsImage.getNbRows();
        final int nbCols = fitsImage.getNbCols();
        final int nData = fitsImage.getNData();
        final float[][] data = fitsImage.getData();

        // prepare 1D data:
        float[] row;
        int n1D = 0;
        final float[] data1D = new float[nData];

        float flux;

        // iterate on rows:
        for (int r = 0, c; r < nbRows; r++) {
            row = data[r];

            // iterate on columns:
            for (c = 0; c < nbCols; c++) {
                flux = row[c];

                // skip values different from zero:
                if (flux > 0f) {
                    // keep this data point:
                    data1D[n1D] = flux;
                    n1D++;
                }
            } // columns

            // fast interrupt :
            if (currentThread.isInterrupted()) {
                return null;
            }
        } // rows

        logger.info("FitsImage: used pixels = {} / {}", n1D, nData);

        Arrays.sort(data1D, 0, n1D);

        logger.info("FitsImage: {} float sorted.", n1D);

        if (n1D != nData) {
            return Arrays.copyOf(data1D, n1D);
        }
        return data1D;
    }

    /** weak reference on a recycled single float array for prepareModelData() */
    private static WeakReference<float[]> recycled_array = null;

    private static float[] getArray(final int length) {
        float[] array;
        if (SwingUtils.isEDT() && (recycled_array != null)) {
            array = recycled_array.get();
            if (array != null && array.length >= length) {
                recycled_array = null; // free reference
                if (logger.isDebugEnabled()) {
                    logger.debug("get array: {} @ {}", array.length, array.hashCode());
                }
                return array;
            }
        }
        return new float[length * 11 / 10]; // 10% more
    }

    private static void putArray(final float[] array) {
        if (SwingUtils.isEDT()) {
            if (logger.isDebugEnabled()) {
                logger.debug("put array: {} @ {}", array.length, array.hashCode());
            }
            recycled_array = new WeakReference<float[]>(array);
        }
    }

    /**
     * Prepare the user model data to compute direct Fourier transform
     *
     * @param fitsImage user model as FitsImage
     * @param modelData prepared model data for direct Fourier transform
     * @param threshold low threshold (ignore too small values)
     */
    private static void prepareModelData(final FitsImage fitsImage, final UserModelData modelData, final float threshold) {

        logger.info("prepareModelData: threshold: {}", threshold);

        /** Get the current thread to check if the computation is interrupted */
        final Thread currentThread = Thread.currentThread();

        // note: square image (and even size):
        final int nbRows = fitsImage.getNbRows();
        final int nbCols = fitsImage.getNbCols();
        final int nData = fitsImage.getNData();
        final float[][] data = fitsImage.getData();
        final double dataMin = fitsImage.getDataMin();
        final double dataMax = fitsImage.getDataMax();

        logger.info("prepareModelData: min: {} - max: {}", dataMin, dataMax);

        final int nPixels = nbRows * nbCols;

        logger.info("prepareModelData: nData: {} / {}", nData, nPixels);

        final double cosTheta;
        final double sinTheta;

        if (fitsImage.isRotAngleDefined()) {
            final double rotationAngle = fitsImage.getRotAngle();

            // angle sign is inverse direction (North -> East):
            final double theta = Math.toRadians(rotationAngle);

            cosTheta = FastMath.cos(theta);
            sinTheta = FastMath.sin(theta);
        } else {
            cosTheta = 1.0;
            sinTheta = 0.0;
        }

        // prepare spatial coordinates:
        final double[] colCoords = UserModelService.computeSpatialCoords(nbCols, fitsImage.getSignedIncCol()); // X
        final double[] rowCoords = UserModelService.computeSpatialCoords(nbRows, fitsImage.getSignedIncRow()); // Y

        // prepare 1D data (eliminate values lower than threshold):
        float[] row;
        int nUsedData = 0;

        // Recycle large data arrays (weak reference cache) for fits cubes (many spectral channels having roughly same nData)
        final float[] data1D = getArray(nData * DATA_1D_POINT_SIZE);

        double totalFlux = 0d;
        float flux;
        double rowCoord, colCoord;

        // iterate on rows:
        for (int r = 0, c; r < nbRows; r++) {
            row = data[r];
            rowCoord = rowCoords[r];

            // iterate on columns:
            for (c = 0; c < nbCols; c++) {
                flux = row[c];

                // skip values lower than threshold:
                if (flux > threshold) {
                    colCoord = colCoords[c];

                    // Transform coordinates:
                    // keep this data point:
                    totalFlux += flux;
                    data1D[nUsedData] = flux;
                    data1D[nUsedData + 1] = (float) Functions.transformU(colCoord, rowCoord, cosTheta, sinTheta);
                    data1D[nUsedData + 2] = (float) Functions.transformV(colCoord, rowCoord, cosTheta, sinTheta);

                    nUsedData += DATA_1D_POINT_SIZE;
                }
            } // columns

            // fast interrupt :
            if (currentThread.isInterrupted()) {
                return;
            }
        } // rows

        logger.info("prepareModelData: used pixels = {} / {}", nUsedData / DATA_1D_POINT_SIZE, nPixels);

        // normalize flux to 1.0:
        logger.info("prepareModelData: totalFlux: {}", totalFlux);

        if (threshold != 0f) {
            final double normFactor = 1d / totalFlux;

            for (int i = 0; i < nUsedData; i += DATA_1D_POINT_SIZE) {
                data1D[i] = (float) (normFactor * data1D[i]);
            }

            totalFlux = 0d;
            for (int i = 0; i < nUsedData; i += DATA_1D_POINT_SIZE) {
                totalFlux += data1D[i];
            }
            logger.info("prepareModelData: totalFlux after normalization: {}", totalFlux);
        }

        // trim array size:
        if (nUsedData != data1D.length) {
            final float[] mData1D = new float[nUsedData];

            System.arraycopy(data1D, 0, mData1D, 0, nUsedData);

            // recycle data1D array:
            putArray(data1D);

            modelData.set(mData1D);
        } else {
            modelData.set(data1D);
        }
    }

    /**
     * Find the threshold index when the sum of values becomes higher than the given upper threshold
     * @param data1D sorted data (ascending order)
     * @param total total of all values
     * @param error upper threshold
     * @return threshold value or -1 if not found
     */
    private static int findThresholdIndex(final float[] data1D, final double total, final double error) {
        final double upperThreshold = total * (1d - error);

        if (logger.isDebugEnabled()) {
            logger.debug("findThresholdIndex: upperThreshold: {}", upperThreshold);
        }

        float value;
        float lastValue = 0f;
        double partialFlux = 0d;

        for (int i = data1D.length - 1; i >= 0; i--) {
            value = data1D[i];
            partialFlux += value;

            if (partialFlux > upperThreshold) {
                // keep equal values
                if (lastValue != 0f) {
                    if (value != lastValue) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("findThresholdValue: threshold reached: {} > {} - value = {} - nPixels = {}",
                                    partialFlux, upperThreshold, value, (data1D.length - 1 - i));
                        }
                        return i;
                    }
                } else {
                    lastValue = value;
                }
            }
        }
        return -1;
    }

}

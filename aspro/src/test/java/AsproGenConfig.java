
/**
 * *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 * ****************************************************************************
 */
import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.model.oi.Channel;
import fr.jmmc.aspro.model.oi.ChannelLink;
import fr.jmmc.aspro.model.oi.DelayLine;
import fr.jmmc.aspro.model.oi.FocalInstrumentConfiguration;
import fr.jmmc.aspro.model.oi.FocalInstrumentConfigurationItem;
import fr.jmmc.aspro.model.oi.InterferometerConfiguration;
import fr.jmmc.aspro.model.oi.InterferometerSetting;
import fr.jmmc.aspro.model.oi.LonLatAlt;
import fr.jmmc.aspro.model.oi.Position3D;
import fr.jmmc.aspro.model.oi.Station;
import fr.jmmc.aspro.model.oi.StationLinks;
import fr.jmmc.aspro.model.oi.SwitchYard;
import fr.jmmc.aspro.service.GeocentricCoords;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.oitools.util.CombUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class converts the VLT switchyard to an XML fragment compliant with the aspro DM
 *
 * @author bourgesl
 */
public final class AsproGenConfig {

    /**
     * refractive index in air (from http://refractiveindex.info/?group=GASES&material=Air)
     */
    public final static double N_AIR = 1.00027316;
    /**
     * EARTH FLATTENING f = 1/(298.257,223,563)
     */
    public final static double EARTH_FLATTENING = 1.0 / 298.257223563;
    /**
     * Earth square excentricity e^2 = 2 x f - f^2
     */
    public final static double EARTH_SQUARE_EXCENTRICITY = 2.0 * EARTH_FLATTENING - EARTH_FLATTENING * EARTH_FLATTENING;
    /**
     * Class logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AsproGenConfig.class.getName());

    /**
     * interferometer enum
     */
    private enum INTERFEROMETER {

        /**
         * VLTI (eso)
         */
        VLTI,
        /**
         * CHARA array
         */
        CHARA,
        /**
         * SUSI
         */
        SUSI,
        /**
         * NPOI (experimental)
         */
        NPOI,
        /**
         * MROI (future)
         */
        MROI,
        /* 1T sites */
        SINGLE_DISH
    };

    /**
     * Forbidden constructor
     */
    private AsproGenConfig() {
        // no-op
    }

    /**
     * Generate the new VLTI switchyard using files: 
     * - vlti_opl.txt: [Tel DLxIPy...] = station | optical path length (m)
     * - vlti_vcm_limit.txt: [Tel DLxIPy...] = station | DL pos (m) in range [0;60]
     *
     * @param absFileOPL absolute file path to the VLTI OPL file
     * @param absFileVcm1 absolute file path to the VLTI VCM file for low value (2.5 bar)
     * @param absFileVcm2 absolute file path to the VLTI VCM file for med. value (2.75 bar)
     * @param absFileVcm3 absolute file path to the VLTI VCM file for high value (3.0 bar)
     */
    private static void generateSwitchYard(final String absFileOPL,
                                           final String absFileVcm1, final String absFileVcm2, final String absFileVcm3) {

        logger.info("convertSwitchYard: " + absFileOPL + ", " + absFileVcm1 + ", " + absFileVcm3);

        // enable / disable delay line throw restrictions (VCM):
        // disabled since Oct 2015:
        final boolean doDLRestrictions = false;

        final StringBuilder sb = new StringBuilder(16384);
        sb.append("<switchyard>\n");

        // column separator :
        final String delimiter = " ";

        final Set<String> channelSet = new LinkedHashSet<String>(6 * 8);

        final int maxDL = 6;
        final int maxIP = 4;
        final int maxSize = maxDL * maxIP;
        final double maximumThrow = 100d;
        /* 100m */

        // load data from file :
        BufferedReader readerOPL = null;
        BufferedReader readerVcm1 = null;
        BufferedReader readerVcm2 = null;
        BufferedReader readerVcm3 = null;
        try {
            readerOPL = new BufferedReader(new FileReader(new File(absFileOPL)));
            readerVcm1 = new BufferedReader(new FileReader(new File(absFileVcm1)));
            readerVcm2 = new BufferedReader(new FileReader(new File(absFileVcm2)));
            readerVcm3 = new BufferedReader(new FileReader(new File(absFileVcm3)));

            int i, j;
            Integer idx;
            String lineOPL, lineVcm1, lineVcm2, lineVcm3;
            StringTokenizer tok;
            // outputs :
            String station, stationVcm, channel;
            double oplFixed, maxDLPupilThrow;
            final Map<String, Integer> oplIdx = new HashMap<String, Integer>(maxSize);
            final Map<String, Integer> vcmIdx = new HashMap<String, Integer>(maxSize);
            final double[] oplDL = new double[maxSize];
            final double[] maxDLThrowVcm1 = new double[maxSize];
            final double[] maxDLThrowVcm2 = new double[maxSize];
            final double[] maxDLThrowVcm3 = new double[maxSize];
            String dl, ip, key;

            while ((lineOPL = readerOPL.readLine()) != null
                    && (lineVcm1 = readerVcm1.readLine()) != null
                    && (lineVcm2 = readerVcm2.readLine()) != null
                    && (lineVcm3 = readerVcm3.readLine()) != null) {

                if (lineOPL.startsWith("#")
                        || lineVcm1.startsWith("#")
                        || lineVcm2.startsWith("#")
                        || lineVcm3.startsWith("#")) {

                    // parse header:
                    tok = new StringTokenizer(lineOPL, delimiter);
                    i = 0;
                    while (tok.hasMoreTokens()) {
                        if (i == 0) {
                            // station name :
                            tok.nextToken();
                        } else {
                            if (i > maxSize) {
                                break;
                            }
                            key = tok.nextToken(); // format 'DLxIPy'
                            oplIdx.put(key, Integer.valueOf(i - 1));
                        }
                        i++;
                    }
                    logger.debug("oplIdx: {}", oplIdx);

                    // parse VCM header (once):
                    tok = new StringTokenizer(lineVcm1, delimiter);
                    i = 0;
                    while (tok.hasMoreTokens()) {
                        if (i == 0) {
                            // station name :
                            tok.nextToken();
                        } else {
                            if (i > maxSize) {
                                break;
                            }
                            key = tok.nextToken(); // format 'DLxIPy'
                            vcmIdx.put(key, Integer.valueOf(i - 1));
                        }
                        i++;
                    }
                    logger.debug("vcmIdx: {}", vcmIdx);

                    if (oplIdx.size() != vcmIdx.size()) {
                        throw new IllegalStateException("Bad dimensions for OPL vs VCM");
                    }

                    continue;
                }

                /* parse OPL */
                station = null;

                tok = new StringTokenizer(lineOPL, delimiter);
                i = 0;
                while (tok.hasMoreTokens()) {
                    if (i == 0) {
                        // station name :
                        station = tok.nextToken();
                    } else {
                        if (i > maxSize) {
                            break;
                        }
                        oplDL[i - 1] = Double.parseDouble(tok.nextToken());
                    }
                    i++;
                }

                /* parse VCM threshold 1 */
                tok = new StringTokenizer(lineVcm1, delimiter);
                i = 0;
                while (tok.hasMoreTokens()) {
                    if (i == 0) {
                        // station name :
                        stationVcm = tok.nextToken();
                        if (!stationVcm.equals(station)) {
                            throw new IllegalStateException("station mismatch [" + stationVcm + " <> " + station + "] !");
                        }
                    } else {
                        if (i > maxSize) {
                            break;
                        }
                        maxDLThrowVcm1[i - 1] = Double.parseDouble(tok.nextToken());
                    }
                    i++;
                }

                /* parse VCM threshold 2 */
                tok = new StringTokenizer(lineVcm2, delimiter);
                i = 0;
                while (tok.hasMoreTokens()) {
                    if (i == 0) {
                        // station name :
                        stationVcm = tok.nextToken();
                        if (!stationVcm.equals(station)) {
                            throw new IllegalStateException("station mismatch [" + stationVcm + " <> " + station + "] !");
                        }
                    } else {
                        if (i > maxSize) {
                            break;
                        }
                        maxDLThrowVcm2[i - 1] = Double.parseDouble(tok.nextToken());
                    }
                    i++;
                }

                /* parse VCM threshold 3 */
                tok = new StringTokenizer(lineVcm3, delimiter);
                i = 0;
                while (tok.hasMoreTokens()) {
                    if (i == 0) {
                        // station name :
                        stationVcm = tok.nextToken();
                        if (!stationVcm.equals(station)) {
                            throw new IllegalStateException("station mismatch [" + stationVcm + " <> " + station + "] !");
                        }
                    } else {
                        if (i > maxSize) {
                            break;
                        }
                        maxDLThrowVcm3[i - 1] = Double.parseDouble(tok.nextToken());
                    }
                    i++;
                }

                if (station != null) {
                    /*
                     <stationLinks>
                     <station>U1</station>
                     <channelLink>
                     <channel>IP1</channel>
                     <opticalLength>165.8497</opticalLength>
                     </channelLink>
                     ...
                     </stationLinks>
                     */

                    // output :
                    sb.append("<stationLinks>\n");
                    sb.append("<station>").append(fixStationName(station)).append("</station>\n");

                    // Generate all combinations: (DL / IP)
                    for (i = 0; i < maxDL; i++) {
                        dl = "DL" + (i + 1);

                        /* only generate IP[1,3,5,7], others are unused */
                        for (j = 1; j < 8; j += 2) {
                            ip = "IP" + (j);

                            key = dl + ip;
                            idx = oplIdx.get(key);

                            if (idx == null) {
                                logger.info("Missing OPL for [{}]", key);
                            } else {
                                oplFixed = oplDL[idx];

                                // skip blanking values (-10000 ie invalid):
                                if (oplFixed > 0.0) {
                                    sb.append("<channelLink>\n");

                                    /* Channel names are IPn */
                                    channel = ip;
                                    channelSet.add(channel);

                                    sb.append("<channel>").append(channel).append("</channel>\n");
                                    sb.append("<opticalLength>").append(trimTo4Digits(oplFixed)).append("</opticalLength>\n");

                                    /* delay line associated */
                                    sb.append("<delayLine>").append(dl).append("</delayLine>\n");

                                    if (doDLRestrictions) {
                                        idx = vcmIdx.get(key);

                                        if (idx != null) {
                                            // skip blanking values for IP/DL (10000 ie no constraint; -10000 ie invalid):
                                            maxDLPupilThrow = maxDLThrowVcm1[idx];
                                            if (maxDLPupilThrow >= maximumThrow) {
                                                maxDLPupilThrow = maximumThrow;
                                            }
                                            if (maxDLPupilThrow > 0.0) {
                                                sb.append("<delayLineThrow restriction=\"vcm1\">").append(trimTo4Digits(maxDLPupilThrow)).append("</delayLineThrow>\n");
                                            }

                                            maxDLPupilThrow = maxDLThrowVcm2[idx];
                                            if (maxDLPupilThrow >= maximumThrow) {
                                                maxDLPupilThrow = maximumThrow;
                                            }
                                            if (maxDLPupilThrow > 0.0) {
                                                sb.append("<delayLineThrow restriction=\"vcm2\">").append(trimTo4Digits(maxDLPupilThrow)).append("</delayLineThrow>\n");
                                            }

                                            maxDLPupilThrow = maxDLThrowVcm3[idx];
                                            if (maxDLPupilThrow >= maximumThrow) {
                                                maxDLPupilThrow = maximumThrow;
                                            }
                                            if (maxDLPupilThrow > 0.0) {
                                                sb.append("<delayLineThrow restriction=\"vcm3\">").append(trimTo4Digits(maxDLPupilThrow)).append("</delayLineThrow>\n");
                                            }
                                        }
                                    }
                                    sb.append("</channelLink>\n");
                                }
                            }
                        }
                    }

                    sb.append("</stationLinks>\n");
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(readerOPL);
            FileUtils.closeFile(readerVcm1);
            FileUtils.closeFile(readerVcm2);
            FileUtils.closeFile(readerVcm3);
        }

        sb.append("</switchyard>\n");

        final StringBuilder sb1 = new StringBuilder(16384);

        /* channels */
        for (String ch : channelSet) {
            sb1.append("<channel>\n  <name>").append(ch).append("</name>\n</channel>\n");
        }
        sb1.append("\n\n");

        /* dl */
        for (int i = 0; i < maxDL; i++) {
            sb1.append("<delayLine>\n  <name>DL").append(i + 1).append("</name>\n")
                    .append("  <maximumThrow>").append(maximumThrow).append("</maximumThrow>\n</delayLine>\n");
        }
        sb1.append("\n\n");

        logger.info("convertSwitchYard : output :\n" + sb1.toString() + sb.toString());
    }

    private static String fixStationName(final String name) {
        // Fix Ux = UTx
        if (name.startsWith("U") && !name.startsWith("UT")) {
            return "UT" + name.substring(1, 2);
        }
        return name;
    }

    /**
     * Convert ESO VLTI horizons for the given station
     *
     * @param station station name
     * @param absFileName absolute file path of the ESO station profile
     * @param sb output buffer for xml output
     */
    private static void convertHorizon(final String station, final String absFileName, final StringBuilder sb) {

        sb.append("<station>\n");
        sb.append("<name>").append(station).append("</name>\n");
        sb.append("<horizon>\n");

        // number of columns filled with double values :
        final int maxCols = 2;
        // column separator :
        final String delimiter = "\t";

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            System.out.println("reading: " + absFileName);

            reader = new BufferedReader(new FileReader(data));

            String line;
            StringTokenizer tok;
            // outputs :
            final double[] values = new double[maxCols];

            final double[] prevVals = new double[]{Double.NaN, Double.NaN};

            double elevAtAz0 = Double.NaN;

            boolean use, prev = false;

            final ArrayList<double[]> rows = new ArrayList<double[]>(1024);

            while ((line = reader.readLine()) != null) {
//                System.out.println("line: ["+line+"]");

                if (!line.startsWith("#")) {

                    // replace multiple delimiters :
                    tok = new StringTokenizer(line, delimiter);

                    int i = 0;
                    while (tok.hasMoreTokens()) {
                        if (i < maxCols) {
                            values[i] = Double.parseDouble(tok.nextToken());
                        }
                        i++;
                    }

                    use = false;

                    // check first Az:
                    if (Double.isNaN(prevVals[0])) {
                        if (values[0] != 0.0) {
                            throw new IllegalStateException("Invalid first azimuth value [expected az=0] !");
                        }
                        elevAtAz0 = values[1];
                        use = true;
                    } else if (prevVals[1] != values[1]) {
                        use = true;
                    } else {
                        prevVals[0] = values[0];
                    }

                    if (use) {
                        /*
                     System.out.println("values: " + Arrays.toString(values));
                         */
                        if (!prev && !Double.isNaN(prevVals[0])) {
//                            System.out.println("add prev: " + Arrays.toString(prevVals));

                            final double[] row = new double[maxCols];
                            row[0] = prevVals[0];
                            row[1] = prevVals[1];
                            rows.add(row);
                        }

                        final double[] row = new double[maxCols];
                        row[0] = values[0];
                        row[1] = values[1];
                        rows.add(row);

                        // copy values:
                        prevVals[0] = values[0];
                        prevVals[1] = values[1];
                    }
                    prev = use;
                }
            }

            if (prevVals[0] != 360.0) {
                if (prevVals[1] != elevAtAz0) {
                    throw new IllegalStateException("Invalid last azimuth value [expected az=360 el=" + elevAtAz0 + "] !");
                }
            }

//            System.out.println("rows: "+rows.size());
            for (int i = 0, len = rows.size(); i < len; i++) {
                final double[] row = rows.get(i);

                // VLTI:
                // fix VLTI azimuth (south = 0, east = 90):
                double az = 180.0 - row[0]; // row[0] is within [0;360]
                if (az < 0.0) {
                    az += 360.0;
                }

//                System.out.println("row : " + Arrays.toString(row) + " Fix az: " + row[0] + " => "+az);
                row[0] = az;
            }

            // Sort rows by az [0;360]:
            Collections.sort(rows, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {
                    return Double.compare(o1[0], o2[0]);
                }
            });

            // Check again continuity at az=0 and az=360:
            double[] row = rows.get(0);
            if (row[0] != 0.0) {
                rows.add(0, new double[]{0.0, row[1]});
            }

            row = rows.get(rows.size() - 1);
            if (row[0] != 360.0) {
                rows.add(new double[]{360.0, row[1]});
            }

//            System.out.println("sorted:");
            for (int i = 0, len = rows.size(); i < len; i++) {
                row = rows.get(i);
//                System.out.println("row : " + Arrays.toString(row));

                // output :
                sb.append("<point>");
                sb.append("<azimuth>").append(row[0]).append("</azimuth>");
                sb.append("<elevation>").append(row[1]).append("</elevation>");
                sb.append("</point>");
            }
            // Add upper limits:
            sb.append("<point><azimuth>360</azimuth><elevation>90</elevation></point>");
            sb.append("<point><azimuth>0</azimuth><elevation>90</elevation></point>");

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }

        sb.append("\n</horizon>\n");
        sb.append("</station>\n");
    }

    private static void convertHorizonTable(final String path, final String tableFileName) {
        final String absFileName = new File(path, tableFileName).getAbsolutePath();

        // outputs :
        final List<String> headers = new ArrayList<String>();
        final List<String> columns = new ArrayList<String>();
        final Map<String, ArrayList<String>> datas = new LinkedHashMap<String, ArrayList<String>>();

        // column separator :
        String delimiter = " ";

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            System.out.println("reading: " + absFileName);

            reader = new BufferedReader(new FileReader(data));

            String line;
            StringTokenizer tok;

            boolean header = true;

            while ((line = reader.readLine()) != null) {
//                System.out.println("line: [" + line + "]");

                if (line.startsWith("#")) {
                    if (header) {
                        // locate # az
                        if (header && line.startsWith("# az")) {
                            // parse columns:
                            tok = new StringTokenizer(line.substring(2), delimiter);

                            while (tok.hasMoreTokens()) {
                                columns.add(tok.nextToken());
                            }

                            logger.info("columns: {}", columns);

                            for (String c : columns) {
                                datas.put(c, new ArrayList<String>(361 * 2)); // step = 0.5 deg
                            }

                            // table header found:
                            header = false;
                        } else {
                            // keep header:
                            headers.add(line);
                        }
                    }
                } else {
                    if (header) {
                        System.err.println("Missing header; stop");
                        break;
                    }
                    // replace multiple delimiters :
                    tok = new StringTokenizer(line, delimiter);

                    int i = 0;
                    while (tok.hasMoreTokens()) {
                        datas.get(columns.get(i++)).add(tok.nextToken());
                    }

                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }

        if (false) {
            logger.info("headers: {}", headers);
            logger.info("columns: {}", columns);

            for (String c : columns) {
                logger.info("column[{}]: {}", c, datas.get(c));
            }
        }

        final String AZ = "az";
        delimiter = "\t";

        for (String c : columns) {
            if (!c.equals(AZ)) {
                final File output = new File(path, c + ".horizon");

                System.out.println("writing: " + output);

                final Writer w = FileUtils.openFile(output);
                try {
                    for (String line : headers) {
                        w.write(line);
                        w.write("\n");
                    }
                    w.write("# az\televation_limit");
                    w.write("\n");

                    final ArrayList<String> az = datas.get(AZ);
                    final ArrayList<String> el = datas.get(c);

                    for (int i = 0, len = az.size(); i < len; i++) {
                        w.write(az.get(i));
                        w.write(delimiter);
                        w.write(el.get(i));
                        w.write("\n");
                    }

                } catch (IOException ioe) {
                    logger.error("IO failure", ioe);
                } finally {
                    FileUtils.closeFile(w);
                }
            }
        }
    }

    /**
     * Load and parse the vlti array list
     *
     * @param absFileName
     * @param periods
     */
    private static void convertArrayList(final String absFileName, final String[] periods) {

        /**
         * package name for JAXB generated code
         */
        final String OI_JAXB_PATH = "fr.jmmc.aspro.model.oi";
        final String CONF_CLASSLOADER_PATH = "fr/jmmc/aspro/model/";

        final String uri = "VLTI.xml";

        final JAXBFactory jf = JAXBFactory.getInstance(OI_JAXB_PATH);

        Object result = null;
        try {
            // use the class loader resource resolver
            final URL url = ResourceUtils.getResource(CONF_CLASSLOADER_PATH + uri);

            // Note : use input stream to avoid JNLP offline bug with URL (Unknown host exception)
            result = jf.createUnMarshaller().unmarshal(new BufferedInputStream(url.openStream()));

        } catch (IOException ioe) {
            throw new IllegalStateException("Load failure on " + uri, ioe);
        } catch (JAXBException je) {
            throw new IllegalArgumentException("Load failure on " + uri, je);
        }

        final InterferometerSetting is = (InterferometerSetting) result;

        // always use the first switchyard:
        final SwitchYard sw = is.getDescription().getSwitchyards().get(0);
        if (sw == null) {
            throw new IllegalStateException("No switchyard found in " + uri + " !");
        }
        logger.info("Using SwitchYard: " + sw.getName());

        final Map<String, Channel> channels = new HashMap<String, Channel>(8);

        for (Channel ch : is.getDescription().getChannels()) {
            channels.put(ch.getName(), ch);
        }

        final Map<String, DelayLine> delayLines = new HashMap<String, DelayLine>(8);

        for (DelayLine dl : is.getDescription().getDelayLines()) {
            delayLines.put(dl.getName(), dl);
        }

        /* Process a single period or update all periods */
        List<InterferometerConfiguration> confToProcess;
        if (periods == null) {
            confToProcess = new ArrayList<InterferometerConfiguration>(is.getConfigurations());
        } else {
            confToProcess = new ArrayList<InterferometerConfiguration>(periods.length);
            for (InterferometerConfiguration ic : is.getConfigurations()) {
                if (contains(periods, ic.getVersion())) {
                    logger.info("Configuration '" + ic.getVersion() + "' match in " + uri + " ...");
                    confToProcess.add(ic);
                }
            }

            if (confToProcess.isEmpty()) {
                throw new IllegalStateException("Configuration '" + periods + "' not found in " + uri + " !");
            }

            logger.info("Configuration(s) found in " + uri + " ...");
        }

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            reader = new BufferedReader(new FileReader(data));

            // station separator:
            final String delimiter = "-";

            int pos, idx, nTel;
            boolean match = false;
            Channel ch;
            List<Channel> confIPs;
            DelayLine dl;
            List<DelayLine> confDLs;

            StringTokenizer tok;
            String line, config;

            // mapping: 0 = delayLine (DL), 1 = station, 2 = channel (IP):
            final int DL = 0;
            final int STA = 1;
            final int IP = 2;
            final String[][] mappings = new String[4][3]; // max 4T

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--")) {
                    continue;
                }
                line = line.replaceAll("\\s+", " ").trim();

                if (line.length() > 0) {
//                        logger.info("line: {}", line);
                    /* "DL6A0IP1-DL5B2IP3-DL1D0IP5-DL2C1IP7" */

                    // Parse values :
                    tok = new StringTokenizer(line, delimiter);

                    idx = 0;

                    while (tok.hasMoreTokens()) {
                        config = tok.nextToken();
//                            logger.info("config: {}", config);

                        mappings[idx][DL] = config.substring(0, 3); // first 3 chars (DL3)
                        // Fix Ux = UTx
                        mappings[idx][STA] = fixStationName(config.substring(3, 5)); // next 2 chars (A1)
                        mappings[idx][IP] = config.substring(5, 8); // last char (IP5)
//                            logger.info("station: {} - DL: {} - IP: {}", mappings[idx][STA], mappings[idx][DL], mappings[idx][IP]);

                        idx++;
                    }
                    nTel = idx;

                    // Check switchyard:
                    /*
                     <switchyard>
                     <stationLinks>
                     <station>A0</station>
                     <channelLink>
                     <channel>IP1</channel>
                     <delayLine>DL1</delayLine>
                     <opticalLength>134.8395</opticalLength>
                     <maximumThrow>39.4</maximumThrow>
                     </channelLink>
                     ...
                     */
                    for (pos = 0; pos < nTel; pos++) {
                        match = false;
                        for (StationLinks sl : sw.getStationLinks()) {
                            if (sl.getStation().getName().equalsIgnoreCase(mappings[pos][STA])) {
                                // station found in switchyard:
                                for (ChannelLink cl : sl.getChannelLinks()) {
                                    if (cl.getChannel().getName().equalsIgnoreCase(mappings[pos][IP])) {
                                        match = true;
                                        if ((cl.getDelayLine() != null) && (!cl.getDelayLine().getName().equalsIgnoreCase(mappings[pos][DL]))) {
                                            match = false;
                                        }
                                        if (match) {
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        if (!match) {
                            logger.error("IP [{}] or DL [{}] not defined in switchyard for station: [{}]",
                                    mappings[pos][IP], mappings[pos][DL], mappings[pos][STA]);
                        }
                    }

                    // Find configuration:
                    match = false;

                    for (InterferometerConfiguration ic : confToProcess) {

                        for (FocalInstrumentConfiguration insConf : ic.getInstruments()) {
                            if (nTel == insConf.getFocalInstrument().getNumberChannels()) {
                                // logger.info("checking instrument [{}]", insConf.getFocalInstrument().getName());

                                for (FocalInstrumentConfigurationItem conf : insConf.getConfigurations()) {

                                    // Find matching stations:
                                    idx = 0;
                                    for (pos = 0; pos < nTel; pos++) {
                                        for (Station sta : conf.getStations()) {
                                            if (sta.getName().equalsIgnoreCase(mappings[pos][STA])) {
                                                idx++;
                                            }
                                        }
                                    }

                                    if (idx == nTel) {
                                        // matching:
                                        match = true;
                                        //                                        logger.info("conf match: [{}]: {}", conf.getName(), line);

                                        // Update channels in conf:
                                        confIPs = conf.getChannels();
                                        confIPs.clear(); // reset

                                        // Update delay lines in conf:
                                        confDLs = conf.getDelayLines();
                                        confDLs.clear(); // reset

                                        // for all stations in conf:
                                        for (Station sta : conf.getStations()) {
                                            ch = null;
                                            dl = null;
                                            /* find again if order is different */
                                            for (pos = 0; pos < nTel; pos++) {
                                                if (sta.getName().equalsIgnoreCase(mappings[pos][STA])) {
                                                    dl = delayLines.get(mappings[pos][DL]);
                                                    ch = channels.get(mappings[pos][IP]);
                                                    break;
                                                }
                                            }
                                            // add channel:
                                            if (ch == null) {
                                                logger.error("no channel found for station: {}", sta.getName());
                                            } else {
                                                confIPs.add(ch);
                                            }
                                            // add DL:
                                            if (dl == null) {
                                                logger.error("no DL found for station: {}", sta.getName());
                                            } else {
                                                confDLs.add(dl);
                                            }
                                        }
                                        if (confIPs.size() != nTel) {
                                            logger.error("Missing channel : [{}] for stations [{}]", confIPs, conf.getStations());
                                        } else {
                                            // logger.info("channels: [{}] for stations [{}]", confIPs, conf.getStations());
                                        }
                                        if (confDLs.size() != nTel) {
                                            logger.error("Missing DL : [{}] for stations [{}]", confDLs, conf.getStations());
                                        } else {
                                            // logger.info("DL: [{}] for stations [{}]", confDLs, conf.getStations());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!match) {
                        logger.error("no conf match: {}", line);
                    }
                }
            } // line

            // AT Relocation:
            // Generate new baselines derived from PIONIER quadruplets:
            InterferometerConfiguration icRelocate = null;
            for (InterferometerConfiguration ic : confToProcess) {
                if ("AT Relocation".equals(ic.getVersion())) {
                    icRelocate = ic;
                    break;
                }
            }
            if (icRelocate != null) {
                FocalInstrumentConfiguration insConfPionier = null;
                for (FocalInstrumentConfiguration insConf : icRelocate.getInstruments()) {
                    if ("PIONIER".equals(insConf.getFocalInstrument().getName())) {
                        insConfPionier = insConf;
                        break;
                    }
                }
                if (insConfPionier != null) {
                    final int maxTel = insConfPionier.getFocalInstrument().getNumberChannels();

                    final List<List<int[]>> combsList = new ArrayList<List<int[]>>(maxTel - 2);
                    for (int i = maxTel - 1; i > 1; i--) {
                        combsList.add(CombUtils.generateCombinations(maxTel, i));
                    }

                    final Comparator<Station> cmp = new Comparator<Station>() {

                        @Override
                        public int compare(final Station s1, final Station s2) {
                            return s1.getName().compareTo(s2.getName());
                        }

                    };

                    final List<Station> confSta = new ArrayList<Station>(maxTel);
                    final List<Station> ordSta = new ArrayList<Station>(maxTel);
                    final List<Station> ordStaOther = new ArrayList<Station>(maxTel);

                    final List<Channel> confCh = new ArrayList<Channel>(maxTel);
                    final List<DelayLine> confDL = new ArrayList<DelayLine>(maxTel);

                    for (FocalInstrumentConfigurationItem conf : insConfPionier.getConfigurations()) {
                        // for all 2T / 3T ...
                        for (List<int[]> combs : combsList) {
                            // Generate all combinations:

                            for (int[] cb : combs) {
                                confSta.clear();
                                confCh.clear();
                                confDL.clear();

                                nTel = cb.length;

                                for (int i = 0; i < nTel; i++) {
                                    idx = cb[i];

                                    confSta.add(conf.getStations().get(idx));
                                    // Suppose we have channels & delay lines:
                                    confCh.add(conf.getChannels().get(idx));
                                    confDL.add(conf.getDelayLines().get(idx));
                                }
                                logger.debug("generated {}T conf: {} {} {}", nTel, confSta, confCh, confDL);

                                // Sort stations:
                                ordSta.clear();
                                ordSta.addAll(confSta);
                                Collections.sort(ordSta, cmp);

                                logger.debug("sorted {}T conf: {}", nTel, ordSta);

                                for (FocalInstrumentConfiguration insConf : icRelocate.getInstruments()) {
                                    if (nTel == insConf.getFocalInstrument().getNumberChannels()) {

                                        // check duplicates:
                                        boolean found = false;
                                        for (FocalInstrumentConfigurationItem confOther : insConf.getConfigurations()) {
                                            // Sort stations:
                                            ordStaOther.clear();
                                            ordStaOther.addAll(confOther.getStations());
                                            Collections.sort(ordStaOther, cmp);

                                            logger.debug("sorted other {}T conf: {}", nTel, ordStaOther);

                                            if (ordSta.equals(ordStaOther)) {
                                                found = true;
                                                break;
                                            }
                                        }

                                        if (!found) {
                                            logger.info("AT Relocation: added new {}T configuration: {}", nTel, confSta);

                                            final FocalInstrumentConfigurationItem confItem = new FocalInstrumentConfigurationItem();
                                            confItem.getStations().addAll(confSta);
                                            confItem.getChannels().addAll(confCh);
                                            confItem.getDelayLines().addAll(confDL);

                                            insConf.getConfigurations().add(confItem);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Check that every configuration has valid Station / DL / IP mapping
            int nErrors = 0;
            final Set<Station> usedStations = new HashSet<Station>(8);
            final Set<Channel> usedChannels = new HashSet<Channel>(8);
            final Set<DelayLine> usedDelayLines = new HashSet<DelayLine>(8);

            for (InterferometerConfiguration ic : confToProcess) {
                logger.debug("checking configuration {}", ic.getVersion());
                for (FocalInstrumentConfiguration insConf : ic.getInstruments()) {
                    logger.debug("checking instrument {}", insConf.getFocalInstrument());
                    for (FocalInstrumentConfigurationItem conf : insConf.getConfigurations()) {

                        final int len = conf.getStations().size();

                        if (len != insConf.getFocalInstrument().getNumberChannels()) {
                            logger.error("invalid configuration for the instrument: {} {} : {} {} {}", ic.getVersion(),
                                    insConf.getFocalInstrument().getName(),
                                    conf.getStations(), conf.getChannels(), conf.getDelayLines());
                            nErrors++;
                            continue;
                        }

                        final boolean hasCh = !conf.getChannels().isEmpty();
                        final boolean hasDL = !conf.getDelayLines().isEmpty();

                        logger.debug("checking Station/Channel/DL: {} {} {}",
                                conf.getStations(), conf.getChannels(), conf.getDelayLines());

                        if (hasCh || hasDL) {
                            // check channels & DL:
                            if ((len != conf.getChannels().size()) || (len != conf.getDelayLines().size())) {
                                logger.error("invalid Station/Channel/DL for configuration: {} {} : {} {} {}", ic.getVersion(),
                                        insConf.getFocalInstrument().getName(),
                                        conf.getStations(), conf.getChannels(), conf.getDelayLines());
                                nErrors++;
                                continue;
                            }
                        }

                        usedStations.clear();
                        usedChannels.clear();
                        usedDelayLines.clear();

                        for (pos = 0; pos < len; pos++) {
                            Station sta = conf.getStations().get(pos);
                            ch = (hasCh) ? conf.getChannels().get(pos) : null;
                            dl = (hasDL) ? conf.getDelayLines().get(pos) : null;

                            // Check duplicates:
                            if (usedStations.contains(sta)) {
                                logger.error("duplicate stations for configuration: {} {} : {} {} {}", ic.getVersion(),
                                        insConf.getFocalInstrument().getName(),
                                        conf.getStations(), conf.getChannels(), conf.getDelayLines());
                                nErrors++;
                                break;
                            }
                            usedStations.add(sta);

                            if (hasCh) {
                                if (usedChannels.contains(ch)) {
                                    logger.error("duplicate channels for configuration: {} {} : {} {} {}", ic.getVersion(),
                                            insConf.getFocalInstrument().getName(),
                                            conf.getStations(), conf.getChannels(), conf.getDelayLines());
                                    nErrors++;
                                    break;
                                }
                                usedChannels.add(ch);
                            }
                            if (hasDL) {
                                if (usedDelayLines.contains(dl)) {
                                    logger.error("duplicate DLs for configuration: {} {} : {} {} {}", ic.getVersion(),
                                            insConf.getFocalInstrument().getName(),
                                            conf.getStations(), conf.getChannels(), conf.getDelayLines());
                                    nErrors++;
                                    break;
                                }
                                usedDelayLines.add(dl);
                            }

                            // check switchyard:
                            boolean found = false;
                            for (StationLinks sl : sw.getStationLinks()) {
                                if (sl.getStation().getName().equalsIgnoreCase(sta.getName())) {
                                    // station found in switchyard:
                                    if (!hasCh) {
                                        found = true;
                                        break;
                                    } else {
                                        for (ChannelLink cl : sl.getChannelLinks()) {
                                            if (cl.getChannel().equals(ch)) {
                                                found = true;
                                                if ((cl.getDelayLine() != null) && (!cl.getDelayLine().equals(dl))) {
                                                    found = false;
                                                }
                                                if (found) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!found) {
                                logger.error("invalid Station/Channel/DL for configuration: {} {}: {} {} {}", ic.getVersion(),
                                        insConf.getFocalInstrument().getName(),
                                        sta, ch, dl);
                                nErrors++;
                            }
                        }
                    }
                }
            }

            if (nErrors != 0) {
                throw new IllegalStateException("Errors detected in VLTI configuration !");
            }

            if (match) {
                // Dump updated VLTI configuration with channels:
                try {
                    final ByteArrayOutputStream bo = new ByteArrayOutputStream(512 * 1024);

                    jf.createMarshaller().marshal(is, bo);

                    final String vltiConfig = bo.toString("UTF-8");

                    logger.info("Updated VLTI configuration:\n{}", vltiConfig);

                } catch (JAXBException je) {
                    throw new IllegalArgumentException("Load failure on " + uri, je);
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }
    }

    /**
     * Convert horizontal coordinates to equatorial coordinates
     *
     * # XOFFSET - East offset in microns from S1 # YOFFSET - North offset in microns from S1 # ZOFFSET - vertical (+ is
     * up) offset in microns from S1
     *
     * @param station station name
     * @param latitude latitude of the interferometer (rad)
     * @param xOffset East offset in microns from S1
     * @param yOffset North offset in microns from S1
     * @param zOffset vertical (+ is up) offset in microns from S1
     * @param sb output buffer for xml output
     * @return equatorial coordinates
     */
    private static Position3D convertHorizToEquatorial(final String station, final double latitude,
                                                       final double xOffset, final double yOffset, final double zOffset,
                                                       final StringBuilder sb) {

        final double x = xOffset * 1e-6;
        final double y = yOffset * 1e-6;
        final double z = zOffset * 1e-6;

        // use geodetic latitude
        final double xx = -Math.sin(latitude) * y + Math.cos(latitude) * z;
        final double yy = x;
        final double zz = Math.cos(latitude) * y + Math.sin(latitude) * z;

        logger.info(station + " = (" + xx + ", " + yy + ", " + zz + ")");

        sb.append("      <relativePosition>\n");
        sb.append("        <posX>").append(xx).append("</posX>\n");
        sb.append("        <posY>").append(yy).append("</posY>\n");
        sb.append("        <posZ>").append(zz).append("</posZ>\n");
        sb.append("      </relativePosition>\n");

        return getPoint3D(xx, yy, zz);
    }

    /**
     * Compute CHARA fixed delay (AIRPATH + LIGHT)
     *
     * # AIRPATH - amount of airpath in microns using default beam #	Note that this assumes the default Beam dn default
     * Pop are used # LIGHT	- length of light pipe in microns #	Note that this assumes the default Beam dn default Pop are
     * used
     *
     * @param station station name
     * @param light length of light pipe in microns
     * @param airPath amount of airpath in microns using default beam
     * @param sb output buffer for xml output
     */
    public static void convertCHARAAirPath(final String station, final double light, final double airPath,
                                           final StringBuilder sb) {

        final double delay = (light + airPath * N_AIR) * 1e-6;

        logger.info(station + " = " + delay);

        sb.append("      <delayLineFixedOffset>").append(delay).append("</delayLineFixedOffset>\n");
    }

    /**
     * Compute CHARA PoP delay (POPX)
     *
     * # POPX	- Extra airpath to add when using POP X on this scope
     *
     * @param config station config
     * @param sb output buffer for xml output
     */
    public static void convertCHARAPoP(final Map<String, Double> config,
                                       final StringBuilder sb) {

        for (int i = 1; i <= 5; i++) {
            sb.append("      <popLink>\n");
            sb.append("        <pop>PoP").append(i).append("</pop>\n");
            sb.append("        <opticalLength>").append(config.get("POP" + i) * 1e-6).append("</opticalLength>\n");
            sb.append("      </popLink>\n");
        }
    }

    /**
     * Convert CHARA station configs to ASPRO station configurations
     *
     * @param stationConfigs station configs
     * @param sb buffer
     */
    private static void convertCHARAStations(final Map<String, Map<String, Double>> stationConfigs, final StringBuilder sb) {

        final double[] lonlat = CHARAposition(sb);

        final double lat = Math.toRadians(lonlat[1]);
        
        if (false) {
            /*
            S3lightpath + S3airpath = W2lightpath+W2airpath in these lines in both cases. 
            We do not plan POPs as for W2 we have never used anything other than POP5. 
            We will use the line on the north side of the beam sampling table that now holds the metrology system. 
            This means the S3airpath = S2airpath - (W2airpath-S2airpath)  and S3lightpath = W2airpath+W2lightpath - S3airpath
            */
            final Map<String, Double> w2 = stationConfigs.get("W2");
            final Map<String, Double> s2 = stationConfigs.get("S2");
            
            final double s3air = s2.get("AIRPATH") - (w2.get("AIRPATH") - s2.get("AIRPATH"));
            final double s3light = w2.get("AIRPATH") + w2.get("LIGHT") - s3air;
            
            System.out.println("s3air: "+s3air);
            System.out.println("s3light: "+s3light);
            
            final Map<String, Double> p4 = stationConfigs.get("P4");
            final Map<String, Double> p5 = stationConfigs.get("P5");
            
            p4.put("AIRPATH", s3air);
            p4.put("LIGHT", s3light);
            
            p5.put("AIRPATH", s3air);
            p5.put("LIGHT", s3light);
        }

        String station;
        Map<String, Double> config;
        for (Map.Entry<String, Map<String, Double>> e : stationConfigs.entrySet()) {
            station = e.getKey();
            config = e.getValue();

            sb.append("    <station>\n");
            sb.append("      <name>").append(station).append("</name>\n");
            sb.append("      <telescope>T</telescope>\n");

            convertHorizToEquatorial(station, lat, config.get("XOFFSET"), config.get("YOFFSET"), config.get("ZOFFSET"), sb);

            convertCHARAAirPath(station, config.get("LIGHT"), config.get("AIRPATH"), sb);

            sb.append("\n");

            convertCHARAPoP(config, sb);

            convertCHARAHorizon(station, sb);

            sb.append("    </station>\n\n");
        }

        /*
         S1          0.000000000   0.000000000     0.00000000    0.0000000
         *
         S1 = (0.0, 0.0, 0.0) 0.0
         *
         * 
         *
         S2        -18.354945174    -5.748393059         28.128580103         4.532654762 (ASPRO 1)
         S2 = (-18.35494517183226,  -5.7483930590000005, 28.128580104400015)
         S2 = (-18.364916206804995, -5.7444145760000005, 28.12661922364987)   4.74371459
         *
         S2 = (-18.360473037751714, -5.746854437,        28.12397435836417)   4.690179333000001
         S2 = (-18.360473037751714, -5.746854437,        28.12397435836417)   5.065469333 (+ INTERNAL)
         S2 = (-18.360473037751714, -5.746854437,        28.12397435836417)   5.065469333
         *
         *
         *
         *
         *
         E1       -176.947579603    125.349003099       249.643974581        15.309717902 (ASPRO 1)
         E1 = (-176.94757958628557, 125.349003099,      249.64397459290652)
         E1 = (-176.95914597777053, 125.33313329999999, 249.6252027666933)   15.504650597999998
         *
         E1 = (-176.95372938066825, 125.333989819,      249.63388874133827)  15.513547531999999
         E1 = (-176.95372938066825, 125.333989819,      249.63388874133827)  15.513547531999999 (+ INTERNAL)
         E1 = (-176.95372938066825, 125.333989819,      249.63388874133827)  15.513547531999999
         *
         *
         *
         E2       -154.003121030    70.400864470       221.444997474         26.721359853 (ASPRO 1)
         E2 = (-154.00312101526163, 70.40086447,       221.44499748445782)
         E2 = (-154.01434025673439, 70.3891451,        221.43497871824562)   26.372594395
         *
         E2 = (-154.00873148624743, 70.39660711799999, 221.43708278416645)   26.370872645
         E2 = (-154.00873148624743, 70.39660711799999, 221.43708278416645)   26.743470664999997 (+ INTERNAL)
         E2 = (-154.00873148624743, 70.39660711799999, 221.43708278416645)   26.743470664999997
         *
         *
         *
         W1       -130.620014605    -175.068583489       172.774093891       29.087218284 (ASPRO 1)
         W1 = (-130.6200145937469,  -175.068583489,      172.7740938996687)
         W1 = (-130.59792281771624, -175.0684101,        172.79538958138997) 29.157963303
         *
         W1 = (-130.58878748879556, -175.07333221099998, 172.79336516697768) 29.122251552999998
         W1 = (-130.58878748879556, -175.07333221099998, 172.79336516697768) 29.122251552999998 (+ INTERNAL)
         W1 = (-130.58878748879556, -175.07333221099998, 172.79336516697768) 29.122251552999998
         * 
         *
         *
         W2       -111.736598481    -69.088236186  165.067607370            -5.413000000 !was "-8.654293636???" (ASPRO 1)
         W2 = (-111.73659847358483, -69.088236186, 165.0676073825641)
         W2 = (-111.72810585237123, -69.0845925,   165.08924273662112)      -8.450162002
         *
         W2 = (-111.72648023453111, -69.093582796, 165.08103431717993)      -8.461862557
         W2 = (-111.72648023453111, -69.093582796, 165.08103431717993)      -8.079204556999999 (+ INTERNAL)
         W2 = (-111.72648023453111, -69.093582796, 165.08103431717993)      -8.079204556999999
         */
    }

    /**
     * Convert CHARA station configs to ASPRO switchyard
     *
     * @param stationConfigs station configs
     * @param sb buffer
     */
    public static void convertCHARASwitchyard(final Map<String, Map<String, Double>> stationConfigs, final StringBuilder sb) {

        sb.append("    <switchyard>\n");

        final double[] values = new double[6];

        String station;
        Map<String, Double> config;
        for (Map.Entry<String, Map<String, Double>> e : stationConfigs.entrySet()) {
            station = e.getKey();
            config = e.getValue();

            for (int i = 0; i < 6; i++) {
                values[i] = config.get("BEAM" + (i + 1)) * N_AIR * 1e-6;
            }

            convertCHARASwitchyardStation(station, values, sb);
        }

        sb.append("    </switchyard>\n\n");
    }

    /**
     * Convert CHARA station config to ASPRO stationLinks
     *
     * @param station station config
     * @param values values for beams
     * @param sb output buffer for xml output
     */
    private static void convertCHARASwitchyardStation(final String station, final double[] values,
                                                      final StringBuilder sb) {

        sb.append("      <stationLinks>\n");
        sb.append("        <station>").append(station).append("</station>\n");

        final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(8);

        double value;
        for (int i = 0, size = values.length; i < size; i++) {
            value = values[i];

            sb.append("        <channelLink>\n");
            sb.append("          <channel>V").append(i + 1).append("</channel>\n");
            sb.append("          <opticalLength>").append(nf.format(value)).append("</opticalLength>\n");
            sb.append("        </channelLink>\n");
        }

        sb.append("      </stationLinks>\n\n");
    }

    /**
     * Load the CHARA config file (telescopes.chara)
     *
     * @param absFileName absolute file path to CHARA config file
     * @return chara station config
     *
     * # The USED labels and their data fields are: # # XOFFSET - East offset in microns from S1 # YOFFSET - North offset
     * in microns from S1 # ZOFFSET - vertical (+ is up) offset in microns from S1
     *
     * # AIRPATH - amount of airpath in microns using default beam #	Note that this assumes the default Beam dn default
     * Pop are used # INTERNAL- Pathlength (with default beam) for internal fringes # LIGHT	- length of light pipe in
     * microns #	Note that this assumes the default Beam dn default Pop are used
     *
     * # BEAMX - Extra airpath to add when using beam X on this scope # POPX	- Extra airpath to add when using POP X on
     * this scope
     */
    private static Map<String, Map<String, Double>> loadCHARAConfig(final String absFileName) {

        logger.info("loadCHARAConfig : " + absFileName);

        final List<String> labels = Arrays.asList(new String[]{
            "XOFFSET", "YOFFSET", "ZOFFSET", "AIRPATH", "LIGHT", "INTERNAL",
            "BEAM1", "BEAM2", "BEAM3", "BEAM4", "BEAM5", "BEAM6",
            "POP1", "POP2", "POP3", "POP4", "POP5"
        });

        final Map<String, Map<String, Double>> stationConfigs = new LinkedHashMap<String, Map<String, Double>>(16);

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            reader = new BufferedReader(new FileReader(data));

            // column separator :
            final String delimiter = " ";

            String name = null, key;
            Double value;
            Map<String, Double> current = null;

            StringTokenizer tok;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", " ").trim();
                if (line.length() > 0 && !line.startsWith("#")) {

                    if (name == null) {
                        // start station block :
                        name = line;
                        current = new LinkedHashMap<String, Double>(16);

//            logger.info("new station : " + name);
                        continue;
                    }

                    if ("END".equals(line)) {
                        // end station block :
                        stationConfigs.put(name, current);

                        logger.info("end station : " + name + " =\n" + current);

                        name = null;
                        current = null;

                        continue;
                    }

                    // Parse values :
//          logger.info("line = " + line);
                    tok = new StringTokenizer(line, delimiter);

                    if (tok.hasMoreTokens()) {
                        // key
                        key = tok.nextToken();

                        if (labels.contains(key)) {
                            // value (only first used) :
                            if (tok.hasMoreTokens()) {
                                value = Double.valueOf(tok.nextToken());

                                current.put(key, value);
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }
        return stationConfigs;
    }

    /**
     * Convert the CHARA config file (telescopes.chara)
     *
     * @param absFileName absolute file path to CHARA config file
     */
    private static void convertCHARAConfig(final String absFileName) {

        final Map<String, Map<String, Double>> stationConfigs = loadCHARAConfig(absFileName);

        final StringBuilder sb = new StringBuilder(12 * 1024);

        sb.append("<a:interferometerSetting>\n\n");
        sb.append("  <description>\n\n    <name>CHARA</name>\n\n");

        convertCHARAStations(stationConfigs, sb);

        convertCHARASwitchyard(stationConfigs, sb);

        sb.append("  </description>\n\n</a:interferometerSetting>\n");

        logger.info("Generated CHARA Configuration : " + sb.length() + "\n" + sb.toString());
    }

    /**
     * Compute the CHARA position (S1 coordinates)
     *
     * @param sb buffer
     * @return long and lat in degrees
     */
    private static double[] CHARAposition(final StringBuilder sb) {
//    #  Geodetic height of the CHARA reference plane [meters]:
//       HEIGHT  1725.21

        // S1 from telescopes.chara:
//    LONG    -118 3 25.31272
//    LAT       34 13 27.78130
        final double lonDeg = -(118.0 + 3.0 / 60.0 + 25.31272 / 3600.0);
        logger.info("CHARA longitude (deg) : " + lonDeg);

        final double latDeg = 34.0 + 13.0 / 60.0 + 27.78130 / 3600.0;
        logger.info("CHARA latitude (deg)  : " + latDeg);

        final double alt = 1725.21;

        final Position3D position = computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        final LonLatAlt coords = GeocentricCoords.getLonLatAlt(position);

        logger.info("CHARA position : " + coords.toString());

        /*
        14:25:39.671 INFO  [main] AsproGenConfig - CHARA longitude (deg) : -118.0570313111111
        14:25:39.671 INFO  [main] AsproGenConfig - CHARA latitude (deg)  : 34.22438369444445
        14:25:39.671 INFO  [main] AsproGenConfig - position (x,y,z) : [-2483801.164432058, -4660154.224654438, 3568032.552411753]
        14:25:39.671 INFO  [main] AsproGenConfig - CHARA position : [-118:03:25.3127, +34:13:27.7813, 1725.209 m][-118.0570313111111, 34.22438369444445]
         */
        return new double[]{lonDeg, latDeg};
    }

    private static void convertCHARAHorizon(final String station, final StringBuilder sb) {

        final double[] az;
        final double[] el;

        if ("S1".equals(station)) {
            /*
             ;S1
             ;clear (latest from Judit)
             s1az_clr=[0,0 , 5 ,  8 , 10 , 20 , 40 , 60 , 80 , 100,120,140,160,180,200,210,215,220,225,240,260,270,280,290,300,305,310,312,320,330,340,350,360,360]
             s1el_clr=[0,29, 27, 20, 20,  20,   20,   20, 20,   20, 20, 20, 20, 20, 20, 20, 22, 22, 20, 20, 20, 20, 20, 37, 38, 40, 41, 42, 37, 42, 42, 36, 29,0]
             */
            az = new double[]{0, 0, 5, 8, 10, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 210, 215, 220, 225, 240, 260, 270, 280, 290, 300, 305, 310, 312, 320, 330, 340, 350, 360, 360};
            el = new double[]{20, 29, 27, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 22, 22, 20, 20, 20, 20, 20, 37, 38, 40, 41, 42, 37, 42, 42, 36, 29, 20};
        } else if ("S2".equals(station)) {
            /*
             ;S2
             ;clear (latest from Judit)
             s2az_clr=[0,65,65,70,80,90,100,110,120,130,235,238,243,244,246,248,250,253,258,275,288,290,292,294,300,303,305,310,315,318,320,323,324,328,330,335,340,344,345,348,350,353,355,355,360]
             s2el_clr=[0,0,20,21,30,31,31,30,22,20,20,25,25.5,26,25.5,25,25,24,21,20,30,30,33,37,41,41,40,44,46,47,48,48,49,48,47,46,34,30,30,27,27,23,20,0,0]
             */
            az = new double[]{0, 65, 65, 70, 80, 90, 100, 110, 120, 130, 235, 238, 243, 244, 246, 248, 250, 253, 258, 275, 288, 290, 292, 294, 300, 303, 305, 310, 315, 318, 320, 323, 324, 328, 330, 335, 340, 344, 345, 348, 350, 353, 355, 355, 360};
            el = new double[]{20, 20, 20, 21, 30, 31, 31, 30, 22, 20, 20, 25, 25.5, 26, 25.5, 25, 25, 24, 21, 20, 30, 30, 33, 37, 41, 41, 40, 44, 46, 47, 48, 48, 49, 48, 47, 46, 34, 30, 30, 27, 27, 23, 20, 20, 20};
        } else if ("W1".equals(station)) {
            /*
             ;W1
             ;clear (latest from Judit)
             w1az_clr=[0,25,25,30,33,40,45,48,50,55,58,60,65,67,70,73,80,85,90,92,95,97,100,105,110,115,117,120,125,130,132,134,136,138,140,140,360]
             w1el_clr=[0,0,20,21,23,22.6,27,30,32,36.2,37,37,37,37.2,38,38,36,35.2,38.2,39.2,38.4,38.6,38,37,34,31,31,31,30.2,29.2,30,29.6,28.4,27.2,27.6,0,0]
             */
            az = new double[]{0, 25, 25, 30, 33, 40, 45, 48, 50, 55, 58, 60, 65, 67, 70, 73, 80, 85, 90, 92, 95, 97, 100, 105, 110, 115, 117, 120, 125, 130, 132, 134, 136, 138, 140, 140, 360};
            el = new double[]{20, 20, 20, 21, 23, 22.6, 27, 30, 32, 36.2, 37, 37, 37, 37.2, 38, 38, 36, 35.2, 38.2, 39.2, 38.4, 38.6, 38, 37, 34, 31, 31, 31, 30.2, 29.2, 30, 29.6, 28.4, 27.2, 27.6, 20, 20};
        } else if ("W2".equals(station)) {
            /*
             ;W2
             ;clear (latest from Judit)
             w2az_clr=[0,0,0.0,20.0,40.0,60.0,75.0,77.0,77.8,80.0,84.0,87.0,89.0,91.0,93.0,96.0,98.0,100.0,105.0,107.0,109.0,109.8,110.0,113.0,115.5,118.0,120.0,123.0,125.0,125.0,126.0,127.0,128.0,129.0,150.0,160.0,185.0,200.0,250.0,280.0,310.0,358.0,358,360]
             w2el_clr=[0,0,20,20,20,20,20,20,20,21,21.5,22,23,25,25,24,22,23,21,20,20,22.5,22.5,22.5,26,24.2,25,24,20,25,23,22,22,20,20,20,20,20,20,20,20,20,0,0]
             */
            az = new double[]{0, 0, 0, 20, 40, 60, 75, 77, 77.8, 80, 84, 87, 89, 91, 93, 96, 98, 100, 105, 107, 109, 109.8, 110, 113, 115.5, 118, 120, 123, 125, 125, 126, 127, 128, 129, 150, 160, 185, 200, 250, 280, 310, 358, 358, 360};

            el = new double[]{20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21.5, 22, 23, 25, 25, 24, 22, 23, 21, 20, 20, 22.5, 22.5, 22.5, 26, 24.2, 25, 24, 20, 25, 23, 22, 22, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20};
        } else if ("E1".equals(station)) {
            /*
             ;E1
             ;clear (latest from Judit)
             e1az_clr=[0,0,2,4,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,70,90,123,176,190,220,250,270,272,273,275,277,279,280,281,283,285,287,288,290,292,292,292,292,294,294,296,300,305,310,314,317,318,319,320,321,322,325,337,340,342,344,346,348,350,352,354,356,358,359.6,360,360]
             e1el_clr=[0,29,30,28,29,26,28,38,38,36,35,33,34,34,31.5,30.5,28.5,30,29,28.5,29,28,27,24,20,20,20,20,20,20,20,20,20,22,22,22,22,24,25,25,26,27,27,27,30,30,30,30,30,30,30,31,32,32,28,30,28.5,29,27,28,25,20,20,20,24,24,26,27.5,27.5,27,27.5,27.2,27,28,28.5,29,0]
             */
            az = new double[]{0, 0, 2, 4, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 70, 90, 123, 176, 190, 220, 250, 270, 272, 273, 275, 277, 279, 280, 281, 283, 285, 287, 288, 290, 292, 292, 292, 292, 294, 294, 296, 300, 305, 310, 314, 317, 318, 319, 320, 321, 322, 325, 337, 340, 342, 344, 346, 348, 350, 352, 354, 356, 358, 359.6, 360, 360};
            el = new double[]{20, 29, 30, 28, 29, 26, 28, 38, 38, 36, 35, 33, 34, 34, 31.5, 30.5, 28.5, 30, 29, 28.5, 29, 28, 27, 24, 20, 20, 20, 20, 20, 20, 20, 20, 20, 22, 22, 22, 22, 24, 25, 25, 26, 27, 27, 27, 30, 30, 30, 30, 30, 30, 30, 31, 32, 32, 28, 30, 28.5, 29, 27, 28, 25, 20, 20, 20, 24, 24, 26, 27.5, 27.5, 27, 27.5, 27.2, 27, 28, 28.5, 29, 20};

        } else if ("E2".equals(station)) {
            /*
             ;E2
             ;clear (latest from Judit)
             e2az_clr=[0,24,25,27,30,33,36,38,39,40,90,180,200,203,220,248,250,252,254,256,258,260,262,320,358,360]
             e2el_clr=[20,30,22,25,26,28,23,22,20,20,20,20,20,20,20,20,20,22,24,26,23,21,20,20,20,0]
             */
            az = new double[]{0, 24, 25, 27, 30, 33, 36, 38, 39, 40, 90, 180, 200, 203, 220, 248, 250, 252, 254, 256, 258, 260, 262, 320, 358, 360};
            el = new double[]{20, 30, 22, 25, 26, 28, 23, 22, 20, 20, 20, 20, 20, 20, 20, 20, 20, 22, 24, 26, 23, 21, 20, 20, 20, 20};
        } else {
            // missing station
            logger.info("No horizon for station " + station);
            az = el = null;
        }

        if ((az != null) && (el != null)) {
            if (az.length != el.length) {
                throw new IllegalStateException("Inconsistent length for azimuth and elevation arrays !");
            }
            final int len = az.length - 1;
            // check az=0 and az=360:
            if (az[0] != 0d || az[len] != 360d) {
                throw new IllegalStateException("Invalid azimuth array [except 0 and 360 range] !");
            }

            // Fix start and end of az/el arrays (ie use max(elevation) for az=0 and az=360:
            int a = 0;
            for (int i = 0; i <= len; i++) {
                if (az[i] == 0d) {
                    a = i;
                    System.out.println("el = " + el[a]);
                } else {
                    break;
                }
            }
            System.out.println("a = " + Arrays.toString(Arrays.copyOfRange(az, 0, a + 1)));

            int b = 0;
            for (int i = len; i >= 0; i--) {
                if (az[i] == 360d) {
                    b = i;
                    System.out.println("el = " + el[a]);
                } else {
                    break;
                }
            }
            System.out.println("b = " + Arrays.toString(Arrays.copyOfRange(az, b, len + 1)));

            // Fix elevation for az=0 and az=360 ie take max:
            double max = 0d;
            for (int i = 0; i <= a; i++) {
                max = Math.max(max, el[i]);
            }
            for (int i = b; i <= len; i++) {
                max = Math.max(max, el[i]);
            }
            System.out.println("max = " + max);
            el[a] = el[b] = max;

            sb.append("      <horizon>\n"
                    + "        ");

            for (int i = a; i <= b; i++) {
                // output :
                sb.append("<point>");
                sb.append("<azimuth>").append(az[i]).append("</azimuth>");
                sb.append("<elevation>").append(el[i]).append("</elevation>");
                sb.append("</point>");
            }
            // close polygon:
            sb.append("<point>");
            sb.append("<azimuth>").append(360.0).append("</azimuth>");
            sb.append("<elevation>").append(90.0).append("</elevation>");
            sb.append("</point>");

            sb.append("<point>");
            sb.append("<azimuth>").append(0.0).append("</azimuth>");
            sb.append("<elevation>").append(90.0).append("</elevation>");
            sb.append("</point>");

            sb.append("\n      </horizon>\n");
        }
    }

    private static void VLTIPosition() {

        final StringBuilder sb = new StringBuilder(128);

        // ASPRO1: 17/04/2012:
        final double lonDeg = -70.40498688; // VLTI site longitude (UV zero) [deg].     
        final double latDeg = -24.62743941; // VLTI site latitude (UV zero) [deg].
        logger.info("VLTI geodetic longitude / latitude (deg) : " + lonDeg + " - " + latDeg);

        final double alt = 2669.0; // VLTI ground site height above WGS84 [m].

        final Position3D position = computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        final LonLatAlt coords = GeocentricCoords.getLonLatAlt(position);

        logger.info("VLTI position : " + coords.toString());

        /*    
14:16:41.368 INFO  [main] AsproGenConfig - VLTI geodetic longitude / latitude (deg) : -70.40498688 - -24.62743941
14:16:41.370 INFO  [main] AsproGenConfig - position (x,y,z) : [1946409.9709899623, -5467660.10589381, -2642728.3998888675]
14:16:41.720 INFO  [main] AsproGenConfig - VLTI position : [-70:24:17.9528, -24:37:38.7819, 2669.0 m][-70.40498688, -24.62743941]
         */
 /*
2018.3: ISS gives (as does astropy):        
ARRAYX  =    1946404.341038839
ARRAYY  =   -5467644.290798524
ARRAYZ  =   -2642728.201444249
         */
        logger.info("Generated VLTI position:\n" + sb.toString());
    }

    private static void MROIposition() {

        final StringBuilder sb = new StringBuilder(128);

        final double lonDeg = -(107.0 + 11.0 / 60.0 + 05.12 / 3600.0);
        logger.info("MROI longitude (deg) : " + lonDeg);

        final double latDeg = 33.0 + 58.0 / 60.0 + 47.6 / 3600.0;
        logger.info("MROI latitude (deg) : " + latDeg);

        final double alt = 3200.0;
        computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        logger.info("Generated MROI position:\n" + sb.toString());
    }

    /**
     * Convert geodetic long/lat/alt to geocentric coordinates
     *
     * @param lon longitude in degrees
     * @param lat latitude in degrees
     * @param alt altitude in meters
     * @param sb buffer
     * @return positions (x,y,z)
     */
    private static Position3D computeInterferometerPosition(final double lon, final double lat, final double alt, final StringBuilder sb) {

        final Position3D geoXYZ = GeocentricCoords.getGeocentric(Math.toRadians(lon), Math.toRadians(lat), alt);

        logger.info("position (x,y,z) : " + geoXYZ);

        sb.append("    <position>\n");
        sb.append("      <posX>").append(geoXYZ.getPosX()).append("</posX>\n");
        sb.append("      <posY>").append(geoXYZ.getPosY()).append("</posY>\n");
        sb.append("      <posZ>").append(geoXYZ.getPosZ()).append("</posZ>\n");
        sb.append("    </position>\n\n");

        return geoXYZ;
    }

    /**
     * Convert the ASPRO 1 station file : W0 0	0	0	0 W1 -1.058755762353468	-7.2772199998144 1.570859050973913 7.52 W2
     * -2.1386491936416	-14.937289999797 3.173079724470378 15.42 W3 -3.163870843607306	-22.209509999956 4.694184747335298
     * 22.92
     *
     * 165.8497 167.1097 -1000000 -1000000 173.8097 175.0697 -1000000 -1000000 U1
     *
     * @param absFileName absolute file path to ASPRO 1 VLTI switchyard file
     */
    private static void convertStationFile(final String absFileName) {

        logger.info("convertStationFile : " + absFileName);

        final StringBuilder sb = new StringBuilder(16384);

        // number of columns filled with double values :
        final int maxCols = 5;
        // column separator :
        final String delimiter = " ";

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            reader = new BufferedReader(new FileReader(data));

            int i;
            String line;
            StringTokenizer tok;
            // outputs :
            String station;
            double[] values = new double[maxCols];

            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", delimiter);
                tok = new StringTokenizer(line, delimiter);

                i = 0;
                station = null;
                while (tok.hasMoreTokens()) {
                    if (i == 0) {
                        // station name :
                        station = tok.nextToken();
                    } else if (i < maxCols) {
                        values[i] = Double.parseDouble(tok.nextToken());
                    } else {
                        break;
                    }
                    i++;
                }

                if (station != null) {
                    /*
                     System.out.println("station : " + station);
                     System.out.println("values : " + Arrays.toString(values));
                     */
 /*
                     <station>
                     <name>S1</name>
                     <telescope>T</telescope>
                     <relativePosition>
                     <posX>0.0</posX>
                     <posY>0.0</posY>
                     <posZ>0.0</posZ>
                     </relativePosition>
                     <delayLineFixedOffset>0.0</delayLineFixedOffset>
                     ...
                     </station>
                     */

                    // output :
                    sb.append("<station>\n");
                    sb.append("<name>").append(station).append("</name>\n");
                    sb.append("<telescope>T</telescope>\n");

                    sb.append("<relativePosition>\n");
                    sb.append("<posX>").append(values[1]).append("</posX>\n");
                    sb.append("<posY>").append(values[2]).append("</posY>\n");
                    sb.append("<posZ>").append(values[3]).append("</posZ>\n");
                    sb.append("</relativePosition>\n");

                    sb.append("<delayLineFixedOffset>").append(values[4]).append("</delayLineFixedOffset>\n");

                    sb.append("</station>\n");
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }

        logger.info("convertStationFile : output :\n" + sb.toString());
    }

    /**
     * Load the SUSI config file (SUSI.txt)
     *
     * @param absFileName absolute file path to SUSI config file
     * @return susi station config
     *
     */
    private static Map<String, Map<String, Double>> loadSUSIConfig(final String absFileName) {

        logger.info("loadSUSIConfig : " + absFileName);

        final List<String> labels = Arrays.asList(new String[]{
            "S1", "S2", "S3", "S4",
            "N1", "N3", "N4"
        });

        final String[] columns = new String[]{
            "index", "XOFFSET", "YOFFSET", "ZOFFSET", "WEIGHT", "DIAMETER"
        };

        final Map<String, Map<String, Double>> stationConfigs = new LinkedHashMap<String, Map<String, Double>>(16);

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            reader = new BufferedReader(new FileReader(data));

            // column separator :
            final String delimiter = " ";

            String name, key;
            Double value;
            Map<String, Double> current;

            StringTokenizer tok;
            String line;
            while ((line = reader.readLine()) != null) {

                line = line.replaceAll("\\s+", " ").trim();

                // Parse values :
                logger.info("line = " + line);

                tok = new StringTokenizer(line, delimiter);

                if (tok.hasMoreTokens()) {
                    // key
                    key = tok.nextToken();

                    if (labels.contains(key)) {
                        name = key;

                        logger.info("new station : " + name);
                        current = new LinkedHashMap<String, Double>(16);

                        int i = 0;
                        // value (only first used) :
                        while (tok.hasMoreTokens()) {
                            key = columns[i];
                            value = Double.valueOf(tok.nextToken());

                            logger.info("value : " + value);

                            current.put(key, value);
                            i++;
                        }

                        // end station block :
                        stationConfigs.put(name, current);

                        logger.info("end station : " + name + " =\n" + current);
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }
        return stationConfigs;
    }

    /**
     * Convert SUSI station configs to ASPRO station configurations
     *
     * @param stationConfigs station configs
     * @param sb buffer
     */
    private static void convertSUSIStations(final Map<String, Map<String, Double>> stationConfigs, final StringBuilder sb) {

        final double[] lonlat = SUSIposition(sb);

        final double lat = Math.toRadians(lonlat[1]);

        double x, y, z, offset;
        String station;
        Map<String, Double> config;
        for (Map.Entry<String, Map<String, Double>> e : stationConfigs.entrySet()) {
            station = e.getKey();
            config = e.getValue();

            sb.append("    <station>\n");
            sb.append("      <name>").append(station).append("</name>\n");
            sb.append("      <telescope>T</telescope>\n");

            x = config.get("XOFFSET");
            y = config.get("YOFFSET");
            z = config.get("ZOFFSET");

            convertHorizToEquatorial(station, lat, 1e6 * x, 1e6 * y, 1e6 * z, sb);

            // norm of baseline vector:
            offset = Math.sqrt(x * x + y * y + z * z);

            if (station.startsWith("N")) {
                offset -= 2.5;
            } else {
                offset += 2.5;
            }

            sb.append("      <delayLineFixedOffset>").append((int) Math.round(10.0 * offset) / 10.0).append("</delayLineFixedOffset>\n");
            sb.append("    </station>\n\n");
        }
    }

    /**
     * Convert the SUSI config file (SUSI.txt)
     *
     * @param absFileName absolute file path to SUSI config file
     */
    private static void convertSUSIConfig(final String absFileName) {

        final Map<String, Map<String, Double>> stationConfigs = loadSUSIConfig(absFileName);

        final StringBuilder sb = new StringBuilder(12 * 1024);

        sb.append("<a:interferometerSetting>\n\n");
        sb.append("  <description>\n\n    <name>SUSI</name>\n\n");

        convertSUSIStations(stationConfigs, sb);

        sb.append("  </description>\n\n</a:interferometerSetting>\n");

        logger.info("Generated SUSI Configuration : " + sb.length() + "\n" + sb.toString());
    }

    /**
     * Compute the SUSI position (S1 coordinates)
     *
     * @param sb buffer
     * @return long and lat in degrees
     */
    private static double[] SUSIposition(final StringBuilder sb) {
        final double lonDeg = 149.548224972222;
        logger.info("SUSI longitude (deg) : " + lonDeg);

        final double latDeg = -30.322273888889;
        logger.info("SUSI latitude (deg) : " + latDeg);

        final double alt = 210.0;
        computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        logger.info("Generated SUSI position:\n" + sb.toString());

        return new double[]{lonDeg, latDeg};
    }

    /**
     * Load the NPOI config file (stations.npoi)
     *
     * @param absFileName absolute file path to NPOI config file
     * @return susi station config
     *
     */
    private static Map<String, Map<String, Double>> loadNPOIConfig(final String absFileName) {

        logger.info("loadNPOIConfig : " + absFileName);

        final String[] columns = new String[]{
            "XOFFSET", "YOFFSET", "ZOFFSET", "DELAY"
        };

        final Map<String, Map<String, Double>> stationConfigs = new LinkedHashMap<String, Map<String, Double>>(16);

        // load data from file :
        BufferedReader reader = null;
        try {
            final File data = new File(absFileName);

            reader = new BufferedReader(new FileReader(data));

            // column separator :
            final String delimiter = " ";

            String name, key;
            Double value;
            Map<String, Double> current;

            StringTokenizer tok;
            String line;
            while ((line = reader.readLine()) != null) {

                line = line.replaceAll("\\s+", " ").trim();

                if (!line.startsWith("!")) {

                    // Parse values :
                    logger.info("line = " + line);

                    tok = new StringTokenizer(line, delimiter);

                    if (tok.hasMoreTokens()) {
                        // key
                        name = tok.nextToken();

                        logger.info("new station : " + name);
                        current = new LinkedHashMap<String, Double>(16);

                        int i = 0;
                        // value (only first used) :
                        while (tok.hasMoreTokens()) {
                            key = columns[i];
                            value = Double.valueOf(tok.nextToken());

                            logger.info("value : " + value);

                            current.put(key, value);
                            i++;
                        }

                        // end station block :
                        stationConfigs.put(name, current);

                        logger.info("end station : " + name + " =\n" + current);
                    }
                }
            }

        } catch (FileNotFoundException fnfe) {
            logger.error("File not found", fnfe);
        } catch (IOException ioe) {
            logger.error("IO failure", ioe);
        } finally {
            FileUtils.closeFile(reader);
        }
        return stationConfigs;
    }

    /**
     * Convert NPOI station configs to ASPRO station configurations
     *
     * @param stationConfigs station configs
     * @param sb buffer
     */
    private static void convertNPOIStations(final Map<String, Map<String, Double>> stationConfigs, final StringBuilder sb) {

        final double[] lonlat = NPOIposition(sb);

        final double lat = Math.toRadians(lonlat[1]);

        double x, y, z, offset;
        String station;
        Map<String, Double> config;
        Position3D p;

        for (Map.Entry<String, Map<String, Double>> e : stationConfigs.entrySet()) {
            station = e.getKey();
            config = e.getValue();

            sb.append("    <station>\n");
            sb.append("      <name>").append(station).append("</name>\n");
            sb.append("      <telescope>").append(station.startsWith("A") ? "AS" : "IS").append("</telescope>\n");

            x = config.get("XOFFSET");
            y = config.get("YOFFSET");
            z = config.get("ZOFFSET");

            p = convertHorizToEquatorial(station, lat, 1e6 * x, 1e6 * y, 1e6 * z, sb);

            offset = config.get("DELAY");

            sb.append("      <delayLineFixedOffset>").append(offset).append("</delayLineFixedOffset>\n");
            sb.append("    </station>\n\n");
        }
    }

    private static Position3D getPoint3D(final double x, final double y, final double z) {
        final Position3D p = new Position3D();
        p.setPosX(x);
        p.setPosY(y);
        p.setPosZ(z);
        return p;
    }

    /**
     * Compute the NPOI position
     *
     * @param sb string buffer
     * @return [longitude, latitude]
     */
    private static double[] NPOIposition(final StringBuilder sb) {

        /*
         * ! Array center on Anderson Mesa
         ! Latitude =   35 05 48.0
         ! Longitude= -111 32 06.0
         ! Height   = 2200.66 m
         */
        final double lonDeg = -(111.0 + 32.0 / 60.0 + 06.0 / 3600.0);
        logger.info("NPOI longitude (deg) : " + lonDeg);

        final double latDeg = 35.0 + 05.0 / 60.0 + 48.0 / 3600.0;
        logger.info("NPOI latitude (deg)  : " + latDeg);

        final double alt = 2200.66d;
        computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        logger.info("Generated NPOI position:\n" + sb.toString());
        /*
         10:31:05.158 [main] INFO  AsproGenConfig - NPOI longitude (deg) : -111.535
         10:31:05.158 [main] INFO  AsproGenConfig - NPOI latitude (deg)  : 35.09666666666667
         10:31:05.158 [main] INFO  AsproGenConfig - position (x,y,z) : -1912993.323267053, -4847730.169323824, 3662270.769851256
         */
        return new double[]{lonDeg, latDeg};
    }

    /**
     * Convert the NPOI config file (NPOI.txt)
     *
     * @param absFileName absolute file path to NPOI config file
     */
    private static void convertNPOIConfig(final String absFileName) {

        final Map<String, Map<String, Double>> stationConfigs = loadNPOIConfig(absFileName);

        final StringBuilder sb = new StringBuilder(16 * 1024);

        sb.append("<a:interferometerSetting>\n\n");
        sb.append("  <description>\n\n    <name>NPOI</name>\n\n");

        convertNPOIStations(stationConfigs, sb);

        sb.append("  </description>\n\n</a:interferometerSetting>\n");

        logger.info("Generated NPOI Configuration : " + sb.length() + "\n" + sb.toString());
    }

    private static void SingleDishes() {

        // SAAO Sutherland:
        /* 32°22'47.9"S 20°48'39.3"E
         -32.379979, 20.810921
         */
        final StringBuilder sb = new StringBuilder(128);

        double lonDeg = +(20.0 + 48.0 / 60.0 + 39.3 / 3600.0);
        logger.info("Sutherland longitude (deg) : " + lonDeg);

        double latDeg = -(32.0 + 22.0 / 60.0 + 47.9 / 3600.0);
        logger.info("Sutherland  latitude (deg) : " + latDeg);

        double alt = 1798.0;
        computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        logger.info("Generated Sutherland position:\n" + sb.toString());

        sb.setLength(0);

        /*
        OHP:
         5° 42' 44" E Latitude = +43° 55' 54" (Télescope de 1m93) IAU Observatory Code 511
        altitude moyenne est de 650 mètres
         */
        lonDeg = +(5.0 + 42.0 / 60.0 + 44.0 / 3600.0);
        logger.info("OHP longitude (deg) : " + lonDeg);

        latDeg = -(43.0 + 55.0 / 60.0 + 54.0 / 3600.0);
        logger.info("OHP  latitude (deg) : " + latDeg);

        alt = 650.0;
        computeInterferometerPosition(lonDeg, latDeg, alt, sb);

        logger.info("Generated OHP position:\n" + sb.toString());
    }

    static double trimTo4Digits(final double value) {
        if (Double.isNaN(value)) {
            return value;
        }
        return (Math.round(1e4d * value)) / 1e4d;
    }

    static boolean contains(final String[] vals, final String val) {
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Main entry point to generate configuration parts (xml)
     *
     * @param args unused
     */
    public static void main(final String[] args) {

        // invoke Bootstrapper method to initialize logback now:
        Bootstrapper.getState();

        // Recompute site positions:
        if (false) {
            VLTIPosition();
            final StringBuilder sb = new StringBuilder(128);
            CHARAposition(sb);
            MROIposition();
            sb.setLength(0);
            SUSIposition(sb);
            sb.setLength(0);
            NPOIposition(sb);
            SingleDishes();
            return;
        }

        final String userHome = SystemUtils.USER_HOME;

        final String aspro1Path = userHome + "/dev/aspro1/etc/";
        final String vltiHorizonPath = userHome + "/dev/aspro/src/test/resources/VLTI_NewDatabase/";

        final String asproTestPath = userHome + "/dev/aspro/src/test/resources/";

        final INTERFEROMETER selected = INTERFEROMETER.CHARA;

        switch (selected) {
            case VLTI:
                VLTIPosition();

                // March 2014 (vcm limit to 2.5)
/*                
                 generateSwitchYard(
                 asproTestPath + "vlti_opl.txt",
                 asproTestPath + "vlti_vcm_limit_2.5bar.txt",
                 asproTestPath + "vlti_vcm_limit_2.75bar.txt",
                 asproTestPath + "vlti_vcm_limit_3.0bar.txt");
                 */
                // Oct 2015 (STS ie no more VCM limit expected)
                generateSwitchYard(
                        asproTestPath + "vlti_opl.txt",
                        asproTestPath + "vlti_vcm_no_limit.txt",
                        asproTestPath + "vlti_vcm_no_limit.txt",
                        asproTestPath + "vlti_vcm_no_limit.txt");

                // 2019.5: convert ESO vlti table to 1 file per station:
                convertHorizonTable(vltiHorizonPath, "newVltiHzn_obsDoors.txt");

                final String[] vltStations = {
                    "U1", "U2", "U3", "U4", "A0", "A1", "B0", "B1", "B2", "B3", "B4", "B5",
                    "C0", "C1", "C2", "C3", "D0", "D1", "D2", "E0", "G0", "G1", "G2", "H0",
                    "I1", "J1", "J2", "J3", "J4", "J5", "J6", "K0", "L0", "M0"};

                final StringBuilder sb = new StringBuilder(65535);

                for (String station : vltStations) {
                    convertHorizon(station, vltiHorizonPath + station + ".horizon", sb);
                }
                logger.info("convertHorizons : \n" + sb.toString());

                // convert arrayList to get DLx and channels (IPn)
                if (false) {
                    convertArrayList(asproTestPath + "vlti_arrayList_2016.txt",
                            new String[]{"Period 98", "Future"});
                }

                break;
            case CHARA:
                convertCHARAConfig(asproTestPath + "telescopes.chara");
                break;
            case SUSI:
                convertSUSIConfig(asproTestPath + "SUSI.txt");
                break;
            case NPOI:
                convertNPOIConfig(asproTestPath + "stations.npoi");
                break;
            case MROI:
                MROIposition();

                convertStationFile(aspro1Path + "MROI.stations");
                break;
            case SINGLE_DISH:
                SingleDishes();
                break;

            default:
                logger.info("unsupported interferometer : " + selected);
        }
    }
}
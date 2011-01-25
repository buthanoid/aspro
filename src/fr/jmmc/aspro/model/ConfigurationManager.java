/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ConfigurationManager.java,v 1.37 2011-01-25 12:29:37 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.36  2010/10/22 11:12:46  bourgesl
 * fixed minimum computation (Double extrema)
 *
 * Revision 1.35  2010/10/14 14:46:23  bourgesl
 * generated fake station horizons or fix them to respect the max elevation limit per telescope
 *
 * Revision 1.34  2010/10/14 14:19:26  bourgesl
 * generated fake station horizons or fix them to respect the max elevation limit
 *
 * Revision 1.33  2010/10/05 18:23:24  bourgesl
 * computeUVLimits made public for a given list of stations
 *
 * Revision 1.32  2010/10/04 14:30:47  bourgesl
 * added compute the minimum baseline
 *
 * Revision 1.31  2010/09/26 12:47:40  bourgesl
 * better exception handling
 *
 * Revision 1.30  2010/09/25 13:41:59  bourgesl
 * better singleton initialisation
 * simplified exception handling
 *
 * Revision 1.29  2010/09/23 19:46:35  bourgesl
 * comments when calling FeedBackReport
 *
 * Revision 1.28  2010/09/20 14:46:02  bourgesl
 * minor refactoring changes
 *
 * Revision 1.27  2010/09/20 12:14:21  bourgesl
 * class made final
 *
 * Revision 1.26  2010/09/09 16:06:10  bourgesl
 * remove Astrogrid Pal dependency
 *
 * Revision 1.25  2010/07/22 12:30:12  bourgesl
 * added getFocalInstrument and getInstrumentSamplingTime methods
 *
 * Revision 1.24  2010/07/07 15:11:28  bourgesl
 * full javadoc
 *
 * Revision 1.23  2010/06/17 10:02:51  bourgesl
 * fixed warning hints - mainly not final static loggers
 *
 * Revision 1.22  2010/06/11 09:10:12  bourgesl
 * added log message to help debugging JNLP Offline problems
 *
 * Revision 1.21  2010/05/26 09:13:15  bourgesl
 * added related channels to the instrument configuration (CHARA)
 *
 * Revision 1.20  2010/05/19 09:30:06  bourgesl
 * do not sort the interferometer list to keep the order defined in the AsproOIConfigurations.xml file
 *
 * Revision 1.19  2010/04/15 12:54:27  bourgesl
 * don't sort the interferometer configuration names
 *
 * Revision 1.18  2010/04/02 14:39:44  bourgesl
 * added FringeTracker list
 *
 * Revision 1.17  2010/02/04 17:05:06  bourgesl
 * UV bounds are coming from UVCoverageService
 *
 * Revision 1.16  2010/02/03 09:48:53  bourgesl
 * target model uvmap added on the uv coverage with zooming supported
 *
 * Revision 1.15  2010/01/15 13:50:17  bourgesl
 * added logs on setters
 * supports instrumentMode is null
 *
 * Revision 1.14  2010/01/08 16:51:17  bourgesl
 * initial uv coverage
 *
 * Revision 1.13  2010/01/05 17:17:28  bourgesl
 * instruments are no more sorted in UI
 *
 * Revision 1.12  2009/12/15 16:32:44  bourgesl
 * added user PoP configuration based on PoP indices
 *
 * Revision 1.11  2009/12/11 16:37:32  bourgesl
 * added Pop field in observation form
 *
 * Revision 1.10  2009/12/04 16:26:58  bourgesl
 * Added Load action in the menu bar (partially handled)
 *
 * Revision 1.9  2009/12/04 15:38:27  bourgesl
 * Added Save action in the menu bar
 *
 * Revision 1.8  2009/11/20 16:55:47  bourgesl
 * Added Beam / Delay Line definition
 * ObservabilityService is stateless to simplify coding
 *
 * Revision 1.7  2009/11/17 17:00:28  bourgesl
 * chosen instrument configuration propagated to observation
 *
 * Revision 1.6  2009/11/05 12:59:39  bourgesl
 * first simple source observability (only min elevation condition)
 *
 * Revision 1.5  2009/10/22 15:47:22  bourgesl
 * beginning of observability computation with jSkyCalc
 *
 * Revision 1.4  2009/10/20 13:08:51  bourgesl
 * ObservationManager has methods to store observation properties
 *
 * Revision 1.3  2009/10/16 15:25:30  bourgesl
 * removed jaxb header + XYZ coords to Long/Lat/Alt for interferometer + stations
 *
 * Revision 1.2  2009/10/14 15:54:38  bourgesl
 * added basicObservationForm + CHARA.xml
 *
 * Revision 1.1  2009/10/13 16:04:14  bourgesl
 * Basic ConfigurationManager to load interferometer configuration file
 *
 * Revision 1.1  2009/09/21 15:38:51  bourgesl
 * initial jmcs gui + jaxb loader
 *
 *
 ******************************************************************************/
package fr.jmmc.aspro.model;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.model.oi.AzEl;
import fr.jmmc.aspro.model.oi.Channel;
import fr.jmmc.aspro.model.oi.Configurations;
import fr.jmmc.aspro.model.oi.FocalInstrument;
import fr.jmmc.aspro.model.oi.FocalInstrumentConfiguration;
import fr.jmmc.aspro.model.oi.FocalInstrumentConfigurationItem;
import fr.jmmc.aspro.model.oi.FocalInstrumentMode;
import fr.jmmc.aspro.model.oi.FringeTracker;
import fr.jmmc.aspro.model.oi.HorizonProfile;
import fr.jmmc.aspro.model.oi.InterferometerConfiguration;
import fr.jmmc.aspro.model.oi.InterferometerDescription;
import fr.jmmc.aspro.model.oi.InterferometerSetting;
import fr.jmmc.aspro.model.oi.LonLatAlt;
import fr.jmmc.aspro.model.oi.Pop;
import fr.jmmc.aspro.model.oi.Position3D;
import fr.jmmc.aspro.model.oi.Station;
import fr.jmmc.aspro.model.oi.StationLinks;
import fr.jmmc.aspro.service.GeocentricCoords;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

/**
 * This class manages configuration files for the Interferometer configurations
 * @author bourgesl
 */
public final class ConfigurationManager extends BaseOIManager {

  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.model.ConfigurationManager";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /** Configurations file name */
  private static final String CONF_FILE = "AsproOIConfigurations.xml";
  /** singleton pattern */
  private static volatile ConfigurationManager instance = null;

  /* members */
  /** Map : id, interferometer description */
  private final Map<String, InterferometerDescription> interferometerDescriptions = new LinkedHashMap<String, InterferometerDescription>();
  /** Map : id, interferometer configuration */
  private final Map<String, InterferometerConfiguration> interferometerConfigurations = new HashMap<String, InterferometerConfiguration>();
  /** default horizon profile */
  private HorizonProfile defaultHorizon = null;

  /**
   * Return the ConfigurationManager singleton
   * @return ConfigurationManager singleton
   *
   * @throws IllegalStateException if the configuration files are not found or IO failure
   * @throws IllegalArgumentException if the load configuration failed
   */
  public static synchronized final ConfigurationManager getInstance()
          throws IllegalStateException, IllegalArgumentException {

    if (instance == null) {
      final ConfigurationManager cm = new ConfigurationManager();

      // can throw RuntimeException :
      cm.initialize();

      instance = cm;
    }
    return instance;
  }

  /**
   * Private constructor
   */
  private ConfigurationManager() {
    super();
  }

  /**
   * Initialize the configuration :
   * - load AsproOIConfigurations.xml to get configuration file paths
   * - load those files (InterferometerSetting)
   * - update interferometer description and configuration maps
   * 
   * @throws IllegalStateException if the configuration files are not found or IO failure
   * @throws IllegalArgumentException if the load configuration failed
   */
  private void initialize()
          throws IllegalStateException, IllegalArgumentException {

    final long start = System.nanoTime();

    final Configurations conf = (Configurations) loadObject(CONF_FILE);

    InterferometerSetting is;
    for (String fileName : conf.getFiles()) {
      if (logger.isLoggable(Level.CONFIG)) {
        logger.config("initialize : loading configuration file = " + fileName);
      }
      is = (InterferometerSetting) loadObject(fileName);

      addInterferometerSetting(is);
    }

    final long time = (System.nanoTime() - start);

    if (logger.isLoggable(Level.INFO)) {
      logger.info("initialize : duration = " + 1e-6d * time + " ms.");
    }

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("descriptions   = " + getInterferometerDescriptions());
      logger.fine("configurations = " + getInterferometerConfigurations());
    }
  }

  /**
   * Add a new interferometer configuration in the cache
   * and compute transient information (long/lat and max uv coverage)
   * @param is interferometer setting
   */
  private void addInterferometerSetting(final InterferometerSetting is) {

    final InterferometerDescription id = is.getDescription();

    // check if the interferometer is unique (name) :
    if (interferometerDescriptions.containsKey(id.getName())) {
      throw new IllegalStateException("The interferometer '" + id.getName() + "' is already present in the loaded configuration !");
    }

    computeInterferometerLocation(id);
    computeLimitsUVCoverage(id);

    adjustStationHorizons(id.getStations());

    interferometerDescriptions.put(id.getName(), id);

    for (InterferometerConfiguration c : is.getConfigurations()) {
      interferometerConfigurations.put(getConfigurationName(c), c);

      // reverse mapping :
      // declare interferometer configurations in the interferometer description
      is.getDescription().getConfigurations().add(c);
    }
  }

  /**
   * Compute the spherical coordinates for the interferometer
   * @param id interferometer description
   */
  private void computeInterferometerLocation(final InterferometerDescription id) {

    // Interferometer center :
    final Position3D center = id.getPosition();
    final LonLatAlt posSph = GeocentricCoords.getLonLatAlt(center);

    id.setPosSph(posSph);

    GeocentricCoords.dump(id.getName(), posSph);
  }

  /**
   * Compute the min and max UV coverage using all station couples
   * Note : some station couples can not be available as instrument baselines
   * @param id interferometer description
   */
  private void computeLimitsUVCoverage(final InterferometerDescription id) {
    final double[] range = computeLimitsUVCoverage(id.getStations());
    id.setMinBaseLine(range[0]);
    id.setMaxBaseLine(range[1]);
  }

  /**
   * Compute the min and max UV coverage using all possible baselines
   * Note : some station couples can not be available as instrument baselines
   * @param stations list of stations
   * @return min - max
   */
  public static double[] computeLimitsUVCoverage(final List<Station> stations) {
    double maxUV = 0d;
    double minUV = Double.POSITIVE_INFINITY;

    final int size = stations.size();

    double x, y, z, dist;
    Station s1, s2;
    for (int i = 0; i < size; i++) {
      s1 = stations.get(i);
      for (int j = i + 1; j < size; j++) {
        s2 = stations.get(j);

        x = s2.getRelativePosition().getPosX() - s1.getRelativePosition().getPosX();
        y = s2.getRelativePosition().getPosY() - s1.getRelativePosition().getPosY();
        z = s2.getRelativePosition().getPosZ() - s1.getRelativePosition().getPosZ();

        dist = Math.sqrt(x * x + y * y + z * z);

        minUV = Math.min(minUV, dist);
        maxUV = Math.max(maxUV, dist);
      }
    }

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("computeLimitsUVCoverage = {" + minUV + " - " + maxUV + " m");
    }
    return new double[]{minUV, maxUV};
  }

  /**
   * Adjust the station horizons to respect the maximum elevation limit (85 deg for CHARA)
   * @param stations station to update
   */
  private void adjustStationHorizons(final List<Station> stations) {
    double maxElev;
    for (Station station : stations) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("station : " + station);
      }

      // maximum elevation in degrees per telescope :
      maxElev = station.getTelescope().getMaxElevation();

      if (station.getHorizon() != null && !station.getHorizon().getPoints().isEmpty()) {
        // horizon is defined : check elevation
        for (AzEl point : station.getHorizon().getPoints()) {
          if (point.getElevation() > maxElev) {
            if (logger.isLoggable(Level.FINE)) {
              logger.fine("station : " + station + " : fix point : " + point);
            }
            point.setElevation(maxElev);
          }
        }

      } else {
        // missing horizon :
        if (defaultHorizon == null) {
          final HorizonProfile horizon = new HorizonProfile();
          final List<AzEl> points = horizon.getPoints();

          points.add(new AzEl(360d, 0d));
          points.add(new AzEl(0d, 0d));
          points.add(new AzEl(0d, maxElev));
          points.add(new AzEl(360d, maxElev));

          defaultHorizon = horizon;
        }

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("station : " + station + " use default horizon");
        }
        // define fake horizon :
        station.setHorizon(defaultHorizon);
      }
    }
  }

  /**
   * Compute the name of the interferometer configuration according to the associated interferometer and the optional version.
   * Store this name in the interferometer configuration in the name field (xml id)
   * @param ic configuration
   * @return name of the interferometer configuration
   */
  private String getConfigurationName(final InterferometerConfiguration ic) {
    // compute configuration name if missing :
    String name = ic.getName();
    if (name == null) {
      // interferometer name is an id :
      name = ic.getInterferometer().getName();

      if (ic.getVersion() != null) {
        name += " " + ic.getVersion();
      }
      ic.setName(name);
    }
    return name;
  }

  // Getter / Setter / API :
  /**
   * Return the interferometer description map keyed by name
   * @return interferometer description map
   */
  private Map<String, InterferometerDescription> getInterferometerDescriptions() {
    return interferometerDescriptions;
  }

  /**
   * Return the interferometer description for the given name
   * @param name interferometer name
   * @return interferometer description or null if not found
   */
  public InterferometerDescription getInterferometerDescription(final String name) {
    return getInterferometerDescriptions().get(name);
  }

  /**
   * Return the list of all interferometer names
   * @return list of all interferometer names
   */
  public Vector<String> getInterferometerNames() {
    final Vector<String> v = new Vector<String>(getInterferometerDescriptions().size());
    for (InterferometerDescription id : getInterferometerDescriptions().values()) {
      v.add(id.getName());
    }
    return v;
  }

  /**
   * Return the list of interferometer configurations associated to the given interferometer
   * @param interferometerName name of the interferometer
   * @return list of interferometer configurations
   */
  public Vector<String> getInterferometerConfigurationNames(final String interferometerName) {
    final InterferometerDescription id = getInterferometerDescription(interferometerName);
    if (id != null) {
      final Vector<String> v = new Vector<String>(id.getConfigurations().size());
      for (InterferometerConfiguration c : id.getConfigurations()) {
        v.add(c.getName());
      }
      return v;
    }
    return EMPTY_VECTOR;
  }

  /**
   * Return the switchyard links for the given station
   * @param id interferometer description
   * @param station station
   * @return switchyard links or null
   */
  public StationLinks getStationLinks(final InterferometerDescription id, final Station station) {
    if (id.getSwitchyard() != null) {
      for (StationLinks sl : id.getSwitchyard().getStationLinks()) {
        if (sl.getStation().equals(station)) {
          return sl;
        }
      }
    }
    return null;
  }

  /**
   * Indicate if the given interferometer has PoPs
   * @param interferometerName name of the interferometer
   * @return true if the interferometer description has PoPs
   */
  public boolean hasPoPs(final String interferometerName) {
    final InterferometerDescription id = getInterferometerDescription(interferometerName);
    if (id != null) {
      return !id.getPops().isEmpty();
    }
    return false;
  }

  /* InterferometerConfiguration */
  /**
   * Return the interferometer configuration map keyed by name
   * @return interferometer configuration map
   */
  private Map<String, InterferometerConfiguration> getInterferometerConfigurations() {
    return interferometerConfigurations;
  }

  /**
   * Return the interferometer configuration for the given name
   * @param name name of the interferometer configuration
   * @return interferometer configuration or null if not found
   */
  public InterferometerConfiguration getInterferometerConfiguration(final String name) {
    return getInterferometerConfigurations().get(name);
  }

  /**
   * Return the list of all instrument names available for the given configuration
   * @param configurationName name of the interferometer configuration
   * @return list of all instrument names
   */
  public Vector<String> getInterferometerInstrumentNames(final String configurationName) {
    final InterferometerConfiguration c = getInterferometerConfiguration(configurationName);
    if (c != null) {
      final Vector<String> v = new Vector<String>(c.getInstruments().size());
      for (FocalInstrumentConfiguration ic : c.getInstruments()) {
        v.add(ic.getFocalInstrument().getName());
      }
      return v;
    }
    return EMPTY_VECTOR;
  }

  /**
   * Return the list of all fringe tracker modes available for the given configuration
   * @param configurationName name of the interferometer configuration
   * @return list of all fringe tracker modes
   */
  public Vector<String> getFringeTrackerModes(final String configurationName) {
    final InterferometerConfiguration c = getInterferometerConfiguration(configurationName);
    if (c != null) {
      final FringeTracker ft = c.getInterferometer().getFringeTracker();
      if (ft != null) {
        final Vector<String> v = new Vector<String>(ft.getModes().size() + 1);
        v.add(AsproConstants.NONE);
        v.addAll(ft.getModes());
        return v;
      }
    }
    return EMPTY_VECTOR;
  }

  /**
   * Return the instrument configuration for the given interferometer configuration and instrument name
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @return focal instrument configuration
   */
  public FocalInstrumentConfiguration getInterferometerInstrumentConfiguration(final String configurationName, final String instrumentName) {
    final InterferometerConfiguration c = getInterferometerConfiguration(configurationName);
    if (c != null) {
      for (FocalInstrumentConfiguration ic : c.getInstruments()) {
        if (ic.getFocalInstrument().getName().equals(instrumentName)) {
          return ic;
        }
      }
    }
    return null;
  }

  /**
   * Return the instrument for the given interferometer configuration and instrument name
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @return focal instrument
   */
  public FocalInstrument getInterferometerInstrument(final String configurationName, final String instrumentName) {
    final FocalInstrumentConfiguration ic = getInterferometerInstrumentConfiguration(configurationName, instrumentName);
    if (ic != null) {
      return ic.getFocalInstrument();
    }
    return null;
  }

  /**
   * Return the list of all instrument configuration names (station list) for the given interferometer configuration and instrument name
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @return list of all instrument configuration names
   */
  public Vector<String> getInstrumentConfigurationNames(final String configurationName, final String instrumentName) {
    final FocalInstrumentConfiguration ic = getInterferometerInstrumentConfiguration(configurationName, instrumentName);
    if (ic != null) {
      final Vector<String> v = new Vector<String>(ic.getConfigurations().size());
      for (FocalInstrumentConfigurationItem c : ic.getConfigurations()) {
        v.add(c.getName());
      }
      return v;
    }
    return EMPTY_VECTOR;
  }

  /**
   * Return the list of stations for the given interferometer configuration, instrument name and instrument configuration
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @param instrumentConfigurationName name of the instrument configuration
   * @return list of stations
   */
  public List<Station> getInstrumentConfigurationStations(final String configurationName, final String instrumentName, final String instrumentConfigurationName) {
    final FocalInstrumentConfiguration ic = getInterferometerInstrumentConfiguration(configurationName, instrumentName);
    if (ic != null) {
      for (FocalInstrumentConfigurationItem c : ic.getConfigurations()) {
        if (c.getName().equals(instrumentConfigurationName)) {
          return c.getStations();
        }
      }
    }
    return null;
  }

  /**
   * Return the (optional) list of instrument channels for the given interferometer configuration, instrument name and instrument configuration
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @param instrumentConfigurationName name of the instrument configuration
   * @return list of instrument channels or null if undefined
   */
  public List<Channel> getInstrumentConfigurationChannels(final String configurationName, final String instrumentName, final String instrumentConfigurationName) {
    final FocalInstrumentConfiguration ic = getInterferometerInstrumentConfiguration(configurationName, instrumentName);
    if (ic != null) {
      for (FocalInstrumentConfigurationItem c : ic.getConfigurations()) {
        if (c.getName().equals(instrumentConfigurationName)) {
          return c.getChannels();
        }
      }
    }
    return null;
  }

  /**
   * Return the default sampling time for the given interferometer configuration and instrument name
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @return default sampling time
   */
  public int getInstrumentSamplingTime(final String configurationName, final String instrumentName) {
    final FocalInstrument ins = getInterferometerInstrument(configurationName, instrumentName);
    if (ins != null) {
      return ins.getDefaultSamplingTime();
    }
    return -1;
  }

  /**
   * Return the list of all instrument modes (spectral configuration) for the given interferometer configuration and instrument name
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @return list of all instrument modes
   */
  public Vector<String> getInstrumentModes(final String configurationName, final String instrumentName) {
    final FocalInstrument ins = getInterferometerInstrument(configurationName, instrumentName);
    if (ins != null) {
      final Vector<String> v = new Vector<String>(ins.getModes().size());
      for (FocalInstrumentMode m : ins.getModes()) {
        v.add(m.getName());
      }
      return v;
    }
    return EMPTY_VECTOR;
  }

  /**
   * Return the instrument mode for the given interferometer configuration, instrument name and mode
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @param instrumentMode instrument mode
   * @return instrument mode or null
   */
  public FocalInstrumentMode getInstrumentMode(final String configurationName, final String instrumentName, final String instrumentMode) {
    if (instrumentMode != null && instrumentMode.length() > 0) {
      final FocalInstrument ins = getInterferometerInstrument(configurationName, instrumentName);
      if (ins != null) {
        for (FocalInstrumentMode m : ins.getModes()) {
          if (m.getName().equals(instrumentMode)) {
            return m;
          }
        }
      }
    }
    return null;
  }

  /**
   * Parse and return the list of PoPs for the given interferometer configuration, instrument name and Pops string.
   * The Pops string must only contain PoP indexes like '12', '111' or '541'.
   * The length of this string must respect the number of channels of the instrument.
   * @param configurationName name of the interferometer configuration
   * @param instrumentName name of the instrument
   * @param configPoPs Pops string
   * @return list of PoPs
   */
  public List<Pop> parseInstrumentPoPs(final String configurationName, final String instrumentName, final String configPoPs) {
    if (configPoPs != null && configPoPs.length() > 0) {
      final FocalInstrument ins = getInterferometerInstrument(configurationName, instrumentName);
      if (ins != null) {
        // number of channels :
        final int numChannels = ins.getNumberChannels();

        if (configPoPs.length() == numChannels) {
          // valid length :

          final InterferometerConfiguration c = getInterferometerConfiguration(configurationName);
          if (c != null) {
            final List<Pop> listPoPs = c.getInterferometer().getPops();

            final List<Pop> config = new ArrayList<Pop>(numChannels);

            int idx;
            for (char ch : configPoPs.toCharArray()) {
              idx = Character.digit(ch, 10);
              if (idx <= 0) {
                return null;
              }
              for (Pop pop : listPoPs) {
                if (pop.getIndex() == idx) {
                  config.add(pop);
                }
              }
            }
            return config;
          }
        }
      }
    }
    return null;
  }
}

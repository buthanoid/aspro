/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: Preferences.java,v 1.4 2010-06-17 10:02:51 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2010/06/09 12:49:39  bourgesl
 * added an hidden preference (splash.screen.show) to show the splashscreen at startup
 *
 * Revision 1.2  2010/05/21 14:27:34  bourgesl
 * added preferences for Model Image Lut & Size
 *
 * Revision 1.1  2010/05/12 08:44:10  mella
 * Add one preferences window first to choose the default style of display for positions
 *
 *
 */
package fr.jmmc.aspro;

import fr.jmmc.mcs.util.PreferencesException;

/**
 * Handles preferences for Aspro.
 *
 * Note : There is a special preference 'splash.screen.show' used to disable the splash screen (dev mode) if its value is 'false'.
 */
public class Preferences extends fr.jmmc.mcs.util.Preferences {

  /** Singleton instance */
  private static Preferences _singleton = null;
  /** Class Name */
  private final static String className_ = "fr.jmmc.aspro.Preferences";
  /** Logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /* Preferences */
  /** Preference : display splash screen */
  public final static String SHOW_SPLASH_SCREEN = "splash.screen.show";
  /** Preference : show help tooltip */
  public final static String HELP_TOOLTIPS_SHOW = "help.tooltips.show";
  /** Preference : edit positions in XY (true) or rho/theta (false) in the model editor */
  public final static String MODELEDITOR_PREFERXY = "modeleditor.preferxy";
  /** Preference : LUT table to use for the object model image in the UV Coverage plot */
  public final static String MODEL_IMAGE_LUT = "model.image.lut";
  /** Preference : Image size to use for the object model image in the UV Coverage plot */
  public final static String MODEL_IMAGE_SIZE = "model.image.size";

  /**
   * Private constructor that must be empty.
   */
  private Preferences() {
    super();
  }

  /**
   * Return the singleton instance of Preferences.
   *
   * @return the singleton preference instance
   */
  public final synchronized static Preferences getInstance() {
    // Build new reference if singleton does not already exist
    // or return previous reference
    if (_singleton == null) {
      logger.fine("Preferences.getInstance()");

      _singleton = new Preferences();
    }

    return _singleton;
  }

  protected void setDefaultPreferences() throws PreferencesException {
    logger.fine("Preferences.setDefaultPreferences()");

    /* Place general preferences  */
    setDefaultPreference(SHOW_SPLASH_SCREEN, "true");

    // Model editor :
    setDefaultPreference(MODELEDITOR_PREFERXY, "false");

    // UV Coverage - image size and LUT :
    setDefaultPreference(MODEL_IMAGE_LUT, AsproConstants.DEFAULT_IMAGE_LUT);
    setDefaultPreference(MODEL_IMAGE_SIZE, AsproConstants.DEFAULT_IMAGE_SIZE);
  }

  /**
   * Return preference filename.
   *
   * @return preference filename.
   */
  protected String getPreferenceFilename() {
    logger.entering(className_, "getPreferenceFilename");
    return "fr.jmmc.aspro.properties";
  }

  /**
   *  Return preference version number.
   *
   * @return preference version number.
   */
  protected int getPreferencesVersionNumber() {
    logger.entering(className_, "getPreferencesVersionNumber");
    return 1;
  }

  /**
   * Return true if the show splash screen preference is undefined or its value is not 'false'
   * @return true if the show splash screen preference is undefined or its value is not 'false'
   */
  public boolean IsShowSplashScreen() {

    final String value = getPreference(SHOW_SPLASH_SCREEN);

    if (value != null && !Boolean.valueOf(value).booleanValue()) {
      return false;
    }
    return true;
  }
}

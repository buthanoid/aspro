/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.gui.action;

import fr.jmmc.aspro.FilePreferences;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.ob.ExportOBVega;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.util.MimeType;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the OB generation for the CHARA Vega instrument.
 *
 * @author bourgesl
 */
public final class ExportOBVegaAction {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class logger */
  private static final Logger logger = LoggerFactory.getLogger(ExportOBVegaAction.class.getName());
  /** double formatter for min elevation */
  private final static NumberFormat df1 = new DecimalFormat("0.#");
  /** StarList MimeType */
  private final static MimeType mimeType = MimeType.STAR_LIST;
  /** action singleton */
  private static final ExportOBVegaAction instance = new ExportOBVegaAction();

  /**
   * Return the singleton ExportOBVegaAction instance
   * @return ExportOBVegaAction instance
   */
  public static ExportOBVegaAction getInstance() {
    return instance;
  }

  /**
   * Forbidden Constructor
   */
  private ExportOBVegaAction() {
    super();
  }

  /**
   * Execute the action.
   */
  public void process() {
    logger.debug("process");

    final File file = FileChooser.showSaveFileChooser("Export targets as one Vega Star List",
            FilePreferences.getInstance().getDirectoryFile(mimeType), mimeType, getDefaultFileName());

    // If a file was defined (No cancel in the dialog)
    if (file != null) {
      FilePreferences.getInstance().setDirectory(mimeType, file.getParent());

      try {
        ExportOBVega.process(file);

        // use main observation :
        final ObservationSetting observation = ObservationManager.getInstance().getMainObservation();
        final double minElev = observation.getInterferometerConfiguration().getMinElevation();

        final String message = "Observing blocks exported with following settings:\n"
                + "  - night restrictions disabled\n"
                + "  - minimum elevation set to " + df1.format(minElev) + " deg";

        MessagePane.showMessage(message);

        StatusBar.show(file.getName() + " created.");

      } catch (IOException ioe) {
        MessagePane.showErrorMessage("Could not export to file : " + file.getAbsolutePath(), ioe);
      }
    }
  }

  /**
   * Generate a default name
   * @return default name [StarList_V01.txt]
   */
  private String getDefaultFileName() {
    return "StarList_V01." + mimeType.getExtension();
  }
}

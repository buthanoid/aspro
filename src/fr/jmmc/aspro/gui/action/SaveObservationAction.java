/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.gui.action;

import fr.jmmc.aspro.FilePreferences;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.mcs.gui.MessagePane;
import fr.jmmc.mcs.gui.StatusBar;
import fr.jmmc.mcs.util.MimeType;
import fr.jmmc.mcs.util.RegisteredAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JFileChooser;

/**
 * Save observation settings action
 * @author bourgesl
 */
public final class SaveObservationAction extends RegisteredAction {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class name. This name is used to register to the ActionRegistrar */
  public final static String className = "fr.jmmc.aspro.gui.action.SaveObservationAction";
  /** Action name. This name is used to register to the ActionRegistrar */
  public final static String actionName = "saveObservation";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(className);
  /** AsproX MimeType */
  private final static MimeType mimeType = MimeType.ASPRO_OBSERVATION;

  /**
   * Public constructor that automatically register the action in RegisteredAction.
   */
  public SaveObservationAction() {
    super(className, actionName);
  }

  /**
   * Handle the action event
   * @param evt action event
   */
  public void actionPerformed(final ActionEvent evt) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("actionPerformed");
    }
    save();
  }

  /**
   * Save the current observation
   * @return true if successfull
   */
  public boolean save() {
    final ObservationManager om = ObservationManager.getInstance();

    File file = om.getObservationFile();

    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(mimeType.getFileFilter());
    fileChooser.setCurrentDirectory(FilePreferences.getInstance().getDirectoryFile(mimeType));
    fileChooser.setSelectedFile(file);

    fileChooser.setDialogTitle("Save the current observation settings");

    final int returnVal = fileChooser.showSaveDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      file = mimeType.checkFileExtension(fileChooser.getSelectedFile());

      if (file.exists()) {
        if (!MessagePane.showConfirmFileOverwrite(file.getName())) {
          file = null;
        }
      }
    } else {
      file = null;
    }

    boolean result = true;

    // If a file was defined (No cancel in the dialog)
    if (file != null) {
      FilePreferences.getInstance().setDirectory(mimeType, file.getParent());

      try {
        om.save(file);

        StatusBar.show("file saved : " + file.getName());

      } catch (IOException ioe) {
        result = false;
        MessagePane.showErrorMessage("Could not save the file : " + file.getAbsolutePath(), ioe);
      }
    }
    return result;
  }
}

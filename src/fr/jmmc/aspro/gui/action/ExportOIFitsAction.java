/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ExportOIFitsAction.java,v 1.3 2010-09-24 15:54:25 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/09/02 15:47:19  bourgesl
 * use OI_VIS2 (always present)
 *
 * Revision 1.1  2010/06/29 12:13:21  bourgesl
 * added ExportToOIFits action
 *
 *
 */
package fr.jmmc.aspro.gui.action;

import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.util.FileUtils;
import fr.jmmc.mcs.gui.MessagePane;
import fr.jmmc.mcs.gui.StatusBar;
import fr.jmmc.mcs.util.FileFilterRepository;
import fr.jmmc.mcs.util.RegisteredAction;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIVis2;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * This registered action represents a File Menu entry to export an OIFits file
 * containing the visibilities of the selected target.
 * @author bourgesl
 */
public class ExportOIFitsAction extends RegisteredAction {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class name. This name is used to register to the ActionRegistrar */
  private final static String className = "fr.jmmc.aspro.gui.action.ExportOIFitsAction";
  /** Action name. This name is used to register to the ActionRegistrar */
  public final static String actionName = "exportOIFits";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(className);
  /** OIFits settings mime type */
  public static final String OIFITS_MIME_TYPE = "application/oifits";
  /** OIFits extension = oifits */
  public static final String OIFITS_EXT = "oifits";

  /* members */
  /** last directory used to save a file; by default = user home */
  private String lastDir = System.getProperty("user.home");

  /**
   * Public constructor that automatically register the action in RegisteredAction.
   */
  public ExportOIFitsAction() {
    super(className, actionName);

    FileFilterRepository.getInstance().put(OIFITS_MIME_TYPE, OIFITS_EXT, "Optical Interferometry FITS (" + OIFITS_EXT + ")");
  }

  /**
   * Handle the action event
   * @param evt action event
   */
  public void actionPerformed(final ActionEvent evt) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("actionPerformed");
    }
    final OIFitsFile oiFitsFile = ObservationManager.getInstance().getObservation().getOIFitsFile();

    if (oiFitsFile != null) {

      File file = null;

      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(getFileFilter());

      fileChooser.setSelectedFile(file);

      if (oiFitsFile.getAbsoluteFilePath() != null) {
        fileChooser.setSelectedFile(new File(oiFitsFile.getAbsoluteFilePath()));
      } else {
        if (this.getLastDir() != null) {
          fileChooser.setCurrentDirectory(new File(this.getLastDir()));
        }

        fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), getName(oiFitsFile)));
      }

      fileChooser.setDialogTitle("Export the current target as an OIFits file");

      final int returnVal = fileChooser.showSaveDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file = checkFileExtension(fileChooser.getSelectedFile());

        if (file.exists()) {
          final int answer = JOptionPane.showConfirmDialog(null, "File \'" + file.getName() + "\' already exists\nDo you want to overwrite this file ?");
          if (answer != JOptionPane.YES_OPTION) {
            file = null;
          }
        }
      } else {
        file = null;
      }

      // If a file was defined (No cancel in the dialog)
      if (file != null) {
        this.setLastDir(file.getParent());

        try {
          OIFitsWriter.writeOIFits(file.getAbsolutePath(), oiFitsFile);

          StatusBar.show(file.getName() + " created.");

        } catch (Exception e) {
          MessagePane.showErrorMessage(
                "Could not export to file : " + file.getName(), e);
        }
      }
    }
  }

  /**
   * Generate a default name for the given OIFits structure
   * @param oiFitsFile  OIFits structure
   * @return default name [ASPRO_<target-name>_<instrument>_date]
   */
  private String getName(final OIFitsFile oiFitsFile) {
    final StringBuilder sb = new StringBuilder(32).append("Aspro2_");

    final String targetName = oiFitsFile.getOiTarget().getTarget()[0];
    final String altName = targetName.replaceAll("[^a-zA-Z_0-9]", "_");

    sb.append(altName).append('_');

    final OIVis2 vis2 = oiFitsFile.getOiVis2()[0];

    final String insName = vis2.getInsName();

    sb.append(insName).append('_');

    final String dateObs = vis2.getDateObs();

    sb.append(dateObs);

    sb.append('.').append(OIFITS_EXT);

    return sb.toString();
  }

  /**
   * Return the file filter
   * @return file filter
   */
  protected FileFilter getFileFilter() {
    return FileFilterRepository.getInstance().get(OIFITS_MIME_TYPE);
  }

  /**
   * Check if the given file has the correct extension. If not, return a new file with it
   * @param file file to check
   * @return given file or new file with the correct extension
   */
  protected File checkFileExtension(final File file) {
    final String ext = FileUtils.getExtension(file);

    if (!OIFITS_EXT.equals(ext)) {
      return new File(file.getParentFile(), file.getName() + "." + OIFITS_EXT);
    }
    return file;
  }

  /**
   * Return the last directory used
   * @return last directory used
   */
  protected String getLastDir() {
    return this.lastDir;
  }

  /**
   * Define the last directory used
   * @param lastDir new value
   */
  protected void setLastDir(String lastDir) {
    this.lastDir = lastDir;
  }
}

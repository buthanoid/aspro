/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package test;

import fr.jmmc.aspro.gui.TargetEditorDialog;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.SwingUtils;
import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Simple
 * @author bourgesl
 */
public class TargetEditorTest {

  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TargetEditorTest.class.getName());

  /**
   * Private constructor
   */
  private TargetEditorTest() {
    super();
  }

  /**
   * Simple GUI test
   * @param args unused
   */
  public static void main(String[] args) {
    // Set the default locale to en-US locale (for Numerical Fields "." ",")
    Locale.setDefault(Locale.US);

    SwingUtils.invokeLaterEDT(new Runnable() {

      /**
       * Create the Gui using EDT
       */
      @Override
      public void run() {

        // invoke App method to initialize logback now:
        App.isReady();

        try {
          ObservationManager.getInstance().load(new File("/home/bourgesl/ASPRO2/VLTI_FUN2.asprox"));

          logger.info("result = " + TargetEditorDialog.showEditor("HIP32768", TargetEditorDialog.TAB_TARGETS));

          System.exit(0);
        } catch (Exception e) { // main (test)
          logger.log(Level.SEVERE, "runtime exception", e);
        }
      }
    });
  }
}
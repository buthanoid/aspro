/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import static java.awt.event.KeyEvent.*;
import static org.fest.swing.core.KeyPressInfo.*;
import static org.fest.swing.core.matcher.DialogMatcher.*;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;

import fest.common.JmcsApplicationSetup;

import fest.common.JmcsFestSwingJUnitTestCase;

import fr.jmmc.aspro.gui.SettingPanel;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JList;

import org.fest.swing.annotation.GUITest;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.matcher.FrameMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.core.matcher.JTextComponentMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.util.Platform;

import org.junit.Test;

/**
 * This simple tests takes screenshots to complete the Aspro2 documentation
 * 
 * @author bourgesl
 */
public final class AsproDocJUnitTest extends JmcsFestSwingJUnitTestCase {

  /**
   * Define the application
   */
  static {
    // disable dev LAF menu :
    System.setProperty("jmcs.laf.menu", "false");

    JmcsApplicationSetup.define(
            fr.jmmc.aspro.AsproGui.class,
            "-open", "/home/bourgesl/dev/aspro/test/Aspro2_sample.asprox");

    // define robot delays :
    defineRobotDelayBetweenEvents(SHORT_DELAY);

    // define delay before taking screenshot :
    defineScreenshotDelay(SHORT_DELAY);

    // disable tooltips :
    enableTooltips(false);

    // customize PDF action to avoid use StatusBar :
    fr.jmmc.aspro.gui.action.ExportPDFAction.setAvoidUseStatusBar(true);
  }

  /**
   * Test if the application started correctly
   */
  @Test
  @GUITest
  public void shouldStart() {
    window.textBox("starSearchField").setText("Simbad");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture initial state :
    showPlotTab(SettingPanel.TAB_INTERFEROMETER_MAP, "Aspro2-map.png");
  }

  /**
   * Capture the main panel
   */
  @Test
  @GUITest
  public void captureMain() {
    captureMainForm("Aspro2-main.png");
  }

  /**
   * Test observability plot
   */
  @Test
  @GUITest
  public void shouldShowObservability() {

    // Capture observability plot :
    showPlotTab(SettingPanel.TAB_OBSERVABILITY, "Aspro2-obs.png");

    final JPanelFixture panel = window.panel("observabilityPanel");

    // enable baseline limits :
    panel.checkBox("jCheckBoxBaseLineLimits").check();

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture observability plot of  baseline limits :
    saveScreenshot(window.tabbedPane(), "Aspro2-obs-bl.png");

    // disable baseline limits :
    panel.checkBox("jCheckBoxBaseLineLimits").uncheck();

    // enable detailed plot :
    panel.checkBox("jCheckBoxDetailedOutput").check();

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture observability plot of detailed plot :
    saveScreenshot(window.tabbedPane(), "Aspro2-obs-det.png");

    // disable detailed plot :
    panel.checkBox("jCheckBoxDetailedOutput").uncheck();

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();
  }

  /**
   * Test UV coverage plot
   */
  @Test
  @GUITest
  public void shouldShowUVCoverage() {

    // Capture UV Coverage plot :
    final BufferedImage image = showPlotTab(SettingPanel.TAB_UV_COVERAGE);

    saveImage(image, "Aspro2-uv.png");
    saveImage(image, "Aspro2-screen.png");

    // TODO : refactor that code :

    // miniature for aspro web page : 350px width :
    final int width = 350;
    final int height = Math.round(1f * width * image.getHeight() / image.getWidth());

    BufferedImage rescaledImage = null;
    ResampleOp resampleOp = null;

    // use Lanczos3 resampler and soft unsharp mask :
    resampleOp = new ResampleOp(width, height);
    resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
    rescaledImage = resampleOp.filter(image, null);

    saveImage(rescaledImage, "Aspro2-screen-small.png");

    // Export OIFits / OB :
    exportOIFits();
    exportOB();
  }

  /**
   * Test Vis2 plot
   */
  @Test
  @GUITest
  public void shouldShowVis2Plot() {

    // Capture Vis2 plot :
    showPlotTab(SettingPanel.TAB_VIS2, "Aspro2-vis2-noErr.png");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    window.list("jListTargets").selectItem("HD 1234");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture Vis2 plot :
    showPlotTab(SettingPanel.TAB_VIS2, "Aspro2-vis2-withErr.png");
    
    window.list("jListTargets").selectItem("HIP1234");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();
  }
  
  /**
   * Test Target editor
   */
  @Test
  @GUITest
  public void shouldOpenTargetEditor() {

    window.button("jButtonTargetEditor").click();

    final DialogFixture dialog = window.dialog(withTitle("Target Editor").andShowing());

    dialog.requireVisible();
    dialog.moveToFront();

    dialog.tabbedPane().selectTab("Models");

    dialog.tree().selectPath("Models/HIP1234/elong_disk1");

    saveScreenshot(dialog, "Aspro2-Model.png");

    dialog.tabbedPane().selectTab("Targets");

    dialog.tree().selectPath("Targets/HD 1234");

    saveScreenshot(dialog, "Aspro2-Target.png");

    // close dialog :
    dialog.button(JButtonMatcher.withText("Cancel")).click();
  }

  /**
   * Test Preferences
   */
  @Test
  @GUITest
  public void shouldOpenPreferences() {
    window.menuItemWithPath("Edit", "Preferences...").click();

    final Frame prefFrame = robot().finder().find(FrameMatcher.withTitle("Preferences"));

    if (prefFrame != null) {
      final FrameFixture frame = new FrameFixture(robot(), prefFrame);

      frame.requireVisible();
      frame.moveToFront();

      saveScreenshot(frame, "Aspro2-prefs.png");

      // close frame :
      frame.close();
    }
  }

  /**
   * Test Interop menu : Start SearchCal and LITpro manually before this test
   */
  @Test
  @GUITest
  public void showInteropMenu() {
    window.menuItemWithPath("Interop").click();
    captureMainForm("Aspro2-interop-menu.png");

    window.menuItemWithPath("Interop", "Show Hub Status").click();

    final Frame hubFrame = robot().finder().find(FrameMatcher.withTitle("SAMP Status"));

    if (hubFrame != null) {
      final FrameFixture frame = new FrameFixture(robot(), hubFrame);

      frame.requireVisible();
      frame.moveToFront();

      frame.list(new GenericTypeMatcher<JList>(JList.class) {

        @Override
        protected boolean isMatching(JList component) {
          return "org.astrogrid.samp.gui.ClientListCellRenderer".equals(component.getCellRenderer().getClass().getName());
        }
      }).selectItem("Aspro2");

      saveScreenshot(frame, "Aspro2-interop-hubStatus.png");

      // close frame :
      frame.close();
    }
  }

  /**
   * Test SearchCal integration : Start SearchCal manually before this test
   */
  @Test
  @GUITest
  public void shouldCallSearchCall() {
    // hack to solve focus trouble in menu items :
    window.menuItemWithPath("Interop").focus();

    try {
      enableTooltips(true);

      window.menuItemWithPath("Interop", "Search calibrators").click();
      window.menuItemWithPath("Interop", "Search calibrators", "SearchCal").focus();

      captureMainForm("Aspro2-calibrators-StartSearchQuery.png");

    } finally {
      enableTooltips(false);
    }
  }

  /**
   * Test LITpro integration : Start LITpro manually before this test
   */
  @Test
  @GUITest
  public void shouldCallLITpro() {

    window.list("jListTargets").selectItem("HD 1234");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Export UV Coverage plot as PDF :
    selectTab(SettingPanel.TAB_UV_COVERAGE);

    // Export OIFits / OB :
    exportOIFits();
    exportOB();

    // hack to solve focus trouble in menu items :
    window.menuItemWithPath("Interop").focus();

    try {
      enableTooltips(true);

      window.menuItemWithPath("Interop", "Perform model fitting").click();
      window.menuItemWithPath("Interop", "Perform model fitting", "LITpro").focus();

      captureMainForm("Aspro2-LITpro-send.png");

    } finally {
      enableTooltips(false);
    }
  }

  /**
   * Test Chara PoPs
   */
  @Test
  @GUITest
  public void shouldShowCharaPoPs() {

    // show observability plot :
    window.tabbedPane().selectTab(SettingPanel.TAB_OBSERVABILITY);

    window.list("jListTargets").selectItem("HIP1234");

    final JPanelFixture form = window.panel("observationForm");

    // select CHARA interferometer :
    form.comboBox("jComboBoxInterferometer").selectItem("CHARA");

    final int left = 5 + window.panel("jPanelTargets").component().getWidth();

    final int width = 5 + window.panel("jPanelMain").component().getWidth()
            + window.panel("jPanelConfigurations").component().getWidth();

    final int height = getMainFormHeight(window) + 68;

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    saveCroppedScreenshotOf("popsAuto.png", left, 0, width, height);

    // set PoPs to '25' :
    final JFormattedTextField jTextPoPs = (JFormattedTextField) form.textBox("jTextPoPs").component();

    GuiActionRunner.execute(new GuiTask() {

      @Override
      protected void executeInEDT() {
        // Integer field :
        jTextPoPs.setValue(Integer.valueOf(25));
      }
    });

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // show observability plot (force plot refresh) :
    window.tabbedPane().selectTab(SettingPanel.TAB_OBSERVABILITY);

    saveCroppedScreenshotOf("popsUser.png", left, 0, width, height);
  }

  /**
   * Test Open file "/home/bourgesl/dev/aspro/test/Aspro2_sample_with_calibrators.asprox"
   */
  @Test
  @GUITest
  public void shouldOpenSampleWithCalibrators() {

    // hack to solve focus trouble in menu items :
    window.menuItemWithPath("File").focus();
    window.menuItemWithPath("File", "Open observation").click();

    window.fileChooser().selectFile(new File("/home/bourgesl/dev/aspro/test/Aspro2_sample_with_calibrators.asprox"));
    window.fileChooser().approve();

    window.list("jListTargets").selectItem("HD 3546 (cal)");

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture observability plot :
    showPlotTab(SettingPanel.TAB_OBSERVABILITY, "Aspro2-calibrators-obs.png");

    // Capture UV Coverage plot :
    showPlotTab(SettingPanel.TAB_UV_COVERAGE, "Aspro2-calibrators-uv.png");

    // Export OIFits / OB :
    exportOIFits();
    exportOB();

    // target editor with calibrators :
    window.button("jButtonTargetEditor").click();

    final DialogFixture dialog = window.dialog(withTitle("Target Editor").andShowing());

    dialog.requireVisible();
    dialog.moveToFront();

    dialog.tabbedPane().selectTab("Models");

    dialog.tree().selectPath("Models/HD 3546 (cal)/disk1");

    saveScreenshot(dialog, "Aspro2-calibrators-Model.png");

    dialog.tabbedPane().selectTab("Targets");

    dialog.tree().selectPath("Targets/HIP1234/HD 3546 (cal)");

    saveScreenshot(dialog, "Aspro2-calibrators-Target.png");

    // close dialog :
    dialog.button(JButtonMatcher.withText("Cancel")).click();
  }

  /**
   * Test Open file "/home/bourgesl/dev/aspro/test/Aspro2_sample_multi.asprox"
   */
  @Test
  @GUITest
  public void shouldOpenSampleMultiConf() {

    // hack to solve focus trouble in menu items :
    window.menuItemWithPath("File").focus();
    window.menuItemWithPath("File", "Open observation").click();

    window.fileChooser().selectFile(new File("/home/bourgesl/dev/aspro/test/Aspro2_sample_multi.asprox"));
    window.fileChooser().approve();

    // waits for computation to finish :
    AsproTestUtils.checkRunningTasks();

    // Capture map plot :
    showPlotTab(SettingPanel.TAB_INTERFEROMETER_MAP, "Aspro2-multiConf-map.png");

    // Capture observability plot :
    showPlotTab(SettingPanel.TAB_OBSERVABILITY, "Aspro2-multiConf-obs.png");

    // Capture UV Coverage plot :
    showPlotTab(SettingPanel.TAB_UV_COVERAGE, "Aspro2-multiConf-uv.png");
  }

  /**
   * Test Feedback report
   */
  @Test
  @GUITest
  public void shouldOpenFeedbackReport() {

    // hack to solve focus trouble in menu items :
    window.menuItemWithPath("Help").focus();

    window.menuItemWithPath("Help", "Report Feedback to JMMC...").click();

    final DialogFixture dialog = window.dialog(withTitle("JMMC Feedback Report ").andShowing());

    dialog.requireVisible();
    dialog.moveToFront();

    final String myEmail = "laurent.bourges@obs.ujf-grenoble.fr";

    final JTextComponentFixture emailField = dialog.textBox(JTextComponentMatcher.withText(myEmail));

    // hide my email address :
    emailField.setText("type your email address here");

    saveScreenshot(dialog, "Aspro2-FeebackReport.png");

    // restore my preferences :
    emailField.setText(myEmail);

    // close dialog :
    dialog.close();
  }

  /**
   * Test the application exit sequence : ALWAYS THE LAST TEST
   */
  @Test
  @GUITest
  public void shouldExit() {
    logger.severe("shouldExit test");

    window.close();

    confirmDialogDontSave();
  }

  /* 
  --- Utility methods  ---------------------------------------------------------
   */
  /**
   * Export Observing block file using default file name
   */
  private void exportOB() {
    // export OB file :
    window.pressAndReleaseKey(keyCode(VK_B).modifiers(Platform.controlOrCommandMask()));

    // use image folder to store OB file and validate :
    window.fileChooser().setCurrentDirectory(getScreenshotFolder()).approve();

    // overwrite any existing file :
    confirmDialogFileOverwrite();
    // close report message (ob files) :
    closeMessage();
    // close check ESO magnitudes :
    closeMessage();
  }

  /**
   * Export OIFits file using default file name
   */
  private void exportOIFits() {
    // export OIFits :
    window.pressAndReleaseKey(keyCode(VK_F).modifiers(Platform.controlOrCommandMask()));

    // use image folder to store OIFits and validate :
    window.fileChooser().setCurrentDirectory(getScreenshotFolder()).approve();

    // overwrite any existing file :
    confirmDialogFileOverwrite();
  }

  /**
   * Export the current plot to PDF
   */
  private void exportPDF() {
    // export PDF :
    window.pressAndReleaseKey(keyCode(VK_P).modifiers(Platform.controlOrCommandMask()));

    // use image folder to store PDF and validate :
    window.fileChooser().setCurrentDirectory(getScreenshotFolder()).approve();

    // overwrite any existing file :
    confirmDialogFileOverwrite();
  }

  /**
   * Show the plot tab of the given name and return the window screenshot
   * @param tabName tab name
   * @return screenshot image
   */
  private BufferedImage showPlotTab(final String tabName) {
    // select tab :
    window.tabbedPane().selectTab(tabName);

    final BufferedImage image = takeScreenshotOf(window);

    // export PDF :
    exportPDF();

    return image;
  }

  /**
   * Show the plot tab of the given name and capture the window screenshot
   * @param tabName tab name
   * @param fileName file name of the window screenshot
   */
  private void showPlotTab(final String tabName, final String fileName) {
    // select tab :
    window.tabbedPane().selectTab(tabName);

    // Capture window screenshot :
    saveScreenshot(window, fileName);

    // export PDF :
    exportPDF();
  }

  /**
   * Show the plot tab of the given name and export PDF using default file name
   * @param tabName tab name
   */
  private void selectTab(final String tabName) {
    window.tabbedPane().selectTab(tabName);

    // export PDF :
    exportPDF();
  }

  /**
   * Capture a screenshot of the main form using the given file name
   * @param fileName the file name (including the png extension)
   */
  private void captureMainForm(final String fileName) {
    saveCroppedScreenshotOf(fileName, 0, 0, -1, getMainFormHeight(window));
  }

  /**
   * Determine the height of the main form
   * @param window window fixture
   * @return height of the main form
   */
  private static int getMainFormHeight(final FrameFixture window) {
    int height = 32 + 10;

    JComponent com;
    com = window.panel("observationForm").component();
    height += com.getHeight();

    com = window.menuItemWithPath("File").component();
    height += com.getHeight();

    return height;
  }
}

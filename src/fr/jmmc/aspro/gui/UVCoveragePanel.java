/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: UVCoveragePanel.java,v 1.57 2010-10-01 15:30:52 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.56  2010/09/26 12:00:07  bourgesl
 * do not catch runtime exceptions
 *
 * Revision 1.55  2010/09/24 15:49:48  bourgesl
 * use MessagePane
 *
 * Revision 1.54  2010/09/23 19:46:35  bourgesl
 * comments when calling FeedBackReport
 *
 * Revision 1.53  2010/09/20 14:46:02  bourgesl
 * minor refactoring changes
 *
 * Revision 1.52  2010/09/15 13:56:31  bourgesl
 * added JMMC copyright on plot
 *
 * Revision 1.51  2010/09/08 16:00:31  bourgesl
 * unregister Preference Observers when the widget is released (Preference View, UV Coverage Panel)
 *
 * Revision 1.50  2010/09/06 13:39:34  bourgesl
 * small changes on Panels (scrollbar added as needed) in order to solve widget display on small screens
 *
 * Revision 1.49  2010/07/22 15:45:43  bourgesl
 * added acquisition time in UV coverage and observation
 *
 * Revision 1.48  2010/07/22 14:34:23  bourgesl
 * sampling time is updated with the default sampling time of the instrument when the instrument is changed
 * atmosphere quality is displayed and updated when an observation is loaded
 *
 * Revision 1.47  2010/07/07 15:12:15  bourgesl
 * fixed NPE on optional fields (load)
 *
 * Revision 1.46  2010/07/05 14:52:26  bourgesl
 * corrected comments
 *
 * Revision 1.45  2010/06/25 14:16:51  bourgesl
 * refactoring to use UV for WMin / WMax
 *
 * Revision 1.44  2010/06/23 12:52:08  bourgesl
 * ObservationManager regsitration for observation events moved in SettingPanel (external)
 *
 * Revision 1.43  2010/06/17 10:02:51  bourgesl
 * fixed warning hints - mainly not final static loggers
 *
 * Revision 1.42  2010/06/10 08:54:06  bourgesl
 * rename variable
 *
 * Revision 1.41  2010/06/09 12:51:09  bourgesl
 * new interface PDFExportable to define a standard method performPDFAction() that use ExportPDFAction to export the chart to PDF
 *
 * Revision 1.40  2010/06/08 12:39:06  bourgesl
 * minor UI changes : pdf button moved to bottom, changed weight constraint (resizing issues)
 * javadoc
 *
 * Revision 1.39  2010/06/08 10:20:42  bourgesl
 * minor UI changes (layout / margins)
 *
 * Revision 1.38  2010/06/07 16:03:29  bourgesl
 * changed tooltip on 'OB' button
 *
 * Revision 1.37  2010/05/26 15:30:24  bourgesl
 * added CHARA Vega Star List generation (OB like)
 *
 * Revision 1.36  2010/05/21 14:27:48  bourgesl
 * use preferences for Model Image Lut & Size
 * removed previous widgets
 *
 * Revision 1.35  2010/05/11 12:04:56  bourgesl
 * minor changes due to ChartUtils & chart duration
 *
 * Revision 1.34  2010/05/06 15:40:20  bourgesl
 * added updateObservation and plot debug logs
 * added better auto update/refresh flag handling
 * HA Min/Max + FT Mode are related to the target (onLoad, onChange, updateObservation)
 *
 * Revision 1.33  2010/05/05 14:28:48  bourgesl
 * on load : restore sampling period + image defaults (size, lut)
 * added ha Min / Max to generate OB with correct LST intervals
 *
 * Revision 1.32  2010/04/14 13:09:59  bourgesl
 * first minimal OB for MIDI
 *
 * Revision 1.31  2010/04/13 14:18:27  bourgesl
 * uniform sizes for sliders and text fields
 *
 * Revision 1.30  2010/04/08 14:06:51  bourgesl
 * javadoc
 *
 * Revision 1.29  2010/04/06 14:40:47  bourgesl
 * minor UI changes for mac os (II)
 *
 * Revision 1.28  2010/04/06 13:58:37  bourgesl
 * minor UI changes for mac os & other LAF
 *
 * Revision 1.27  2010/04/02 10:05:24  bourgesl
 * added OB for AMBER
 *
 * Revision 1.26  2010/03/30 12:10:33  bourgesl
 * disable model image widgets for targets without any defined model
 *
 * Revision 1.25  2010/02/19 16:06:08  bourgesl
 * added image size & LUT combo boxes
 *
 * Revision 1.24  2010/02/18 15:52:38  bourgesl
 * added parameter argument validation with an user message
 *
 * Revision 1.23  2010/02/17 15:13:18  bourgesl
 * image mode disabled if plot image unchecked
 *
 * Revision 1.22  2010/02/16 14:48:26  bourgesl
 * if the model editor was successfull (ok), update the plots
 *
 * Revision 1.21  2010/02/15 16:47:26  bourgesl
 * model editor supports add / remove model
 *
 * Revision 1.20  2010/02/12 15:53:18  bourgesl
 * added target model editor
 *
 * Revision 1.19  2010/02/09 16:51:09  bourgesl
 * added change listener for image modes
 *
 * Revision 1.18  2010/02/08 17:00:16  bourgesl
 * added U-V max selector + checkboxes
 *
 * Revision 1.17  2010/02/05 16:17:01  bourgesl
 * added widgets for UV Model
 *
 * Revision 1.16  2010/02/05 13:13:30  bourgesl
 * fixed NPE
 *
 * Revision 1.15  2010/02/04 17:05:06  bourgesl
 * UV bounds are coming from UVCoverageService
 *
 * Revision 1.14  2010/02/04 14:54:11  bourgesl
 * UVMapData refactoring (uvRect, min/max values) to keep the color mapping consistent when zooming
 * Compute an sub Image when a zoom occurs while the correct model is computed in the background
 *
 * Revision 1.13  2010/02/03 16:07:49  bourgesl
 * refactoring to use the custom swing worker executor
 * when zomming uv map is computed asynchronously
 *
 * Revision 1.12  2010/02/03 09:48:53  bourgesl
 * target model uvmap added on the uv coverage with zooming supported
 *
 * Revision 1.11  2010/01/22 14:25:29  bourgesl
 * fixed log level
 *
 * Revision 1.10  2010/01/22 13:17:20  bourgesl
 * change color association to plots
 *
 * Revision 1.9  2010/01/21 16:41:30  bourgesl
 * added HA min / max sliders and used only to constraint the UV tracks
 *
 * Revision 1.8  2010/01/20 16:18:38  bourgesl
 * observation form refactoring
 *
 * Revision 1.7  2010/01/19 13:20:20  bourgesl
 * NPE fixed when the observability displays the baseline limits
 *
 * Revision 1.6  2010/01/15 16:14:16  bourgesl
 * added computation of UV points compatible with observability ranges, bandpass and sampling periodicity
 *
 * Revision 1.5  2010/01/15 13:52:14  bourgesl
 * instrumentMode synchronized properly between the observation and the UI widgets (load/change/reset)
 *
 * Revision 1.4  2010/01/14 17:03:37  bourgesl
 * refactoring for observation LOAD / CHANGE events
 *
 * Revision 1.3  2010/01/13 16:12:31  bourgesl
 * added export to PDF button
 *
 * Revision 1.2  2010/01/12 16:54:19  bourgesl
 * added PoPs in title + several changes on charts
 *
 * Revision 1.1  2010/01/11 13:58:43  bourgesl
 * bad class name for UV Coverage Panel
 *
 * Revision 1.1  2010/01/08 16:48:30  bourgesl
 * package refactoring
 *
 */
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.Preferences;
import fr.jmmc.aspro.gui.action.ExportOBVLTIAction;
import fr.jmmc.aspro.gui.action.ExportOBVegaAction;
import fr.jmmc.aspro.gui.action.ExportPDFAction;
import fr.jmmc.aspro.gui.chart.ChartUtils;
import fr.jmmc.aspro.gui.chart.SquareChartPanel;
import fr.jmmc.aspro.gui.chart.SquareXYPlot;
import fr.jmmc.aspro.gui.chart.ZoomEvent;
import fr.jmmc.aspro.gui.chart.ZoomEventListener;
import fr.jmmc.aspro.gui.util.ColorPalette;
import fr.jmmc.aspro.gui.util.FieldSliderAdapter;
import fr.jmmc.aspro.gui.util.SwingWorkerExecutor;
import fr.jmmc.aspro.model.ConfigurationManager;
import fr.jmmc.aspro.model.ObservationListener;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.model.Range;
import fr.jmmc.aspro.model.observability.ObservabilityData;
import fr.jmmc.aspro.model.observability.StarData;
import fr.jmmc.aspro.model.oi.AtmosphereQuality;
import fr.jmmc.aspro.model.oi.InterferometerConfiguration;
import fr.jmmc.aspro.model.uvcoverage.UVCoverageData;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.model.oi.Pop;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.aspro.model.oi.TargetConfiguration;
import fr.jmmc.aspro.model.util.AtmosphereQualityUtils;
import fr.jmmc.aspro.model.uvcoverage.UVBaseLineData;
import fr.jmmc.aspro.model.uvcoverage.UVRangeBaseLineData;
import fr.jmmc.aspro.service.UVCoverageService;
import fr.jmmc.mcs.gui.FeedbackReport;
import fr.jmmc.mcs.gui.MessagePane;
import fr.jmmc.mcs.gui.StatusBar;
import fr.jmmc.mcs.image.ColorModels;
import fr.jmmc.mcs.model.ModelUVMapService;
import fr.jmmc.mcs.model.ModelUVMapService.ImageMode;
import fr.jmmc.mcs.model.UVMapData;
import fr.jmmc.mcs.model.targetmodel.Model;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.IndexColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingworker.SwingWorker;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 * This panel presents the UV coverage plot with its parameters (target, instrument mode ...)
 * @author bourgesl
 */
public final class UVCoveragePanel extends javax.swing.JPanel implements ChartProgressListener, ZoomEventListener,
                                                                         ActionListener, ChangeListener, ObservationListener, Observer, PDFExportable, Disposable {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.gui.UVChartPanel";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /** flag to log a stack trace in method updateObservation() to detect multiple calls */
  private final static boolean DEBUG_UPDATE_EVENT = false;
  /** flag to log a stack trace in method plot() to detect multiple calls */
  private final static boolean DEBUG_PLOT_EVENT = false;
  /** scaling factor to Mega Lambda for U,V points */
  private final static double MEGA_LAMBDA_SCALE = 1e-6;

  /* members */
  /** observation manager */
  private final ObservationManager om = ObservationManager.getInstance();
  /** preference singleton */
  private final Preferences myPreferences = Preferences.getInstance();
  /** jFreeChart instance */
  private JFreeChart localJFreeChart;
  /** xy plot instance */
  private SquareXYPlot localXYPlot;
  /** JMMC annotation */
  private XYTextAnnotation aJMMC = null;
  /** uv coordinates scaling factor */
  private double uvPlotScalingFactor = MEGA_LAMBDA_SCALE;
  /* cached data */
  /** last computed Observability Data to get star data */
  private ObservabilityData currentObsData = null;
  /** last zoom event to check if the zoom area changed */
  private ZoomEvent lastZoomEvent = null;
  /** last computed UV Map Data to have a reference UV Map */
  private UVMapData currentUVMapData = null;
  /** current interferometer configuration name to track changes */
  private String interferometerConfigurationName = null;
  /** current instrument name to track changes */
  private String instrumentName = null;
  /* swing */
  /** chart panel */
  private SquareChartPanel chartPanel;
  /** flag to enable / disable the automatic refresh of the plot when any swing component changes */
  private boolean doAutoRefresh = true;
  /** flag to enable / disable the automatic update of the observation when any swing component changes */
  private boolean doAutoUpdateObservation = true;
  /** custom adapter for HA min fields */
  private FieldSliderAdapter haMinAdapter = null;
  /** custom adapter for HA max fields */
  private FieldSliderAdapter haMaxAdapter = null;
  /** custom adapter for UV max fields */
  private FieldSliderAdapter uvMaxAdapter = null;

  /**
   * Constructor
   */
  public UVCoveragePanel() {
    initComponents();

    postInit();
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jSplitPane = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    jPanelLeft = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jPanelButtons = new javax.swing.JPanel();
    jButtonModelEditor = new javax.swing.JButton();
    jButtonOB = new javax.swing.JButton();
    jComboBoxTarget = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    jComboBoxInstrumentMode = new javax.swing.JComboBox();
    jLabel3 = new javax.swing.JLabel();
    jFieldSamplingPeriod = new javax.swing.JFormattedTextField();
    jLabel4 = new javax.swing.JLabel();
    jSliderHAMin = new javax.swing.JSlider();
    jLabel5 = new javax.swing.JLabel();
    jSliderHAMax = new javax.swing.JSlider();
    jFieldHAMin = new javax.swing.JFormattedTextField();
    jFieldHAMax = new javax.swing.JFormattedTextField();
    jTargetHAMin = new javax.swing.JLabel();
    jTargetHAMax = new javax.swing.JLabel();
    jComboBoxImageMode = new javax.swing.JComboBox();
    jCheckBoxModelImage = new javax.swing.JCheckBox();
    jSeparator1 = new javax.swing.JSeparator();
    jCheckBoxPlotUVSupport = new javax.swing.JCheckBox();
    jLabel6 = new javax.swing.JLabel();
    jSeparator3 = new javax.swing.JSeparator();
    jSliderUVMax = new javax.swing.JSlider();
    jFieldUVMax = new javax.swing.JFormattedTextField();
    jLabel7 = new javax.swing.JLabel();
    jLabelAtmQual = new javax.swing.JLabel();
    jComboBoxAtmQual = new javax.swing.JComboBox();
    jLabelFTMode = new javax.swing.JLabel();
    jComboBoxFTMode = new javax.swing.JComboBox();
    jPanelBottom = new javax.swing.JPanel();
    jButtonPDF = new javax.swing.JButton();
    jFieldObsDuration = new javax.swing.JFormattedTextField();
    jLabel8 = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    jSplitPane.setDividerSize(5);
    jSplitPane.setResizeWeight(0.05);
    jSplitPane.setMinimumSize(new java.awt.Dimension(320, 400));
    jSplitPane.setPreferredSize(new java.awt.Dimension(320, 400));

    jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 400));
    jScrollPane1.setPreferredSize(new java.awt.Dimension(220, 400));

    jPanelLeft.setMinimumSize(new java.awt.Dimension(185, 550));
    jPanelLeft.setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("Target");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.ipadx = 2;
    gridBagConstraints.ipady = 2;
    jPanelLeft.add(jLabel1, gridBagConstraints);

    jPanelButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 2));

    jButtonModelEditor.setText("Model Editor");
    jButtonModelEditor.setMargin(new java.awt.Insets(0, 0, 0, 0));
    jButtonModelEditor.setMinimumSize(new java.awt.Dimension(50, 25));
    jButtonModelEditor.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonModelEditorActionPerformed(evt);
      }
    });
    jPanelButtons.add(jButtonModelEditor);

    jButtonOB.setText("OB");
    jButtonOB.setToolTipText("Only CHARA VEGA or VLTI AMBER/MIDI instruments are supported");
    jButtonOB.setMargin(new java.awt.Insets(0, 0, 0, 0));
    jButtonOB.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonOBActionPerformed(evt);
      }
    });
    jPanelButtons.add(jButtonOB);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 0.3;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    jPanelLeft.add(jPanelButtons, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jComboBoxTarget, gridBagConstraints);

    jLabel2.setText("Instrument mode");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabel2, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jComboBoxInstrumentMode, gridBagConstraints);

    jLabel3.setText("Sampling Periodicity (min)");
    jLabel3.setToolTipText("One set of calibrated visibilities in the u-v plane is taken at this interval (minutes)"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabel3, gridBagConstraints);

    jFieldSamplingPeriod.setColumns(3);
    jFieldSamplingPeriod.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
    jFieldSamplingPeriod.setMinimumSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jFieldSamplingPeriod, gridBagConstraints);

    jLabel4.setText("HA min");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 17;
    jPanelLeft.add(jLabel4, gridBagConstraints);

    jSliderHAMin.setMajorTickSpacing(30);
    jSliderHAMin.setMaximum(240);
    jSliderHAMin.setPaintTicks(true);
    jSliderHAMin.setMaximumSize(new java.awt.Dimension(80, 32767));
    jSliderHAMin.setPreferredSize(new java.awt.Dimension(80, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 18;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jSliderHAMin, gridBagConstraints);

    jLabel5.setText("HA max");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 19;
    jPanelLeft.add(jLabel5, gridBagConstraints);

    jSliderHAMax.setMajorTickSpacing(30);
    jSliderHAMax.setMaximum(240);
    jSliderHAMax.setPaintTicks(true);
    jSliderHAMax.setMaximumSize(new java.awt.Dimension(80, 32767));
    jSliderHAMax.setPreferredSize(new java.awt.Dimension(80, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 20;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jSliderHAMax, gridBagConstraints);

    jFieldHAMin.setColumns(6);
    jFieldHAMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
    jFieldHAMin.setMinimumSize(new java.awt.Dimension(50, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 18;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jFieldHAMin, gridBagConstraints);

    jFieldHAMax.setColumns(6);
    jFieldHAMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
    jFieldHAMax.setMinimumSize(new java.awt.Dimension(50, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 20;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jFieldHAMax, gridBagConstraints);

    jTargetHAMin.setText("targetHAMin");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 17;
    jPanelLeft.add(jTargetHAMin, gridBagConstraints);

    jTargetHAMax.setText("targetHAMax");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 19;
    jPanelLeft.add(jTargetHAMax, gridBagConstraints);

    jComboBoxImageMode.setModel(new DefaultComboBoxModel(ModelUVMapService.ImageMode.values()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 25;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jComboBoxImageMode, gridBagConstraints);

    jCheckBoxModelImage.setSelected(true);
    jCheckBoxModelImage.setText("<html>Underplot a model image</html>");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 24;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanelLeft.add(jCheckBoxModelImage, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 23;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelLeft.add(jSeparator1, gridBagConstraints);

    jCheckBoxPlotUVSupport.setSelected(true);
    jCheckBoxPlotUVSupport.setText("<html>Plot rise/set uv tracks</html>");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 21;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanelLeft.add(jCheckBoxPlotUVSupport, gridBagConstraints);

    jLabel6.setText("Plot what ...");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 25;
    jPanelLeft.add(jLabel6, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelLeft.add(jSeparator3, gridBagConstraints);

    jSliderUVMax.setMajorTickSpacing(10);
    jSliderUVMax.setPaintTicks(true);
    jSliderUVMax.setValue(100);
    jSliderUVMax.setMaximumSize(new java.awt.Dimension(80, 32767));
    jSliderUVMax.setPreferredSize(new java.awt.Dimension(80, 30));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jSliderUVMax, gridBagConstraints);

    jFieldUVMax.setColumns(6);
    jFieldUVMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
    jFieldUVMax.setMinimumSize(new java.awt.Dimension(50, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jFieldUVMax, gridBagConstraints);

    jLabel7.setText("U-V range to plot");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabel7, gridBagConstraints);

    jLabelAtmQual.setText("Atmosphere quality");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabelAtmQual, gridBagConstraints);

    jComboBoxAtmQual.setModel(new DefaultComboBoxModel(AtmosphereQualityUtils.getAtmosphereQualityList()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jComboBoxAtmQual, gridBagConstraints);

    jLabelFTMode.setText("Fringe tracker mode");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabelFTMode, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelLeft.add(jComboBoxFTMode, gridBagConstraints);

    jPanelBottom.setLayout(new javax.swing.BoxLayout(jPanelBottom, javax.swing.BoxLayout.LINE_AXIS));

    jButtonPDF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/fr/jmmc/aspro/gui/icons/icon_pdf.gif"))); // NOI18N
    jButtonPDF.setAlignmentY(1.0F);
    jButtonPDF.setMargin(new java.awt.Insets(0, 0, 0, 0));
    jButtonPDF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonPDFActionPerformed(evt);
      }
    });
    jPanelBottom.add(jButtonPDF);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 26;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 0.5;
    jPanelLeft.add(jPanelBottom, gridBagConstraints);

    jFieldObsDuration.setColumns(3);
    jFieldObsDuration.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
    jFieldObsDuration.setMinimumSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 16;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelLeft.add(jFieldObsDuration, gridBagConstraints);

    jLabel8.setText("Total Integration time (s)");
    jLabel8.setToolTipText("Time REALLY spent on-source, in seconds, per calibrated point");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.gridwidth = 2;
    jPanelLeft.add(jLabel8, gridBagConstraints);

    jScrollPane1.setViewportView(jPanelLeft);

    jSplitPane.setLeftComponent(jScrollPane1);

    add(jSplitPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Open the Model Editor with the selected target
   * @param evt action event
   */
  private void jButtonModelEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModelEditorActionPerformed
    final String targetName = getSelectedTargetName();

    // show model editor :
    if (TargetModelForm.showModelEditor(targetName)) {
      changeStateForModelImageWidgets();
      refreshPlot();
    }
}//GEN-LAST:event_jButtonModelEditorActionPerformed

  /**
   * Export the selected target as an Observing Block (OB)
   * @param evt action event
   */
  private void jButtonOBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOBActionPerformed
    this.performOBAction(evt);
  }//GEN-LAST:event_jButtonOBActionPerformed

  /**
   * Export the selected target as an Observing Block (OB)
   * @param evt action event
   */
  public void performOBAction(final ActionEvent evt) {
    final ObservationSetting observation = this.om.getObservation();
    final String insName = observation.getInstrumentConfiguration().getName();

    if (AsproConstants.INS_AMBER.equals(insName) || AsproConstants.INS_MIDI.equals(insName)) {
      // set the source with this instance :
      evt.setSource(this);

      ExportOBVLTIAction.getInstance().process(evt);

    } else if (insName.startsWith(AsproConstants.INS_VEGA)) {

      ExportOBVegaAction.getInstance().process();

    } else {
      MessagePane.showMessage("The application can not generate an Observing Block for this instrument [" + insName + "] !");
    }
  }

  /**
   * Export the current chart as a PDF document
   * @param evt action event
   */
  private void jButtonPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPDFActionPerformed
    this.performPDFAction();
  }//GEN-LAST:event_jButtonPDFActionPerformed

  /**
   * Export the current chart as a PDF document
   */
  public void performPDFAction() {
    ExportPDFAction.exportPDF(this.localJFreeChart);
  }

  /**
   * This method is useful to set the models and specific features of initialized swing components :
   */
  private void postInit() {

    this.localJFreeChart = ChartUtils.createSquareXYLineChart("U (M\u03BB)", "V (M\u03BB)", true);
    this.localXYPlot = (SquareXYPlot) localJFreeChart.getPlot();

    // Adjust background settings :
    this.localXYPlot.setBackgroundImageAlpha(1.0f);
    // Adjust outline :
    this.localXYPlot.setOutlineStroke(new BasicStroke(1.f));

    // add listener :
    this.localJFreeChart.addProgressListener(this);

    this.chartPanel = new SquareChartPanel(this.localJFreeChart,
            400, 400, /* prefered size */
            200, 200, /* minimum size before scaling */
            1600, 1600, /* maximum size before scaling */
            true, /* use buffer */
            false, /* properties */
            true, /* copy */
            true, /* save */
            true, /* print */
            false, /* zoom */
            false /* tooltips */);

    // zoom options :
    this.chartPanel.setDomainZoomable(AsproConstants.ENABLE_ZOOM);
    this.chartPanel.setRangeZoomable(AsproConstants.ENABLE_ZOOM);

    // define zoom listener :
    this.chartPanel.setZoomEventListener(this);

    // define min and prefered size for chart panel used by the split pane container :
    this.chartPanel.setMinimumSize(new Dimension(650, 500));
    this.jSplitPane.setRightComponent(this.chartPanel);

    // define change listeners :
    this.jComboBoxTarget.addActionListener(this);
    this.jComboBoxInstrumentMode.addActionListener(this);
    this.jComboBoxFTMode.addActionListener(this);
    this.jComboBoxAtmQual.addActionListener(this);

    this.uvMaxAdapter = new FieldSliderAdapter(jSliderUVMax, jFieldUVMax, 0d, 0d, 0d);
    this.uvMaxAdapter.addChangeListener(this);

    // default sampling Period and property change listener :
    this.jFieldSamplingPeriod.setValue(AsproConstants.DEFAULT_SAMPLING_PERIOD);
    this.jFieldSamplingPeriod.addPropertyChangeListener("value", new PropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent evt) {
        final double newValue = ((Number) jFieldSamplingPeriod.getValue()).doubleValue();

        if (newValue <= 0d) {
          // invalid value :
          jFieldSamplingPeriod.setValue(AsproConstants.DEFAULT_SAMPLING_PERIOD);
        }

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("samplingPeriod changed : " + newValue);
        }
        if (updateObservation()) {
          refreshPlot();
        }
      }
    });

    // default obs duration and property change listener :
    this.jFieldObsDuration.setValue(AsproConstants.DEFAULT_OBSERVATION_DURATION);
    this.jFieldObsDuration.addPropertyChangeListener("value", new PropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent evt) {
        final double newValue = ((Number) jFieldObsDuration.getValue()).doubleValue();

        if (newValue <= 0d) {
          // invalid value :
          jFieldObsDuration.setValue(AsproConstants.DEFAULT_OBSERVATION_DURATION);
        }

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("obsDuration changed : " + newValue);
        }
        if (updateObservation()) {
          refreshPlot();
        }
      }
    });

    this.haMinAdapter = new FieldSliderAdapter(jSliderHAMin, jFieldHAMin, AsproConstants.HA_MIN, AsproConstants.HA_MAX, AsproConstants.HA_MIN);
    this.haMinAdapter.addChangeListener(this);

    this.haMaxAdapter = new FieldSliderAdapter(jSliderHAMax, jFieldHAMax, AsproConstants.HA_MIN, AsproConstants.HA_MAX, AsproConstants.HA_MAX);
    this.haMaxAdapter.addChangeListener(this);

    this.jTargetHAMin.setText(null);
    this.jTargetHAMax.setText(null);

    this.jCheckBoxPlotUVSupport.addItemListener(new ItemListener() {

      public void itemStateChanged(final ItemEvent e) {
        refreshPlot();
      }
    });

    this.jCheckBoxModelImage.addItemListener(new ItemListener() {

      public void itemStateChanged(final ItemEvent e) {
        final boolean enabled = jCheckBoxModelImage.isSelected();
        jComboBoxImageMode.setEnabled(enabled);

        refreshPlot();
      }
    });

    this.jComboBoxImageMode.addActionListener(this);

    // register this instance as a Preference Observer :
    myPreferences.addObserver(this);
  }

  /**
   * Free any ressource or reference to this instance :
   * remove this instance form Preference Observers
   */
  public void dispose() {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("dispose : " + this);
    }

    // unregister this instance as a Preference Observer :
    myPreferences.deleteObserver(this);
  }

  /**
   * Overriden method to give object identifier
   * @return string identifier
   */
  @Override
  public String toString() {
    return "UVCoveragePanel@" + Integer.toHexString(hashCode());
  }

  /**
   * Listen to preferences changes
   * @param o Preferences
   * @param arg unused
   */
  public void update(final Observable o, final Object arg) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Preferences updated on : " + this);
    }

    this.refreshPlot();
  }

  /**
   * Update the information relative to the interferometer (configuration) : UV max length
   * @param observation current observation settings
   */
  private void updateInteferometerData(final ObservationSetting observation) {
    final String intConfName = observation.getInterferometerConfiguration().getName();
    // test if the interferometer changed :
    final boolean changed = intConfName != null && !intConfName.equals(this.interferometerConfigurationName);
    if (changed) {
      this.interferometerConfigurationName = intConfName;
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("interferometer configuration changed : " + intConfName);
      }

      final InterferometerConfiguration intConf = observation.getInterferometerConfiguration().getInterferometerConfiguration();

      // update the UV Max :
      final double maxBaseLine = intConf.getInterferometer().getMaxBaseLine();
      this.uvMaxAdapter.reset(0, maxBaseLine, maxBaseLine);
    }
  }

  /**
   * Refresh the information relative to the instrument : sampling time and modes
   * @param observation current observation settings
   */
  private void updateInstrumentData(final ObservationSetting observation) {
    final String insName = observation.getInstrumentConfiguration().getName();
    // test if the instrument changed :
    final boolean changed = insName != null && !insName.equals(this.instrumentName);
    if (changed) {
      this.instrumentName = insName;
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("instrument changed : " + insName);
      }

      // reset the sampling time to the default sampling time of the instrument :
      final int defaultSamplingTime = ConfigurationManager.getInstance().getInstrumentSamplingTime(
              observation.getInterferometerConfiguration().getName(),
              observation.getInstrumentConfiguration().getName());
      this.jFieldSamplingPeriod.setValue(Double.valueOf(defaultSamplingTime));

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("defaultSamplingTime : " + defaultSamplingTime);
      }

      // update instrument modes :
      final Vector<String> v = ConfigurationManager.getInstance().getInstrumentModes(
              observation.getInterferometerConfiguration().getName(),
              observation.getInstrumentConfiguration().getName());
      this.jComboBoxInstrumentMode.setModel(new DefaultComboBoxModel(v));

      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("jComboBoxInstrumentMode updated : " + this.jComboBoxInstrumentMode.getSelectedItem());
      }
    }
  }

  /**
   * Refresh the fringe tracker modes
   * @param observation current observation settings
   */
  private void updateComboFTModes(final ObservationSetting observation) {
    final Object oldValue = this.jComboBoxFTMode.getSelectedItem();

    final Vector<String> modes = ConfigurationManager.getInstance().getFringeTrackerModes(
            observation.getInterferometerConfiguration().getName());

    // modes can be empty :
    this.jComboBoxFTMode.setModel(new DefaultComboBoxModel(modes));

    // restore previous selected item :
    if (oldValue != null) {
      this.jComboBoxFTMode.setSelectedItem(oldValue);
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("jComboBoxFTMode updated : " + this.jComboBoxFTMode.getSelectedItem());
    }

    final boolean visible = !modes.isEmpty();
    this.jComboBoxFTMode.setVisible(visible);
    this.jLabelFTMode.setVisible(visible);
  }

  /**
   * Refresh the target list
   */
  private void updateComboTarget() {
    final Object oldValue = getSelectedTargetName();

    final Vector<String> v = this.om.getTargetNames();
    this.jComboBoxTarget.setModel(new DefaultComboBoxModel(v));

    // restore previous selected item :
    if (oldValue != null) {
      this.jComboBoxTarget.setSelectedItem(oldValue);
    }
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("jComboBoxTarget updated : " + getSelectedTargetName());
    }
  }

  /**
   * Update UI according to the target configuration
   */
  private void updateTargetConfiguration() {
    final String targetName = getSelectedTargetName();

    final TargetConfiguration targetConf = this.om.getTargetConfiguration(targetName);
    if (targetConf != null) {

      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("updateTargetConfiguration : " + targetName);
      }

      // disable the automatic update observation :
      final boolean prevAutoUpdateObservation = this.setAutoUpdateObservation(false);
      try {
        // update HA Min / Max :
        final Double haMin = targetConf.getHAMin();
        this.haMinAdapter.setValue((haMin != null) ? haMin.doubleValue() : AsproConstants.HA_MIN);

        final Double haMax = targetConf.getHAMax();
        this.haMaxAdapter.setValue((haMax != null) ? haMax.doubleValue() : AsproConstants.HA_MAX);

        // update ft mode :
        if (this.jComboBoxFTMode.getModel().getSize() > 0) {
          final String ftMode = targetConf.getFringeTrackerMode();
          this.jComboBoxFTMode.setSelectedItem((ftMode != null) ? ftMode : AsproConstants.NONE);
        }
      } finally {
        // restore the automatic update observation :
        this.setAutoUpdateObservation(prevAutoUpdateObservation);
      }
    }
  }

  /**
   * Update the HA min / max according to the selected target and computed observability data (star data)
   */
  private void updateTargetHA() {
    if (this.currentObsData != null) {
      final String targetName = getSelectedTargetName();

      final StarData starData = this.currentObsData.getStarData(targetName);
      if (starData != null) {
        final Double min = Range.getMinimum(starData.getObsRangesHA());
        final Double max = Range.getMaximum(starData.getObsRangesHA());

        this.jTargetHAMin.setText(format(this.jFieldHAMin, min));
        this.jTargetHAMax.setText(format(this.jFieldHAMax, max));

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("target HA min : " + min);
          logger.fine("target HA max : " + min);
        }
      } else {
        // baseline limits case :
        this.jTargetHAMin.setText(null);
        this.jTargetHAMax.setText(null);
      }
    }
  }

  /**
   * If the current target has no model defined, then disable model options widgets
   */
  private void changeStateForModelImageWidgets() {
    final String targetName = getSelectedTargetName();

    final Target target = this.om.getTarget(targetName);
    if (target != null) {
      final List<Model> models = target.getModels();

      final boolean hasModel = (models != null && !models.isEmpty());

      this.jCheckBoxModelImage.setEnabled(hasModel);
      this.jComboBoxImageMode.setEnabled(hasModel);
    }
  }

  /**
   * Process any comboBox change event (target, instrument mode, image mode ...).
   * Refresh the dependent combo boxes and update the observation according to the form state
   * @param e action event
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == this.jComboBoxTarget) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("target changed : " + getSelectedTargetName());
      }
      updateTargetConfiguration();
      updateTargetHA();
      changeStateForModelImageWidgets();
      refreshPlot();
    } else if (e.getSource() == this.jComboBoxInstrumentMode) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("instrument mode changed : " + this.jComboBoxInstrumentMode.getSelectedItem());
      }
      if (updateObservation()) {
        refreshPlot();
      }
    } else if (e.getSource() == this.jComboBoxFTMode) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("ft mode changed : " + this.jComboBoxFTMode.getSelectedItem());
      }
      if (updateObservation()) {
        refreshPlot();
      }
    } else if (e.getSource() == this.jComboBoxAtmQual) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("atmQuality changed : " + this.jComboBoxAtmQual.getSelectedItem());
      }
      if (updateObservation()) {
        refreshPlot();
      }
    } else if (e.getSource() == this.jComboBoxImageMode) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("image mode changed : " + this.jComboBoxImageMode.getSelectedItem());
      }
      refreshPlot();
    }
  }

  /**
   * Handle the stateChanged event from the FieldSliderAdapter instances
   * @param ce change event
   */
  public void stateChanged(final ChangeEvent ce) {
    final FieldSliderAdapter source = (FieldSliderAdapter) ce.getSource();

    if (source == this.haMinAdapter) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("haMin changed : " + source.getValue());
      }
      this.haMaxAdapter.setMinValue(source.getValue());
      if (updateObservation()) {
        refreshPlot();
      }
    } else if (source == this.haMaxAdapter) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("haMax changed : " + source.getValue());
      }
      this.haMinAdapter.setMaxValue(source.getValue());
      if (updateObservation()) {
        refreshPlot();
      }
    } else if (source == this.uvMaxAdapter) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("U-V Max changed : " + source.getValue());
      }
      refreshPlot();
    }
  }

  /**
   * Update the observation with the form fields if the automatic update flag is enabled.
   * @return true if the observation changed
   */
  private boolean updateObservation() {
    boolean changed = false;
    // check if the automatic update flag is enabled :
    if (this.doAutoUpdateObservation) {
      if (DEBUG_UPDATE_EVENT) {
        logger.log(Level.SEVERE, "UPDATE", new Throwable());
      }

      final String targetName = getSelectedTargetName();

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("updateObservation : " + targetName);
      }

      if (targetName != null) {

        // Change the instrument mode :
        changed |= this.om.setInstrumentMode((String) this.jComboBoxInstrumentMode.getSelectedItem());

        // Update the sampling period :
        final Number samplingPeriod = (Number) this.jFieldSamplingPeriod.getValue();
        changed |= this.om.setInstrumentSamplingPeriod(Double.valueOf(samplingPeriod.doubleValue()));

        // Update the acquisition time :
        final Number obsDuration = (Number) this.jFieldObsDuration.getValue();
        changed |= this.om.setInstrumentAcquisitionTime(Double.valueOf(obsDuration.doubleValue()));

        // Update target HA Min/Max :
        changed |= this.om.setTargetHAMin(targetName, Double.valueOf(this.haMinAdapter.getValue()));
        changed |= this.om.setTargetHAMax(targetName, Double.valueOf(this.haMaxAdapter.getValue()));

        // update ft mode :
        changed |= this.om.setTargetFTMode(targetName, (String) this.jComboBoxFTMode.getSelectedItem());

        // update atmQuality :
        changed |= this.om.setAtmosphereQuality(AtmosphereQualityUtils.getAtmosphereQuality((String) this.jComboBoxAtmQual.getSelectedItem()));

      } else {
        // clean up i.e. the panel is then invalid :

        // reset instrument configuration :
        this.om.setInstrumentMode(null);
        this.om.setInstrumentSamplingPeriod(null);
        this.om.setInstrumentAcquisitionTime(null);

        // reset atmosphere quality :
        this.om.setAtmosphereQuality(AtmosphereQuality.AVERAGE);
      }

      // TODO : fire event ??
      // NOTE : the onChange event is already handled : risk of cyclic loop !

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("updateObservation : " + changed);
      }
    }
    return changed;
  }

  /**
   * Update the UI widgets from the given changed observation
   * @param observation observation
   */
  private void onChangeObservation(final ObservationSetting observation) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("observation :\n" + ObservationManager.toString(observation));
    }

    // When the observation changes, it means that the observability will be computed in background,
    // and soon an ObservabilityDone event will be sent.

    // Only refresh the UI widgets and NOT the plot :

    // disable the automatic update observation :
    final boolean prevAutoUpdateObservation = this.setAutoUpdateObservation(false);
    // disable the automatic refresh :
    final boolean prevAutoRefresh = this.setAutoRefresh(false);
    try {

      // update data related to the interferometer :
      updateInteferometerData(observation);

      // refresh data related to the instrument :
      updateInstrumentData(observation);

      // refresh the fringe tracker modes :
      updateComboFTModes(observation);

      // finally refresh the targets, that fires the target changed event
      // refresh the fields HA Min/Max, FT Mode and computed HA Min / Max :
      updateComboTarget();

    } finally {
      // restore the automatic refresh :
      this.setAutoRefresh(prevAutoRefresh);
      // restore the automatic update observation :
      this.setAutoUpdateObservation(prevAutoUpdateObservation);
    }

    // finally, update the observation :
    updateObservation();
  }

  /**
   * Update the UI widgets from the given loaded observation
   *
   * @param observation observation
   */
  private void onLoadObservation(final ObservationSetting observation) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("onLoadObservation :\n" + ObservationManager.toString(observation));
    }
    // disable the automatic update observation :
    final boolean prevAutoUpdateObservation = this.setAutoUpdateObservation(false);
    // disable the automatic refresh :
    final boolean prevAutoRefresh = this.setAutoRefresh(false);
    try {

      // update the data related to the interferometer :
      this.interferometerConfigurationName = null;
      updateInteferometerData(observation);

      // refresh data related to the instrument :
      this.instrumentName = null;
      updateInstrumentData(observation);

      // update the selected instrument mode :
      this.jComboBoxInstrumentMode.setSelectedItem(observation.getInstrumentConfiguration().getInstrumentMode());

      // update the sampling period :
      if (observation.getInstrumentConfiguration().getSamplingPeriod() != null) {
        this.jFieldSamplingPeriod.setValue(Double.valueOf(observation.getInstrumentConfiguration().getSamplingPeriod()));
      }

      // update the acquisition time :
      if (observation.getInstrumentConfiguration().getAcquisitionTime() != null) {
        this.jFieldObsDuration.setValue(Double.valueOf(observation.getInstrumentConfiguration().getAcquisitionTime()));
      }

      // refresh the fringe tracker modes :
      updateComboFTModes(observation);

      // update atmQuality :
      if (observation.getWhen().getAtmosphereQuality() != null) {
        this.jComboBoxAtmQual.setSelectedItem(observation.getWhen().getAtmosphereQuality().value());
      }

      // reset HA limits :
      this.haMinAdapter.setValue(AsproConstants.HA_MIN);
      this.haMaxAdapter.setValue(AsproConstants.HA_MAX);

      // finally refresh the targets, that fires the target changed event
      // refresh the fields HA Min/Max, FT Mode and computed HA Min / Max :
      updateComboTarget();

      // reset to defaults :
      this.jCheckBoxPlotUVSupport.setSelected(true);
      this.jCheckBoxModelImage.setSelected(true);
      this.jComboBoxImageMode.setSelectedItem(ImageMode.AMP);

      // reset cached data :
      this.currentObsData = null;
      this.lastZoomEvent = null;
      this.currentUVMapData = null;

    } finally {
      // restore the automatic refresh :
      this.setAutoRefresh(prevAutoRefresh);
      // restore the automatic update observation :
      this.setAutoUpdateObservation(prevAutoUpdateObservation);
    }
  }

  /**
   * Handle the given event on the given observation =
   * 1/ If the observation changed, refresh the UI widgets (targets ...)
   * 2/ If the observability is computed, then refresh the plot
   * @param type event type
   * @param observation observation
   */
  public void onProcess(final ObservationEventType type, final ObservationSetting observation) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + type + "] process IN");
    }
    switch (type) {
      case CHANGED:
        this.onChangeObservation(observation);
        break;
      case LOADED:
        this.onLoadObservation(observation);
        break;
      case OBSERVABILITY_DONE:
        this.updateObservabilityData(observation.getObservabilityData());
        this.plot(observation);
        break;
      default:
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + type + "] process OUT");
    }
  }

  /**
   * Update the computed Observability Data used to have star data (HA min / max)
   * @param obsData
   */
  private void updateObservabilityData(final ObservabilityData obsData) {
    this.currentObsData = obsData;
    updateTargetHA();
  }

  /**
   * Refresh the plot when an UI widget changes.
   * Check the doAutoRefresh flag to avoid unwanted refresh (resetOptions)
   */
  protected void refreshPlot() {
    if (this.doAutoRefresh) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("refreshPlot");
      }
      this.plot(this.om.getObservation());
    }
  }

  /**
   * Plot the UV Coverage using a SwingWorker to do the computation in the background.
   * This code is executed by the Swing Event Dispatcher thread (EDT)
   * @param observation observation data to use
   */
  protected void plot(final ObservationSetting observation) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("plot : " + ObservationManager.toString(observation));
    }
    if (DEBUG_PLOT_EVENT) {
      logger.log(Level.SEVERE, "PLOT", new Throwable());
    }

    /* get plot options from swing components */

    final String targetName = getSelectedTargetName();

    final double uvMax = this.uvMaxAdapter.getValue();

    final boolean doUVSupport = this.jCheckBoxPlotUVSupport.isSelected();
    final boolean doModelImage = this.jCheckBoxModelImage.isSelected();

    // model image options :
    final ImageMode imageMode = (ImageMode) this.jComboBoxImageMode.getSelectedItem();

    // Use model image Preferences :
    final int imageSize = myPreferences.getPreferenceAsInt(Preferences.MODEL_IMAGE_SIZE);
    final IndexColorModel colorModel = ColorModels.getColorModel(myPreferences.getPreference(Preferences.MODEL_IMAGE_LUT));

    // check if observability data are available :
    final ObservabilityData obsData = observation.getObservabilityData();

    if (obsData != null) {
      /*
       * Use the SwingWorker backport for Java 5 = swing-worker-1.2.jar (org.jdesktop.swingworker.SwingWorker)
       */
      final SwingWorker<UVCoverageData, Void> worker = new SwingWorker<UVCoverageData, Void>() {

        /**
         * Compute the UV Coverage data in background
         * @return UV Coverage data
         */
        @Override
        public UVCoverageData doInBackground() {
          logger.fine("SwingWorker[UV].doInBackground : IN");

          // first reset the warning container in the current observation :
          ObservationManager.getInstance().setWarningContainer(null);
          // then reset the OIFits structure in the current observation :
          ObservationManager.getInstance().setOIFitsFile(null);

          UVCoverageData uvData = new UVCoverageService(observation, targetName, uvMax,
                  doUVSupport, doModelImage, imageMode, imageSize, colorModel).compute();

          if (isCancelled()) {
            logger.fine("SwingWorker[UV].doInBackground : CANCELLED");
            // no result if task is cancelled :
            uvData = null;
          } else {
            logger.fine("SwingWorker[UV].doInBackground : OUT");
          }
          return uvData;
        }

        /**
         * Reset the plot in case of baseline limits or model exception
         */
        public void reset() {
          lastZoomEvent = null;
          currentUVMapData = null;

          // reset bounds to [-1;1] (before setDataset) :
          localXYPlot.defineBounds(1d);
          // reset dataset for baseline limits :
          localXYPlot.setDataset(null);

          // update the background image :
          updateUVMap(null);
        }

        /**
         * Refresh the plot using the computed UV Coverage data.
         * This code is executed by the Swing Event Dispatcher thread (EDT)
         */
        @Override
        public void done() {
          // check if the worker was cancelled :
          if (!isCancelled()) {
            logger.fine("SwingWorker[UV].done : IN");
            try {
              // Get the computation results with all data necessary to draw the plot :
              final UVCoverageData uvData = get();

              if (uvData != null) {
                logger.fine("SwingWorker[UV].done : refresh Chart");

                // update the warning container in the current observation :
                ObservationManager.getInstance().setWarningContainer(uvData.getWarningContainer());

                // update the OIFits structure in the current observation :
                ObservationManager.getInstance().setOIFitsFile(uvData.getOiFitsFile());

                ChartUtils.clearTextSubTitle(localJFreeChart);

                if (uvData.getName() == null) {
                  // Baseline limits case :
                  reset();
                } else {

                  lastZoomEvent = null;
                  currentUVMapData = null;

                  // title :
                  final StringBuilder sb = new StringBuilder(observation.getInterferometerConfiguration().getName());
                  sb.append(" - ").append(observation.getInstrumentConfiguration().getStations());

                  if (obsData.getBestPops() != null) {
                    sb.append(" + ");
                    for (Pop pop : obsData.getBestPops().getPopList()) {
                      sb.append(pop.getName()).append(" ");
                    }
                  }
                  ChartUtils.addSubtitle(localJFreeChart, sb.toString());
                  ChartUtils.addSubtitle(localJFreeChart, "Source : " + uvData.getName());

                  if (observation.getWhen().isNightRestriction()) {
                    // date :
                    ChartUtils.addSubtitle(localJFreeChart, "Day : " + observation.getWhen().getDate().toString());
                  }

                  // change the scaling factor : (???)
                  // lambda ??
                  setUvPlotScalingFactor(MEGA_LAMBDA_SCALE);

                  // computed data are valid :
                  updateChart(uvData);

                  // update the uv map data :
                  currentUVMapData = uvData.getUvMapData();

                  // update the background image :
                  if (currentUVMapData == null) {
                    updateUVMap(null);
                  } else {
                    updateUVMap(currentUVMapData.getUvMap());
                  }
                }

                // update theme at end :
                ChartUtilities.applyCurrentTheme(localJFreeChart);
              }

            } catch (InterruptedException ignore) {
            } catch (ExecutionException ee) {
              reset();
              if (ee.getCause() instanceof IllegalArgumentException) {
                MessagePane.showErrorMessage(ee.getCause().getMessage());
              } else {
                // Show feedback report (modal and do not exit on close) :
                new FeedbackReport(true, ee.getCause());
              }
            }

            // update the status bar :
            StatusBar.show("uv coverage done.");

            logger.fine("SwingWorker[UV].done : OUT");
          }
        }
      };

      // update the status bar :
      StatusBar.show("computing uv coverage ... (please wait, this may take a while)");

      // Cancel other uv coverage task and execute this new task :
      SwingWorkerExecutor.getInstance().execute("UVCoverage", worker);

    } // observability data check
  }

  /**
   * Process the zoom event to refresh the model UV map according to the new coordinates
   * @param ze zoom event
   */
  public void chartChanged(final ZoomEvent ze) {
    // check if the zoom changed :
    if (this.currentUVMapData != null && !ze.equals(this.lastZoomEvent)) {
      this.lastZoomEvent = ze;

      if (this.aJMMC != null) {
        this.localXYPlot.getRenderer(0).removeAnnotations();
        this.aJMMC.setX(ze.getDomainUpperBound());
        this.aJMMC.setY(ze.getRangeLowerBound());

        this.localXYPlot.getRenderer(0).addAnnotation(this.aJMMC, Layer.BACKGROUND);
      }

      // Update model uv map :
      final String targetName = getSelectedTargetName();

      final Target target = this.om.getTarget(targetName);

      if (target != null) {
        final List<Model> models = target.getModels();

        if (models != null && models.size() > 0) {

          final Rectangle2D.Float uvRect = new Rectangle2D.Float();
          uvRect.setFrameFromDiagonal(
                  fromUVPlotScale(ze.getDomainLowerBound()), fromUVPlotScale(ze.getRangeLowerBound()),
                  fromUVPlotScale(ze.getDomainUpperBound()), fromUVPlotScale(ze.getRangeUpperBound()));

          // compute an approximated uv map from the reference UV Map :
          computeSubUVMap(uvRect);

          // visibility reference extrema :
          final Float refMin = Float.valueOf(this.currentUVMapData.getMin());
          final Float refMax = Float.valueOf(this.currentUVMapData.getMax());

          // model image options :
          final ImageMode imageMode = (ImageMode) this.jComboBoxImageMode.getSelectedItem();

          // Use model image Preferences :
          final int imageSize = myPreferences.getPreferenceAsInt(Preferences.MODEL_IMAGE_SIZE);
          final IndexColorModel colorModel = ColorModels.getColorModel(myPreferences.getPreference(Preferences.MODEL_IMAGE_LUT));

          if (logger.isLoggable(Level.FINE)) {
            logger.fine("computing model uv map ...");
          }

          /*
           * Use the SwingWorker backport for Java 5 = swing-worker-1.2.jar (org.jdesktop.swingworker.SwingWorker)
           */
          final SwingWorker<UVMapData, Void> worker = new SwingWorker<UVMapData, Void>() {

            /**
             * Compute the UV Map in background
             * @return Image
             */
            @Override
            public UVMapData doInBackground() {
              logger.fine("SwingWorker[UVMap].doInBackground : IN");

              UVMapData uvMapData = ModelUVMapService.computeUVMap(
                      models, uvRect, refMin, refMax, imageMode, imageSize, colorModel);

              if (isCancelled()) {
                logger.fine("SwingWorker[UVMap].doInBackground : CANCELLED");
                // no result if task is cancelled :
                uvMapData = null;
              } else {
                logger.fine("SwingWorker[UVMap].doInBackground : OUT");
              }
              return uvMapData;
            }

            /**
             * Refresh the plot using the computed UV Map.
             * This code is executed by the Swing Event Dispatcher thread (EDT)
             */
            @Override
            public void done() {
              // check if the worker was cancelled :
              if (!isCancelled()) {
                logger.fine("SwingWorker[UVMap].done : IN");
                try {
                  // Get the computation results with all data necessary to draw the plot :
                  final UVMapData uvMapData = get();

                  if (uvMapData != null) {
                    logger.fine("SwingWorker[UVMap].done : refresh Chart");

                    // update the background image :
                    updateUVMap(uvMapData.getUvMap());
                  }

                } catch (InterruptedException ignore) {
                } catch (ExecutionException ee) {
                  if (ee.getCause() instanceof IllegalArgumentException) {
                    MessagePane.showErrorMessage(ee.getCause().getMessage());
                  } else {
                    // Show feedback report (modal and do not exit on close) :
                    new FeedbackReport(true, ee.getCause());
                  }
                }

                // update the status bar :
                StatusBar.show("uv map done.");

                logger.fine("SwingWorker[UVMap].done : OUT");
              }
            }
          };

          // update the status bar :
          StatusBar.show("computing uv map ... (please wait, this may take a while)");

          // Cancel other uv map task and execute this task :
          SwingWorkerExecutor.getInstance().execute("UVMap", worker);
        }
      }
    }
  }

  /**
   * Compute a sub image for the UV Map given the new uv area
   * @param uvRect uv area
   */
  private void computeSubUVMap(final Rectangle2D.Float uvRect) {
    if (this.currentUVMapData != null) {
      final int imageSize = this.currentUVMapData.getImageSize();
      // uv area reference :
      final Rectangle2D.Float uvRectRef = this.currentUVMapData.getUvRect();

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("uv map rect     = " + uvRect);
        logger.fine("uv map rect REF = " + uvRectRef);
      }

      // note : floor/ceil to be sure to have at least 1x1 pixel image
      final int x = (int) Math.floor(imageSize * (uvRect.getX() - uvRectRef.getX()) / uvRectRef.getWidth());
      int y = (int) Math.floor(imageSize * (uvRect.getY() - uvRectRef.getY()) / uvRectRef.getHeight());
      final int w = (int) Math.ceil(imageSize * uvRect.getWidth() / uvRectRef.getWidth());
      final int h = (int) Math.ceil(imageSize * uvRect.getHeight() / uvRectRef.getHeight());

      // Note : the image is produced from an array where 0,0 corresponds to the upper left corner
      // whereas it corresponds in UV to the lower U and Upper V coordinates => inverse the V axis

      // Inverse V axis issue :
      y = imageSize - y - h;

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("sub uvMap [" + x + ", " + y + " - " + w + ", " + h + "]");
      }

      // crop a small sub image waiting for the correct model to be computed :
      final Image subUVMap = this.currentUVMapData.getUvMap().getSubimage(x, y, w, h);

      // update the background image :
      updateUVMap(subUVMap);
    }
  }

  /**
   * Update the background image of the chart with the UV Map
   * @param uvMap image or null
   */
  private void updateUVMap(final Image uvMap) {
    if (uvMap != null) {
      this.localXYPlot.setBackgroundPaint(null);
      this.localXYPlot.setBackgroundImage(uvMap);
    } else {
      this.localXYPlot.setBackgroundPaint(Color.lightGray);
      this.localXYPlot.setBackgroundImage(null);
    }
  }

  /**
   * Update the datasets
   * @param uvData uv coverage data
   */
  private void updateChart(final UVCoverageData uvData) {
    // renderer :
    final AbstractRenderer renderer = (AbstractRenderer) this.localXYPlot.getRenderer();

    // reset colors :
    renderer.clearSeriesPaints(false);
    // side effect with chart theme :
    renderer.setAutoPopulateSeriesPaint(false);

    final XYSeriesCollection dataset = new XYSeriesCollection();

    this.updateUVTracks(dataset, uvData);
    this.updateUVTracksRiseSet(dataset, uvData);

    // define bounds to the uv maximum value (before setDataset) :
    final double boxSize = toUVPlotScale(uvData.getUvMax());
    this.localXYPlot.defineBounds(boxSize);

    // set the main data set :
    this.localXYPlot.setDataset(dataset);

    // annotation JMMC (moving position) :
    this.localXYPlot.getRenderer(0).removeAnnotations();
    if (this.aJMMC == null) {
      this.aJMMC = new XYTextAnnotation(AsproConstants.JMMC_ANNOTATION, boxSize, -boxSize);
      this.aJMMC.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
      this.aJMMC.setPaint(Color.BLACK);
    } else {
      this.aJMMC.setX(boxSize);
      this.aJMMC.setY(-boxSize);
    }
    this.localXYPlot.getRenderer(0).addAnnotation(this.aJMMC, Layer.BACKGROUND);
  }

  /**
   * Update the dataset with UV rise/set tracks
   * @param dataset dataset to use
   * @param uvData uv coverage data
   */
  private void updateUVTracksRiseSet(final XYSeriesCollection dataset, final UVCoverageData uvData) {
    final ColorPalette palette = ColorPalette.getDefaultColorPalette();

    // renderer :
    final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.localXYPlot.getRenderer();

    // process uv rise/set :
    final List<UVBaseLineData> targetUVRiseSet = uvData.getTargetUVRiseSet();

    if (targetUVRiseSet != null) {
      // target is visible :

      XYSeries xySeriesBL;

      double[] u;
      double[] v;
      double x, y;
      int n = 0;
      // serie offset :
      final int offset = dataset.getSeriesCount();


      for (UVBaseLineData uvBL : targetUVRiseSet) {
        xySeriesBL = new XYSeries("Rise/Set " + uvBL.getName(), false);
        xySeriesBL.setNotify(false);

        u = uvBL.getU();
        v = uvBL.getV();

        // first ellipse line :
        for (int i = 0, size = uvBL.getNPoints(); i < size; i++) {
          x = toUVPlotScale(u[i]);
          y = toUVPlotScale(v[i]);

          xySeriesBL.add(x, y);
        } // points

        // add an invalid point to break the line between the 2 segments :
        xySeriesBL.add(Double.NaN, Double.NaN);

        // second symetric ellipse line :
        for (int i = 0, size = uvBL.getNPoints(); i < size; i++) {
          x = toUVPlotScale(-u[i]);
          y = toUVPlotScale(-v[i]);

          xySeriesBL.add(x, y);
        } // points

        xySeriesBL.setNotify(true);
        dataset.addSeries(xySeriesBL);

        // color :
        renderer.setSeriesPaint(n + offset, palette.getColor(n), false);

        n++;
      } // BL
    }
  }

  /**
   * Update the dataset with UV observable tracks
   * @param dataset dataset to use
   * @param uvData uv coverage data
   */
  private void updateUVTracks(final XYSeriesCollection dataset, final UVCoverageData uvData) {
    final ColorPalette palette = ColorPalette.getDefaultColorPalette();

    // renderer :
    final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.localXYPlot.getRenderer();

    // process observable uv ranges :
    final List<UVRangeBaseLineData> targetUVObservability = uvData.getTargetUVObservability();

    if (targetUVObservability != null) {
      // target is observable :

      XYSeries xySeriesBL;

      double[] uWMin;
      double[] vWMin;
      double[] uWMax;
      double[] vWMax;
      double x1, y1, x2, y2;
      int n = 0;

      for (UVRangeBaseLineData uvBL : targetUVObservability) {
        xySeriesBL = new XYSeries("Observable " + uvBL.getName(), false);
        xySeriesBL.setNotify(false);

        uWMin = uvBL.getUWMin();
        vWMin = uvBL.getVWMin();
        uWMax = uvBL.getUWMax();
        vWMax = uvBL.getVWMax();

        for (int i = 0, size = uvBL.getNPoints(); i < size; i++) {
          x1 = toUVPlotScale(uWMax[i]);
          y1 = toUVPlotScale(vWMax[i]);

          x2 = toUVPlotScale(uWMin[i]);
          y2 = toUVPlotScale(vWMin[i]);

          // first segment :
          xySeriesBL.add(x1, y1);
          xySeriesBL.add(x2, y2);

          // add an invalid point to break the line between the 2 segments :
          xySeriesBL.add(Double.NaN, Double.NaN);

          // second symetric segment :
          xySeriesBL.add(-x1, -y1);
          xySeriesBL.add(-x2, -y2);

          // add an invalid point to break the line between the 2 segments :
          xySeriesBL.add(Double.NaN, Double.NaN);

        } // points

        xySeriesBL.setNotify(true);
        dataset.addSeries(xySeriesBL);

        // color :
        renderer.setSeriesPaint(n, palette.getColor(n), false);

        n++;
      } // BL
    }
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButtonModelEditor;
  private javax.swing.JButton jButtonOB;
  private javax.swing.JButton jButtonPDF;
  private javax.swing.JCheckBox jCheckBoxModelImage;
  private javax.swing.JCheckBox jCheckBoxPlotUVSupport;
  private javax.swing.JComboBox jComboBoxAtmQual;
  private javax.swing.JComboBox jComboBoxFTMode;
  private javax.swing.JComboBox jComboBoxImageMode;
  private javax.swing.JComboBox jComboBoxInstrumentMode;
  private javax.swing.JComboBox jComboBoxTarget;
  private javax.swing.JFormattedTextField jFieldHAMax;
  private javax.swing.JFormattedTextField jFieldHAMin;
  private javax.swing.JFormattedTextField jFieldObsDuration;
  private javax.swing.JFormattedTextField jFieldSamplingPeriod;
  private javax.swing.JFormattedTextField jFieldUVMax;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabelAtmQual;
  private javax.swing.JLabel jLabelFTMode;
  private javax.swing.JPanel jPanelBottom;
  private javax.swing.JPanel jPanelButtons;
  private javax.swing.JPanel jPanelLeft;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JSlider jSliderHAMax;
  private javax.swing.JSlider jSliderHAMin;
  private javax.swing.JSlider jSliderUVMax;
  private javax.swing.JSplitPane jSplitPane;
  private javax.swing.JLabel jTargetHAMax;
  private javax.swing.JLabel jTargetHAMin;
  // End of variables declaration//GEN-END:variables
  /** drawing started time value */
  private long lastTime = 0l;

  /**
   * Handle the chart progress event to log the chart rendering delay
   * @param event chart progress event
   */
  public void chartProgress(final ChartProgressEvent event) {
    if (logger.isLoggable(Level.FINE)) {
      switch (event.getType()) {
        case ChartProgressEvent.DRAWING_STARTED:
          this.lastTime = System.nanoTime();
          break;
        case ChartProgressEvent.DRAWING_FINISHED:
          logger.fine("Drawing chart time : " + 1e-6d * (System.nanoTime() - this.lastTime) + " ms.");
          this.lastTime = 0l;
          break;
        default:
      }
    }
  }

  /**
   * Format the given double value using the text field formatter
   * @param field formatted text field
   * @param value value to use
   * @return formatted string
   */
  private static String format(final JFormattedTextField field, final Double value) {
    String res = "";
    try {
      res = field.getFormatter().valueToString(value);
    } catch (ParseException pe) {
      logger.log(Level.SEVERE, "parsing exception", pe);
    }
    return res;
  }

  /**
   * Enable / Disable the automatic update of the observation when any swing component changes.
   * Return its previous value.
   *
   * Typical use is as following :
   * // first disable the automatic update observation :
   * final boolean prevAutoUpdateObservation = this.setAutoUpdateObservation(false);
   * try {
   *   // operations ...
   *
   * } finally {
   *   // restore the automatic update observation :
   *   this.setAutoUpdateObservation(prevAutoUpdateObservation);
   * }
   *
   * @param value new value
   * @return previous value
   */
  private boolean setAutoUpdateObservation(final boolean value) {
    // first backup the state of the automatic update observation :
    final boolean previous = this.doAutoUpdateObservation;

    // then change its state :
    this.doAutoUpdateObservation = value;

    // return previous state :
    return previous;
  }

  /**
   * Enable / Disable the automatic refresh of the plot when any swing component changes.
   * Return its previous value.
   *
   * Typical use is as following :
   * // disable the automatic refresh :
   * final boolean prevAutoRefresh = this.setAutoRefresh(false);
   * try {
   *   // operations ...
   *
   * } finally {
   *   // restore the automatic refresh :
   *   this.setAutoRefresh(prevAutoRefresh);
   * }
   *
   * @param value new value
   * @return previous value
   */
  private boolean setAutoRefresh(final boolean value) {
    // first backup the state of the automatic update observation :
    final boolean previous = this.doAutoRefresh;

    // then change its state :
    this.doAutoRefresh = value;

    // return previous state :
    return previous;
  }

  /**
   * Define the uv scaling factor
   * @param uvPlotScalingFactor new value
   */
  private void setUvPlotScalingFactor(double uvPlotScalingFactor) {
    this.uvPlotScalingFactor = uvPlotScalingFactor;
  }

  /**
   * Convert the given value (u or v) to the plot scale
   * @param value u or v coordinate in rad-1
   * @return u or v coordinate in the plot unit
   */
  private final double toUVPlotScale(final double value) {
    return uvPlotScalingFactor * value;
  }

  /**
   * Convert the given plot value (u or v) to the standard unit (rad-1)
   * @param value u or v coordinate in the plot unit
   * @return u or v coordinate in rad-1
   */
  private final double fromUVPlotScale(final double value) {
    return value / uvPlotScalingFactor;
  }

  /**
   * Return the currently selected target name
   * @return target name
   */
  public String getSelectedTargetName() {
    return (String) this.jComboBoxTarget.getSelectedItem();
  }
}

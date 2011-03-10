/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: InterferometerMapPanel.java,v 1.21 2011-03-01 17:09:09 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.20  2011/02/28 17:14:01  bourgesl
 * use new result containers
 *
 * Revision 1.19  2011/02/25 16:49:54  bourgesl
 * simplify title / file name via observation collection API
 *
 * Revision 1.18  2011/02/24 17:14:11  bourgesl
 * Major refactoring to support / handle observation collection (multi-conf)
 *
 * Revision 1.17  2011/02/22 18:11:29  bourgesl
 * Major UI changes : configuration multi-selection, unique target selection in main form
 *
 * Revision 1.16  2011/02/04 17:16:21  bourgesl
 * new ChartData inner class to have chart state used by PDF export
 *
 * Revision 1.15  2011/02/02 17:39:01  bourgesl
 * added to do
 *
 * Revision 1.14  2011/01/28 16:32:36  mella
 * Add new observationEvents (CHANGED replaced by DO_UPDATE, REFRESH and REFRESH_UV)
 * Modify the observationListener interface
 *
 * Revision 1.13  2011/01/27 17:04:22  bourgesl
 * renamed chart vars
 *
 * Revision 1.12  2011/01/21 16:23:44  bourgesl
 * import ObservationEventType
 *
 * Revision 1.11  2010/12/15 13:36:43  bourgesl
 * new getPDFDefaultFileName implementation
 *
 * Revision 1.10  2010/10/21 16:51:01  bourgesl
 * JMMC trademark made less important
 *
 * Revision 1.9  2010/10/15 16:59:43  bourgesl
 * new PDF options (page size and orientation)
 * PDFExportable refactoring to include prepareChart, postPDF and getPDFOptions methods
 *
 * Revision 1.8  2010/09/15 13:52:55  bourgesl
 * added JMMC copyright on plot
 *
 * Revision 1.7  2010/06/23 12:52:08  bourgesl
 * ObservationManager regsitration for observation events moved in SettingPanel (external)
 *
 * Revision 1.6  2010/06/17 10:02:51  bourgesl
 * fixed warning hints - mainly not final static loggers
 *
 * Revision 1.5  2010/06/10 08:53:46  bourgesl
 * added a test to determine if the plot must be refresh (configuration changed)
 * removed dead code (save plot to png)
 *
 * Revision 1.4  2010/06/09 12:51:09  bourgesl
 * new interface PDFExportable to define a standard method performPDFAction() that use ExportPDFAction to export the chart to PDF
 *
 * Revision 1.3  2010/06/08 12:32:11  bourgesl
 * javadoc
 *
 * Revision 1.2  2010/06/08 10:20:41  bourgesl
 * minor UI changes (layout / margins)
 *
 * Revision 1.1  2010/05/11 12:08:27  bourgesl
 * simple Interferometer Map (stations + baselines) automatically refreshed when the chosen baseline configuration changes
 *
 */
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.gui.action.ExportPDFAction;
import fr.jmmc.aspro.gui.chart.ChartUtils;
import fr.jmmc.aspro.gui.chart.NameLabelGenerator;
import fr.jmmc.aspro.gui.chart.PDFOptions;
import fr.jmmc.aspro.gui.chart.SquareChartPanel;
import fr.jmmc.aspro.gui.chart.SquareXYPlot;
import fr.jmmc.aspro.gui.chart.XYZNameDataSet;
import fr.jmmc.aspro.gui.chart.ZoomEvent;
import fr.jmmc.aspro.gui.chart.ZoomEventListener;
import fr.jmmc.aspro.gui.util.ColorPalette;
import fr.jmmc.aspro.model.InterferometerMapData;
import fr.jmmc.aspro.model.ObservationCollectionMapData;
import fr.jmmc.aspro.model.event.ObservationListener;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.model.event.ObservationEvent;
import fr.jmmc.aspro.model.oi.ObservationCollection;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.service.InterferometerMapService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 * This panel presents the interferometer plot (station, base lines ...)
 * @author bourgesl
 */
public final class InterferometerMapPanel extends javax.swing.JPanel implements ChartProgressListener, ZoomEventListener,
                                                                                ObservationListener, PDFExportable {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.gui.InterferometerMapPanel";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);

  /* members */
  /** jFreeChart instance */
  private JFreeChart chart;
  /** xy plot instance */
  private SquareXYPlot xyPlot;
  /** JMMC annotation */
  private XYTextAnnotation aJMMC = null;
  /** current configuration to track changes */
  private String configuration = null;

  /* plot data */
  /** observation collection associated with interferometer map data */
  private ObservationCollectionMapData chartData = null;

  /* swing */
  /** chart panel */
  private SquareChartPanel chartPanel;

  /**
   * Constructor
   */
  public InterferometerMapPanel() {
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

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Export the chart component as a PDF document
   */
  public void performPDFAction() {
    ExportPDFAction.exportPDF(this);
  }

  /**
   * Return the PDF default file name (without extension)
   * @return PDF default file name
   */
  public String getPDFDefaultFileName() {
    if (this.getChartData() != null) {
      final StringBuilder sb = new StringBuilder(32);
      sb.append("MAP_");
      sb.append(this.getChartData().getInterferometerConfiguration(true)).append('_');
      sb.append(this.getChartData().getDisplayConfigurations("_", true)).append('.').append(PDF_EXT);

      return sb.toString();
    }
    return null;
  }

  /**
   * Return the PDF options
   * @return PDF options
   */
  public PDFOptions getPDFOptions() {
    return PDFOptions.DEFAULT_PDF_OPTIONS;
  }

  /**
   * Return the chart to export as a PDF document
   * @return chart
   */
  public JFreeChart prepareChart() {
    return this.chart;
  }

  /**
   * Callback indicating the chart was processed by the PDF engine
   */
  public void postPDFExport() {
    // no-op
  }

  /**
   * This method is useful to set the models and specific features of initialized swing components :
   */
  private void postInit() {

    this.chart = ChartUtils.createSquareXYLineChart(null, null, true);
    this.xyPlot = (SquareXYPlot) this.chart.getPlot();

    final XYItemRenderer lineRenderer = this.xyPlot.getRenderer();

    // Use Bubble Renderer for the first dataset :
    this.xyPlot.setRenderer(0, new XYBubbleRenderer());

    // Use Line Renderer for the second dataset :
    this.xyPlot.setRenderer(1, lineRenderer);

    // Hide grid lines :
    this.xyPlot.setDomainGridlinesVisible(false);
    this.xyPlot.setRangeGridlinesVisible(false);

    // hide axes at [0,0] :
    this.xyPlot.setDomainZeroBaselineVisible(false);
    this.xyPlot.setRangeZeroBaselineVisible(false);

    final XYItemRenderer renderer = this.xyPlot.getRenderer();

    // station labels :
    renderer.setBaseItemLabelGenerator(new NameLabelGenerator());
    renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BOTTOM_RIGHT));
    renderer.setBaseItemLabelsVisible(true);

    // Adjust outline :
    this.xyPlot.setOutlineStroke(new BasicStroke(1.f));

    // add listener :
    this.chart.addProgressListener(this);
    this.chartPanel = ChartUtils.createSquareChartPanel(this.chart);

    // zoom options :
    this.chartPanel.setDomainZoomable(AsproConstants.ENABLE_ZOOM);
    this.chartPanel.setRangeZoomable(AsproConstants.ENABLE_ZOOM);

    // define zoom listener :
    this.chartPanel.setZoomEventListener(this);

    this.chartPanel.setMinimumSize(new Dimension(650, 500));
    this.add(this.chartPanel);
  }

  /**
   * Handle the changed event to plot the interferometer map synchronously.
   * As this instance is the first observation listener, the plot is first done
   * before other plots / computations are done.
   * @param event event
   */
  public void onProcess(final ObservationEvent event) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + event.getType() + "] process IN");
    }
    switch (event.getType()) {
      case REFRESH:
        this.plot(event.getObservationCollection());
        break;
      default:
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + event.getType() + "] process OUT");
    }
  }

  /**
   * Plot the interferometer map synchronously.
   * This code must be executed by the Swing Event Dispatcher thread (EDT)
   * @param obsCollection observation collection to use
   */
  protected void plot(final ObservationCollection obsCollection) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("plot : " + ObservationManager.toString(obsCollection));
    }

    // create a unique key (interferometer configuration|stations...)
    // to check if the map must be refreshed
    final StringBuilder sb = new StringBuilder(64);
    sb.append(obsCollection.getInterferometerConfiguration()).append('|');
    obsCollection.getAllConfigurations(sb, "|");
    final String config = sb.toString();

    if (!config.equals(this.configuration)) {
      // refresh the plot :
      this.configuration = config;

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("plot : refresh");
      }

      final long start = System.nanoTime();

      final List<InterferometerMapData> mapDataList = new ArrayList<InterferometerMapData>(obsCollection.size());

      for (ObservationSetting observation : obsCollection.getObservations()) {
        mapDataList.add(InterferometerMapService.compute(observation));
      }

      this.updatePlot(new ObservationCollectionMapData(obsCollection, mapDataList));

      if (logger.isLoggable(Level.INFO)) {
        logger.info("plot : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }
    }
  }

  /**
   * Return the chart data
   * @return chart data
   */
  private ObservationCollectionMapData getChartData() {
    return this.chartData;
  }

  /**
   * Define the chart data
   * @param chartData chart data
   */
  private void setChartData(final ObservationCollectionMapData chartData) {
    this.chartData = chartData;
  }

  /**
   * Refresh the plot using chart data.
   * This code is executed by the Swing Event Dispatcher thread (EDT)
   *
   * @param chartData chart data
   */
  private void updatePlot(final ObservationCollectionMapData chartData) {
    // memorize chart data (used by export PDF) :
    setChartData(chartData);

    // title :
    ChartUtils.clearTextSubTitle(this.chart);

    final StringBuilder sb = new StringBuilder(32);
    sb.append(chartData.getInterferometerConfiguration(false)).append(" - ");
    sb.append(chartData.getDisplayConfigurations(" / "));

    ChartUtils.addSubtitle(this.chart, sb.toString());

    // computed data are valid :
    updateChart(chartData);

    // update theme at end :
    ChartUtilities.applyCurrentTheme(this.chart);

    this.xyPlot.setBackgroundPaint(Color.WHITE);
  }

  /**
   * Update the datasets
   * @param chartData chart data
   */
  private void updateChart(final ObservationCollectionMapData chartData) {

    // First map Data is always defined :
    final InterferometerMapData mapData1 = chartData.getFirstMapData();

    // 1 - Stations :

    // define bounds to the maximum value + 10% (before setDataset) :
    final double boxSize = mapData1.getMaxXY() * 1.10d;
    this.xyPlot.defineBounds(boxSize);

    // first plot stations :
    final XYZNameDataSet stationDataSet = new XYZNameDataSet();

    stationDataSet.addSeries("Stations", new double[][]{mapData1.getStationX(), mapData1.getStationY(), mapData1.getDiameter()}, mapData1.getStationName());

    // set the first data set :
    this.xyPlot.setDataset(stationDataSet);

    // 2 - Baselines :

    // renderer for base lines :
    final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.xyPlot.getRenderer(1);

    // reset colors :
    renderer.clearSeriesPaints(false);
    // side effect with chart theme :
    renderer.setAutoPopulateSeriesPaint(false);

    final ColorPalette palette = ColorPalette.getDefaultColorPalette();

    final XYSeriesCollection dataset = new XYSeriesCollection();

    XYSeries xySeries = null;

    String[] blName;
    double[] blX1, blY1, blX2, blY2;
    int n = 0;

    final boolean single = chartData.isSingle();

    // Iterate over map data (multi conf) :
    for (InterferometerMapData mapData : chartData.getMapDataList()) {
      blName = mapData.getBaselineName();
      blX1 = mapData.getBaselineStationX1();
      blY1 = mapData.getBaselineStationY1();
      blX2 = mapData.getBaselineStationX2();
      blY2 = mapData.getBaselineStationY2();

      if (!single) {
        // 1 color per configuration (i.e. per XYSeries) :
        xySeries = new XYSeries(mapData.getStationNames(), false);
        xySeries.setNotify(false);

        dataset.addSeries(xySeries);
        n = dataset.getSeriesCount() - 1;
        renderer.setSeriesPaint(n, palette.getColor(n), false);
      }

      for (int i = 0, len = blName.length; i < len; i++) {

        if (single) {
          // 1 color per base line (i.e. per XYSeries) :
          xySeries = new XYSeries(blName[i], false);
          xySeries.setNotify(false);

          dataset.addSeries(xySeries);
          n = dataset.getSeriesCount() - 1;
          renderer.setSeriesPaint(n, palette.getColor(n), false);
        }

        // first station :
        xySeries.add(blX1[i], blY1[i]);

        // second station :
        xySeries.add(blX2[i], blY2[i]);

        // add an invalid point to break the line between the 2 segments :
        xySeries.add(Double.NaN, Double.NaN);

      } // BL
    }

    // set the second data set :
    this.xyPlot.setDataset(1, dataset);

    // annotation JMMC (moving position) :
    this.xyPlot.getRenderer(0).removeAnnotations();
    if (this.aJMMC == null) {
      this.aJMMC = ChartUtils.createXYTextAnnotation(AsproConstants.JMMC_ANNOTATION, boxSize, -boxSize);
      this.aJMMC.setFont(ChartUtils.SMALL_TEXT_ANNOTATION_FONT);
      this.aJMMC.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
      this.aJMMC.setPaint(Color.DARK_GRAY);
    } else {
      this.aJMMC.setX(boxSize);
      this.aJMMC.setY(-boxSize);
    }
    this.xyPlot.getRenderer(0).addAnnotation(this.aJMMC, Layer.BACKGROUND);
  }

  /**
   * Process the zoom event to refresh the model UV map according to the new coordinates
   * @param ze zoom event
   */
  public void chartChanged(final ZoomEvent ze) {
    if (this.aJMMC != null) {
      this.xyPlot.getRenderer(0).removeAnnotations();
      this.aJMMC.setX(ze.getDomainUpperBound());
      this.aJMMC.setY(ze.getRangeLowerBound());

      this.xyPlot.getRenderer(0).addAnnotation(this.aJMMC, Layer.BACKGROUND);
    }
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
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
}

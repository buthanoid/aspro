/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: BasicObservationForm.java,v 1.54 2011-01-27 17:04:00 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.53  2011/01/21 16:23:53  bourgesl
 * import ObservationEventType
 *
 * Revision 1.52  2010/12/17 15:20:26  bourgesl
 * major change : target list use display targets instead of target names
 *
 * Revision 1.51  2010/12/14 09:23:33  bourgesl
 * refactoring to use target list instead of target names
 * remove calibrator (all occurences) and science target
 * use target changed event to refresh target list
 *
 * Revision 1.50  2010/12/10 17:14:37  bourgesl
 * refactoring to use a display target list instead of target names
 *
 * Revision 1.49  2010/12/01 16:35:42  bourgesl
 * 'Model editor' renamed to 'Target editor'
 *
 * Revision 1.48  2010/11/25 07:58:17  bourgesl
 * select the correct target when opening the target editor
 *
 * Revision 1.47  2010/11/23 16:56:10  bourgesl
 * custom pops formatter made static
 *
 * Revision 1.46  2010/11/19 16:55:42  bourgesl
 * added J2000 in tooltip
 *
 * Revision 1.45  2010/11/18 17:18:23  bourgesl
 * use new TargetEditorDialog
 *
 * Revision 1.44  2010/11/18 15:08:10  bourgesl
 * open the model editor for all targets (instead of the selected one)
 *
 * Revision 1.43  2010/10/14 12:55:46  bourgesl
 * editable star resolver moved in JMCS
 *
 * Revision 1.42  2010/10/08 09:39:03  bourgesl
 * removed log when the selected target changes
 *
 * Revision 1.41  2010/10/07 15:02:58  bourgesl
 * added forceUpdateListTargets() hack
 *
 * Revision 1.40  2010/10/05 18:22:52  bourgesl
 * added getSelectedTargetName() to replace soon the same method in UVCoveragePanel
 *
 * Revision 1.39  2010/10/05 15:06:22  bourgesl
 * sycnhronize target selection from UV coverage panel to observation form
 *
 * Revision 1.38  2010/10/04 05:14:16  bourgesl
 * use an html tolltip for warning messages
 *
 * Revision 1.37  2010/10/01 16:02:13  bourgesl
 * use warning container directly to check if is null (reset occured in observation)
 *
 * Revision 1.36  2010/10/01 13:24:02  bourgesl
 * added tooltips on target list and PoPs field
 * Warnings are always refreshed
 * use MessagePane
 *
 * Revision 1.35  2010/09/20 14:46:02  bourgesl
 * minor refactoring changes
 *
 * Revision 1.34  2010/09/02 15:46:43  bourgesl
 * added status panel (warnings)
 *
 * Revision 1.33  2010/07/07 15:12:41  bourgesl
 * use configuration manager member
 *
 * Revision 1.32  2010/06/28 12:26:54  bourgesl
 * date is always enabled because it is used in OIFits files
 *
 * Revision 1.31  2010/06/23 12:52:08  bourgesl
 * ObservationManager regsitration for observation events moved in SettingPanel (external)
 *
 * Revision 1.30  2010/06/17 10:02:51  bourgesl
 * fixed warning hints - mainly not final static loggers
 *
 * Revision 1.29  2010/06/07 16:04:15  bourgesl
 * change the default behaviour on the date spinner to adjust the day field
 *
 * Revision 1.28  2010/05/20 12:14:13  bourgesl
 * constraints are reordered : night restriction, date and min elevation
 *
 * Revision 1.27  2010/05/11 10:17:31  mella
 * Add star name as optional field to put one new named star without simbad
 *
 * Revision 1.26  2010/05/06 15:38:38  bourgesl
 * added fireChangeEvent debug logs
 * added better auto update flag handling
 *
 * Revision 1.25  2010/04/08 14:33:32  bourgesl
 * fixed NPE on tooltips
 *
 * Revision 1.24  2010/04/08 14:22:04  bourgesl
 * added tooltip on StarResolverWidget
 *
 * Revision 1.23  2010/04/08 14:05:52  bourgesl
 * fixed several resizing issues (target list, search widget)
 * use the custom StarResolverWidget to have manually edited targets
 * added tooltips on the target list
 *
 * Revision 1.22  2010/04/06 14:40:47  bourgesl
 * minor UI changes for mac os (II)
 *
 * Revision 1.21  2010/04/02 10:05:08  bourgesl
 * minor visual changes
 *
 * Revision 1.20  2010/02/16 14:48:26  bourgesl
 * if the model editor was successfull (ok), update the plots
 *
 * Revision 1.19  2010/02/12 15:53:18  bourgesl
 * added target model editor
 *
 * Revision 1.18  2010/01/21 16:39:11  bourgesl
 * simplified StarResolverWidget integration
 *
 * Revision 1.17  2010/01/20 16:18:37  bourgesl
 * observation form refactoring
 *
 * Revision 1.16  2010/01/14 17:03:37  bourgesl
 * refactoring for observation LOAD / CHANGE events
 *
 * Revision 1.15  2010/01/08 16:51:17  bourgesl
 * initial uv coverage
 *
 * Revision 1.14  2010/01/04 15:42:47  bourgesl
 * added missing fields in Target : proper motion, parallax, magnitudes and spectral types (cds raw data)
 *
 * Revision 1.13  2009/12/18 14:50:11  bourgesl
 * check StarResolverWidget notification to process only valid CDS star results
 *
 * Revision 1.12  2009/12/15 16:32:44  bourgesl
 * added user PoP configuration based on PoP indices
 *
 * Revision 1.11  2009/12/11 16:37:32  bourgesl
 * added Pop field in observation form
 *
 * Revision 1.10  2009/12/07 15:17:59  bourgesl
 * Load observation action now refreshes the observation form completely
 *
 * Revision 1.9  2009/12/04 15:38:27  bourgesl
 * Added Save action in the menu bar
 *
 * Revision 1.8  2009/11/24 15:12:09  bourgesl
 * first step to handle delay line limits
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
 *
 *
 ******************************************************************************/
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.gui.util.GenericListModel;
import fr.jmmc.aspro.gui.util.TargetListRenderer;
import fr.jmmc.aspro.gui.util.TargetRenderer;
import fr.jmmc.aspro.model.ConfigurationManager;
import fr.jmmc.aspro.model.event.ObservationEventType;
import fr.jmmc.aspro.model.event.ObservationListener;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.model.WarningContainer;
import fr.jmmc.aspro.model.oi.FocalInstrumentConfigurationChoice;
import fr.jmmc.aspro.model.oi.InterferometerConfiguration;
import fr.jmmc.aspro.model.oi.InterferometerConfigurationChoice;
import fr.jmmc.aspro.model.oi.ObservationSetting;
import fr.jmmc.aspro.model.oi.Pop;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.aspro.model.oi.TargetUserInformations;
import fr.jmmc.mcs.astro.star.Star;
import fr.jmmc.mcs.gui.MessagePane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * This form allows the user to select main observation settings : date, interferometer, configuration, stations and targets ...
 * @author bourgesl
 */
public final class BasicObservationForm extends javax.swing.JPanel implements ChangeListener, ActionListener, Observer, ObservationListener {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.gui.BasicObservationForm";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /** flag to log a stack trace in method updateObservation() to detect multiple calls */
  private final static boolean DEBUG_UPDATE_EVENT = false;

  /* members */
  /** configuration manager */
  private final ConfigurationManager cm = ConfigurationManager.getInstance();
  /** observation manager */
  private final ObservationManager om = ObservationManager.getInstance();
  /** flag to enable / disable the automatic update of the observation when any swing component changes */
  private boolean doAutoUpdateObservation = true;
  /** Warning image icon */
  private ImageIcon warningIcon = null;

  /** Creates new form BasicObservationForm */
  public BasicObservationForm() {
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

    jPanelMain = new javax.swing.JPanel();
    jLabelInterferometer = new javax.swing.JLabel();
    jComboBoxInterferometer = new javax.swing.JComboBox();
    jLabelPeriod = new javax.swing.JLabel();
    jComboBoxInterferometerConfiguration = new javax.swing.JComboBox();
    jLabelInstrument = new javax.swing.JLabel();
    jComboBoxInstrument = new javax.swing.JComboBox();
    jLabelConfiguration = new javax.swing.JLabel();
    jComboBoxInstrumentConfiguration = new javax.swing.JComboBox();
    jLabelPops = new javax.swing.JLabel();
    jTextPoPs = new JFormattedTextField(getPopsFormatter());
    jLabelTargets = new javax.swing.JLabel();
    jScrollPaneTargets = new javax.swing.JScrollPane();
    jListTargets = createTargetList();
    jButtonDeleteTarget = new javax.swing.JButton();
    jButtonTargetEditor = new javax.swing.JButton();
    starSearchField = new fr.jmmc.mcs.astro.star.EditableStarResolverWidget();
    jPanelOptions = new javax.swing.JPanel();
    jCheckBoxNightLimit = new javax.swing.JCheckBox();
    jLabelDate = new javax.swing.JLabel();
    jDateSpinner = new javax.swing.JSpinner();
    jLabelMinElev = new javax.swing.JLabel();
    jFieldMinElev = new javax.swing.JFormattedTextField();
    jPanelStatus = new javax.swing.JPanel();
    jLabelState = new javax.swing.JLabel();
    jLabelStatus = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    jPanelMain.setBorder(javax.swing.BorderFactory.createTitledBorder("Observation"));
    jPanelMain.setLayout(new java.awt.GridBagLayout());

    jLabelInterferometer.setLabelFor(jComboBoxInterferometer);
    jLabelInterferometer.setText("Interferometer");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelInterferometer, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jComboBoxInterferometer, gridBagConstraints);

    jLabelPeriod.setLabelFor(jComboBoxInstrumentConfiguration);
    jLabelPeriod.setText("Period");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelPeriod, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jComboBoxInterferometerConfiguration, gridBagConstraints);

    jLabelInstrument.setLabelFor(jComboBoxInstrument);
    jLabelInstrument.setText("Instrument");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelInstrument, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jComboBoxInstrument, gridBagConstraints);

    jLabelConfiguration.setLabelFor(jComboBoxInstrumentConfiguration);
    jLabelConfiguration.setText("Configuration");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelConfiguration, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelMain.add(jComboBoxInstrumentConfiguration, gridBagConstraints);

    jLabelPops.setLabelFor(jTextPoPs);
    jLabelPops.setText("PoPs");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelPops, gridBagConstraints);

    jTextPoPs.setColumns(4);
    jTextPoPs.setToolTipText("<html>\ndefine a specific PoPs combination (PoP 1 to 5) by giving the list of PoP numbers<br/>\nin the same order than stations of the selected base line. For example:<ul>\n<li>VEGA_2T with baseline S1-S2<br/>'34' means PoP3 on S1 and PoP4 on S2</li>\n<li>MIRC (4T) with baseline S1-S2-E1-W2<br/>'1255' means PoP1 on S1, PoP2 on S2 and Pop5 on E1 and W2</li>\n</ul>\n<b>If you left this field blank, ASPRO 2 will compute the 'best PoP' combination<br/>\nmaximizing the observability of your complete list of targets</b>\n</html>");
    jTextPoPs.setMinimumSize(new java.awt.Dimension(40, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    jPanelMain.add(jTextPoPs, gridBagConstraints);

    jLabelTargets.setLabelFor(jListTargets);
    jLabelTargets.setText("Targets");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jLabelTargets, gridBagConstraints);

    jScrollPaneTargets.setPreferredSize(new java.awt.Dimension(100, 50));

    jListTargets.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jListTargets.setToolTipText("Target list : use the SimBad field to enter your targets\n");
    jListTargets.setVisibleRowCount(3);
    jScrollPaneTargets.setViewportView(jListTargets);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jScrollPaneTargets, gridBagConstraints);

    jButtonDeleteTarget.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
    jButtonDeleteTarget.setIcon(new javax.swing.ImageIcon(getClass().getResource("/fr/jmmc/aspro/gui/icons/delete.png"))); // NOI18N
    jButtonDeleteTarget.setToolTipText("delete the selected target");
    jButtonDeleteTarget.setMargin(new java.awt.Insets(0, 0, 0, 0));
    jButtonDeleteTarget.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonDeleteTargetActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jButtonDeleteTarget, gridBagConstraints);

    jButtonTargetEditor.setText("Target editor");
    jButtonTargetEditor.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonTargetEditorActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(jButtonTargetEditor, gridBagConstraints);

    starSearchField.setToolTipText("<html>\nEnter targets here :<br/>\nTarget identifier (SimBad resolution)<br/>\nor RA / DEC coordinates (J2000) with optional star name ('HMS DMS [star name]')\n</html>");
    starSearchField.setMinimumSize(new java.awt.Dimension(100, 23));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelMain.add(starSearchField, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.8;
    gridBagConstraints.weighty = 1.0;
    add(jPanelMain, gridBagConstraints);

    jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("Constraints"));
    jPanelOptions.setLayout(new java.awt.GridBagLayout());

    jCheckBoxNightLimit.setText("Night restriction");
    jCheckBoxNightLimit.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    jCheckBoxNightLimit.setIconTextGap(20);
    jCheckBoxNightLimit.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelOptions.add(jCheckBoxNightLimit, gridBagConstraints);

    jLabelDate.setLabelFor(jDateSpinner);
    jLabelDate.setText("Date");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelOptions.add(jLabelDate, gridBagConstraints);

    jDateSpinner.setModel(new javax.swing.SpinnerDateModel());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 2;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelOptions.add(jDateSpinner, gridBagConstraints);

    jLabelMinElev.setLabelFor(jFieldMinElev);
    jLabelMinElev.setText("Min. Elevation");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelOptions.add(jLabelMinElev, gridBagConstraints);

    jFieldMinElev.setColumns(2);
    jFieldMinElev.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelOptions.add(jFieldMinElev, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    add(jPanelOptions, gridBagConstraints);

    jPanelStatus.setMinimumSize(new java.awt.Dimension(57, 30));
    jPanelStatus.setPreferredSize(new java.awt.Dimension(100, 30));
    jPanelStatus.setLayout(new java.awt.GridBagLayout());

    jLabelState.setText("Status : ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanelStatus.add(jLabelState, gridBagConstraints);

    jLabelStatus.setText("Ok");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jPanelStatus.add(jLabelStatus, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(jPanelStatus, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Process the remove target action
   * @param evt action event
   */
  private void jButtonDeleteTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteTargetActionPerformed

    // TODO : multi selection support to delete multiple targets at the same time :

    final Target selectedTarget = getSelectedTarget();

    if (selectedTarget != null) {

      if (this.om.isCalibrator(selectedTarget)) {
        if (MessagePane.showConfirmMessage(this.jButtonDeleteTarget,
                "Do you want to delete the calibrator target [" + selectedTarget.getName() + "] and all associations ?")) {

          // update the data model and fire change events :
          this.om.removeCalibrator(selectedTarget);
        }
      } else if (MessagePane.showConfirmMessage(this.jButtonDeleteTarget,
              "Do you want to delete the science target [" + selectedTarget.getName() + "] ?")) {

        // update the data model and fire change events :
        this.om.removeTarget(selectedTarget);
      }
    }
  }//GEN-LAST:event_jButtonDeleteTargetActionPerformed

  private void jButtonTargetEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTargetEditorActionPerformed
    final Target target = getSelectedTarget();

    // show model editor :
    TargetEditorDialog.showEditor((target != null) ? target.getName() : null);
  }//GEN-LAST:event_jButtonTargetEditorActionPerformed

  /**
   * Return the Pops custom formatter : number format that accepts null values
   * @return number formatter
   */
  private static NumberFormatter getPopsFormatter() {
    final NumberFormatter nf = new NumberFormatter(new DecimalFormat("####")) {

      /** default serial UID for Serializable interface */
      private static final long serialVersionUID = 1;

      /**
       * Hack to allow empty string
       */
      @Override
      public Object stringToValue(String text) throws ParseException {
        if (text == null || text.length() == 0) {
          return null;
        }
        return super.stringToValue(text);
      }
    };
    nf.setValueClass(Integer.class);
    // reject invalid characters (digits only)
    nf.setAllowsInvalid(false);
    nf.setCommitsOnValidEdit(false);
    return nf;
  }

  /**
   * This method is useful to set the models and specific features of initialized swing components.
   * Add the star search field and refresh content of the combo boxes.
   * Finally update the observation according to the form state
   */
  private void postInit() {

    this.warningIcon = new ImageIcon(getClass().getResource("/fr/jmmc/aspro/gui/icons/dialog-warning.png"));

    this.resetStatus();

    // add observer to the StarResolverWidget :
    this.starSearchField.getStar().addObserver(this);

    // update component models :
    final DateEditor de = new JSpinner.DateEditor(jDateSpinner, "yyyy/MM/dd");
    this.jDateSpinner.setEditor(de);

    // custom focus listener to let the user change the day field by default :
    de.getTextField().addFocusListener(new FocusAdapter() {

      @Override
      public void focusGained(FocusEvent e) {
        if (e.getSource() instanceof JTextComponent) {
          final JTextComponent textComponent = ((JTextComponent) e.getSource());

          SwingUtilities.invokeLater(new Runnable() {

            public void run() {
              final int last = textComponent.getDocument().getLength();
              // select the day field to force the spinner to use it
              textComponent.setCaretPosition(last - 2);
              textComponent.moveCaretPosition(last);
            }
          });
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        // nothing to do
      }
    });

    this.jComboBoxInterferometer.setModel(new DefaultComboBoxModel(this.cm.getInterferometerNames()));

    // default values :
    this.jCheckBoxNightLimit.setSelected(AsproConstants.DEFAULT_USE_NIGHT_LIMITS);
    this.jFieldMinElev.setValue(AsproConstants.DEFAULT_MIN_ELEVATION);

    // define change listeners :
    this.jDateSpinner.addChangeListener(this);
    this.jComboBoxInterferometer.addActionListener(this);
    this.jComboBoxInterferometerConfiguration.addActionListener(this);
    this.jComboBoxInstrument.addActionListener(this);
    this.jComboBoxInstrumentConfiguration.addActionListener(this);

    this.jTextPoPs.addPropertyChangeListener("value", new PropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent evt) {
        jTextPoPsPropertyChange(evt);
      }
    });

    this.jFieldMinElev.addPropertyChangeListener("value", new PropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent evt) {
        final double minElevNew = ((Number) jFieldMinElev.getValue()).doubleValue();

        if (minElevNew < 0d || minElevNew >= 90d) {
          // invalid value :
          jFieldMinElev.setValue(AsproConstants.DEFAULT_MIN_ELEVATION);
        }
        updateObservation();
      }
    });

    this.jCheckBoxNightLimit.addItemListener(new ItemListener() {

      public void itemStateChanged(final ItemEvent e) {
        updateObservation();
      }
    });

    // update combo boxes :
    this.updateComboInterferometerConfiguration();
    this.updateComboInstrument();
    this.updateComboInstrumentConfiguration();
    this.updateListTargets();
    this.checkPops();

    // initial observation synchronization :
    this.updateObservation();
  }

  /**
   * Refresh the list of interferometer configurations : depends on the chosen interferometer
   */
  private void updateComboInterferometerConfiguration() {
    final Vector<String> v = this.cm.getInterferometerConfigurationNames((String) this.jComboBoxInterferometer.getSelectedItem());
    this.jComboBoxInterferometerConfiguration.setModel(new DefaultComboBoxModel(v));
    final boolean visible = (v.size() > 1);
    this.jLabelPeriod.setVisible(visible);
    this.jComboBoxInterferometerConfiguration.setVisible(visible);
  }

  /**
   * Refresh the list of instruments : depends on the chosen interferometer configuration
   */
  private void updateComboInstrument() {
    final Vector<String> v = this.cm.getInterferometerInstrumentNames((String) this.jComboBoxInterferometerConfiguration.getSelectedItem());
    this.jComboBoxInstrument.setModel(new DefaultComboBoxModel(v));
  }

  /**
   * Refresh the list of instrument configurations : depends on the chosen instrument (also the interferometer configuration)
   */
  private void updateComboInstrumentConfiguration() {
    final Vector<String> v = this.cm.getInstrumentConfigurationNames((String) this.jComboBoxInterferometerConfiguration.getSelectedItem(),
            (String) this.jComboBoxInstrument.getSelectedItem());
    this.jComboBoxInstrumentConfiguration.setModel(new DefaultComboBoxModel(v));
    final boolean visible = (v.size() > 1);
    this.jLabelConfiguration.setVisible(visible);
    this.jComboBoxInstrumentConfiguration.setVisible(visible);
  }

  /**
   * Refresh the target list
   */
  private void updateListTargets() {
    final Target selectedTarget = getSelectedTarget();

    final List<Target> displayTargets = this.om.getDisplayTargets();
    final TargetUserInformations targetUserInfos = this.om.getTargetUserInfos();

    // note : read only :
    this.jListTargets.setModel(new GenericListModel<Target>(displayTargets));
    this.jListTargets.setCellRenderer(new TargetListRenderer(new TargetRenderer(targetUserInfos)));

    // restore previous selected item :
    if (selectedTarget != null) {
      updateSelectedTarget(selectedTarget);
    }

    // disable buttons if the target list is empty :
    this.jButtonDeleteTarget.setEnabled(!displayTargets.isEmpty());
    this.jButtonTargetEditor.setEnabled(!displayTargets.isEmpty());

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("jListTargets updated : " + getSelectedTarget());
    }
  }

  /**
   * Define the selected target in the target list
   *
   * @param target target to select
   */
  protected void updateSelectedTarget(final Target target) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("selected target = " + target);
    }

    if (target != null && !target.equals(getSelectedTarget())) {
      this.jListTargets.setSelectedValue(target, true);
    }
  }

  /**
   * Return the currently selected target
   * @return target
   */
  public Target getSelectedTarget() {
    return (Target) this.jListTargets.getSelectedValue();
  }

  /**
   * Process any comboBox change event (interferometer, interferometer configuration, instrument, instrument configuration).
   * Refresh the dependent combo boxes and update the observation according to the form state
   * @param e action event
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == this.jComboBoxInterferometer) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Interferometer changed : " + this.jComboBoxInterferometer.getSelectedItem());
      }
      updateComboInterferometerConfiguration();
      updateComboInstrument();
      updateComboInstrumentConfiguration();
      checkPops();
    } else if (e.getSource() == this.jComboBoxInterferometerConfiguration) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Interferometer Configuration changed : " + this.jComboBoxInterferometerConfiguration.getSelectedItem());
      }
      updateComboInstrument();
      updateComboInstrumentConfiguration();
    } else if (e.getSource() == this.jComboBoxInstrument) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Instrument changed : " + this.jComboBoxInstrument.getSelectedItem());
      }
      updateComboInstrumentConfiguration();
      checkPops();

    } else if (e.getSource() == this.jComboBoxInstrumentConfiguration) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Instrument Configuration changed : " + this.jComboBoxInstrumentConfiguration.getSelectedItem());
      }
    }
    updateObservation();
  }

  /**
   * Check if this interferometer has PoPs. If false, disable and reset the PoPs text field
   */
  private void checkPops() {
    final boolean hasPops = this.cm.hasPoPs((String) this.jComboBoxInterferometer.getSelectedItem());
    this.jLabelPops.setVisible(hasPops);
    this.jTextPoPs.setVisible(hasPops);
    // reset the pops configuration because it can be invalid because of the chosen instrument
    resetPops();
  }

  /**
   * Process the change event for the PoPs configuration text field.
   * Validates the new input (digits corresponds to valid PoPs indices)
   * @param evt property change event
   */
  public void jTextPoPsPropertyChange(final PropertyChangeEvent evt) {
    List<Pop> listPoPs = null;

    if (evt.getNewValue() != null) {
      final String popConfig = evt.getNewValue().toString();

      // parse the configuration (instrument = number of channels) + (interferometer = pop indexes [1-5]) :

      listPoPs = this.cm.parseInstrumentPoPs((String) this.jComboBoxInterferometerConfiguration.getSelectedItem(),
              (String) this.jComboBoxInstrument.getSelectedItem(), popConfig);
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Pops changed = " + evt.getNewValue() + " : " + listPoPs);
    }

    if (listPoPs == null && evt.getNewValue() != null) {
      // invalid, reset the field to empty :
      resetPops();
    }
    // then update the observation :
    updateObservation();
  }

  /**
   * Reset PoPs text field
   */
  private void resetPops() {
    // disable the automatic update observation :
    final boolean prevAutoUpdateObservation = this.setAutoUpdateObservation(false);
    try {
      // note : setValue() can fire a property change event :
      this.jTextPoPs.setValue(null);

    } finally {
      // restore the automatic update observation :
      this.setAutoUpdateObservation(prevAutoUpdateObservation);
    }
  }

  /**
   * Process the date spinner change event.
   * Update the observation according to the form state
   * @param ce change event
   */
  public void stateChanged(final ChangeEvent ce) {
    if (ce.getSource() == this.jDateSpinner) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Date changed : " + this.jDateSpinner.getModel().getValue());
      }
      updateObservation();
    }
  }

  /**
   * Observer implementation used for the StarResolver (called by EDT)
   * Create a new Target object with the retrieved data from SimBAD and
   * fire an observation change event
   * @param o Observable instance
   * @param arg unused argument
   */
  public void update(final Observable o, final Object arg) {
    if (o instanceof Star) {
      final Star star = (Star) o;

      final Star.Notification notification = (Star.Notification) arg;

      if (notification == Star.Notification.QUERY_COMPLETE) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Star resolved : \n" + star);
        }

        // update the data model and fire change events :
        this.om.addTarget(Target.formatName(star.getName()), star);
      }
    }
  }

  /**
   * Update the observation with the form fields if the automatic update flag is enabled.
   * If the observation changed, then fire an observation change event
   */
  private void updateObservation() {
    // check if the automatic update flag is enabled :
    if (this.doAutoUpdateObservation) {
      if (DEBUG_UPDATE_EVENT) {
        logger.log(Level.SEVERE, "UPDATE", new Throwable());
      }

      boolean changed = false;

      // observation :
      changed |= this.om.setInterferometerConfigurationName((String) this.jComboBoxInterferometerConfiguration.getSelectedItem());
      changed |= this.om.setInstrumentConfigurationName((String) this.jComboBoxInstrument.getSelectedItem());
      changed |= this.om.setInstrumentConfigurationStations((String) this.jComboBoxInstrumentConfiguration.getSelectedItem());
      changed |= this.om.setInstrumentConfigurationPoPs(this.jTextPoPs.getText());

      // constraints :
      changed |= this.om.setWhen((Date) this.jDateSpinner.getModel().getValue());
      changed |= this.om.setMinElevation(((Number) this.jFieldMinElev.getValue()).doubleValue());
      changed |= this.om.setNightRestriction(this.jCheckBoxNightLimit.isSelected());

      if (changed) {
        // fire an observation change event :
        this.om.fireObservationChanged();
      }
    }
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
    try {
      // observation :

      // update the interferometer and interferometer configuration :
      final InterferometerConfigurationChoice interferometerChoice = observation.getInterferometerConfiguration();

      final InterferometerConfiguration ic = interferometerChoice.getInterferometerConfiguration();

      if (ic != null) {
        // update the selected interferometer :
        this.jComboBoxInterferometer.setSelectedItem(ic.getInterferometer().getName());
        // update the selected interferometer configuration :
        this.jComboBoxInterferometerConfiguration.setSelectedItem(ic.getName());
      }

      final FocalInstrumentConfigurationChoice instrumentChoice = observation.getInstrumentConfiguration();

      // update the selected instrument :
      this.jComboBoxInstrument.setSelectedItem(instrumentChoice.getName());

      // update the selected instrument configuration (stations) :
      this.jComboBoxInstrumentConfiguration.setSelectedItem(instrumentChoice.getStations());

      // update the selected pops (pops) :
      // note : setText() does not fire a property change event :
      this.jTextPoPs.setText(instrumentChoice.getPops());

      // update the target list :
      this.updateListTargets();

      // constraints :

      // update the date spinner :
      final XMLGregorianCalendar date = observation.getWhen().getDate();
      if (date != null) {
        this.jDateSpinner.setValue(date.toGregorianCalendar().getTime());
      }

      // update the min elevation :
      this.jFieldMinElev.setValue(interferometerChoice.getMinElevation());

      // update the night restriction :
      this.jCheckBoxNightLimit.setSelected(observation.getWhen().isNightRestriction());

    } finally {
      // restore the automatic update observation :
      this.setAutoUpdateObservation(prevAutoUpdateObservation);
    }
  }

  /**
   * Handle the given event on the given observation.
   * Refresh the UI component according to the loaded observation settings
   *
   * @param type event type
   * @param observation observation
   */
  public void onProcess(final ObservationEventType type, final ObservationSetting observation) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + type + "] process IN");
    }
    switch (type) {
      case LOADED:
        onLoadObservation(observation);
        break;
      case TARGET_CHANGED:
        this.updateListTargets();
        break;
      case CHANGED:
        this.resetStatus();
        break;
      case WARNINGS_READY:
        this.updateStatus(observation.getWarningContainer());
        break;
      default:
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("event [" + type + "] process OUT");
    }
  }

  /**
   * Reset status panel
   */
  private void resetStatus() {
    this.updateStatus(null);
  }

  /**
   * Update status panel
   * @param warningContainer warning container or null to reset content
   */
  private void updateStatus(final WarningContainer warningContainer) {
    if (warningContainer == null || !warningContainer.hasWarningMessages()) {
      // reset
      this.jLabelStatus.setIcon(null);
      this.jLabelStatus.setText("Ok");
      this.jLabelStatus.setToolTipText(null);
    } else {
      this.jLabelStatus.setIcon(this.warningIcon);
      this.jLabelStatus.setText("Warning");

      final StringBuilder sb = new StringBuilder(256);
      sb.append("<html>");
      for (String msg : warningContainer.getWarningMessages()) {
        sb.append(msg).append("<br>");
      }
      sb.append("</html>");
      this.jLabelStatus.setToolTipText(sb.toString());
    }
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButtonDeleteTarget;
  private javax.swing.JButton jButtonTargetEditor;
  private javax.swing.JCheckBox jCheckBoxNightLimit;
  private javax.swing.JComboBox jComboBoxInstrument;
  private javax.swing.JComboBox jComboBoxInstrumentConfiguration;
  private javax.swing.JComboBox jComboBoxInterferometer;
  private javax.swing.JComboBox jComboBoxInterferometerConfiguration;
  private javax.swing.JSpinner jDateSpinner;
  private javax.swing.JFormattedTextField jFieldMinElev;
  private javax.swing.JLabel jLabelConfiguration;
  private javax.swing.JLabel jLabelDate;
  private javax.swing.JLabel jLabelInstrument;
  private javax.swing.JLabel jLabelInterferometer;
  private javax.swing.JLabel jLabelMinElev;
  private javax.swing.JLabel jLabelPeriod;
  private javax.swing.JLabel jLabelPops;
  private javax.swing.JLabel jLabelState;
  private javax.swing.JLabel jLabelStatus;
  private javax.swing.JLabel jLabelTargets;
  private javax.swing.JList jListTargets;
  private javax.swing.JPanel jPanelMain;
  private javax.swing.JPanel jPanelOptions;
  private javax.swing.JPanel jPanelStatus;
  private javax.swing.JScrollPane jScrollPaneTargets;
  private javax.swing.JFormattedTextField jTextPoPs;
  private fr.jmmc.mcs.astro.star.EditableStarResolverWidget starSearchField;
  // End of variables declaration//GEN-END:variables

  /**
   * Create the custom JList to support tooltips for targets
   * @return JList
   */
  private static JList createTargetList() {
    return new JList() {

      /** default serial UID for Serializable interface */
      private static final long serialVersionUID = 1;

      /** This method is called as the cursor moves within the list */
      @Override
      public String getToolTipText(final MouseEvent evt) {
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("getToolTipText : " + evt);
        }
        // Get item index :
        final int index = locationToIndex(evt.getPoint());
        if (index != -1) {
          // Get target :
          final Target target = (Target) getModel().getElementAt(index);
          if (target != null) {
            // Return the tool tip text :
            return target.toHtml();
          }
        }
        return getToolTipText();
      }
    };
  }

  /**
   * Enable / Disable the automatic update of the observation when any swing component changes.
   * Return its previous value. 
   * 
   * Typical use is as following :
   * // disable the automatic update observation :
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
}

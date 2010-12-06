/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TargetForm.java,v 1.15 2010-12-06 17:00:55 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.14  2010/12/03 16:11:52  bourgesl
 * refactoring to use new JTree classes
 *
 * Revision 1.13  2010/12/03 09:34:01  bourgesl
 * first try using drag and drop between calibrator list and target tree
 * added calibrator list coupled with Calibrator button
 * changed font for target tree
 *
 * Revision 1.12  2010/12/01 16:18:57  bourgesl
 * updated GUI to use an action toolbar and have a calibrator list
 *
 * Revision 1.11  2010/11/30 17:04:18  bourgesl
 * fixed focus/editor problems when the tree selection changes (use invokeLater)
 *
 * Revision 1.10  2010/11/29 15:27:30  bourgesl
 * small GUI changes for platform compatibility
 *
 * Revision 1.9  2010/11/29 15:07:28  bourgesl
 * smaller insets (mac)
 *
 * Revision 1.8  2010/11/29 14:56:29  bourgesl
 * fixed UI problems with special LAF (GTK ...)
 *
 * Revision 1.7  2010/11/29 13:52:11  bourgesl
 * fixed x and y weights (swing)
 *
 * Revision 1.6  2010/11/26 15:57:17  bourgesl
 * magnitudes moved upper
 * minor UI changes
 *
 * Revision 1.5  2010/11/25 17:55:26  bourgesl
 * updated aspro data model to use xsd:id / xsd:idref for target references
 *
 * Revision 1.4  2010/11/25 08:00:35  bourgesl
 * added open simbad action
 * updated data model
 *
 * Revision 1.3  2010/11/23 16:57:35  bourgesl
 * complete editor with optional fields (magnitudes ...) and user information
 * custom number formatter to allow null values in text fields
 *
 * Revision 1.2  2010/11/19 16:57:04  bourgesl
 * always open full editor with selected target
 * added target name, RA/DEC, magnitudes
 *
 * Revision 1.1  2010/11/18 17:20:33  bourgesl
 * initial GUI for target editor
 *
 */
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.gui.util.GenericListModel;
import fr.jmmc.aspro.gui.util.TargetJTree;
import fr.jmmc.aspro.gui.util.TargetTransferHandler;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.aspro.model.oi.TargetInformation;
import fr.jmmc.aspro.model.oi.TargetUserInformations;
import fr.jmmc.mcs.gui.BrowserLauncher;
import fr.jmmc.mcs.gui.MessagePane;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class represents the target information editor ...
 *
 * @author bourgesl
 */
public final class TargetForm extends javax.swing.JPanel implements PropertyChangeListener, TreeSelectionListener, ListSelectionListener {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.gui.TargetForm";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /** SimBad URL (query by identifier) */
  private static final String SIMBAD_QUERY_ID = "http://simbad.u-strasbg.fr/simbad/sim-id?Ident=";
  /** custom number field formatter */
  private static NumberFormatter numberFieldFormatter = null;

  /* members */
  /** list of edited targets (clone) */
  private final List<Target> editTargets;
  /** edited target user informations (clone) */
  private final TargetUserInformations editTargetUserInfos;
  /** cached mapping between target and Target Information */
  private final Map<String, TargetInformation> mapIDTargetInformations = new HashMap<String, TargetInformation>();
  /** current edited target */
  private Target currentTarget = null;
  /** flag to enable / disable the automatic update of the target when any swing component changes */
  private boolean doAutoUpdateTarget = true;
  /* Swing */
  /** JList model containing calibrators */
  private GenericListModel<Target> calibratorsModel;

  /**
   * Creates new form TargetForm (used by NetBeans editor only)
   */
  public TargetForm() {
    this(null, null);
  }

  /**
   * Creates new form TargetForm
   * @param targets list of targets to edit
   * @param targetUserInfos target user informations
   */
  public TargetForm(final List<Target> targets, final TargetUserInformations targetUserInfos) {
    super();

    this.editTargets = targets;
    this.editTargetUserInfos = targetUserInfos;

    initComponents();

    postInit();
  }

  /**
   * This method is useful to set the specific features of initialized swing components.
   */
  private void postInit() {

    // tree selection listener :
    this.jTreeTargets.addTreeSelectionListener(this);

    // list selection listener :
    this.jListCalibrators.getSelectionModel().addListSelectionListener(this);

    // add property change listener to editable fields :

    // radial velocity :
    this.jFieldSysVel.addPropertyChangeListener("value", this);

    // proper motion :
    this.jFieldPMRA.addPropertyChangeListener("value", this);
    this.jFieldPMDEC.addPropertyChangeListener("value", this);

    // parallax :
    this.jFieldParallax.addPropertyChangeListener("value", this);
    this.jFieldParaErr.addPropertyChangeListener("value", this);

    // Fluxes :
    this.jFieldMagV.addPropertyChangeListener("value", this);
    this.jFieldMagI.addPropertyChangeListener("value", this);
    this.jFieldMagJ.addPropertyChangeListener("value", this);
    this.jFieldMagH.addPropertyChangeListener("value", this);
    this.jFieldMagK.addPropertyChangeListener("value", this);
    this.jFieldMagN.addPropertyChangeListener("value", this);

    // add document listener to target description :
    this.jTextAreaTargetInfos.getDocument().addDocumentListener(new DocumentListener() {

      public void insertUpdate(final DocumentEvent e) {
        targetInfosChanged();
      }

      public void removeUpdate(final DocumentEvent e) {
        targetInfosChanged();
      }

      public void changedUpdate(DocumentEvent e) {
        //Plain text components do not fire these events
      }
    });
  }

  /**
   * Initialize the internal model (tree) from the given list of targets
   * @param targetName target name to select
   */
  protected void initialize(final String targetName) {

    this.calibratorsModel = new GenericListModel<Target>(this.editTargetUserInfos.getCalibrators());
    this.jListCalibrators.setModel(this.calibratorsModel);

    this.generateTree(this.editTargets);
    this.selectTarget(Target.getTarget(targetName, this.editTargets));

    // Add custom DnD support :
    final TransferHandler targetTransferHandler = new TargetTransferHandler(this.editTargets, this.editTargetUserInfos);
    this.jListCalibrators.setTransferHandler(targetTransferHandler);
    this.jTreeTargets.setTransferHandler(targetTransferHandler);
  }

  /* Tree related methods */
  /**
   * Return the custom TargetJTree
   * @return TargetJTree
   */
  private final TargetJTree getTreeTargets() {
    return (TargetJTree) this.jTreeTargets;
  }

  /**
   * Generate the tree from the given list of targets (single or all)
   * @param targets list of targets to edit
   */
  private void generateTree(final List<Target> targets) {

    final DefaultMutableTreeNode rootNode = this.getTreeTargets().getRootNode();

    TargetInformation targetInfo;
    DefaultMutableTreeNode targetNode;
    for (Target target : targets) {

      targetNode = this.getTreeTargets().addNode(rootNode, target);

      // add calibrators as children of the target Node :
      targetInfo = getTargetUserInformation(target);
      for (Target calibrator : targetInfo.getCalibrators()) {
        this.getTreeTargets().addNode(targetNode, calibrator);
      }
    }

    // fire node structure changed :
    this.getTreeTargets().fireNodeChanged(rootNode);
  }

  /**
   * Select the target node for the given target
   * @param target to select
   */
  protected void selectTarget(final Target target) {
    this.getTreeTargets().selectTarget(target);
  }

  /**
   * Process the tree selection events
   * @param e tree selection event
   */
  public void valueChanged(final TreeSelectionEvent e) {
    final DefaultMutableTreeNode node = this.getTreeTargets().getLastSelectedNode();

    /* if nothing is selected */
    if (node == null) {
      return;
    }

    /* React to the node selection. */

    // Use invokeLater to avoid event ordering problems with focusLost on JTextArea
    // or JTable editors :
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        // Check if it is the root node :
        final DefaultMutableTreeNode rootNode = getTreeTargets().getRootNode();
        if (node == rootNode) {
          getTreeTargets().selectFirstChildNode(rootNode);
          return;
        }

        /* retrieve the node that was selected */
        final Object userObject = node.getUserObject();

        if (userObject != null) {
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("tree selection : " + userObject);
          }

          if (userObject instanceof Target) {
            // Target :
            processTargetSelection((Target) userObject);
          }
        }
      }
    });
  }

  /**
   * Called whenever the value of the selection changes.
   * @param e the event that characterizes the change.
   */
  public void valueChanged(final ListSelectionEvent e) {
    final ListSelectionModel lsm = (ListSelectionModel) e.getSource();

    if (e.getValueIsAdjusting() || lsm.isSelectionEmpty()) {
      return;
    }

    // Single selection mode :
    final int minIndex = lsm.getMinSelectionIndex();

    if (minIndex != -1) {
      final Target target = this.calibratorsModel.get(minIndex);

      final DefaultMutableTreeNode targetNode = this.getTreeTargets().findTreeNode(target);

      if (targetNode != null) {
        // Select the target node :
        this.getTreeTargets().selectPath(new TreePath(targetNode.getPath()));
      } else {
        this.processTargetSelection(target);
      }
    }
  }


  /**
   * Update the UI when a target is selected in the target tree
   * @param target selected target
   */
  private void processTargetSelection(final Target target) {

    // update the current target :
    this.currentTarget = target;

    // disable the automatic update target :
    final boolean prevAutoUpdateTarget = this.setAutoUpdateTarget(false);
    try {

      // note : setText() / setValue() methods fire a property change event :

      // name :
      this.jFieldName.setText(target.getName());
      // RA / DEC :
      this.jFieldRA.setText(target.getRA());
      this.jFieldDEC.setText(target.getDEC());

      // radial velocity :
      this.jFieldSysVel.setValue(target.getSYSVEL());

      // proper motion :
      this.jFieldPMRA.setValue(target.getPMRA());
      this.jFieldPMDEC.setValue(target.getPMDEC());

      // parallax :
      this.jFieldParallax.setValue(target.getPARALLAX());
      this.jFieldParaErr.setValue(target.getPARAERR());

      // Fluxes :
      this.jFieldMagV.setValue(target.getFLUXV());
      this.jFieldMagI.setValue(target.getFLUXI());
      this.jFieldMagJ.setValue(target.getFLUXJ());
      this.jFieldMagH.setValue(target.getFLUXH());
      this.jFieldMagK.setValue(target.getFLUXK());
      this.jFieldMagN.setValue(target.getFLUXN());

      // spectral type :
      this.jFieldSpecType.setText(target.getSPECTYP());
      // object types :
      this.jFieldObjTypes.setText(target.getOBJTYP());
      // identifiers :
      this.jTextAreaIds.setText(target.getIDS());
      this.jTextAreaIds.setCaretPosition(0);

      // user description :
      this.jTextAreaTargetInfos.setText(getTargetUserInformation(target).getDescription());

      // update calibrator flag :
      this.jToggleButtonCalibrator.setSelected(isCalibrator(target));

    } finally {
      // restore the automatic update target :
      this.setAutoUpdateTarget(prevAutoUpdateTarget);
    }
  }

  /**
   * Return true if the given target is a calibrator
   * i.e. the calibrator list contains the given target
   * @param target target to use
   * @return true if the given target is a calibrator
   */
  public final boolean isCalibrator(final Target target) {
    return this.editTargetUserInfos.isCalibrator(target);
  }

  /**
   * Process the change event for any number field.
   * Validates the new input (check valid range) and update the associated target
   * @param evt property change event
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // check if the automatic update flag is enabled :
    if (this.doAutoUpdateTarget) {

      final JFormattedTextField field = (JFormattedTextField) evt.getSource();
      final Double oldValue = (Double) evt.getOldValue();
      Double value = (Double) evt.getNewValue();

      // check if value changed (null supported) :
      if (!isChanged(value, oldValue)) {
        return;
      }

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("field " + field.getName() + " new: " + value + " old: " + oldValue);
      }

      if (value != null) {

        // check the new value :
        final double val = value.doubleValue();

        if (field == this.jFieldParaErr) {
          // check if error is negative :
          if (val < 0d) {
            if (logger.isLoggable(Level.FINE)) {
              logger.fine("Parallax Error negative : " + val);
            }

            field.setValue(oldValue);
            return;
          }
        } else if (field.getName().startsWith("FLUX")) {
          // check if magnitudes are in range [-30;100]
          if (val < -30d || val > 100d) {
            if (logger.isLoggable(Level.FINE)) {
              logger.fine("Magnitude " + field.getName() + " invalid : " + val);
            }

            field.setValue(oldValue);
            return;
          }
        }
      }

      // update the target :

      // note : we could use introspection to avoid such if/else cascade ...
      if (field == this.jFieldSysVel) {
        this.currentTarget.setSYSVEL(value);
      } else if (field == this.jFieldPMRA) {
        this.currentTarget.setPMRA(value);
      } else if (field == this.jFieldPMDEC) {
        this.currentTarget.setPMDEC(value);
      } else if (field == this.jFieldParallax) {
        this.currentTarget.setPARALLAX(value);
      } else if (field == this.jFieldParaErr) {
        this.currentTarget.setPARAERR(value);
      } else if (field == this.jFieldMagV) {
        this.currentTarget.setFLUXV(value);
      } else if (field == this.jFieldMagI) {
        this.currentTarget.setFLUXI(value);
      } else if (field == this.jFieldMagJ) {
        this.currentTarget.setFLUXJ(value);
      } else if (field == this.jFieldMagH) {
        this.currentTarget.setFLUXH(value);
      } else if (field == this.jFieldMagK) {
        this.currentTarget.setFLUXK(value);
      } else if (field == this.jFieldMagN) {
        this.currentTarget.setFLUXN(value);
      } else {
        logger.severe("unsupported field : " + field);
      }
    }
  }

  /**
   * Process the document change event for the target user information
   */
  protected void targetInfosChanged() {
    // check if the automatic update flag is enabled :
    if (this.doAutoUpdateTarget) {

      final String text = this.jTextAreaTargetInfos.getText();

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("user infos : " + text);
      }

      getTargetUserInformation(this.currentTarget).setDescription((text.length() > 0) ? text : null);
    }
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

    jScrollPaneTreeTargets = new javax.swing.JScrollPane();
    jTreeTargets = new TargetJTree(this.editTargetUserInfos);
    jToolBarActions = new javax.swing.JToolBar();
    jButtonUp = new javax.swing.JButton();
    jButtonDown = new javax.swing.JButton();
    jSeparator1 = new javax.swing.JToolBar.Separator();
    jToggleButtonCalibrator = new javax.swing.JToggleButton();
    jButtonRemoveCalibrator = new javax.swing.JButton();
    jPanelTarget = new javax.swing.JPanel();
    jLabelName = new javax.swing.JLabel();
    jFieldName = new javax.swing.JTextField();
    jLabelRA = new javax.swing.JLabel();
    jFieldRA = new javax.swing.JTextField();
    jLabelDEC = new javax.swing.JLabel();
    jFieldDEC = new javax.swing.JTextField();
    jLabelSysVel = new javax.swing.JLabel();
    jFieldSysVel = new JFormattedTextField(getNumberFieldFormatter());
    jSeparator3 = new javax.swing.JSeparator();
    jLabelPMRA = new javax.swing.JLabel();
    jFieldPMRA = new JFormattedTextField(getNumberFieldFormatter())
    ;
    jLabelRMDEC = new javax.swing.JLabel();
    jFieldPMDEC = new JFormattedTextField(getNumberFieldFormatter());
    jSeparator4 = new javax.swing.JSeparator();
    jLabelMag = new javax.swing.JLabel();
    jLabelMagV = new javax.swing.JLabel();
    jFieldMagV = new JFormattedTextField(getNumberFieldFormatter());
    jLabelMagI = new javax.swing.JLabel();
    jFieldMagI = new JFormattedTextField(getNumberFieldFormatter());
    jLabelMagJ = new javax.swing.JLabel();
    jFieldMagJ = new JFormattedTextField(getNumberFieldFormatter());
    jLabelMagH = new javax.swing.JLabel();
    jFieldMagH = new JFormattedTextField(getNumberFieldFormatter());
    jLabelMagK = new javax.swing.JLabel();
    jFieldMagK = new JFormattedTextField(getNumberFieldFormatter());
    jLabelMagN = new javax.swing.JLabel();
    jFieldMagN = new JFormattedTextField(getNumberFieldFormatter());
    jSeparator5 = new javax.swing.JSeparator();
    jLabelObjTypes = new javax.swing.JLabel();
    jLabelSpecTypes = new javax.swing.JLabel();
    jFieldSpecType = new javax.swing.JTextField();
    jFieldObjTypes = new javax.swing.JTextField();
    jLabelParallax = new javax.swing.JLabel();
    jFieldParallax = new JFormattedTextField(getNumberFieldFormatter());
    jLabelParaErr = new javax.swing.JLabel();
    jFieldParaErr = new JFormattedTextField(getNumberFieldFormatter());
    jLabelIds = new javax.swing.JLabel();
    jScrollPaneIds = new javax.swing.JScrollPane();
    jTextAreaIds = new javax.swing.JTextArea();
    jButtonSimbad = new javax.swing.JButton();
    jPanelDescription = new javax.swing.JPanel();
    jScrollPaneTargetInfos = new javax.swing.JScrollPane();
    jTextAreaTargetInfos = new javax.swing.JTextArea();
    jPanelCalibrators = new javax.swing.JPanel();
    jScrollPaneCalibrators = new javax.swing.JScrollPane();
    jListCalibrators = new javax.swing.JList();

    setLayout(new java.awt.GridBagLayout());

    jScrollPaneTreeTargets.setMinimumSize(new java.awt.Dimension(80, 100));
    jScrollPaneTreeTargets.setPreferredSize(new java.awt.Dimension(130, 100));

    jTreeTargets.setFont(new java.awt.Font("Dialog", 1, 12));
    javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Targets");
    jTreeTargets.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
    jTreeTargets.setDragEnabled(true);
    jScrollPaneTreeTargets.setViewportView(jTreeTargets);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.2;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(jScrollPaneTreeTargets, gridBagConstraints);

    jToolBarActions.setFloatable(false);
    jToolBarActions.setRollover(true);

    jButtonUp.setText("Up");
    jButtonUp.setEnabled(false);
    jToolBarActions.add(jButtonUp);

    jButtonDown.setText("Down");
    jButtonDown.setEnabled(false);
    jToolBarActions.add(jButtonDown);
    jToolBarActions.add(jSeparator1);

    jToggleButtonCalibrator.setText("Flag Target as Calibrator");
    jToggleButtonCalibrator.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jToggleButtonCalibratorActionPerformed(evt);
      }
    });
    jToolBarActions.add(jToggleButtonCalibrator);

    jButtonRemoveCalibrator.setText("Remove Calibrator");
    jButtonRemoveCalibrator.setFocusable(false);
    jButtonRemoveCalibrator.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jButtonRemoveCalibrator.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    jButtonRemoveCalibrator.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonRemoveCalibratorActionPerformed(evt);
      }
    });
    jToolBarActions.add(jButtonRemoveCalibrator);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(jToolBarActions, gridBagConstraints);

    jPanelTarget.setBorder(javax.swing.BorderFactory.createTitledBorder("Target"));
    jPanelTarget.setLayout(new java.awt.GridBagLayout());

    jLabelName.setText("Name");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelName, gridBagConstraints);

    jFieldName.setColumns(5);
    jFieldName.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldName, gridBagConstraints);

    jLabelRA.setText("RA [HMS]");
    jLabelRA.setToolTipText("RA coordinate (J2000) (HMS)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelRA, gridBagConstraints);

    jFieldRA.setColumns(5);
    jFieldRA.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldRA, gridBagConstraints);

    jLabelDEC.setText("DEC [DMS]");
    jLabelDEC.setToolTipText("DEC coordinate (J2000) (DMS)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelDEC, gridBagConstraints);

    jFieldDEC.setColumns(5);
    jFieldDEC.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldDEC, gridBagConstraints);

    jLabelSysVel.setText("Radial Velocity");
    jLabelSysVel.setToolTipText("radial velocity in km/s");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelSysVel, gridBagConstraints);

    jFieldSysVel.setColumns(5);
    jFieldSysVel.setName("SYSVEL"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldSysVel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    jPanelTarget.add(jSeparator3, gridBagConstraints);

    jLabelPMRA.setText("PMRA");
    jLabelPMRA.setToolTipText("proper motion in RA (mas/yr)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelPMRA, gridBagConstraints);

    jFieldPMRA.setColumns(5);
    jFieldPMRA.setName("PMRA"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldPMRA, gridBagConstraints);

    jLabelRMDEC.setText("PMDEC");
    jLabelRMDEC.setToolTipText("proper motion in DEC (mas/yr)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelRMDEC, gridBagConstraints);

    jFieldPMDEC.setColumns(5);
    jFieldPMDEC.setName("PMDEC"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldPMDEC, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    jPanelTarget.add(jSeparator4, gridBagConstraints);

    jLabelMag.setText("Magnitudes :");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
    jPanelTarget.add(jLabelMag, gridBagConstraints);

    jLabelMagV.setText("V");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagV, gridBagConstraints);

    jFieldMagV.setColumns(5);
    jFieldMagV.setName("FLUXV"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagV, gridBagConstraints);

    jLabelMagI.setText("I");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagI, gridBagConstraints);

    jFieldMagI.setColumns(5);
    jFieldMagI.setName("FLUXI"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagI, gridBagConstraints);

    jLabelMagJ.setText("J");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagJ, gridBagConstraints);

    jFieldMagJ.setColumns(5);
    jFieldMagJ.setName("FLUXJ"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagJ, gridBagConstraints);

    jLabelMagH.setText("H");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagH, gridBagConstraints);

    jFieldMagH.setColumns(5);
    jFieldMagH.setName("FLUXH"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagH, gridBagConstraints);

    jLabelMagK.setText("K");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagK, gridBagConstraints);

    jFieldMagK.setColumns(5);
    jFieldMagK.setName("FLUXK"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagK, gridBagConstraints);

    jLabelMagN.setText("N");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelMagN, gridBagConstraints);

    jFieldMagN.setColumns(5);
    jFieldMagN.setName("FLUXN"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldMagN, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weighty = 0.1;
    jPanelTarget.add(jSeparator5, gridBagConstraints);

    jLabelObjTypes.setText("Object types");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelObjTypes, gridBagConstraints);

    jLabelSpecTypes.setText("Spectral type");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelSpecTypes, gridBagConstraints);

    jFieldSpecType.setColumns(10);
    jFieldSpecType.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldSpecType, gridBagConstraints);

    jFieldObjTypes.setColumns(10);
    jFieldObjTypes.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldObjTypes, gridBagConstraints);

    jLabelParallax.setText("Parallax");
    jLabelParallax.setToolTipText("parallax in mas");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelParallax, gridBagConstraints);

    jFieldParallax.setColumns(5);
    jFieldParallax.setName("PARALLAX"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldParallax, gridBagConstraints);

    jLabelParaErr.setText("Error");
    jLabelParaErr.setToolTipText("Error in parallax (mas/yr)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelParaErr, gridBagConstraints);

    jFieldParaErr.setColumns(5);
    jFieldParaErr.setName("PARA_ERR"); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jFieldParaErr, gridBagConstraints);

    jLabelIds.setText("Identifiers");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jLabelIds, gridBagConstraints);

    jTextAreaIds.setColumns(20);
    jTextAreaIds.setEditable(false);
    jTextAreaIds.setLineWrap(true);
    jTextAreaIds.setRows(1);
    jTextAreaIds.setTabSize(2);
    jTextAreaIds.setWrapStyleWord(true);
    jScrollPaneIds.setViewportView(jTextAreaIds);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weighty = 0.3;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jScrollPaneIds, gridBagConstraints);

    jButtonSimbad.setText("Simbad");
    jButtonSimbad.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonSimbadActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    jPanelTarget.add(jButtonSimbad, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.8;
    gridBagConstraints.weighty = 0.7;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    add(jPanelTarget, gridBagConstraints);

    jPanelDescription.setBorder(javax.swing.BorderFactory.createTitledBorder("Target notes"));
    jPanelDescription.setMinimumSize(new java.awt.Dimension(10, 50));
    jPanelDescription.setPreferredSize(new java.awt.Dimension(100, 80));
    jPanelDescription.setLayout(new java.awt.GridBagLayout());

    jTextAreaTargetInfos.setBackground(new java.awt.Color(255, 255, 153));
    jTextAreaTargetInfos.setColumns(20);
    jTextAreaTargetInfos.setFont(new java.awt.Font("Monospaced", 0, 10));
    jTextAreaTargetInfos.setRows(1);
    jScrollPaneTargetInfos.setViewportView(jTextAreaTargetInfos);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jPanelDescription.add(jScrollPaneTargetInfos, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 0.2;
    gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
    add(jPanelDescription, gridBagConstraints);

    jPanelCalibrators.setBorder(javax.swing.BorderFactory.createTitledBorder("Calibrators"));
    jPanelCalibrators.setLayout(new java.awt.BorderLayout());

    jListCalibrators.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jListCalibrators.setToolTipText("this list contains targets considered as calibrators");
    jListCalibrators.setCellRenderer(createTargetListCellRenderer());
    jListCalibrators.setDragEnabled(true);
    jListCalibrators.setFixedCellWidth(80);
    jListCalibrators.setVisibleRowCount(3);
    jScrollPaneCalibrators.setViewportView(jListCalibrators);

    jPanelCalibrators.add(jScrollPaneCalibrators, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
    add(jPanelCalibrators, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void jButtonSimbadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimbadActionPerformed
    try {
      final String url = SIMBAD_QUERY_ID + URLEncoder.encode(this.currentTarget.getName(), "UTF-8");

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Simbad url = " + url);
      }

      BrowserLauncher.openURL(url);
    } catch (UnsupportedEncodingException uee) {
      logger.log(Level.SEVERE, "unsupported encoding : ", uee);
    }
  }//GEN-LAST:event_jButtonSimbadActionPerformed

  private void jToggleButtonCalibratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonCalibratorActionPerformed

    final boolean isCalibrator = this.jToggleButtonCalibrator.isSelected();

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("isCalibrator = " + isCalibrator + " for " + this.currentTarget.getName());
    }

    if (isCalibrator) {
      boolean confirm = true;

      // check that the target has no calibrator yet :
      if (this.editTargetUserInfos.hasCalibrators(this.currentTarget)) {

        if (MessagePane.showConfirmMessage(this.jToggleButtonCalibrator,
                "Do you really want to use this target as a calibrator [" + this.currentTarget.getName() + "] ?")) {
          confirm = true;
          // TODO : use the JTree to remove calibrators from the current target and update the model ...

        } else {
          this.jToggleButtonCalibrator.setSelected(false);
          confirm = false;
        }
      }

      // TODO : Should be done directly by our data model classes (editTargetUserInfos)
      if (confirm && !this.calibratorsModel.contains(this.currentTarget)) {
        this.calibratorsModel.add(this.currentTarget);
      }

    } else {

      // First remove this calibrator from target's calibrator lists :

      if (MessagePane.showConfirmMessage(this.jToggleButtonCalibrator,
              "Do you really want to remove associations with this calibrator [" + this.currentTarget.getName() + "] ?")) {

        // TODO : use the JTree to remove the occurences of the calibrator and update the model ...

        this.calibratorsModel.remove(this.currentTarget);
      } else {
        this.jToggleButtonCalibrator.setSelected(true);
      }
    }

    // Refresh selected node to show/hide (cal) suffix :
    final DefaultMutableTreeNode targetNode = this.getTreeTargets().findTreeNode(this.currentTarget);

    if (targetNode != null) {

      // fire node structure changed :
      this.getTreeTargets().fireNodeChanged(targetNode);
    }
  }//GEN-LAST:event_jToggleButtonCalibratorActionPerformed

  private void jButtonRemoveCalibratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveCalibratorActionPerformed

    // remove the current calibrator from its science target :
    if (isCalibrator(this.currentTarget)) {

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("remove calibrator " + this.currentTarget.getName());
      }

      final DefaultMutableTreeNode currentNode = this.getTreeTargets().getLastSelectedNode();

      if (currentNode == null) {
        return;
      }

      // Parent can be a target or null :
      final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) currentNode.getParent();

      if (parentNode.getUserObject() instanceof Target) {
        final Target parentTarget = (Target) parentNode.getUserObject();

        // Remove calibrator from target :
        this.getTreeTargets().removeCalibrator(currentNode, this.currentTarget, parentNode, parentTarget);
      }
    }
  }//GEN-LAST:event_jButtonRemoveCalibratorActionPerformed

  /**
   * Validate the form
   * @return true only if the data are valid
   */
  protected boolean validateForm() {
    // TODO : is there something to validate ?
    return true;
  }

  /**
   * Return the current edited target
   * @return current edited target
   */
  protected final Target getCurrentTarget() {
    return currentTarget;
  }

  /**
   * Enable / Disable the automatic update of the target when any swing component changes.
   * Return its previous value.
   *
   * Typical use is as following :
   * // disable the automatic update target :
   * final boolean prevAutoUpdateTarget = this.setAutoUpdateTarget(false);
   * try {
   *   // operations ...
   *
   * } finally {
   *   // restore the automatic update target :
   *   this.setAutoUpdateTarget(prevAutoUpdateTarget);
   * }
   *
   * @param value new value
   * @return previous value
   */
  private boolean setAutoUpdateTarget(final boolean value) {
    // first backup the state of the automatic update target :
    final boolean previous = this.doAutoUpdateTarget;

    // then change its state :
    this.doAutoUpdateTarget = value;

    // return previous state :
    return previous;
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButtonDown;
  private javax.swing.JButton jButtonRemoveCalibrator;
  private javax.swing.JButton jButtonSimbad;
  private javax.swing.JButton jButtonUp;
  private javax.swing.JTextField jFieldDEC;
  private javax.swing.JFormattedTextField jFieldMagH;
  private javax.swing.JFormattedTextField jFieldMagI;
  private javax.swing.JFormattedTextField jFieldMagJ;
  private javax.swing.JFormattedTextField jFieldMagK;
  private javax.swing.JFormattedTextField jFieldMagN;
  private javax.swing.JFormattedTextField jFieldMagV;
  private javax.swing.JTextField jFieldName;
  private javax.swing.JTextField jFieldObjTypes;
  private javax.swing.JFormattedTextField jFieldPMDEC;
  private javax.swing.JFormattedTextField jFieldPMRA;
  private javax.swing.JFormattedTextField jFieldParaErr;
  private javax.swing.JFormattedTextField jFieldParallax;
  private javax.swing.JTextField jFieldRA;
  private javax.swing.JTextField jFieldSpecType;
  private javax.swing.JFormattedTextField jFieldSysVel;
  private javax.swing.JLabel jLabelDEC;
  private javax.swing.JLabel jLabelIds;
  private javax.swing.JLabel jLabelMag;
  private javax.swing.JLabel jLabelMagH;
  private javax.swing.JLabel jLabelMagI;
  private javax.swing.JLabel jLabelMagJ;
  private javax.swing.JLabel jLabelMagK;
  private javax.swing.JLabel jLabelMagN;
  private javax.swing.JLabel jLabelMagV;
  private javax.swing.JLabel jLabelName;
  private javax.swing.JLabel jLabelObjTypes;
  private javax.swing.JLabel jLabelPMRA;
  private javax.swing.JLabel jLabelParaErr;
  private javax.swing.JLabel jLabelParallax;
  private javax.swing.JLabel jLabelRA;
  private javax.swing.JLabel jLabelRMDEC;
  private javax.swing.JLabel jLabelSpecTypes;
  private javax.swing.JLabel jLabelSysVel;
  private javax.swing.JList jListCalibrators;
  private javax.swing.JPanel jPanelCalibrators;
  private javax.swing.JPanel jPanelDescription;
  private javax.swing.JPanel jPanelTarget;
  private javax.swing.JScrollPane jScrollPaneCalibrators;
  private javax.swing.JScrollPane jScrollPaneIds;
  private javax.swing.JScrollPane jScrollPaneTargetInfos;
  private javax.swing.JScrollPane jScrollPaneTreeTargets;
  private javax.swing.JToolBar.Separator jSeparator1;
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JSeparator jSeparator4;
  private javax.swing.JSeparator jSeparator5;
  private javax.swing.JTextArea jTextAreaIds;
  private javax.swing.JTextArea jTextAreaTargetInfos;
  private javax.swing.JToggleButton jToggleButtonCalibrator;
  private javax.swing.JToolBar jToolBarActions;
  private javax.swing.JTree jTreeTargets;
  // End of variables declaration//GEN-END:variables

  /**
   * Create a custom list renderer to display target name instead of target.toString()
   * @return custom list renderer
   */
  private static ListCellRenderer createTargetListCellRenderer() {
    return new DefaultListCellRenderer() {

      /** default serial UID for Serializable interface */
      private static final long serialVersionUID = 1;

      /**
       * Return a component that has been configured to display the specified
       * value. That component's <code>paint</code> method is then called to
       * "render" the cell.  If it is necessary to compute the dimensions
       * of a list because the list cells do not have a fixed size, this method
       * is called to generate a component on which <code>getPreferredSize</code>
       * can be invoked.
       *
       * @param list The JList we're painting.
       * @param value The value returned by list.getModel().getElementAt(index).
       * @param index The cells index.
       * @param isSelected True if the specified cell was selected.
       * @param cellHasFocus True if the specified cell has the focus.
       * @return A component whose paint() method will render the specified value.
       *
       * @see JList
       * @see ListSelectionModel
       * @see ListModel
       */
      @Override
      public Component getListCellRendererComponent(
              final JList list,
              final Object value,
              final int index,
              final boolean isSelected,
              final boolean cellHasFocus) {
        final String val;
        if (value == null) {
          val = null;
        } else {
          final Target target = (Target) value;
          val = target.getName();
        }
        return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
      }
    };
  }

  /**
   * Return the custom double formatter that accepts null values
   * @return number formatter
   */
  private static NumberFormatter getNumberFieldFormatter() {
    if (numberFieldFormatter != null) {
      return numberFieldFormatter;
    }
    final NumberFormatter nf = new NumberFormatter(new DecimalFormat("####.####")) {

      /** default serial UID for Serializable interface */
      private static final long serialVersionUID = 1;

      /**
       * Hack to allow empty string
       */
      @Override
      public Object stringToValue(final String text) throws ParseException {
        if (text == null || text.length() == 0) {
          return null;
        }
        return super.stringToValue(text);
      }
    };
    nf.setValueClass(Double.class);
    nf.setCommitsOnValidEdit(false);

    numberFieldFormatter = nf;
    return nf;
  }

  /**
   * Check if the objects are different supporting null values
   * @param value1 string 1
   * @param value2 string 2
   * @return true only if objects are different
   */
  private static boolean isChanged(final Object value1, final Object value2) {
    return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.equals(value2));
  }

  /**
   * Fast access to the target information associated to the given target (cached).
   * @param target target to use
   * @return target information
   */
  private final TargetInformation getTargetUserInformation(final Target target) {

    TargetInformation targetInfo = this.mapIDTargetInformations.get(target.getIdentifier());

    if (targetInfo == null) {
      targetInfo = this.editTargetUserInfos.getOrCreateTargetInformation(target);
      this.mapIDTargetInformations.put(target.getIdentifier(), targetInfo);
    }

    return targetInfo;
  }
}
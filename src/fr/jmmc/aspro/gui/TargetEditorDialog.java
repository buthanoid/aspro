/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TargetEditorDialog.java,v 1.3 2010-11-23 16:56:29 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/11/19 16:57:04  bourgesl
 * always open full editor with selected target
 * added target name, RA/DEC, magnitudes
 *
 * Revision 1.1  2010/11/18 17:19:35  bourgesl
 * new target and model editor using tabs (modal dialog)
 *
 */
package fr.jmmc.aspro.gui;

import fr.jmmc.aspro.gui.util.ComponentResizeAdapter;
import fr.jmmc.aspro.model.ObservationManager;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.mcs.gui.App;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This dialog contains tabs to edit both target properties and models
 * @author bourgesl
 */
public final class TargetEditorDialog extends javax.swing.JPanel {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class Name */
  private static final String className_ = "fr.jmmc.aspro.gui.TargetEditorDialog";
  /** Class logger */
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          className_);
  /* members */
  /** editor result = true if the user validates the inputs */
  private boolean result = false;
  /** list of edited targets (clone) */
  private final List<Target> editTargets;
  /* Swing */
  /** dialog window */
  private JDialog dialog;

  /**
   * Display the model editor using the given target name as the initial selected target
   * @param targetName optional target name to display only the models of this target
   * @return true if the model editor changed anything
   */
  public static boolean showEditor(final String targetName) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("showing Editor : " + targetName);
    }
    boolean result = false;

    // Prepare the list of targets :
    final List<Target> targets = ObservationManager.getInstance().getTargets();

    final TargetEditorDialog form = new TargetEditorDialog(targets);

    // initialise the full editor and select the target :
    form.initialize(targetName);

    JDialog dialog = null;

    try {
      // 1. Create the dialog
      dialog = new JDialog(App.getFrame(), "Target Editor", true);

      final Dimension dim = new Dimension(600, 600);
      dialog.setMinimumSize(dim);
      dialog.addComponentListener(new ComponentResizeAdapter(dim));

      // 2. Optional: What happens when the dialog closes ?
      dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      // 3. Create components and put them in the dialog
      dialog.add(form);

      // Associate the dialog to the form :
      form.setDialog(dialog);

      // 4. Size the dialog.
      dialog.pack();

      // Center it :
      dialog.setLocationRelativeTo(dialog.getOwner());

      // 5. Show it.
      dialog.setVisible(true);

      // get editor result :
      result = form.isResult();

    } finally {
      if (dialog != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("dispose Model Editor ...");
        }
        dialog.dispose();
      }
    }

    if (result) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("update the targets ...");
      }
      // full editor :
      ObservationManager.getInstance().setTargets(form.getEditTargets());
    }

    return result;
  }

  /** 
   * Creates new form TargetEditorDialog
   */
  public TargetEditorDialog() {
    this(null);
  }

  /**
   * Creates new form TargetEditorDialog with the given list of targets (single or all)
   * @param targets list of targets to edit
   */
  protected TargetEditorDialog(final List<Target> targets) {
    super();

    final int size = (targets != null) ? targets.size() : 0;

    this.editTargets = new ArrayList<Target>(size);

    if (targets != null) {
      Target target;
      for (Target t : targets) {
        // clone target and models to allow undo/cancel actions :
        target = (Target) t.clone();

        // add the cloned target to the edited targets :
        this.editTargets.add(target);
      }
    }

    initComponents();
  }

  /**
   * Initialise the editor with targets (single or all)
   * @param targetName target name to select
   */
  protected void initialize(final String targetName) {

    // TODO
    this.targetModelForm.generateTree(getEditTargets());
    this.targetModelForm.selectTarget(targetName);

    this.targetForm.generateTree(getEditTargets());
    this.targetForm.selectTarget(targetName);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jTabbedPane = new javax.swing.JTabbedPane();
    targetModelForm = new fr.jmmc.aspro.gui.TargetModelForm();
    targetForm = new fr.jmmc.aspro.gui.TargetForm();
    jPanelButtons = new javax.swing.JPanel();
    jButtonOK = new javax.swing.JButton();
    jButtonCancel = new javax.swing.JButton();

    setLayout(new java.awt.GridBagLayout());

    jTabbedPane.addTab("Models", null, targetModelForm, "edit target models");
    jTabbedPane.addTab("Targets", null, targetForm, "edit target information and associate calibrators");

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jTabbedPane, gridBagConstraints);

    jButtonOK.setText("OK");
    jButtonOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonOKActionPerformed(evt);
      }
    });
    jPanelButtons.add(jButtonOK);

    jButtonCancel.setText("Cancel");
    jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonCancelActionPerformed(evt);
      }
    });
    jPanelButtons.add(jButtonCancel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(jPanelButtons, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed

      // delegate the validation to inner child panels :
      if (!this.targetModelForm.validateForm()) {
        return;
      }
      if (!this.targetForm.validateForm()) {
        return;
      }

      // update the validation flag :
      this.result = true;

      if (this.dialog != null) {
        this.dialog.setVisible(false);
      }
}//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
      if (this.dialog != null) {
        this.dialog.setVisible(false);
      }
}//GEN-LAST:event_jButtonCancelActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButtonCancel;
  private javax.swing.JButton jButtonOK;
  private javax.swing.JPanel jPanelButtons;
  private javax.swing.JTabbedPane jTabbedPane;
  private fr.jmmc.aspro.gui.TargetForm targetForm;
  private fr.jmmc.aspro.gui.TargetModelForm targetModelForm;
  // End of variables declaration//GEN-END:variables

  /**
   * Return the editor result
   * @return true if the user validated (ok button)
   */
  protected boolean isResult() {
    return result;
  }

  /**
   * Return the list of target whom models are edited
   * @return list of target
   */
  protected List<Target> getEditTargets() {
    return editTargets;
  }

  /**
   * Define the JDialog for this form
   * @param dialog JDialog instance
   */
  protected void setDialog(final JDialog dialog) {
    this.dialog = dialog;
  }

  // --- TESTS -----------------------------------------------------------------
  /**
   * Simple GUI test
   * @param args unused
   */
  public static void main(String[] args) {
    // Set the default locale to en-US locale (for Numerical Fields "." ",")
    Locale.setDefault(Locale.US);

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {

        try {
          ObservationManager.getInstance().load(new File("/home/bourgesl/VLTI_FUN2.asprox"));
          logger.info("result = " + showEditor("HIP32768"));
          System.exit(0);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "runtime exception", e);
        }
      }
    });

  }
}

/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.gui.action;

import fr.jmmc.aspro.Aspro2;
import fr.jmmc.aspro.gui.BasicObservationForm;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action opens the target editor
 * @author bourgesl
 */
public final class TargetEditorAction extends RegisteredAction {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;
  /** Class name. This name is used to register to the ActionRegistrar */
  public final static String className = TargetEditorAction.class.getName();
  /** Action name. This name is used to register to the ActionRegistrar */
  public final static String actionName = "targetEditor";
  /** Class logger */
  private static final Logger logger = LoggerFactory.getLogger(className);

  /**
   * Public constructor that automatically register the action in RegisteredAction.
   */
  public TargetEditorAction() {
    super(className, actionName);
  }

  /**
   * Handle the action event
   * @param evt action event
   */
  @Override
  public void actionPerformed(final ActionEvent evt) {
    logger.debug("actionPerformed");

    final BasicObservationForm form = Aspro2.getInstance().getSettingPanel().getObservationForm();

    form.showTargetEditor();
  }
}

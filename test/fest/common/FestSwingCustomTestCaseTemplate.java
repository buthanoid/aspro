/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FestSwingCustomTestCaseTemplate.java,v 1.1 2011-03-11 12:55:35 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fest.common;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.Robot;

/**
 * This custom FestSwingTestCaseTemplate modifies the robot behaviour :
 * use
 * @see BasicRobot#robotWithCurrentAwtHierarchy()
 * instead of
 * @see BasicRobot#robotWithNewAwtHierarchy()
 *
 * @author bourgesl
 *
 * Original header :
 * 
 * Understands a template for test cases that use FEST-Swing.
 * @since 1.1
 *
 * @author Alex Ruiz
 *
 * Created on Jan 17, 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright @2009-2010 the original author or authors.
 */
public abstract class FestSwingCustomTestCaseTemplate {

  /* members */
  /** robot instance */
  private Robot robot = null;

  /**
   * Public constructor required by JUnit
   */
  public FestSwingCustomTestCaseTemplate() {
    super();
  }

  /**
   * Creates this test's <code>{@link Robot}</code> using the current AWT hierarchy.
   */
  protected final void setUpRobot() {
    robot = BasicRobot.robotWithCurrentAwtHierarchy();
  }

  /**
   * Cleans up resources used by this test's <code>{@link Robot}</code>.
   */
  protected final void cleanUp() {
    robot.cleanUp();
  }

  /**
   * Returns this test's <code>{@link Robot}</code>.
   * @return this test's <code>{@link Robot}</code>
   */
  protected final Robot robot() {
    return robot;
  }
}

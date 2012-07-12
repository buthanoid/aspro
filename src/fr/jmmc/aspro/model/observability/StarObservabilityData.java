/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.model.observability;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class contains several results for the observability of a target
 *
 * @author bourgesl
 */
public final class StarObservabilityData {
  /* type of data */

  /** star observability */
  public final static int TYPE_STAR = 0; // red
  /** calibrator observability */
  public final static int TYPE_CALIBRATOR = 1; // blue
  /** rise/set intervals */
  public final static int TYPE_RISE_SET = 2; // green
  /** horizon intervals */
  public final static int TYPE_HORIZON = 4; // violet
  /** moon distance OK intervals */
  public final static int TYPE_MOON_DIST = 3; // yellow
  /** wind intervals */
  public final static int TYPE_WIND = 5; // cyan
  /** baseline intervals */
  public final static int TYPE_BASE_LINE = 6; // automatic color

  /* members */
  /** name of the target */
  private final String targetName;
  /** additional information on data (rise/set, horizon, base line ...) */
  private final String info;
  /** type of data */
  private final int type;
  /** visible date intervals */
  private final List<DateTimeInterval> visible = new ArrayList<DateTimeInterval>(3);
  /** transit date */
  private Date transitDate;
  /** sampled target position */
  private final List<TargetPositionDate> targetPositions = new ArrayList<TargetPositionDate>(8);

  /**
   * Constructor
   * @param targetName target name
   * @param type type of observability
   */
  public StarObservabilityData(final String targetName, final int type) {
    this(targetName, null, type);
  }

  /**
   * Constructor
   * @param targetName target name
   * @param info additional information on data (moon, rise/set, horizon, base line ...)
   * @param type type of observability
   */
  public StarObservabilityData(final String targetName, final String info, final int type) {
    this.targetName = targetName;
    this.info = info;
    this.type = type;
  }

  /**
   * Return the name of the target
   * @return name of the target
   */
  public String getTargetName() {
    return targetName;
  }

  /**
   * Return the additional information on data (rise/set, horizon, base line ...)
   * @return additional information on data (rise/set, horizon, base line ...)
   */
  public String getInfo() {
    return info;
  }

  /**
   * Return the legend label (Science or Calibrator target else return additional information on data)
   * @param idx type to use
   * @return string representing this type
   */
  public String getLegendLabel(final int idx) {
    switch (idx) {
      case TYPE_STAR:
        return "Science";
      case TYPE_CALIBRATOR:
        return "Calibrator";
      default:
    }
    return this.getInfo();
  }

  /**
   * Return the type of data
   * @return type of data
   */
  public int getType() {
    return type;
  }

  /**
   * Return the visible date intervals
   * @return visible date intervals
   */
  public List<DateTimeInterval> getVisible() {
    return visible;
  }

  /**
   * Return the transit date
   * @return transit date
   */
  public Date getTransitDate() {
    return transitDate;
  }

  /**
   * Define the transit date
   * @param transitDate transit date
   */
  public void setTransitDate(final Date transitDate) {
    this.transitDate = transitDate;
  }

  /**
   * Return the sampled target position
   * @return sampled target position
   */
  public List<TargetPositionDate> getTargetPositions() {
    return targetPositions;
  }

  @Override
  public String toString() {
    return getTargetName() + " " + ((this.info != null) ? this.info : "") + " : " + getVisible();
  }
}

/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ObservationVersion.java,v 1.1 2011-02-02 17:41:22 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.aspro.model;

/**
 * This class handles observation versioning (target listn main, uv).
 *
 * Note: no synchronization needed as this class is always used by Swing EDT (single threaded)
 *
 * @author bourgesl
 */
public final class ObservationVersion {

  /* members */
  /** target list version (only for debugging purposes) */
  private int targetVersion;
  /** observation main version */
  private int mainVersion;
  /** observation UV version (only for debugging purposes) */
  private int uvVersion;

  /**
   * Create an new Observation Version (versions are set to 0)
   */
  public ObservationVersion() {
    this.targetVersion = 0;
    this.mainVersion = 0;
    this.uvVersion = 0;
  }

  /**
   * Copy the given Observation Version (versions are copied)
   * @param source Observation Version to copy
   */
  public ObservationVersion(final ObservationVersion source) {
    this.targetVersion = source.getTargetVersion();
    this.mainVersion = source.getMainVersion();
    this.uvVersion = source.getUVVersion();
  }

  /**
   * Return the target list version
   * @return target list version
   */
  public final int getTargetVersion() {
    return this.targetVersion;
  }

  /**
   * Increment the target list version
   */
  public final void incTargetVersion() {
    this.targetVersion++;
  }

  /**
   * Return the observation main version
   * @return observation main version
   */
  public final int getMainVersion() {
    return this.mainVersion;
  }

  /**
   * Increment the observation main version
   */
  public final void incMainVersion() {
    this.mainVersion++;
  }

  /**
   * Return the observation UV version
   * @return observation UV version
   */
  public final int getUVVersion() {
    return this.uvVersion;
  }

  /**
   * Increment the observation UV version
   */
  public final void incUVVersion() {
    this.uvVersion++;
  }

  /* version checking */

  /**
   * Return true only if the current main version is equal to the main version of the other version object
   * @param otherVersion observation version to compare with
   * @return true if main versions are equals
   */
  public final boolean isSameMainVersion(final ObservationVersion otherVersion) {
    return this.getMainVersion() == otherVersion.getMainVersion();
  }

  /**
   * Return a string giving versions for debugging purposes
   * @return versions as string
   */
  @Override
  public final String toString() {
    return "{targetVersion=" + getTargetVersion() + ", version=" + getMainVersion() + ", uvVersion=" + getUVVersion() + "}";
  }
}

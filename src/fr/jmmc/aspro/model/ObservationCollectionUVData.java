/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ObservationCollectionUVData.java,v 1.1 2011-02-28 17:12:51 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.aspro.model;

import fr.jmmc.aspro.model.observability.ObservabilityData;
import fr.jmmc.aspro.model.oi.ObservationCollection;
import fr.jmmc.aspro.model.uvcoverage.UVCoverageData;
import fr.jmmc.mcs.model.UVMapData;
import fr.jmmc.oitools.model.OIFitsFile;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extends the observation collection to have observability, UV Coverage and UV Map map data
 * @author bourgesl
 */
public final class ObservationCollectionUVData extends ObservationCollectionObsData {

  /** UV Coverage data */
  private final List<UVCoverageData> uvDataList;
  /** uv map data */
  private UVMapData uvMapData = null;
  /** warning container (merged) */
  private WarningContainer warningContainer;
  /** oifits structure (merged) : TODO implement merge OIFITS */
  private OIFitsFile oiFitsFile;

  /**
   * Public constructor : copy information from the given observation collection (by reference)
   * @param obsCollection observation collection to copy
   * @param obsDataList observability data
   * @param uvDataList UV Coverage data
   */
  public ObservationCollectionUVData(final ObservationCollection obsCollection,
                                     final List<ObservabilityData> obsDataList, final List<UVCoverageData> uvDataList) {
    super(obsCollection, obsDataList);
    this.uvDataList = uvDataList;
  }

  /**
   * Return the UV Coverage data
   * @return UV Coverage data
   */
  public List<UVCoverageData> getUVDataList() {
    return this.uvDataList;
  }

  /**
   * Return the first UV Coverage data
   * @return first UV Coverage data
   */
  public UVCoverageData getFirstUVData() {
    return this.getUVDataList().get(0);
  }

  /**
   * Return the target name corresponding to this data collection
   * @return target name
   */
  public final String getTargetName() {
    return getFirstUVData().getTargetName();
  }

  /**
   * Return the UV Map data
   * @return UV Map data
   */
  public UVMapData getUVMapData() {
    return this.uvMapData;
  }

  /**
   * Define the UV Map data
   * @param uvMapData UV Map data
   */
  public void setUvMapData(final UVMapData uvMapData) {
    this.uvMapData = uvMapData;
  }

  /**
   * Return the warning container (merged)
   * @return warning container (merged)
   */
  public WarningContainer getWarningContainer() {
    return warningContainer;
  }

  /**
   * Define the warning container (merged)
   * @param warningContainer warning container (merged)
   */
  public void setWarningContainer(final WarningContainer warningContainer) {
    this.warningContainer = warningContainer;
  }

  /**
   * Return the oifits structure (merged)
   * @return oifits structure (merged)
   */
  public OIFitsFile getOiFitsFile() {
    return oiFitsFile;
  }

  /**
   * Define the oifits structure (merged)
   * @param oiFitsFile oifits structure (merged)
   */
  public void setOiFitsFile(final OIFitsFile oiFitsFile) {
    this.oiFitsFile = oiFitsFile;
  }
}
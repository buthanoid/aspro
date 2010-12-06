package fr.jmmc.aspro.model.oi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.aspro.model.OIBase;

/**
 * 
 *         This type describes the user information related to all targets.
 *       
 * 
 * <p>Java class for TargetUserInformations complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TargetUserInformations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="calibrators" type="{http://www.w3.org/2001/XMLSchema}IDREFS" minOccurs="0"/>
 *         &lt;element name="targetInfo" type="{http://www.jmmc.fr/aspro-oi/0.1}TargetInformation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetUserInformations", propOrder = {
  "calibrators",
  "targetInfos"
})
public class TargetUserInformations
        extends OIBase {

  @XmlList
  @XmlElement(type = Object.class)
  @XmlIDREF
  @XmlSchemaType(name = "IDREFS")
  protected List<Target> calibrators;
  @XmlElement(name = "targetInfo")
  protected List<TargetInformation> targetInfos;

  /**
   * Gets the value of the calibrators property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the calibrators property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getCalibrators().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Object }
   *
   *
   */
  public List<Target> getCalibrators() {
    if (calibrators == null) {
      calibrators = new ArrayList<Target>();
    }
    return this.calibrators;
  }

  /**
   * Gets the value of the targetInfos property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the targetInfos property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getTargetInfos().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link TargetInformation }
   *
   *
   */
  public List<TargetInformation> getTargetInfos() {
    if (targetInfos == null) {
      targetInfos = new ArrayList<TargetInformation>();
    }
    return this.targetInfos;
  }

//--simple--preserve
  @Override
  public final String toString() {
    return "TargetUserInformations : \ncalibrators : " + getCalibrators() + "\ntargets : " + getTargetInfos();
  }

  /**
   * Return true if the given target is a calibrator 
   * i.e. the calibrator list contains the given target 
   * @param target target to use
   * @return true if the given target is a calibrator 
   */
  public final boolean isCalibrator(final Target target) {
    return this.getCalibrators().contains(target);
  }

  /**
   * Return the target user information corresponding to the target
   * or create a new instance if the target is missing
   * @param target target
   * @return target user information
   */
  public final TargetInformation getTargetUserInformation(final Target target) {
    for (TargetInformation targetInfo : getTargetInfos()) {
      if (targetInfo.getTargetRef().equals(target)) {
        return targetInfo;
      }
    }
    // create a new instance if the target is not found :
    final TargetInformation targetInfo = new TargetInformation();
    targetInfo.setTargetRef(target);
    getTargetInfos().add(targetInfo);
    return targetInfo;
  }

  /*
  TODO : prune TargetInformation orphans
   */
  /**
   * Return a deep "copy" of this instance
   * @return deep "copy" of this instance
   */
  @Override
  public final Object clone() {
    final TargetUserInformations copy = (TargetUserInformations) super.clone();

    // note : targets are not cloned as only there (immutable) identifier is useful
    // see  : updateTargetReferences(Map<ID, Target>) to replace target instances to have a clean object graph
    // i.e. (no leaking references)

    // Simple copy of calibrators (Target instances) :
    if (copy.calibrators != null) {
      copy.calibrators = OIBase.copyList(copy.calibrators);
    }

    // Deep copy of target informations :
    if (copy.targetInfos != null) {
      copy.targetInfos = OIBase.deepCopyList(copy.targetInfos);
    }

    return copy;
  }

  /**
   * Check bad references and update target references in this instance using the given Map<ID, Target> index
   * @param mapIDTargets Map<ID, Target> index
   */
  protected final void updateTargetReferences(final java.util.Map<String, Target> mapIDTargets) {

    if (this.calibrators != null) {
      Target target, newTarget;

      for (final java.util.ListIterator<Target> it = this.calibrators.listIterator(); it.hasNext();) {
        target = it.next();

        newTarget = mapIDTargets.get(target.getIdentifier());
        if (newTarget != null) {
          if (newTarget != target) {
            it.set(newTarget);
          }
        } else {
          logger.info("Removing missing target reference '" + target.getIdentifier() + "'.");
          it.remove();
        }
      }
    }

    if (this.targetInfos != null) {
      TargetInformation targetInfo;
      Target target, newTarget;

      for (final java.util.ListIterator<TargetInformation> it = this.targetInfos.listIterator(); it.hasNext();) {
        targetInfo = it.next();

        target = targetInfo.getTargetRef();

        if (target == null) {
          logger.info("Removing invalid target reference.");
          it.remove();
        } else {
          newTarget = mapIDTargets.get(target.getIdentifier());
          if (newTarget != null) {
            if (newTarget != target) {
              targetInfo.setTargetRef(newTarget);
            }

            targetInfo.updateTargetReferences(mapIDTargets);
          } else {
            logger.info("Removing missing target reference '" + target.getIdentifier() + "'.");
            it.remove();
          }
        }
      }
    }
  }
//--simple--preserve
}
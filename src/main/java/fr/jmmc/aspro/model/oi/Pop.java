
package fr.jmmc.aspro.model.oi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fr.jmmc.aspro.model.OIBase;


/**
 * 
 *                 This type describes a Pipe of Pan (PoP) present in the interferometer.
 *                 This only defines the identifier / name of the PoP that is then used in
 *                 the station definition to give the fixed delay due to the PoP.
 *             
 * 
 * <p>Java class for Pop complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Pop"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}ID"/&gt;
 *         &lt;element name="index" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Pop", propOrder = {
    "name",
    "index"
})
public class Pop
    extends OIBase
{

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String name;
    protected int index;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the index property.
     * 
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     */
    public void setIndex(int value) {
        this.index = value;
    }
    
//--simple--preserve

  @Override
  public final String toString() {
    return this.name;
  }

  /**
   * Return a string containing only the pop identifiers
   * @param popList PoP1 ... PoPN
   * @return string like '1...N'
   */
  public static String toString(final java.util.List<Pop> popList) {
    return toString(new StringBuilder(popList.size()), popList);
  }
  
  /**
   * Return a string containing only the pop identifiers
   * @param sb string builder used to build the identifier (not empty when exiting this method)
   * @param popList PoP1 ... PoPN
   * @return string like '1...N'
   */
  public static String toString(final StringBuilder sb, final java.util.List<Pop> popList) {
    for (Pop pop : popList) {
      sb.append(pop.getIndex());
    }
    return sb.toString();
  }

  /**
   * Return a string containing only the pop identifiers
   * @param sb string builder used to build the identifier (not empty when exiting this method)
   * @param pops PoP1 ... PoPN
   * @return string like '1...N'
   */
  public static String toString(final StringBuilder sb, final Pop[] pops) {
    for (Pop pop : pops) {
      sb.append(pop.getIndex());
    }
    return sb.toString();
  }
  
//--simple--preserve

}

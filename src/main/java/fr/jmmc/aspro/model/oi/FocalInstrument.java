
package fr.jmmc.aspro.model.oi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import fr.jmmc.aspro.model.OIBase;


/**
 * 
 *                 This type describes a focal instrument (AMBER, MIDI ...)
 *             
 * 
 * <p>Java class for FocalInstrument complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FocalInstrument"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}ID"/&gt;
 *         &lt;element name="alias" type="{http://www.w3.org/2001/XMLSchema}NCName" minOccurs="0"/&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="oiVis" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="oiVisData" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="oiVisAmpDiff" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="oiVisPhiDiff" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="oiVis2Extra" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="fringeTracker" type="{http://www.w3.org/2001/XMLSchema}IDREF" minOccurs="0"/&gt;
 *         &lt;element name="fringeTrackerRequired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="setup" type="{http://www.jmmc.fr/aspro-oi/0.1}FocalInstrumentSetup" maxOccurs="unbounded"/&gt;
 *         &lt;element name="mode" type="{http://www.jmmc.fr/aspro-oi/0.1}FocalInstrumentMode" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FocalInstrument", propOrder = {
    "name",
    "alias",
    "description",
    "oiVis",
    "oiVisData",
    "oiVisAmpDiff",
    "oiVisPhiDiff",
    "oiVis2Extra",
    "fringeTracker",
    "fringeTrackerRequired",
    "setups",
    "modes"
})
public class FocalInstrument
    extends OIBase
{

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String name;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String alias;
    @XmlElement(required = true)
    protected String description;
    protected Boolean oiVis;
    protected Boolean oiVisData;
    protected Boolean oiVisAmpDiff;
    protected Boolean oiVisPhiDiff;
    protected Boolean oiVis2Extra;
    @XmlElement(type = Object.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected FringeTracker fringeTracker;
    protected Boolean fringeTrackerRequired;
    @XmlElement(name = "setup", required = true)
    protected List<FocalInstrumentSetup> setups;
    @XmlElement(name = "mode")
    protected List<FocalInstrumentMode> modes;

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
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the oiVis property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOiVis() {
        return oiVis;
    }

    /**
     * Sets the value of the oiVis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOiVis(Boolean value) {
        this.oiVis = value;
    }

    /**
     * Gets the value of the oiVisData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOiVisData() {
        return oiVisData;
    }

    /**
     * Sets the value of the oiVisData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOiVisData(Boolean value) {
        this.oiVisData = value;
    }

    /**
     * Gets the value of the oiVisAmpDiff property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOiVisAmpDiff() {
        return oiVisAmpDiff;
    }

    /**
     * Sets the value of the oiVisAmpDiff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOiVisAmpDiff(Boolean value) {
        this.oiVisAmpDiff = value;
    }

    /**
     * Gets the value of the oiVisPhiDiff property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOiVisPhiDiff() {
        return oiVisPhiDiff;
    }

    /**
     * Sets the value of the oiVisPhiDiff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOiVisPhiDiff(Boolean value) {
        this.oiVisPhiDiff = value;
    }

    /**
     * Gets the value of the oiVis2Extra property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOiVis2Extra() {
        return oiVis2Extra;
    }

    /**
     * Sets the value of the oiVis2Extra property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOiVis2Extra(Boolean value) {
        this.oiVis2Extra = value;
    }

    /**
     * Gets the value of the fringeTracker property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public FringeTracker getFringeTracker() {
        return fringeTracker;
    }

    /**
     * Sets the value of the fringeTracker property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setFringeTracker(FringeTracker value) {
        this.fringeTracker = value;
    }

    /**
     * Gets the value of the fringeTrackerRequired property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFringeTrackerRequired() {
        return fringeTrackerRequired;
    }

    /**
     * Sets the value of the fringeTrackerRequired property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFringeTrackerRequired(Boolean value) {
        this.fringeTrackerRequired = value;
    }

    /**
     * Gets the value of the setups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the setups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSetups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FocalInstrumentSetup }
     * 
     * 
     */
    public List<FocalInstrumentSetup> getSetups() {
        if (setups == null) {
            setups = new ArrayList<FocalInstrumentSetup>();
        }
        return this.setups;
    }

    /**
     * Gets the value of the modes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FocalInstrumentMode }
     * 
     * 
     */
    public List<FocalInstrumentMode> getModes() {
        if (modes == null) {
            modes = new ArrayList<FocalInstrumentMode>();
        }
        return this.modes;
    }
    
//--simple--preserve
    /** lower number of channels of this instrument */
    @javax.xml.bind.annotation.XmlTransient
    protected int numberChannelsMin;

    /**
     * @return lower number of channels of this instrument
     */
    public int getNumberChannelsMin() {
        return numberChannelsMin;
    }

    /**
     * Define the lower number of channels of this instrument
     * @param value lower number of channels of this instrument
     */
    public void setNumberChannelsMin(int value) {
        this.numberChannelsMin = value;
    }

    /** higher number of channels of this instrument */
    @javax.xml.bind.annotation.XmlTransient
    protected int numberChannelsMax;

    /**
     * @return higher number of channels of this instrument
     */
    public int getNumberChannelsMax() {
        return numberChannelsMin;
    }

    /**
     * Define the higher number of channels of this instrument
     * @param value higher number of channels of this instrument
     */
    public void setNumberChannelsMax(int value) {
        this.numberChannelsMin = value;
    }

    /** lower wave length of this instrument (micrometer) */
    @javax.xml.bind.annotation.XmlTransient
    protected double waveLengthMin;

    /**
     * Return the lower wave length of this instrument (micrometer)
     * @return lower wave length of this instrument (micrometer)
     */
    public final double getWaveLengthMin() {
        return waveLengthMin;
    }

    /**
     * Define the lower wave length of this instrument (micrometer)
     * @param waveLengthMin lower wave length of this instrument (micrometer)
     */
    private final void setWaveLengthMin(final double waveLengthMin) {
        this.waveLengthMin = waveLengthMin;
    }
    /** upper wave length of this instrument (micrometer) */
    @javax.xml.bind.annotation.XmlTransient
    protected double waveLengthMax;

    /**
     * Return the upper wave length of this instrument (micrometer)
     * @return upper wave length of this instrument (micrometer)
     */
    public final double getWaveLengthMax() {
        return waveLengthMax;
    }

    /**
     * Define the upper wave length of this instrument (micrometer)
     * @param waveLengthMax upper wave length of this instrument (micrometer)
     */
    private final void setWaveLengthMax(final double waveLengthMax) {
        this.waveLengthMax = waveLengthMax;
    }

    /**
     * Define the instrument wavelength range
     */
    public void defineWaveLengthRange() {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (FocalInstrumentMode mode : getModes()) {
            if (mode.getWaveLengthMin() < min) {
                min = mode.getWaveLengthMin();
            }
            if (mode.getWaveLengthMax() > max) {
                max = mode.getWaveLengthMax();
            }
        }
        setWaveLengthMin(min);
        setWaveLengthMax(max);
    }

    /**
     * Return the alias if defined or the name
     * @return alias or name
     */
    public String getAliasOrName() {
        return (alias != null) ? alias : name;
    }

    @Override
    public final String toString() {
        return "FocalInstrument[" + ((this.name != null) ? this.name : "undefined") + "]";
    }

    /**
     * Initialize and check this instance
     * @param logger logger to use
     * @throws IllegalStateException if the configuration is severly invalid !
     */
    public void init(final org.slf4j.Logger logger) throws IllegalStateException {
        if (this.name == null) {
            throw new IllegalStateException("Invalid name !");
        }

        if (isOiVis() == null) {
            setOiVis(Boolean.TRUE); // true by default
        }
        if (isOiVisData() == null) {
            setOiVisData(Boolean.FALSE); // false by default
        }
        if (isOiVisAmpDiff() == null) {
            setOiVisAmpDiff(Boolean.FALSE); // false by default
        }
        if (isOiVisPhiDiff() == null) {
            setOiVisPhiDiff(Boolean.FALSE); // false by default
        }
        if (isOiVis2Extra() == null) {
            setOiVis2Extra(Boolean.FALSE); // false by default
        }

        if (isEmpty(this.setups)) {
            throw new IllegalStateException("Missing setup !");
        }

        // TODO: check setupIds unicity (map) ?
        // Check setups:
        for (FocalInstrumentSetup setup : getSetups()) {
            setup.init(logger);
        }

        // Use first setup as default:
        final boolean singleSetup = getSetups().size() == 1;
        final FocalInstrumentSetup defSetup = getSetups().get(0);

        // Check modes:
        for (FocalInstrumentMode insMode : getModes()) {
            insMode.init(logger);

            if (insMode.getSetupRef() == null) {
                // Define the setup to the default setup:
                insMode.setSetupRef(defSetup);
                if (!singleSetup) {
                    logger.warn("Missing setupRef in the instrument mode [" + insMode.getName() + "] "
                            + "of the instrument [" + this.getName() + "] (several setups defined) !");
                }
            } else {
                // check if the setup exists and belongs to this instrument setups:
                if (!getSetups().contains(insMode.getSetupRef())) {
                    throw new IllegalStateException("Invalid setupRef[" + insMode.getSetupRef().getName() + "] "
                            + "defined in the instrument mode [" + insMode.getName() + "] "
                            + "of the instrument [" + this.getName() + "] !");
                }
            }
        }
    }

    public void dump(final org.slf4j.Logger logger) {
        logger.info("Instrument[{}] {", getName());
        logger.info("  name: {}", getName());
        logger.info("  alias: {}", getAlias());
        logger.info("  description: {}", getDescription());

        logger.info("  numberChannelsMin: {}", getNumberChannelsMin());
        logger.info("  numberChannelsMax: {}", getNumberChannelsMax());

        logger.info("  oiVis:        {}", isOiVis());
        logger.info("  oiVisData:    {}", isOiVisData());
        logger.info("  oiVisAmpDiff: {}", isOiVisAmpDiff());
        logger.info("  oiVisPhiDiff: {}", isOiVisPhiDiff());
        logger.info("  oiVis2Extra:  {}", isOiVis2Extra());

        logger.info("  fringeTracker: {}", getFringeTracker());
        logger.info("  fringeTrackerRequired: {}", isFringeTrackerRequired());

        // computed values:
        logger.info("  waveLengthMin: {}", fr.jmmc.jmcs.util.NumberUtils.trimTo5Digits(getWaveLengthMin()));
        logger.info("  waveLengthMax: {}", fr.jmmc.jmcs.util.NumberUtils.trimTo5Digits(getWaveLengthMax()));

        for (FocalInstrumentSetup setup : getSetups()) {
            setup.dump(logger);
        }
        for (FocalInstrumentMode insMode : getModes()) {
            insMode.dump(logger);
        }

        logger.info("}");
    }

    /**
     * Return the instrument setup of the given identifier in the given list of instrument setups
     * @param id target identifier
     * @param setups list of instrument setups
     * @return instrument setup or null if the instrument setup was not found
     */
    public static FocalInstrumentSetup getSetupById(final String id, final List<FocalInstrumentSetup> setups) {
        if (id != null) {
            for (FocalInstrumentSetup s : setups) {
                if (s.getName().equals(id)) {
                    return s;
                }
            }
        }
        return null;
    }
//--simple--preserve

}

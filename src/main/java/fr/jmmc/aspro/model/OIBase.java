/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for all generated classes in model.oi (Optical Interferometry Data Model)
 * @author bourgesl
 */
@XmlTransient
public class OIBase implements Cloneable {

    /** Class logger for aspro model classes */
    protected static final Logger logger = LoggerFactory.getLogger("fr.jmmc.aspro.model");

    /**
     * Public Constructor
     */
    public OIBase() {
        super();
    }

    /**
     * Return by default the simple class name
     * @return simple class name
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Return a "shallow copy" of this instance
     * @return "shallow copy" of this instance
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
        }
        return null;
    }

    /**
     * Return a deep "copy" of the list of objects (recursive call to clone() on each object instance)
     * @param <K> OIBase child class
     * @param list list of objects to clone
     * @return deep "copy" of the list
     */
    @SuppressWarnings("unchecked")
    public static final <K extends OIBase> List<K> deepCopyList(final List<K> list) {
        if (list != null) {
            final List<K> newList = new ArrayList<K>(list.size());
            for (K o : list) {
                newList.add((K) o.clone());
            }
            return newList;
        }
        return null;
    }

    /**
     * Return a simple "copy" of the list of objects without cloning each object instance
     * @param <K> OIBase child class
     * @param list list of objects to clone
     * @return deep "copy" of the list
     */
    @SuppressWarnings("unchecked")
    public static final <K extends OIBase> List<K> copyList(final List<K> list) {
        if (list != null) {
            final List<K> newList = new ArrayList<K>(list.size());
            for (K o : list) {
                newList.add(o);
            }
            return newList;
        }
        return null;
    }

    /**
     * Utility method for <code>equals()</code> methods.
     *
     * @param o1 one object
     * @param o2 another object
     *
     * @return <code>true</code> if they're both <code>null</code> or both equal
     */
    public static final boolean areEquals(final Object o1, final Object o2) {
        return (o1 == o2) || ((o1 != null) && o1.equals(o2));
    }

    /**
     * @param value value to check
     * @return true if the given value is NaN
     */
    public static final boolean isNaN(final Float value) {
        return (value != null && Float.isNaN(value));
    }

    /**
     * @param value value to check
     * @return true if the given value is NaN
     */
    public static final boolean isNaN(final Double value) {
        return (value != null && Double.isNaN(value));
    }
    
    /**
     * Test if value is empty (no chars)
     * 
     * @param value string value
     * @return true if value is empty (null or no chars)
     */
    public static boolean isEmpty(final String value) {
        return value != null && value.isEmpty();
    }
}

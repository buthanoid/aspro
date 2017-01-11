/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.model.util;

import fr.jmmc.aspro.AsproConstants;
import fr.jmmc.aspro.model.oi.Target;
import fr.jmmc.jmal.ALX;
import fr.jmmc.jmal.CoordUtils;
import fr.jmmc.jmal.star.Star;
import fr.jmmc.jmcs.util.StringUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public final class TargetUtils {
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(TargetUtils.class.getName());

    /** distance in degrees to consider same targets = 1 arcsecs */
    public final static double SAME_TARGET_DISTANCE = 1d * fr.jmmc.jmal.ALX.ARCSEC_IN_DEGREES;

    /**
     * Forbidden constructor
     */
    private TargetUtils() {
        // no-op
    }

    /**
     * Fix RA: parse given value as HMS and re-format to HMS (normalization)
     * @param ra right ascension as HMS
     * @return right ascension as HMS
     */
    public static String fixRA(final String ra) {
        return ALX.toHMS(ALX.parseHMS(ra));
    }

    /**
     * Fix DEC: parse given value as DMS and re-format to DMS (normalization)
     * @param dec declination as DMS
     * @return declination as DMS
     */
    public static String fixDEC(final String dec) {
        return ALX.toDMS(ALX.parseDEC(dec));
    }

    /**
     * Check the distance between the given source target and the given list of targets (5 arcesecs)
     * @param srcTarget source target
     * @param targets list of targets
     * @return Target if found or null
     * @throws IllegalArgumentException if the target is too close to another target present in the given list of targets
     */
    public static TargetMatch matchTargetCoordinates(final Target srcTarget, final List<Target> targets) throws IllegalArgumentException {
        final double srcRaDeg = srcTarget.getRADeg();
        final double srcDecDeg = srcTarget.getDECDeg();

        double distance, min = Double.MAX_VALUE;
        Target match = null;

        for (Target target : targets) {
            distance = CoordUtils.computeDistanceInDegrees(srcRaDeg, srcDecDeg, target.getRADeg(), target.getDECDeg());

            if (distance <= SAME_TARGET_DISTANCE) {
                // check simbad identifiers to avoid false-positives:
                Boolean check = matchTargetIds(srcTarget, target);
                if (check != null) {
                    // only 1 common identifier may be enough (depends on the catalog accuracy)
                    // TODO: to be confirmed ?
                    if (check.booleanValue()) {
                        return new TargetMatch(target);
                    } else {
                        // no common identifier at all => skip to ignore that target
                        continue;
                    }
                }

                // keep closest target:
                if (distance < min) {
                    min = distance;
                    match = target;
                }
            }
        }
        if (match != null) {
            // return the closest match:
            return new TargetMatch(match, min);
        }
        return null;
    }

    public static Boolean matchTargetIds(final Target src, final Target other) {
        final String sIds = src.getIDS();
        final String oIds = other.getIDS();

        if (sIds != null && oIds != null) {
            // exactly the same identifiers (simbad):
            if (sIds.equals(oIds)) {
                return Boolean.TRUE;
            }
            final String[] sIdArray = sIds.split(",");
            final String[] oIdArray = oIds.split(",");

            for (String s : sIdArray) {
                for (String o : oIdArray) {
                    if (s.equals(o)) {
                        logger.info("match[{}] from ids [{}] x [{}]", s, sIds, oIds);
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Convert the given Star instance to a Target instance
     * @param star star instance
     * @return new Target instance
     */
    public static Target convert(final Star star) {
        if (star == null || StringUtils.isEmpty(star.getName())) {
            return null;
        }
        /* 
         Star data:
         Strings = {DEC=+43 49 23.910, RA=05 01 58.1341, OTYPELIST=**,Al*,SB*,*,Em*,V*,IR,UV, SPECTRALTYPES=A8Iab:}
         Doubles = {PROPERMOTION_RA=0.18, PARALLAX=1.6, DEC_d=43.8233083, FLUX_J=1.88, PROPERMOTION_DEC=-2.31, FLUX_K=1.533, PARALLAX_err=1.16, FLUX_V=3.039, FLUX_H=1.702, RA_d=75.4922254}
         */
        final Target newTarget = new Target();
        // format the target name:
        newTarget.updateNameAndIdentifier(star.getName());

        // coordinates (HMS / DMS) (mandatory):
        newTarget.setCoords(
                star.getPropertyAsString(Star.Property.RA).replace(' ', ':'),
                star.getPropertyAsString(Star.Property.DEC).replace(' ', ':'),
                AsproConstants.EPOCH_J2000);

        // Proper motion (mas/yr) (optional) :
        newTarget.setPMRA(star.getPropertyAsDouble(Star.Property.PROPERMOTION_RA));
        newTarget.setPMDEC(star.getPropertyAsDouble(Star.Property.PROPERMOTION_DEC));

        // Parallax (mas) (optional) :
        newTarget.setPARALLAX(star.getPropertyAsDouble(Star.Property.PARALLAX));
        newTarget.setPARAERR(star.getPropertyAsDouble(Star.Property.PARALLAX_err));

        // Magnitudes (optional) :
        newTarget.setFLUXB(star.getPropertyAsDouble(Star.Property.FLUX_B));
        newTarget.setFLUXV(star.getPropertyAsDouble(Star.Property.FLUX_V));
        newTarget.setFLUXR(star.getPropertyAsDouble(Star.Property.FLUX_R));
        newTarget.setFLUXI(star.getPropertyAsDouble(Star.Property.FLUX_I));
        newTarget.setFLUXJ(star.getPropertyAsDouble(Star.Property.FLUX_J));
        newTarget.setFLUXH(star.getPropertyAsDouble(Star.Property.FLUX_H));
        newTarget.setFLUXK(star.getPropertyAsDouble(Star.Property.FLUX_K));
        // LMN magnitudes are missing in Simbad !

        // Spectral types :
        newTarget.setSPECTYP(star.getPropertyAsString(Star.Property.SPECTRALTYPES));

        // Object types :
        newTarget.setOBJTYP(star.getPropertyAsString(Star.Property.OTYPELIST));

        // Radial velocity (km/s) (optional) :
        newTarget.setSYSVEL(star.getPropertyAsDouble(Star.Property.RV));
        newTarget.setVELTYP(star.getPropertyAsString(Star.Property.RV_DEF));

        // Identifiers :
        newTarget.setIDS(star.getPropertyAsString(Star.Property.IDS));

        // fix NaN / null values:
        newTarget.checkValues();

        return newTarget;
    }
}

/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.aspro.model;

import fr.jmmc.jmcs.util.ObjectUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple range of double values
 * @author bourgesl
 */
public final class Range {

    /** minimum value */
    private double min;
    /** maximum value */
    private double max;

    /**
     * Constructor
     */
    public Range() {
        this(0d, 0d);
    }

    /**
     * Constructor with given minimum and maximum value
     * @param min minimum value
     * @param max maximum value
     */
    public Range(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Update with given minimum and maximum value
     * @param min minimum value
     * @param max maximum value
     */
    public void set(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Return the minimum value
     * @return minimum value
     */
    public double getMin() {
        return min;
    }

    /**
     * Set the minimum value
     * @param min minimum value
     */
    public void setMin(final double min) {
        this.min = min;
    }

    /**
     * Return the maximum value
     * @return maximum value
     */
    public double getMax() {
        return max;
    }

    /**
     * Set the maximum value
     * @param max maximum value
     */
    public void setMax(final double max) {
        this.max = max;
    }

    /**
     * Return the center value
     * @return center value
     */
    public double getCenter() {
        return 0.5d * (this.min + this.max);
    }

    /**
     * Return the length (max - min)
     * @return length
     */
    public double getLength() {
        return this.max - this.min;
    }

    /**
     * Return true if this range contains the given value (including bounds)
     * @param value value to check
     * @return true if this range contains the given value
     */
    public boolean contains(final double value) {
        return value >= this.min && value <= this.max;
    }

    /**
     * Return true if this range contains the given value (including bounds and given precision error)
     * @param value value to check
     * @param err precision error against range boundaries
     * @return true if this range contains the given value
     */
    public boolean contains(final double value, final double err) {
        return value >= (this.min - err) && value <= (this.max + err);
    }

    /**
     * Returns true if this range equals the given range
     * @param obj another object instance
     * @return true if this range equals the given range
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // identity check:
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Range other = (Range) obj;
        if (this.min != other.getMin()) {
            return false;
        }
        if (this.max != other.getMax()) {
            return false;
        }
        return true;
    }

    /**
     * Return a string representation of this range
     * @return string representation of this range
     */
    @Override
    public String toString() {
        return "[" + this.min + ", " + this.max + "]";
    }

    /* --- Utility methods ---------------------------------------------------- */
    /**
     * Return true if both range list are equals
     * @param ranges list of ranges
     * @param otherRanges other list of ranges
     * @return true if both range list are equals
     */
    public static boolean equals(final List<Range> ranges, final List<Range> otherRanges) {
        if (ranges == otherRanges) {
            // identity or both nulls:
            return true;
        }
        if ((ranges == null && otherRanges != null) || (ranges != null && otherRanges == null)) {
            return false;
        }
        // not nulls:
        final int len = ranges.size();
        if (len != otherRanges.size()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            final Range range = ranges.get(i);
            final Range otherRange = otherRanges.get(i);

            if (!ObjectUtils.areEquals(range, otherRange)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if the given value is inside the given list of ranges
     * @param ranges list of ranges
     * @param value value to test
     * @return true if the given value is inside given ranges
     */
    public static boolean contains(final List<Range> ranges, final double value) {
        if (ranges == null) {
            return false;
        }
        return find(ranges, value) != null;
    }

    /**
     * Find the range containing the given value
     * @param ranges list of ranges
     * @param value value to test
     * @return range containing the given value or null
     */
    public static Range find(final List<Range> ranges, final double value) {
        for (int i = 0, len = ranges.size(); i < len; i++) {
            final Range range = ranges.get(i);

            if (range.contains(value)) {
                return range;
            }
        }
        return null;
    }

    /**
     * Find the range containing the given value
     * @param ranges list of ranges
     * @param value value to test
     * @param err precision error against range boundaries
     * @return range containing the given value or null
     */
    public static Range find(final List<Range> ranges, final double value, final double err) {
        for (int i = 0, len = ranges.size(); i < len; i++) {
            final Range range = ranges.get(i);

            if (range.contains(value, err)) {
                return range;
            }
        }
        return null;
    }

    /**
     * Find the minimum value of the given list of ranges
     * @param ranges list of ranges
     * @return minimum value of the given list of ranges
     */
    public static Double getMinimum(final List<Range> ranges) {
        if (ranges != null && !ranges.isEmpty()) {
            double min = Double.POSITIVE_INFINITY;

            for (int i = 0, len = ranges.size(); i < len; i++) {
                final Range range = ranges.get(i);

                if (min > range.getMin()) {
                    min = range.getMin();
                }
            }
            return min;
        }
        return null;
    }

    /**
     * Find the maximum value of the given list of ranges
     * @param ranges list of ranges
     * @return maximum value of the given list of ranges
     */
    public static Double getMaximum(final List<Range> ranges) {
        if (ranges != null && !ranges.isEmpty()) {
            double max = Double.NEGATIVE_INFINITY;

            for (int i = 0, len = ranges.size(); i < len; i++) {
                final Range range = ranges.get(i);

                if (max < range.getMax()) {
                    max = range.getMax();
                }
            }
            return max;
        }
        return null;
    }

    /**
     * Return the list of ranges cropped to stay inside range [min;max]
     * @param ranges list of ranges to use
     * @param min lower bound
     * @param max upper bound
     * @return list of ranges inside range [min;max] or null
     */
    public static List<Range> restrictRange(final List<Range> ranges, final double min, final double max) {
        List<Range> intervals = null;

        Range rangeToAdd;
        double start, end;

        for (int i = 0, len = ranges.size(); i < len; i++) {
            final Range range = ranges.get(i);

            start = range.getMin();
            end = range.getMax();

            rangeToAdd = null;

            if (start >= min) {
                if (end <= max) {
                    // interval in inside [min;max]
                    rangeToAdd = range;
                } else {
                    if (start > max) {
                        // two points over max : skip
                    } else {
                        // end occurs after max :
                        rangeToAdd = new Range(start, max);
                    }
                }
            } else {
                // start occurs before min :
                if (end < min) {
                    // two points before min : skip
                } else if (end > max) {
                    // two points overlapping [min;max] : keep
                    rangeToAdd = new Range(min, max);
                } else {
                    rangeToAdd = new Range(min, end);
                }
            }

            if (rangeToAdd != null) {
                if (intervals == null) {
                    intervals = new ArrayList<Range>(ranges.size());
                }
                intervals.add(rangeToAdd);
            }
        }
        return intervals;
    }

    /**
     * Traverse the given list of date intervals to merge contiguous intervals.
     *
     * @param ranges SORTED ranges to fix
     */
    public static void union(final List<Range> ranges) {
        final int size = ranges.size();
        if (size > 1) {
            Range range1, range2;
            for (int i = size - 2, j = size - 1; i >= 0; i--, j--) {
                range1 = ranges.get(i);
                range2 = ranges.get(j);

                if (range1.getMax() >= range2.getMin()) {
                    // merge interval :
                    range1.setMax(range2.getMax());
                    // remove interval2 :
                    ranges.remove(j);
                }
            }
        }
    }

    /**
     * Intersect overlapping ranges according to the nValid parameter that indicates how many times a point must be inside a range
     * to consider the point as valid
     * @param ranges list of ranges to merge
     * @param nValid number of ranges to consider a point is valid
     * @param rangeFactory Factory used to create Range instances
     * @return new list of ranges
     */
    public static List<Range> intersectRanges(final List<Range> ranges, final int nValid, final RangeFactory rangeFactory) {
        return intersectRanges(ranges, nValid, null, rangeFactory);
    }

    /**
     * Intersect overlapping ranges according to the nValid parameter that indicates how many times a point must be inside a range
     * to consider the point as valid
     * @param ranges list of ranges to merge
     * @param nValid number of ranges to consider a point is valid
     * @param results output list of ranges
     * @param rangeFactory Factory used to create Range instances
     * @return new list of ranges or null
     */
    public static List<Range> intersectRanges(final List<Range> ranges, final int nValid,
                                              final List<Range> results, final RangeFactory rangeFactory) {

        final int len = ranges.size();

        // check if ranges has enough elements to satisfy the valid range condition:
        if (len < nValid) {
            return results;
        }

        // table of start/end time :
        final RangeLimit[] limits = new RangeLimit[2 * len];

        int n = 0;

        for (int i = 0; i < len; i++) {
            final Range range = ranges.get(i);

            limits[n++] = new RangeLimit(range.getMin(), 1);
            limits[n++] = new RangeLimit(range.getMax(), -1);
        }

        return intersectRanges(limits, n, nValid, results, rangeFactory);
    }

    /**
     * Intersect overlapping ranges according to the nValid parameter that indicates how many times a point must be inside a range
     * to consider the point as valid
     * @param limits array of range limits to intersect
     * @param nLimits number of range limits present in the given array
     * @param nValid number of ranges to consider a point is valid
     * @param results output list of ranges
     * @param rangeFactory Factory used to create Range instances
     * @return new list of ranges or null
     */
    public static List<Range> intersectRanges(final RangeLimit[] limits, final int nLimits, final int nValid,
                                              final List<Range> results, final RangeFactory rangeFactory) {

        // check if limits has enough elements to satisfy the valid range condition:
        if (nLimits < (nValid >> 1)) {
            return results;
        }

        // use customized binary sort to be in-place (no memory usage):
        // inspired from Arrays.sort but do not use tim sort as it makes array copies 
        // when size > threshold (~ 20 or 40 depending on JDK):
//        binarySort(limits, 0, nLimits, countRunAndMakeAscending(limits, 0, nLimits));
        sort1(limits, 0, nLimits);

        //  Explore range: when the running sum of flag is equal to the
        //  number nValid, we are in a valid range
        List<Range> mRanges = results;

        for (int i = 0, s = 0, len = nLimits - nValid; i < len; i++) {

            // sum of flags :
            s += limits[i].flag;

            if (s == nValid) {
                // ensure interval is not empty when finding intervals complement (nValid less than maximum)
                if ((limits[i + 1].position - limits[i].position) > 0.0) {
                    if (mRanges == null) {
                        // lazy instanciation (statically 1 range only) :
                        mRanges = new ArrayList<Range>(1);
                    }
                    mRanges.add(rangeFactory.valueOf(limits[i].position, limits[i + 1].position));
                }
            }
        }

        return mRanges;
    }

    // --- Customized QuickSort from Arrays.sort(double[]) OpenJDK 1.8 --------
    /**
     * Sorts the specified sub-array of doubles into ascending order.
     */
    private static void sort1(final RangeLimit[] limits, final int off, final int len) {
        // Insertion sort on smallest arrays
        if (len < 20) { // 7 in JDK for double[]
            for (int i = off; i < len + off; i++) {
                for (int j = i; j > off && limits[j - 1].position > limits[j].position; j--) {
                    swap(limits, j, j - 1);
                }
            }
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(limits, l, l + s, l + 2 * s);
                m = med3(limits, m - s, m, m + s);
                n = med3(limits, n - 2 * s, n - s, n);
            }
            m = med3(limits, l, m, n); // Mid-size, med of 3
        }
        final double v = limits[m].position;

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && limits[b].position <= v) {
                if (limits[b].position == v) {
                    swap(limits, a++, b);
                }
                b++;
            }
            while (c >= b && limits[c].position >= v) {
                if (limits[c].position == v) {
                    swap(limits, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(limits, b++, c--);
        }

        // Swap partition elements back to middle
        int s;
        final int n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(limits, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(limits, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            sort1(limits, off, s);
        }
        if ((s = d - c) > 1) {
            sort1(limits, n - s, s);
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(final RangeLimit[] x, final int a, final int b) {
        final RangeLimit t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(final RangeLimit[] x, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, a, b);
        }
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private static int med3(final RangeLimit[] x, final int a, final int b, final int c) {
        return (x[a].position < x[b].position
                ? (x[b].position < x[c].position ? b : x[a].position < x[c].position ? c : a)
                : (x[b].position > x[c].position ? b : x[a].position > x[c].position ? c : a));
    }

}

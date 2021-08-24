package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Contains static methods operating on 64 bit vectors stored in ints
 * 
 * @author Yingxuan Duan (282512)
 */
public final class Bits64 {

    /**
     * Private constructor to disable creation of instances of this class
     * because it only contains static utility methods
     */
    private Bits64() {
    }

    /**
     * Creates a mask with size ones commencing at start
     * 
     * @param start
     *            (int): index from which ones appear
     * @param size
     *            (int): number of ones in the mask
     * @return (long): a long which has 1s from start (included) to start + size
     *         (excluded) and 0s elsewhere
     */
    public static long mask(int start, int size) {
        // Checks that start and size form a valid bit range
        checkArgument(start >= 0 && start <= Long.SIZE && size >= 0
                && size <= Long.SIZE && start + size <= Long.SIZE);

        long unshiftedMask;
        // Special case: shifting a long by 64 is the same as shifting it by 0,
        // so instead we shift 2 (10 in binary) by 63, which has the desired
        // result
        if (size == Long.SIZE)
            unshiftedMask = - 1L;
        else
            // Creating a value whose binary representation has size 1s as its
            // LSBs
            unshiftedMask = (1L << size) - 1;

        // Shifting those 1s to the correct position
        return unshiftedMask << start;
    }

    /**
     * Extracts a value from start index to start + size
     * 
     * @param bits
     *            (long): from which we will extract the value
     * @param start
     *            (int): index from which we start to extract
     * @param size
     *            (int): length in bits of the value to extract
     * @return (long): the extracted value
     */
    public static long extract(long bits, int start, int size) {
        // Checks that start and size form a valid bit range
        checkArgument(start >= 0 && start <= Long.SIZE && size >= 0
                && size <= Long.SIZE && start + size <= Long.SIZE);

        // Applies a mask to bits to retain only the bits we want, then shifts
        // those to the LSBs (using >>> so that the MSB isn't copied
        // behind)
        long maskedBits = bits & mask(start, size);
        return maskedBits >>> start;
    }

    /**
     * Packs two values together into a long
     * 
     * @param v1
     *            (long): value to pack in the LSBs
     * @param s1
     *            (int): number of bits occupied by v1 in the packed long
     * @param v2
     *            (long): value to put next to v1
     * @param s2
     *            (int): number of bits occupied by v2 in the packed long
     * @return (long): the packed long
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        // Checks that start and size form a valid bit range and that a value
        // doesn't take up more bits than its given size
        checkArgument(verify(v1, s1) && verify(v2, s2) && s1 + s2 <= Long.SIZE);

        v2 <<= s1;
        return v1 | v2;
    }

    /*
     * An auxiliary method which checks that the size is between 1 and 31 (both
     * included) and if the value does not take more place than its size
     * 
     * @param value (long): must not have more than size bits
     * 
     * @param size (int): limits the number of bits in value
     * 
     * @return (boolean): if value and size are valid
     */
    private static boolean verify(long value, int size) {
        return size > 0 && size < Long.SIZE
                && Long.SIZE - Long.numberOfLeadingZeros(value) <= size;
    }
}
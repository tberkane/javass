package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Contains static methods operating on 32 bit vectors stored in ints
 * 
 * @author Yingxuan Duan (282512)
 */
public final class Bits32 {

    /**
     * Private constructor to disable creation of instances of this class
     * because it only contains static utility methods
     */
    private Bits32() {
    }

    /**
     * Creates a mask with size ones commencing at start
     * 
     * @param start
     *            (int): index from which ones appear
     * @param size
     *            (int): number of ones in the mask
     * @return (int): an int which has 1s from start (included) to start + size
     *         (excluded) and 0s elsewhere
     */
    public static int mask(int start, int size) {
        // Checks that start and size form a valid bit range
        checkArgument(start >= 0 && start <= Integer.SIZE && size >= 0
                && size <= Integer.SIZE && start + size <= Integer.SIZE);

        // Creating a value whose binary representation has size 1s as its
        // LSBs (1L is used instead of 1 to bypass the int
        // type's bit shifting limit)
        int unshiftedMask = (int) (1L << size) - 1;

        // Shifting those 1s to the correct position
        return unshiftedMask << start;
    }

    /**
     * Extracts a value from start index to start + size
     * 
     * @param bits
     *            (int): from which we will extract the value
     * @param start
     *            (int): index from which we start to extract
     * @param size
     *            (int): length in bits of the value to extract
     * @return (int): the extracted value
     */
    public static int extract(int bits, int start, int size) {
        // Checks that start and size form a valid bit range
        checkArgument(start >= 0 && start <= Integer.SIZE && size >= 0
                && size <= Integer.SIZE && start + size <= Integer.SIZE);

        // Applies a mask to bits to retain only the bits we need,
        // then shifts those to the LSBs (using >>> so that the MSB isn't copied
        // behind)
        int maskedBits = bits & mask(start, size);
        return maskedBits >>> start;
    }

    /**
     * Packs two values together into an int
     * 
     * @param v1
     *            (int): value to pack in the LSBs
     * @param s1
     *            (int): number of bits occupied by v1 in the packed int
     * @param v2
     *            (int): value to put next to v1
     * @param s2
     *            (int): number of bits occupied by v2 in the packed int
     * @return (int): packed int
     */
    public static int pack(int v1, int s1, int v2, int s2) {
        // Checks that start and size form a valid bit range and that a value
        // doesn't take up more bits than its given size
        checkArgument(
                verify(v1, s1) && verify(v2, s2) && s1 + s2 <= Integer.SIZE);
        return packGeneral(v1, s1, v2, s2);
    }

    /**
     * Packs 3 values together into an int
     * 
     * @param v1
     *            (int): value to pack in the LSBs
     * @param s1
     *            (int): number of bits occupied by v1
     * @param v2
     *            (int): value to put next to v1
     * @param s2
     *            (int): number of bits occupied by v2
     * @param v3
     *            (int): value to put next to v2
     * @param s3
     *            (int): number of bits occupied by v3
     * @return (int): packed int
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        // Checks that start and size form a valid bit range and that a value
        // doesn't take up more bits than its given size
        checkArgument(verify(v1, s1) && verify(v2, s2) && verify(v3, s3)
                && s1 + s2 + s3 <= Integer.SIZE);
        return packGeneral(v1, s1, v2, s2, v3, s3);
    }

    /**
     * Packs 7 values together into an int
     * 
     * @param v1
     *            (int): value to pack in the LSBs
     * @param s1
     *            (int): number of bits occupied by v1
     * @param v2
     *            (int): value to put next to v1
     * @param s2
     *            (int): number of bits occupied by v2
     * @param v3
     *            (int): value to put next to v2
     * @param s3
     *            (int): number of bits occupied by v3
     * @param v4
     *            (int): value to put next to v3
     * @param s4
     *            (int): number of bits occupied by v4
     * @param v5
     *            (int): value to put next to v4
     * @param s5
     *            (int): number of bits occupied by v5
     * @param v6
     *            (int): value to put next to v5
     * @param s6
     *            (int): number of bits occupied by v6
     * @param v7
     *            (int): value to put next to v6
     * @param s7
     *            (int): number of bits occupied by v7
     * @return (int): packed int
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6, int v7, int s7) {
        // Checks that start and size form a valid bit range and that a value
        // doesn't take up more bits than its given size
        checkArgument(verify(v1, s1) && verify(v2, s2) && verify(v3, s3)
                && verify(v4, s4) && verify(v5, s5) && verify(v6, s6)
                && verify(v7, s7)
                && s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);
        return packGeneral(v1, s1, v2, s2, v3, s3, v4, s4, v5, s5, v6, s6, v7,
                s7);
    }

    /*
     * Packs an arbitrary number of values into an int
     * 
     * @param vs (int): contains values to pack and their sizes
     * 
     * @return (int): packed int
     */
    private static int packGeneral(int... vs) {
        int packed = 0;
        int currentSize = 0;

        // Iterating on even elements of vs (values) and packing them into
        // packed
        for (int i = 0; i < vs.length; i += 2) {
            packed |= (vs[i] << currentSize);
            // Adding the odd elements of vs (sizes) to currentSize
            currentSize += vs[i + 1];
        }
        return packed;
    }

    /*
     * An auxiliary method which checks that the size is between 1 and 31 (both
     * included) and if the value does not take more place than its size
     * 
     * @param value (int): must not have more than size bits
     * 
     * @param size (int): limits the number of bits in value
     * 
     * @return (boolean): if value and size are valid
     */
    private static boolean verify(int value, int size) {
        return size > 0 && size < Integer.SIZE
                && Integer.SIZE - Integer.numberOfLeadingZeros(value) <= size;
    }
}

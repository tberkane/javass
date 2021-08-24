package ch.epfl.javass;

/**
 * Class containing utility methods for checking various preconditions on
 * arguments
 * 
 * @author Thomas Berkane (297780)
 */
public final class Preconditions {

    /**
     * Private constructor to disable creation of instances of this class
     * because it only contains static utility methods
     */
    private Preconditions() {
    }

    /**
     * Checks if a condition is verified
     * 
     * @param b
     *            (boolean): boolean condition to check
     * @throws IllegalArgumentException:
     *             if the condition is not true
     */
    public static void checkArgument(boolean b) {
        if (!b)
            throw (new IllegalArgumentException("Invalid argument"));
    }

    /**
     * Checks if the index is valid for the given size
     * 
     * @param index
     *            (int): the index to check
     * @param size
     *            (int): the size to compare the index with
     * @return (int): the index if it is valid
     * @throws IndexOutOfBoundsException:
     *             if the index is invalid
     */
    public static int checkIndex(int index, int size) {
        if (index < 0 || index >= size)
            throw (new IndexOutOfBoundsException("Invalid index"));
        return index;
    }
}

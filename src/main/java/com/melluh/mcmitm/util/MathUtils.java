package com.melluh.mcmitm.util;

public class MathUtils {

    private MathUtils() {}

    public static int smallestEncompassingPowerOfTwo(int val) {
        val -= 1;
        val |= val >> 1;
        val |= val >> 2;
        val |= val >> 4;
        val |= val >> 8;
        val |= val >> 16;
        return val + 1;
    }

    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9 };

    public static int ceillog2(int val) {
        val = isPowerOfTwo(val) ? val : smallestEncompassingPowerOfTwo(val);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)(val * 125613361L >> 27L) & 0x1F];
    }

    public static int log2(int val) {
        return ceillog2(val) - (isPowerOfTwo(val) ? 0 : 1);
    }

    public static boolean isPowerOfTwo(int val) {
        return val != 0 && (val & val - 1) == 0;
    }

}

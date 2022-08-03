package com.melluh.mcmitm.util;

public record BlockPos(int x, int y, int z) {

    private static final int POSITION_X_SIZE = 38;
    private static final int POSITION_Y_SIZE = 12;
    private static final int POSITION_Z_SIZE = 38;
    private static final int POSITION_Y_SHIFT = 0xFFF;
    private static final int POSITION_WRITE_SHIFT = 0x3FFFFFF;

    public long asLong() {
        long xVal = x & POSITION_WRITE_SHIFT;
        long yVal = y & POSITION_Y_SHIFT;
        long zVal = z & POSITION_WRITE_SHIFT;
        return xVal << POSITION_X_SIZE | zVal << POSITION_Y_SIZE | yVal;
    }

    public static BlockPos of(long val) {
        int x = (int) (val >> POSITION_X_SIZE);
        int y = (int) (val << 52 >> 52);
        int z = (int) (val << 26 >> POSITION_Z_SIZE);
        return new BlockPos(x, y, z);
    }

}

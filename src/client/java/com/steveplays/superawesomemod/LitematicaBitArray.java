package com.steveplays.superawesomemod;

/**
 * Decodes the packed {@code long[]} block-state storage used in {@code .litematic} files.
 * Entries are packed contiguously across long boundaries (unlike vanilla chunk sections).
 */
public final class LitematicaBitArray {

    private final long[] data;
    private final int bitsPerEntry;
    private final long maxEntryValue;
    private final int size;

    public LitematicaBitArray(int bitsPerEntry, int size, long[] data) {
        this.bitsPerEntry = bitsPerEntry;
        this.size = size;
        this.data = data;
        this.maxEntryValue = (1L << bitsPerEntry) - 1L;
    }

    public int get(int index) {
        long bitOffset = (long) index * bitsPerEntry;
        int longIndex = (int) (bitOffset >>> 6); // divide by 64
        int bitStart = (int) (bitOffset & 63);   // mod 64

        long val = data[longIndex] >>> bitStart;

        int bitsInFirstLong = 64 - bitStart;
        if (bitsInFirstLong < bitsPerEntry) {
            // Entry spans two longs
            val |= data[longIndex + 1] << bitsInFirstLong;
        }

        return (int) (val & maxEntryValue);
    }

    public int size() {
        return size;
    }
}

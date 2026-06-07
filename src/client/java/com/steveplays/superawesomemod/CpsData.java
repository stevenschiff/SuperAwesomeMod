package com.steveplays.superawesomemod;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsData {
    private static boolean enabled = false;
    private static int     scale   = 5; // 1-10, default 5
    private static int     offset  = 8; // 0-100, pixels right of hotbar

    // Timestamps (System.currentTimeMillis) of clicks within the last second.
    private static final Deque<Long> leftClicks  = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();

    private CpsData() {}

    public static boolean isEnabled()           { return enabled; }
    public static void    setEnabled(boolean e) { enabled = e; }

    public static int  getScale()      { return scale; }
    public static void setScale(int s) { scale = Math.clamp(s, 1, 10); }

    public static int  getOffset()      { return offset; }
    public static void setOffset(int o) { offset = Math.clamp(o, 0, 100); }

    public static void recordLeftClick() {
        leftClicks.addLast(System.currentTimeMillis());
    }

    public static void recordRightClick() {
        rightClicks.addLast(System.currentTimeMillis());
    }

    public static int getLeftCps() {
        prune(leftClicks);
        return leftClicks.size();
    }

    public static int getRightCps() {
        prune(rightClicks);
        return rightClicks.size();
    }

    private static void prune(Deque<Long> deque) {
        long cutoff = System.currentTimeMillis() - 1000;
        while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
            deque.pollFirst();
        }
    }
}

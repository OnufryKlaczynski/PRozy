package proz;

import java.util.concurrent.atomic.AtomicInteger;

public class Clock {
    private static AtomicInteger clock = new AtomicInteger(0);

    public static int getClock() {
        return clock.get();
    }

    public static int setHigherClock(int newClock) {
        return clock.getAndUpdate(value -> newClock > value ? newClock + 1 : value + 1);
    }

    public static int clockPlusOne() {
        return clock.addAndGet(1);
    }
}

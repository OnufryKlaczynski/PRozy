package proz;

public class Clock {
    private static int clock = 0;

    public static int getClock() {
        return clock;
    }

    public static void setClock(int newClock) {
        if (newClock > clock) {
            clock = newClock;
        }
    }

    public static void clockPlusOne() {
        clock++;
    }
}

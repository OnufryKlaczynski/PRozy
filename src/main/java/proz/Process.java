package proz;

import utils.Colors;

import java.util.concurrent.atomic.AtomicBoolean;

public class Process {

    public final int myrank;
    public final int processesCount;
    public Thread travelingThread;
    private int STORE_SPACE;
    private int MEDIUM_COUNT;

    public final Colors color;
//    public AtomicBoolean holdingMedium = new AtomicBoolean(false);


    public TouristState touristState = TouristState.RESTING;
    public int requestedMediumId = -1;
    public int requestedMediumPriority = 0;


    public Process(int myrank, int processesCount, int STORE_SPACE, int MEDIUM_COUNT) {
        this.myrank = myrank;
        color = Colors.values()[myrank];
        this.processesCount = processesCount;
        this.STORE_SPACE = STORE_SPACE;
        this.MEDIUM_COUNT = MEDIUM_COUNT;
        touristState = TouristState.RESTING;

    }



    @Override
    public String toString() {
        return "Process{" +
                "myrank=" + myrank +
                ", touristState=" + touristState +
                '}';
    }
}

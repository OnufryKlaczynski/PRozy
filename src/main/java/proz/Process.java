package proz;

import utils.Colors;

public class Process {

    public final int myrank;
    public final int processesCount;
    private int STORE_SPACE;
    private int MEDIUM_COUNT;

    public final Colors color;



    public TouristState touristState = TouristState.RESTING;
    public int requestedMediumId = -1;
    public int requestedMediumPriority = 0;


    public Process(int myrank, int processesCount, int STORE_SPACE, int MEDIUM_COUNT) {
        this.myrank = myrank;
        color = Colors.values()[myrank];
        this.processesCount = processesCount;
        this.STORE_SPACE = STORE_SPACE;
        this.MEDIUM_COUNT = MEDIUM_COUNT;
        touristState = TouristState.WAITING_FOR_STORE;

    }



    @Override
    public String toString() {
        return "Process{" +
                "myrank=" + myrank +
                ", touristState=" + touristState +
                '}';
    }
}

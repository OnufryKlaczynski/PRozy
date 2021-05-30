package proz.requests;

public class StoreRequest {
    final int clock;
    final int sourceId;

    public StoreRequest(int clock, int sourceId) {
        this.clock = clock;
        this.sourceId = sourceId;
    }

    public int getClock() {
        return clock;
    }

    public int getSourceId() {
        return sourceId;
    }


}

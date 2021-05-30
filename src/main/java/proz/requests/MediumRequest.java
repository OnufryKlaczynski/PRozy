package proz.requests;

public class MediumRequest {
    final int clock;
    final int sourceId;

    public MediumRequest(int clock, int sourceId) {
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

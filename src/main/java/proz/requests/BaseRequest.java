package proz.requests;

public class BaseRequest {
    final int clock;
    final int sourceId;

    public BaseRequest(int clock, int sourceId) {
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

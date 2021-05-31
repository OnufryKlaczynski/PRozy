package proz.requests;

public class TunnelRequest {

    final int clock;
    final int sourceId;

    public TunnelRequest(int clock, int sourceId) {
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

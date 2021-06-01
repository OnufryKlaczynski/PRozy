package proz.requests;

public class TunnelRequest extends BaseRequest{


    public TunnelRequest(int clock, int sourceId) {
        super(clock, sourceId);
    }

    public int getClock() {
        return clock;
    }

    public int getSourceId() {
        return sourceId;
    }

}

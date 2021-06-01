package proz.requests;

public class StoreRequest extends BaseRequest {

    public StoreRequest(int clock, int sourceId) {
        super(clock, sourceId);
    }

    public int getClock() {
        return clock;
    }

    public int getSourceId() {
        return sourceId;
    }


}

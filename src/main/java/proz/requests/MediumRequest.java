package proz.requests;

public class MediumRequest extends BaseRequest{
    final int priority;

    public MediumRequest(int clock, int sourceId, int priority) {
        super(clock, sourceId);
        this.priority = priority;
    }

    public int getClock() {
        return clock;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getPriority() {
        return priority;
    }
}

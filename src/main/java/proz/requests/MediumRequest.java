package proz.requests;

public class MediumRequest {
    final int clock;
    final int sourceId;
    final int priority;

    public MediumRequest(int clock, int sourceId, int priority) {
        this.clock = clock;
        this.sourceId = sourceId;
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

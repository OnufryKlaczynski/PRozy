package proz;

import proz.requests.MediumRequest;
import proz.requests.StoreRequest;

import java.util.ArrayList;
import java.util.List;

public class Queues {
    public static List<StoreRequest> storeRequests = new ArrayList<>();
    public static int ackStoreCount = 0;

    public static List<List<MediumRequest>> mediumQueue = new ArrayList<>();

    static {
        for (int i = 0; i < Main.MEDIUM_COUNT; i++) {
            mediumQueue.add(new ArrayList<>());
        }
    }

}

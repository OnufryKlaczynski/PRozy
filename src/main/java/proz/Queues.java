package proz;

import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Queues {
    public static List<StoreRequest> storeRequests = new ArrayList<>();
    public static int ackStoreCount = 0;

    public static List<List<MediumRequest>> mediumQueue = new ArrayList<>();
    public static int ackMediumCount = 0;
    public static Set<Integer> blockedMediums = new HashSet<>();


    public static List<List<TunnelRequest>> tunnelRequests = new ArrayList<>();

    static {
        for (int i = 0; i < Main.MEDIUM_COUNT; i++) {
            mediumQueue.add(new ArrayList<>());
            tunnelRequests.add(new ArrayList<>());
        }
    }

}

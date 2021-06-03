package proz.resolvers;

import proz.Queues;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

import java.util.Comparator;

public class Utils {

    public static void addRequestStoreToQueue(int source, int hisClock) {
        Queues.storeRequests.add(new StoreRequest(hisClock, source));
        Queues.storeRequests.sort(
                Comparator.comparing(StoreRequest::getClock)
                        .thenComparing(StoreRequest::getSourceId)
        );
    }

    public static void addMediumRequestToQueue(int source, int hisClock, int mediumId, int priority) {
        Queues.mediumRequests.get(mediumId).add(new MediumRequest(hisClock, source, priority));
        Queues.mediumRequests.get(mediumId).sort(
                Comparator.comparing(MediumRequest::getClock)
                        .thenComparing(MediumRequest::getPriority, Comparator.reverseOrder())
                        .thenComparing(MediumRequest::getSourceId)
        );
    }

    public static void addTunnelRequestToQueue(int source, int hisClock, int requestedTunnelId) {
        Queues.tunnelRequests.get(requestedTunnelId).add(new TunnelRequest(hisClock, source));
        Queues.tunnelRequests.get(requestedTunnelId).sort(
                Comparator.comparing(TunnelRequest::getClock)
                        .thenComparing(TunnelRequest::getSourceId)
        );
    }
}

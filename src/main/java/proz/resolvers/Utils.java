package proz.resolvers;

import proz.Queues;
import proz.Tag;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;
import utils.Colors;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
                Comparator
                        .comparing(MediumRequest::getPriority, Comparator.reverseOrder())
                        .thenComparing(MediumRequest::getClock)
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

    public static void gotMessageReleaseMedium(int source, int releasedMedium) {
        Queues.blockedMediums.removeIf(id -> id == releasedMedium);
//        System.out.println("Usunąłem medium: " + releasedMedium + " Teraz blockedMedium wygląda tak: " + Queues.blockedMediums);
        for (List<MediumRequest> list : Queues.mediumRequests) {
            list.removeIf(mediumRequest -> mediumRequest.getSourceId() == source);
        }
    }
}

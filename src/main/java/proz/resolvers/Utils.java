package proz.resolvers;

import mpi.MPIException;
import proz.*;
import proz.Process;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

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
        Queues.releaseMediumCounter += 1;
        Queues.blockedMediums.removeIf(id -> id == releasedMedium);
//        System.out.println("Usunąłem medium: " + releasedMedium + " Teraz blockedMedium wygląda tak: " + Queues.blockedMediums);
        for (List<MediumRequest> list : Queues.mediumRequests) {
            list.removeIf(mediumRequest -> mediumRequest.getSourceId() == source);
        }
    }

    public static void sendAckMedium(int source, Process process, Communication communication) throws MPIException {
        if (source == process.myrank) {
            return;
        }
        int heldMediumId = -1;
        if (process.holdingMedium.get()) {
            System.out.println(process.color.getColor() + " Trzyma medium: " + process.requestedMediumId);
            heldMediumId = process.requestedMediumId;
        }
        communication.sendToOne(new int[]{Clock.getClock(), heldMediumId, -1}, Tag.ACK_MEDIUM, source);
    }
}

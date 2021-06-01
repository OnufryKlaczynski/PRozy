package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

import java.util.Comparator;

public class RestingResolver{


    public static void resolve(Status messageInfo, int[] message, Communication communication) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];


        switch(messageTag) {
            case REQ_STORE:
                communication.sendToOne(new int[] {Clock.getClock(),  -1, -1}, Tag.ACK_STORE, source);
                Queues.storeRequests.add(new StoreRequest(hisClock, source));
                Queues.storeRequests.sort(
                        Comparator.comparing(StoreRequest::getClock)
                                .thenComparing(StoreRequest::getSourceId)
                );
                break;
            case ACK_STORE:
                throw new IllegalStateException();
            case RELEASE_STORE:
                Queues.storeRequests.removeIf(storeRequest -> storeRequest.getSourceId() == source);

                break;
            case REQ_MEDIUM:
                communication.sendToOne(new int[] {Clock.getClock(),  -1, -1}, Tag.ACK_MEDIUM, source);
                int mediumId = message[1];
                int priority = message[2];
                Queues.mediumRequests.get(mediumId).add(new MediumRequest(hisClock, source, priority));
                Queues.mediumRequests.get(mediumId).sort(
                        Comparator.comparing(MediumRequest::getClock)
                                .thenComparing(MediumRequest::getPriority, Comparator.reverseOrder())
                                .thenComparing(MediumRequest::getSourceId)
                );
                break;
            case ACK_MEDIUM:
                throw new IllegalStateException();
            case RELEASE_MEDIUM:
                mediumId = message[1];
                Queues.mediumRequests.get(mediumId).removeIf(mediumRequest -> mediumRequest.getSourceId() == source);

                break;
            case REQ_TUNNEL:
                int requestedTunnelId = message[1];
                Queues.tunnelRequests.get(requestedTunnelId).add(new TunnelRequest(hisClock, source));
                Queues.tunnelRequests.get(requestedTunnelId).sort(
                        Comparator.comparing(TunnelRequest::getClock)
                                .thenComparing(TunnelRequest::getSourceId)
                );
                communication.sendToOne(new int[] {Clock.getClock(), -1, -1}, Tag.ACK_TUNNEL, source);
                break;
            case ACK_TUNNEL:
                throw new IllegalStateException();
            case RELEASE_TUNNEL:
                int releasedTunnelId = message[1];
                Queues.tunnelRequests.get(releasedTunnelId).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);
                break;
        }
    }


}

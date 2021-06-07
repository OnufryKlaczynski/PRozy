package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;

import static proz.resolvers.Utils.*;

public class RestingResolver {


    public static void resolve(Status messageInfo, int[] message, Communication communication) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];


        switch (messageTag) {
            case REQ_STORE:
                communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_STORE, source);
                addRequestStoreToQueue(source, hisClock);
                break;
            case ACK_STORE:
                throw new IllegalStateException();
            case RELEASE_STORE:
                Queues.storeRequests.removeIf(storeRequest -> storeRequest.getSourceId() == source);

                break;
            case REQ_MEDIUM:
                communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_MEDIUM, source);
                int mediumId = message[1];
                int priority = message[2];
                addMediumRequestToQueue(source, hisClock, mediumId, priority);
                break;
            case ACK_MEDIUM:
                throw new IllegalStateException();
            case RELEASE_MEDIUM:
                mediumId = message[1];
                gotMessageReleaseMedium(source, mediumId);
                break;
            case REQ_TUNNEL:
                int requestedTunnelId = message[1];
                addTunnelRequestToQueue(source, hisClock, requestedTunnelId);
                communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_TUNNEL, source);
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

package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;
import proz.requests.StoreRequest;

import java.util.Comparator;

public class TravelingResolver {

    public static void resolve(Status messageInfo, int[] message, Communication communication, Process process) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];

        switch(messageTag) {
            case REQ_STORE:
                communication.sendToOne(new int[] {Clock.getClock()}, Tag.ACK_STORE, source);
                Queues.storeRequests.add(new StoreRequest(hisClock, source));
                Queues.storeRequests.sort(
                        Comparator.comparing(StoreRequest::getClock)
                                .thenComparing(StoreRequest::getSourceId)
                );
                break;
            case ACK_STORE:
                throw new IllegalStateException();
            case RELEASE_STORE:
                break;
            case REQ_MEDIUM:
                break;
            case ACK_MEDIUM:
                break;
            case RELEASE_MEDIUM:
                break;
            case REQ_TUNNEL:
                break;
            case ACK_TUNNEL:
                break;
            case RELEASE_TUNNEL:
                break;
        }

    }
}

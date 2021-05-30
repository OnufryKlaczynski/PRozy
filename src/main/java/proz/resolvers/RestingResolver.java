package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;

import java.util.Comparator;

public class RestingResolver{


    public static void resolve(Status messageInfo, int[] message, Communication communication) throws MPIException {
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
                Queues.storeRequests.removeIf(storeRequest -> storeRequest.getSourceId() == source);

                break;
            case REQ_MEDIUM:
                communication.sendToOne(new int[] {Clock.getClock()}, Tag.ACK_MEDIUM, source);
                int mediumId = message[1];
                Queues.mediumQueue.get(mediumId).add(new MediumRequest(hisClock, source));
                Queues.mediumQueue.get(mediumId).sort(
                        Comparator.comparing(MediumRequest::getClock)
                                .thenComparing(MediumRequest::getSourceId)
                );
                break;
            case ACK_MEDIUM:
                throw new IllegalStateException();
            case RELEASE_MEDIUM:
                mediumId = message[1];
                Queues.mediumQueue.get(mediumId).removeIf(mediumRequest -> mediumRequest.getSourceId() == source);

                break;
            case REQ_TUNNEL:
                communication.sendToOne(new int[] {Clock.getClock()}, Tag.ACK_TUNNEL, source);
                break;
            case ACK_TUNNEL:
                throw new IllegalStateException();
            case RELEASE_TUNNEL:
                break;
        }
    }


}

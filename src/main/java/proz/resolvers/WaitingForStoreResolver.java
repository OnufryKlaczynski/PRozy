package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;
import proz.requests.MediumRequest;
import proz.requests.TunnelRequest;

import java.util.Comparator;

public class WaitingForStoreResolver {
    
    public static void resolve(Status messageInfo, int[] message, Communication communication, Process process) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];

        switch (messageTag) {
            case REQ_STORE:
//                Nic nie odpowiadaj bo sam chcesz wejść od skelpu
                break;
            case ACK_STORE:
                Queues.ackStoreCount += 1;

                if (Main.PROCESS_COUNT - Main.STORE_SPACE -  Queues.ackStoreCount < 0 ) {
                    System.out.println("Wchodzę do sklepu" + "\n");
                    process.touristState = TouristState.WAITING_FOR_MEDIUM;
                    process.requestedMediumId = process.myrank % Main.MEDIUM_COUNT;
                    int[] requestMedium = {Clock.getClock(), process.requestedMediumId, process.requestedMediumPriority};
                    communication.sendToAll(requestMedium, Tag.REQ_MEDIUM);
                }

            case RELEASE_STORE:
                break;
            case REQ_MEDIUM:
                communication.sendToOne(new int[] {Clock.getClock()}, Tag.ACK_MEDIUM, source);
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
                communication.sendToOne(new int[] {Clock.getClock()}, Tag.ACK_TUNNEL, source);
                break;
            case ACK_TUNNEL:
                throw new IllegalStateException();

            case RELEASE_TUNNEL:
                int releasedTunnel = message[1];
                Queues.tunnelRequests.get(releasedTunnel).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);
                break;
        }
    }
    
}

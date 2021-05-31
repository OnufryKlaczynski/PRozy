package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

import java.util.Comparator;

public class TravelingResolver {

    public static void resolve(Status messageInfo, int[] message, Communication communication, Process process) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];

        switch (messageTag) {
            case REQ_STORE:
                communication.sendToOne(new int[]{Clock.getClock()}, Tag.ACK_STORE, source);
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
                communication.sendToOne(new int[]{Clock.getClock()}, Tag.ACK_MEDIUM, source);
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
                int tunnelId = message[1];
                Queues.tunnelRequests.get(tunnelId).add(new TunnelRequest(hisClock, source));
                Queues.tunnelRequests.get(tunnelId).sort(
                        Comparator.comparing(TunnelRequest::getClock)
                                .thenComparing(TunnelRequest::getSourceId)
                );
// TODO: jeśli prosi o ten sam tunel to musimy sprwadzić czy możemy odesła ACK czy gość ma gorszy zegarek niż my?
                //TODO czy odsyłamy mu wiadomośc ACK??
                break;
            case ACK_TUNNEL:
                Queues.ackTunnelCount += 1;
                if (Queues.ackTunnelCount == Main.PROCESS_COUNT) {
                    //Możemy wejść jeśli jesteśmy pierwsi w kolejce
                    //TODO czasowe wyjście, czas podróży
                    if (Queues.tunnelRequests.get(process.requestedMediumId).get(0).getSourceId() == process.myrank) {
                        communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId}, Tag.RELEASE_TUNNEL);
                        process.touristState = TouristState.RESTING;
                        process.requestedMediumId = -1;
                    }
                }
                break;
            case RELEASE_TUNNEL:
                int releasedTunnel = message[1];
                Queues.tunnelRequests.get(releasedTunnel).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);
                if (releasedTunnel == process.requestedMediumId) {
                    if (Queues.tunnelRequests.get(process.requestedMediumId).get(0).getSourceId() == process.myrank) {
                        communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId}, Tag.RELEASE_TUNNEL);
                        process.touristState = TouristState.RESTING;
                        process.requestedMediumId = -1;
                    }
                }
                break;
        }

    }
}

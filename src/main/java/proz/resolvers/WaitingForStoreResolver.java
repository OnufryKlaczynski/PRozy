package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;


import static proz.resolvers.Utils.*;

public class WaitingForStoreResolver {

    public static void resolve(Status messageInfo, int[] message, Communication communication, Process process) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        int source = messageInfo.getSource();
        int hisClock = message[0];

        switch (messageTag) {
            case REQ_STORE:
                addRequestStoreToQueue(source, hisClock);
                if (source != process.myrank) {
                    communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_STORE, source);
                }
                break;
            case ACK_STORE:
                Queues.ackStoreCount += 1;
                if (canChangeStatusToWaitingForMedium(process)) {
                    changingStateToWaitingForMedium(communication, process);
                }
                break;

            case RELEASE_STORE:
                Queues.storeRequests.removeIf(storeRequest -> storeRequest.getSourceId() == source);
                if (canChangeStatusToWaitingForMedium(process)) {
                    changingStateToWaitingForMedium(communication, process);
                }
                break;

            case REQ_MEDIUM:
                sendAckMedium(source, process, communication);
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
                int releasedTunnel = message[1];
                Queues.tunnelRequests.get(releasedTunnel).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);

                break;
        }
    }


    private static boolean canChangeStatusToWaitingForMedium(Process process) {
        boolean gotEnoughAck = Queues.ackStoreCount == Main.PROCESS_COUNT - 1;
        boolean canGoIntoStore = Queues.storeRequests.stream()
                .limit(Main.STORE_SPACE)
                .anyMatch(storeRequest -> storeRequest.getSourceId() == process.myrank);
        return gotEnoughAck && canGoIntoStore;
    }

    private static void changingStateToWaitingForMedium(Communication communication, Process process) throws MPIException {
        process.touristState = TouristState.WAITING_FOR_MEDIUM;
        process.requestedMediumId = process.myrank % Main.MEDIUM_COUNT;
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Wchodzę do sklepu i ubiegam się o Medium: " + process.requestedMediumId + " z priorytetem: " + process.requestedMediumPriority + "\n");
        int[] requestMedium = {Clock.getClock(), process.requestedMediumId, process.requestedMediumPriority};
        communication.sendToAll(requestMedium, Tag.REQ_MEDIUM);
    }

}

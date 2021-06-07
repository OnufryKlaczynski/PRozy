package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;

import static proz.resolvers.Utils.*;

public class TravelingResolver {

    public static void resolve(Status messageInfo, int[] message, Communication communication, Process process) throws MPIException {
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
                int tunnelId = message[1];
                addTunnelRequestToQueue(source, hisClock, tunnelId);
                if (source != process.myrank) {
                    communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_TUNNEL, source);
                }

                break;
            case ACK_TUNNEL:
                Queues.ackTunnelCount += 1;
                if (Queues.ackTunnelCount == Main.PROCESS_COUNT - 1) {
                    boolean firstInQueue = Queues.tunnelRequests.get(process.requestedMediumId).get(0).getSourceId() == process.myrank;
                    if (firstInQueue) {
                        tryToLeaveTunnel(communication, process);
                    }
                }
                break;

            case RELEASE_TUNNEL:
                int releasedTunnel = message[1];
                Queues.tunnelRequests.get(releasedTunnel).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);
                if (releasedTunnel == process.requestedMediumId && Queues.ackTunnelCount == Main.PROCESS_COUNT - 1) {
                    boolean firstInQueue = Queues.tunnelRequests.get(process.requestedMediumId).get(0).getSourceId() == process.myrank;
                    if (firstInQueue) {
                        tryToLeaveTunnel(communication, process);
                    }
                }
                break;
        }

    }


    private static void tryToLeaveTunnel(Communication communication, Process process) throws MPIException {
        boolean travelingThreadAlive = process.travelingThread.isAlive();
        if (!travelingThreadAlive) {
            leaveTunnel(communication, process);
        } else {
            waitForTravelingToEndAndLeaveTunnel(communication, process);
        }
    }

    private static void waitForTravelingToEndAndLeaveTunnel(Communication communication, Process process) {
        new Thread(() -> {
            try {
                process.travelingThread.join();
                leaveTunnel(communication, process);
            } catch (InterruptedException | MPIException e) {
                e.printStackTrace();
            }
        });
    }

    private static void leaveTunnel(Communication communication, Process process) throws MPIException {
        communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId, -1}, Tag.RELEASE_TUNNEL);
        resetCounters(process);
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Przeszedłem tunel i wychodzę");
    }

    private static void resetCounters(Process process) {
        process.touristState = TouristState.RESTING;
        process.requestedMediumId = -1;
        process.requestedMediumPriority = 0;
        Queues.ackStoreCount = 0;
        Queues.ackMediumCount = 0;
        Queues.ackTunnelCount = 0;
    }
}

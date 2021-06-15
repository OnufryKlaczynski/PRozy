package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;

import java.sql.SQLOutput;
import java.util.Random;

import static proz.resolvers.Utils.*;

public class WaitingForMediumResolver {

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
                break;
            case REQ_MEDIUM:
//              Sam czeka na medium i ktoś inny przysła mu info że też chce medium
                sendAckMedium(source, process, communication);

                int requestedMedium = message[1];
                int hisPriority = message[2];
                addMediumRequestToQueue(source, hisClock, requestedMedium, hisPriority);
                break;
            case ACK_MEDIUM:
//              Ktoś inny przysyła mu odpoiwada na info o medium
                Queues.ackMediumCount += 1;

                int blockedMediumId = message[1];
                if (blockedMediumId != -1) {
                    System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " dodaje zablokowane medium " + blockedMediumId + " dostałem tą widaomość od " + source + "\n");

                    Queues.blockedMediums.add(blockedMediumId);
                }
//              TODO: Czy to powinno być - 1 ? chyba tak bo siebie nie liczymy
                if (Queues.ackMediumCount == Main.PROCESS_COUNT - 1) {
                    boolean allMediumBlocked = Queues.blockedMediums.size() == Main.MEDIUM_COUNT;
                    if (allMediumBlocked) {
                        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Wszystkie media są zajęte:" + Queues.blockedMediums + "\n");
                        return;
                    }
//TODO: ale tu trzeba dodać sprwadzanie kolejek przecież nie ma XD
                    if (Queues.blockedMediums.contains(process.requestedMediumId)) {
                        tryRequestNextMedium(communication, process);
                    } else {
                        boolean firstInMediumRequestQueue = Queues.mediumRequests.get(process.requestedMediumId).get(0).getSourceId() == process.myrank;
                        if (!firstInMediumRequestQueue) {
//                            System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " " + Queues.mediumRequests.toString() + "\n");
//                            System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Nie jestem pierwszy w kolejce" + "\n");

                            tryRequestNextMedium(communication, process);
                            return;
                        }
                        changeStatusToLeavingTunnel(communication, process);

                        return;
                    }
                }

                break;
            case RELEASE_MEDIUM:
                int releasedMedium = message[1];
                gotMessageReleaseMedium(source, releasedMedium);
//                System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + "Dostałem wiadomośc żeby zwonlić medium");
                if (Queues.ackMediumCount == Main.PROCESS_COUNT - 1) {
                    tryRequestNextMedium(communication, process);
                }

                break;
            case REQ_TUNNEL:
                int tunnelId = message[1];
                addTunnelRequestToQueue(source, hisClock, tunnelId);

                int[] response = new int[]{Clock.getClock(), -1, -1};
                communication.sendToOne(response, Tag.ACK_TUNNEL, source);
                break;
            case ACK_TUNNEL:
                throw new IllegalStateException();

            case RELEASE_TUNNEL:
                tunnelId = message[1];
                Queues.tunnelRequests.get(tunnelId).removeIf(tunnelRequest -> tunnelRequest.getSourceId() == source);
                break;
        }

    }

    private static void changeStatusToLeavingTunnel(Communication communication, Process process) throws MPIException {
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + Queues.mediumRequests + " ackCounter " + Queues.ackMediumCount + "\n");
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Zaczynam podróżować i zmieniam stan na: " + TouristState.LEAVING_TUNNEL + "\n");
        process.touristState = TouristState.LEAVING_TUNNEL;
        communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId, -1}, Tag.REQ_TUNNEL);
        boolean mediumShouldRest = Queues.releaseMediumCounter % Main.MEDIUM_RESTING[process.requestedMediumId] == 0;
        if (mediumShouldRest) {
            process.holdingMedium.set(true);
            Thread mediumRestingThread = createMediumRestingThread(communication, process);
            mediumRestingThread.start();
        } else {
            communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId, -1}, Tag.RELEASE_MEDIUM); //TODO: iloś ile medium ma tuneli
        }

        communication.sendToAll(new int[]{Clock.getClock(), -1, -1}, Tag.RELEASE_STORE);
        process.travelingThread = createTravelingThread(process);
        process.travelingThread.start();
    }

    private static Thread createTravelingThread(Process process) {
        return new Thread(() -> {
            Random random = new Random();
            try {
                int travelTime = (int) (random.nextDouble() * 3 * Main.SLOWER_MODE);
                System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Będę podróżować: " + travelTime + " ms" + "\n");
                Thread.sleep(travelTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static Thread createMediumRestingThread(Communication communication, Process process) {
        return new Thread(() -> {
            Random random = new Random();
            int mediumRestTime = (int) (random.nextDouble() * 3 * Main.SLOWER_MODE);
            try {
                System.out.println(process.color.getColor() + " Medium musi odpoczać: " + process.requestedMediumId);
                Thread.sleep(mediumRestTime);
                process.holdingMedium.set(false);
                communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId, -1}, Tag.RELEASE_MEDIUM); //TODO: iloś ile medium ma tuneli

            } catch (InterruptedException | MPIException e) {
                e.printStackTrace();
            }
        });
    }


    private static void tryRequestNextMedium(Communication communication, Process process) throws MPIException {
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Próbuję znaleźć następne wolne medium" + "\n");

        int oldMediumId = process.requestedMediumId;
        process.requestedMediumPriority += 1;
        for (int i = 1; i < Main.MEDIUM_COUNT; i++) {
            int nextMediumToCheck = (process.requestedMediumId + i) % Main.MEDIUM_COUNT;
            if (!Queues.blockedMediums.contains(nextMediumToCheck)) {
                process.requestedMediumId = nextMediumToCheck;
                break;
            }
        }

        int[] requestMedium = {Clock.getClock(), process.requestedMediumId, process.requestedMediumPriority};
        Queues.ackMediumCount = 0;
        System.out.println(process.color.getColor() + "Clock: " + Clock.getClock() + " Strzelam po następne medium" + "\n");

        communication.sendToAll(requestMedium, Tag.REQ_MEDIUM);
    }

}

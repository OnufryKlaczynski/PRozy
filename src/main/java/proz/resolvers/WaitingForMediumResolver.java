package proz.resolvers;

import mpi.MPIException;
import mpi.Status;
import proz.*;
import proz.Process;
import proz.requests.MediumRequest;
import proz.requests.StoreRequest;
import proz.requests.TunnelRequest;

import java.util.Comparator;
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
                if (source != process.myrank) {
                    communication.sendToOne(new int[]{Clock.getClock(), -1, -1}, Tag.ACK_MEDIUM, source);
                }
                int mediumId = message[1];
                int hisPriority = message[2];
                addMediumRequestToQueue(source, hisClock, mediumId, hisPriority);
                break;
            case ACK_MEDIUM:
//              Ktoś inny przysyła mu odpoiwada na info o medium
                Queues.ackMediumCount += 1;

                int blockedMediumId = message[1];
                if (blockedMediumId != -1) {
                    Queues.blockedMediums.add(blockedMediumId);
                }
//              TODO: Czy to powinno być - 1 ? chyba tak bo siebie nie liczymy
                if (Queues.ackMediumCount == Main.PROCESS_COUNT - 1) {
                    if (Queues.blockedMediums.size() == Main.MEDIUM_COUNT) {
//                      TODO: nie prosimy o medium dopóki nie dostaniemy release
                        return;
                    }

                    if (Queues.blockedMediums.contains(process.requestedMediumId)) {
                        process.requestedMediumPriority += 1;
                        for (int i = 1; i < Main.MEDIUM_COUNT; i++) {
                            int nextMediumToCheck = (process.requestedMediumId + i) % Main.MEDIUM_COUNT;
                            if (!Queues.blockedMediums.contains(nextMediumToCheck)) {
                                process.requestedMediumId = nextMediumToCheck;
                                break;
                            }
                        }
                        int[] requestMedium = {Clock.getClock(), process.requestedMediumId, process.requestedMediumPriority};
                        communication.sendToAll(requestMedium, Tag.REQ_MEDIUM);
                    } else {
                        System.out.println(process.color.getColor() + "Zaczynam podróżować i zmieniam stan na: " + TouristState.LEAVING_TUNNEL + "\n");
                        process.touristState = TouristState.LEAVING_TUNNEL;
                        communication.sendToAll(new int[]{Clock.getClock(), -1, -1}, Tag.RELEASE_STORE);
                        communication.sendToAll(new int[]{Clock.getClock(), process.requestedMediumId, -1}, Tag.REQ_TUNNEL);
                        process.travelingThread = new Thread(() -> {
                            Random random = new Random();
                            try {
                                int travelTime = (int) (random.nextDouble() * 3 * Main.SLOWER_MODE);
                                System.out.println(process.color.getColor() + "Będę podróżować: " + travelTime + " ms" + "\n");
                                Thread.sleep(travelTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        process.travelingThread.run();

                        return;
                    }
                }

                break;
            case RELEASE_MEDIUM:
                mediumId = message[1];
                // TODO To jest źle bo to miał być array a jest lista i indeksy się popierdolą, zamienić na null value przy usuwaniu?
                //TODO czy możę trzymam jednak indeks tego medium?
                //TODO a jednak chyba się nie pierdoli? bo pierwsza lista jest o stałym rozmiarze
                Queues.mediumRequests.get(mediumId).removeIf(mediumRequest -> mediumRequest.getSourceId() == source);
                Queues.blockedMediums.removeIf(id -> id == mediumId);
                if (Queues.blockedMediums.size() == Main.MEDIUM_COUNT - 1) {
                    //TODO czy tu powinno być requestMediumPriority + 1?
                    process.requestedMediumId = mediumId;
                    int[] requestMedium = {Clock.getClock(), process.requestedMediumId, process.requestedMediumPriority};
                    communication.sendToAll(requestMedium, Tag.REQ_MEDIUM);
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

}

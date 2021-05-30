package proz.resolvers;

import mpi.Status;
import proz.*;

public class WaitingForStoreResolver {
    
    public static void resolve(Status messageInfo, int[] message, Communication communication, TouristState touristState) {
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
                    System.out.println("Wchodzę do sklepu");
                    touristState = TouristState.WAITING_FOR_MEDIUM;
                }

            case RELEASE_STORE:
                break;
            case REQ_MEDIUM:
                break;
            case ACK_MEDIUM:
                throw new IllegalStateException();

            case RELEASE_MEDIUM:
                break;
            case REQ_TUNNEL:
                break;
            case ACK_TUNNEL:
                throw new IllegalStateException();

            case RELEASE_TUNNEL:
                break;
        }
    }
    
}

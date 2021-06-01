package proz;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;
import proz.resolvers.RestingResolver;
import proz.resolvers.TravelingResolver;
import proz.resolvers.WaitingForMediumResolver;
import proz.resolvers.WaitingForStoreResolver;
import utils.Colors;

import java.util.Arrays;

public class ProcessService {


    public void run(Process process) throws MPIException {
        int[] message = new int[] {Clock.getClock(), -1, -1};


        Communication communication = new Communication(process.processesCount, process.color);
        System.out.println(process.color.getColor() + " My rank is " +  process.myrank + "\n");


        if (0 == process.myrank || 1 == process.myrank) {
            process.touristState = TouristState.WAITING_FOR_STORE;
            communication.sendToAll(message, Tag.REQ_STORE);
        }

        while (true) {
            Status messageInfos = MPI.COMM_WORLD.recv(message, 3, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
            Clock.setClock(Integer.max(message[0], Clock.getClock()) + 1);


            respond(messageInfos, message, process, communication);
            System.out.println(process.color.getColor() +
                    " Process " + process.myrank +
                            " in state: " + process.touristState +
                            " got message from:" + messageInfos.getSource() +
                            " " + Colors.values()[messageInfos.getSource()].getColor() +
                            "tag:" + Tag.of(messageInfos.getTag()) +
                            " message: " + Arrays.toString(message)
                    + "\n"
            );

        }

    }

    public void respond(Status messageInfo, int[] message, Process process, Communication communication) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        switch (process.touristState) {

            case RESTING: {
                RestingResolver.resolve(messageInfo, message, communication);
                break;
            }

            case WAITING_FOR_STORE: {
                WaitingForStoreResolver.resolve(messageInfo, message, communication,  process);
                break;
            }

            case WAITING_FOR_MEDIUM: {
                WaitingForMediumResolver.resolve(messageInfo, message, communication, process);
                break;
            }

            case LEAVING_TUNNEL: {
                TravelingResolver.resolve(messageInfo, message, communication, process);
                break;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + process.touristState);
        }

    }
}

package proz;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Process {

    final int myrank;
    final int processesCount;
    private int STORE_SPACE;
    private int MEDIUM_COUNT;

    TouristState touristState = TouristState.RESTING;



    public Process(int myrank, int processesCount, int STORE_SPACE, int MEDIUM_COUNT) {
        this.myrank = myrank;
        this.processesCount = processesCount;
        this.STORE_SPACE = STORE_SPACE;
        this.MEDIUM_COUNT = MEDIUM_COUNT;
    }

    public void run() throws MPIException {
        int[] message = new int[] {Clock.getClock()};


        Communication communication = new Communication(processesCount);
        MessageResolver messageResolver = new MessageResolver();
        System.out.printf("My rank is %d \n", myrank);


        if (0 == myrank) {
            communication.sendToAll(message, Tag.REQ_STORE);
        }

        while (true) {
            Status messageInfos = MPI.COMM_WORLD.recv(message, 1, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
            Clock.setClock(Integer.max(message[0], Clock.getClock()) + 1);


            messageResolver.respond(messageInfos, message, touristState, communication);
            System.out.println(
                    "Process " + myrank + " got message  from:" + messageInfos.getSource() + " tag:" + messageInfos.getTag() + " message: " + Arrays.toString(message)
            );

        }

    }

    @Override
    public String toString() {
        return "Process{" +
                "myrank=" + myrank +
                ", touristState=" + touristState +
                '}';
    }
}

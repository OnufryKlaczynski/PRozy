package proz;

import mpi.MPI;
import mpi.MPIException;
import utils.Colors;

import java.util.Arrays;


public class Communication {

    private final int processesCount;
    private Colors color;

    public Communication(int processesCount, Colors color) {
        this.processesCount = processesCount;
        this.color = color;
    }


    public void sendToAll(int[] message, Tag messageTag) throws MPIException {
        Clock.clockPlusOne();
        for (int process = 0; process < processesCount; process++) {
            sendToOne(message, messageTag, process);
        }
    }


    public void sendToOne(int[] message, Tag messageTag, int toWho) throws MPIException {
        Clock.clockPlusOne();
        System.out.println(color.getColor() +
                " Process: " + processesCount +
                " sending message: " + Arrays.toString(message) +
                " with tag: " + messageTag +
                " to: " + toWho + " " + Colors.values()[toWho].getColor() + "\n");
        send(message, messageTag, toWho);
    }


    private void send(int[] message, Tag messageTag, int toWho) throws MPIException {
        MPI.COMM_WORLD.send(message, 1, MPI.INT, toWho, messageTag.messageTag);
    }
}

package proz;

import mpi.*;

import java.util.Arrays;
import java.util.Random;

public class Main {
    public static final int STORE_SPACE = 1;
    public static final int MEDIUM_COUNT = 2;

    public static int PROCESS_COUNT = 0;


    public static int SLOWER_MODE = 300;
    public static Random RANDOM = new Random();
    public static MessageMode MESSAGE_MODE = MessageMode.IMPORTANT;

    public static void main(String[] args) throws MPIException, InterruptedException {
        MPI.Init(args);

        int myrank = MPI.COMM_WORLD.getRank();
        int processesCount = MPI.COMM_WORLD.getSize();
        PROCESS_COUNT = processesCount;

        Process process = new Process(myrank, processesCount, STORE_SPACE, MEDIUM_COUNT);
        ProcessService processService = new ProcessService();
        processService.run(process);


        System.out.println("Process: " + myrank + " exiting" + "\n");
        MPI.Finalize();

    }

}

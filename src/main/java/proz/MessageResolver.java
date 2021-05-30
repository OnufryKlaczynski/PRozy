package proz;

import mpi.MPIException;
import mpi.Status;
import proz.resolvers.RestingResolver;
import proz.resolvers.WaitingForStoreResolver;

import java.util.ArrayList;
import java.util.List;

public class MessageResolver {


    int clock = 0;
    List<Integer> storeRequests = new ArrayList<>();
    List<Integer> mediumQueue = new ArrayList<>();


    public void respond(Status messageInfo, int[] message, TouristState touristState, Communication communication) throws MPIException {
        int tag = messageInfo.getTag();
        Tag messageTag = Tag.of(tag);
        switch (touristState) {

            case RESTING: {
                RestingResolver.resolve(messageInfo, message, communication);
                break;
            }
            case WAITING_FOR_STORE: {
                WaitingForStoreResolver.resolve(messageInfo, message, communication,  touristState);
                break;
            }

            case LEAVING_TUNNEL: {
                switch (messageTag) {

                }
                break;
            }

            case WAITING_FOR_MEDIUM: {
                switch (messageTag) {

                }
                break;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + touristState);
        }

    }



}

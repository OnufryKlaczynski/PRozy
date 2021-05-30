package proz;

import java.util.Arrays;

public enum Tag {

    REQ_STORE(10),
    ACK_STORE(20),
    RELEASE_STORE(25),

    REQ_MEDIUM(30),
    ACK_MEDIUM(40),
    RELEASE_MEDIUM(50),

    REQ_TUNNEL(60),
    ACK_TUNNEL(70),
    RELEASE_TUNNEL(80),
    ;

    public final int messageTag;

    Tag(int messageTag) {
        this.messageTag = messageTag;
    }

    public static Tag of(int messageTag) {
        return Arrays.stream(values()).filter(x -> x.messageTag == messageTag).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}

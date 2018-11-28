package messages;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ActualMessage {
    public enum TYPE {
        CHOCK, UNCHOCK, INTERESTED, UNINTERESTED, HAVE, BITFIELD, REQUEST, PIECE
    }
    int type;
    byte[] payload;

    public int getType() {
        return type;
    }
    public byte[] getPayload() {
        return payload;
    }

    public ActualMessage(byte[] bytes) {
        type = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 1)).getInt();
        payload = Arrays.copyOfRange(bytes, 0, 1);
    }

    public ActualMessage makeMessage(TYPE, payload) {

    }

    // message 加工处理还有问题
    public byte[] toByteArray() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        return bytes.toByteArray();
    }
}

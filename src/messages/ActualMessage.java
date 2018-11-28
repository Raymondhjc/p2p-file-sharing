package messages;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ActualMessage {
    int type;
    byte[] payload;

    public int getType() {
        return type;
    }
    public byte[] getPayload() {
        return payload;
    }

    public ActualMessage(byte[] bytes) {
        type = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 1)).get();
        payload = Arrays.copyOfRange(bytes, 1, bytes.length);
    }

    ActualMessage (int type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            if(this.payload == null) {
                bytes.write(ByteBuffer.allocate(4).putInt(1).array());
            } else {
                bytes.write(ByteBuffer.allocate(4).putInt(payload.length).array());
            }
            bytes.write(this.type);
            if(payload != null) {
                bytes.write(this.payload, 0, this.payload.length);
            }
        } catch(Exception e) {
            System.out.println("writing actual message error");
            e.printStackTrace();
        }
        return bytes.toByteArray();
    }
}

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
//        type = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 0, 1)).get();
//        payload = Arrays.copyOfRange(bytes, 1, bytes.length);
        byte[] typeField = Arrays.copyOfRange(bytes, 0, 1);
        this.type = ByteBuffer.wrap(typeField).get();
        if (bytes.length > 1) {
            this.payload = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
    }

    ActualMessage (int type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            // length field
            if(this.payload == null) {
                bytes.write(ByteBuffer.allocate(4).putInt(1).array());
            } else {
                bytes.write(ByteBuffer.allocate(4).putInt(payload.length + 1).array());
            }
            // type field
            bytes.write(this.type);
            // payload field
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

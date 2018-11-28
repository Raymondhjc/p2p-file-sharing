package messages;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HandshakeMessage {

    private int peerId;

    public HandshakeMessage(int peerId) {
        this.peerId = peerId;
    }

    // byte -> msg
    public HandshakeMessage(byte[] bytes) throws Exception {
        if(bytes.length != 32) {
            throw new Exception("invalid handshake message length");
        }
        String header = new String(Arrays.copyOfRange(bytes, 0, 18), "UTF-8");
        if(!header.equals("P2PFILESHARINGPROJ")) {
            throw new Exception("wrong handshake header:" + header);
        }
        peerId = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 28, 32)).getInt();
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String header = "P2PFILESHARINGPROJ";
        try {
            bytes.write(header.getBytes("UTF-8"));
            bytes.write(new byte[10]);
            bytes.write(ByteBuffer.allocate(4).putInt(peerId).array());
        } catch(Exception e) {
            System.out.println("writing handshake message error");
        }
        return bytes.toByteArray();
    }

    public int getPeerId() {
        return peerId;
    }
}

package messages;

import configs.CommonInfo;
import file.FileHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

public class MessageFactory {

    public static ActualMessage requestMessage(int pieceIndex) {
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        return new ActualMessage(6, payload);
    }

    public static ActualMessage interestedMessage() {
        return new ActualMessage(2, null);
    }

    public static ActualMessage uninterestedMessage() {
        return new ActualMessage(3, null);
    }

    public static ActualMessage bitfieldMessage(boolean[] bitfield) {
        byte[] bytes = new byte[(bitfield.length + 7) / 8];
        for(int i = 0; i < bitfield.length; ++i) {
            bytes[i / 8] *= 2;
            bytes[i / 8] += bitfield[i] ? 1 : 0;
        }
        return new ActualMessage(5, bytes);
    }

    public static ActualMessage chockMessage() {
        return new ActualMessage(0, null);
    }

    public static ActualMessage unchockMessage() {
        return new ActualMessage(1, null);
    }

    public static ActualMessage pieceMessage(byte[] payload, int readFromIndex, CommonInfo commonInfo) {
        int pieceIndex = ByteBuffer.wrap(payload).getInt();
        FileHandler fh = new FileHandler(commonInfo);

        byte[] content;
        int fileSize = commonInfo.getFileSize();
        int pieceSize = commonInfo.getPieceSize();
        int remainingBytes = fileSize - pieceIndex * pieceSize;
        if (remainingBytes < pieceSize) {
            content = new byte[remainingBytes];
        } else {
            content = new byte[pieceSize];
        }
        try {
            content = fh.readPiece(pieceIndex, readFromIndex);
        }catch (IOException e) {
            e.printStackTrace();
        }
        byte[] newPayload = new byte[payload.length + content.length];
        System.arraycopy(payload, 0, newPayload, 0, payload.length);
        System.arraycopy(content, 0, newPayload, payload.length, content.length);
        return new ActualMessage(7, newPayload);
    }
}

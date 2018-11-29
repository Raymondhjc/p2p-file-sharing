package connections;

import log.LogWriter;
import messages.MessageHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerConnection extends Thread {
    private int myId;
    private Socket connection;
    private DataInputStream in;

    private MessageHandler messageHandler;
    private LogWriter logWriter;

    private ConcurrentLinkedQueue<byte[]> messageQueue;

    ServerConnection(Socket connection, int myId, MessageHandler messageHandler, LogWriter logWriter) {
        this.myId = myId;
        this.connection = connection;
        this.logWriter = logWriter;
        this.messageHandler = messageHandler;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }
    @Override
    public void run() {
        try {
            System.out.println("new connection");
            // if skip, should add while(true) here, waiting for handshake message
            in = new DataInputStream(connection.getInputStream());
            int peerId = -1;
            try {
                while(peerId == -1) {
                    byte[] bytes = new byte[32];
                    in.readFully(bytes);
                    peerId = messageHandler.handleHandShakeMessage(bytes);
                    new LogWriter().tcpConnectionFrom(myId, peerId);
                }
            } catch (Exception e) {
                System.out.println("illegal handshake message. " + e);
            }
            try {
                while(true) {
                    int len = in.readInt();
                    byte[] bytes = new byte[len];
                    in.readFully(bytes);
                    for(byte b : bytes) {
                        System.out.print(Byte.toUnsignedInt(b) + " ");
                    }
                    messageQueue.add(bytes);
                    while(!messageQueue.isEmpty()) {
                        messageHandler.handleActualMessage(messageQueue.poll(), peerId);
                    }
                }
            } catch (Exception e) {
                System.out.println("read actual message error: " + e);
            }

        } catch (IOException ioException) {
            System.out.println("Disconnect with connections.Client ");
        }
    }
}
package connections;

import log.LogWriter;
import messages.MessageHandler;

import java.net.ServerSocket;

public class Server {

    ServerSocket serverSocket;
    private int peerId;

    public Server(int peerId) {
        this.peerId = peerId;
    }

    public void startServer(int port, MessageHandler messageHandler, LogWriter logWriter) throws Exception{
        serverSocket = new ServerSocket(port);
        while(true) {
            ServerConnection sc = new ServerConnection(serverSocket.accept(), peerId, messageHandler, logWriter);
            sc.start();
        }
    }

    public void closeServer() throws Exception {
        serverSocket.close();
    }

}

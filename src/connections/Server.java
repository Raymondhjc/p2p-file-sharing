package connections;

import log.LogWriter;
import messages.MessageHandler;

import java.net.ServerSocket;

public class Server{

    private ServerSocket serverSocket;

    public void startServer(int port, MessageHandler messageHandler, LogWriter logWriter) throws Exception{
        serverSocket = new ServerSocket(port);
        System.out.println("Server running on port " + port);
        while(true) {
            ServerConnection sc = new ServerConnection(serverSocket.accept(), messageHandler, logWriter);
            sc.start();
        }
    }

    public void closeServer() throws Exception {
        serverSocket.close();
    }

}

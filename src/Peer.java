import configs.CommonInfo;
import configs.PeerInfo;
import connections.Client;
import connections.Server;
import file.FileHandler;
import log.LogWriter;
import messages.ActualMessage;
import messages.HandshakeMessage;
import messages.MessageFactory;
import messages.MessageHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Peer implements MessageHandler{
    private int myId;
    private CommonInfo commonInfo;
    private PeerInfo myPeerInfo;
    private boolean[] myBitField;
    private Server server;

    private int optimisticallyUnchockedNeighbor;
    private ConcurrentHashMap<Integer, Client> clients;
    private Set<Integer> interested;
    private ConcurrentHashMap<Integer, boolean[]> bitMap;
    private ConcurrentHashMap<Integer, PeerInfo> peerInfoMap;
    private ConcurrentHashMap<Integer, Integer> downloadRateMap;
    private ConcurrentHashMap<Integer, Integer> requestedMap;
    private Set<Integer> hasHandshaked;

    private Vector<Integer> preferredNeighbors;

    private LogWriter logWriter;

    Peer(int peerId, CommonInfo commonInfo) {
        this.myId = peerId;
        this.commonInfo = commonInfo;
        this.logWriter = new LogWriter();

        this.clients = new ConcurrentHashMap<>();
        this.interested = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
        this.bitMap = new ConcurrentHashMap<>();
        this.peerInfoMap = new ConcurrentHashMap<>();
        this.downloadRateMap = new ConcurrentHashMap<>();
        this.requestedMap = new ConcurrentHashMap<>();

        this.hasHandshaked = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

        this.myBitField = new boolean[commonInfo.getFileSize() / commonInfo.getPieceSize() + 1];

        this.preferredNeighbors = new Vector<>();
        this.optimisticallyUnchockedNeighbor = -1;
    }

    public void start(List<PeerInfo> peerList) throws Exception {
        // load neighbor info
        for(PeerInfo info : peerList) {
            if(info.getPeerId() != myId) {
                peerInfoMap.put(info.getPeerId(), info);
            } else {
                myPeerInfo = info;
            }
        }

        // load this peer config
        if(myPeerInfo.hasFile()) {
            Arrays.fill(myBitField, true);
        }

        //start server
        startServer();

        // init clients, start the client whose id before it
        for(PeerInfo info : peerList) {
            if(info.getPeerId() < myId) {
                hasHandshaked.add(info.getPeerId());
                clients.put(info.getPeerId(), new Client(myId, info));
                clients.get(info.getPeerId()).start();
            }
        }

        getPreferredPeers();

        getOptiUnchokedPeers();
    }

    private void startServer() {
        server = new Server(myId);
        new Thread(() -> {
            try {
                // start the server
                server.startServer(myPeerInfo.getPeerPort(), this, logWriter);
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                try {
                    server.closeServer();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    private void getPreferredPeers() {
        Timer getPereferPeersTimer = new Timer();
        getPereferPeersTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(myPeerInfo.getPeerId() + " getPreferredPeers started");
                Vector<Integer> nextPreferredNeighbors = new Vector<>();
                if (myPeerInfo.hasFile()) {
                    if (interested.size() > 0) {
                        Vector<Integer> tempOfInterest = new Vector<>();
                        for(int interest : interested) {
                            tempOfInterest.add(interest);
                        }
                        while (nextPreferredNeighbors.size() < commonInfo.getNumberOfPreferredNeighbors()) {
                            if (tempOfInterest.size() == 0) {break;}
                            int randomIndex = new Random().nextInt(tempOfInterest.size());
                            nextPreferredNeighbors.add(tempOfInterest.get(randomIndex));
                            tempOfInterest.remove(tempOfInterest.get(randomIndex));
                        }
                    }
                } else {
                    if (!downloadRateMap.isEmpty()) {
                        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(downloadRateMap.entrySet());
                        list.sort(Map.Entry.comparingByValue());
                        for (Map.Entry<Integer, Integer> entry : list) {
                            nextPreferredNeighbors.add(entry.getKey());
                            if (nextPreferredNeighbors.size() >= commonInfo.getNumberOfPreferredNeighbors()) break;;
                        }
                        downloadRateMap.clear();
                    }
                }
                for (Integer newPreferredNeighbor : nextPreferredNeighbors) {
                    System.out.println(myPeerInfo.getPeerId() + " newPreferredNeighbor " + newPreferredNeighbor);

                    if (!preferredNeighbors.contains(newPreferredNeighbor)) {
                        // new comer
                        System.out.println(myPeerInfo.getPeerId() + " send unchockmessage to newPreferredNeighbor " + newPreferredNeighbor);
                        clients.get(newPreferredNeighbor).sendMessage(MessageFactory.unchockMessage());
                    }
                }

                for (Integer oldPreferredNeighbor : preferredNeighbors) {
                    System.out.println(myPeerInfo.getPeerId() + " oldPreferredNeighbor " + oldPreferredNeighbor);
                    if (!nextPreferredNeighbors.contains(oldPreferredNeighbor)) {
                        System.out.println(myPeerInfo.getPeerId() + " send chockmessage to oldPreferredNeighbor " + oldPreferredNeighbor);

                        clients.get(oldPreferredNeighbor).sendMessage(MessageFactory.chockMessage());
                    }
                }
                preferredNeighbors = nextPreferredNeighbors;
//                logWriter.changePreferredNeighbors(myPeerInfo.getPeerId(), preferredNeighbors);
            }
        }, 0, 1000 * commonInfo.getUnchokingInterval());
    }
    private void getOptiUnchokedPeers() {
        Timer getOptiUnchokedPeersTimer = new Timer();
        getOptiUnchokedPeersTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Integer newOptimisticallyUnchokedNeighbor = -1;
                if (interested.size() > 0) {
                    while (true) {
                        if (interested.isEmpty()) {
                            break;
                        }
                        int randomIndex = new Random().nextInt(interested.size());
                        Iterator<Integer> iterator = interested.iterator();
                        for(int i = 0; i <= randomIndex; ++i) {
                            newOptimisticallyUnchokedNeighbor = iterator.next();
                        }
                        if (interested.size() <= preferredNeighbors.size() + 1 || (newOptimisticallyUnchokedNeighbor != optimisticallyUnchockedNeighbor &&
                                !preferredNeighbors.contains(newOptimisticallyUnchokedNeighbor))) {
                            break;
                        }
                    }
                } else {
                    if (myPeerInfo.hasFile() && bitMap.size() == peerInfoMap.size()) {
                        try {
                            server.closeServer();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    if(clients.size() != 0 && newOptimisticallyUnchokedNeighbor != -1 && newOptimisticallyUnchokedNeighbor != optimisticallyUnchockedNeighbor) {
                        System.out.println(myPeerInfo.getPeerId() + " send unchoke to newOptimisticallyUnchokedNeighbor " + newOptimisticallyUnchokedNeighbor);
                        clients.get(newOptimisticallyUnchokedNeighbor).sendMessage(MessageFactory.unchockMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!preferredNeighbors.contains(optimisticallyUnchockedNeighbor)) {
                    try {
                        if(clients.size() != 0 && optimisticallyUnchockedNeighbor != -1) {

                            clients.get(optimisticallyUnchockedNeighbor).sendMessage(MessageFactory.chockMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                optimisticallyUnchockedNeighbor = newOptimisticallyUnchokedNeighbor;
                // TODO log writer
            }
        }, 0, 1000 * commonInfo.getOptimisticUnchokingInterval());
    }

    public int handleHandShakeMessage(byte[] bytes) {
        int peerId = 0;
        try {
            HandshakeMessage hsm = new HandshakeMessage(bytes);
            peerId = hsm.getPeerId();
            System.out.println(myId + " received handshake from " + peerId);
        } catch (Exception e) {
            System.out.println("handshake message error: " + e);
        }
        if(!hasHandshaked.contains(peerId)) {
            clients.put(peerId, new Client(myId, peerInfoMap.get(peerId)));
            clients.get(peerId).start();
            hasHandshaked.add(peerId);
        }
        if(!allFalse(myBitField)) {
            System.out.println("sent bitfield to " + peerId);
            clients.get(peerId).sendMessage(MessageFactory.bitfieldMessage(myBitField));
        }
        return peerId;
    }

    private boolean allFalse(boolean[] bitfield) {
        for(boolean b : bitfield) {
            if(b) return false;
        }
        return true;
    }

    public void handleActualMessage(byte[] bytes, int peerId) {
        ActualMessage respMessage = null;
        try {
            ActualMessage am = new ActualMessage(bytes);
            switch(am.getType()) {
                case 0:
                    respMessage = handleChockMessage(am.getPayload(), peerId);
                    break;
                case 1:
                    respMessage = handleUnchockMessage(am.getPayload(), peerId);
                    break;
                case 2:
                    respMessage = handleInterestedMessage(am.getPayload(), peerId);
                    break;
                case 3:
                    respMessage = handleUnInterestedMessage(am.getPayload(), peerId);
                    break;
                case 4:
                    respMessage = handleHaveMessage(am.getPayload(), peerId);
                    break;
                case 5:
                    respMessage = handleBitfieldMessage(am.getPayload(), peerId);
                    break;
                case 6:
                    respMessage = handleRequestMessage(am.getPayload(), peerId);
                    break;
                case 7:
                    respMessage = handlePieceMessage(am.getPayload(), peerId);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(respMessage != null) {
            clients.get(peerId).sendMessage(respMessage);
        }
    }

    private ActualMessage handleChockMessage(byte[] payload, int peerId) {
        logWriter.choking(myId, peerId);
        if (requestedMap.containsKey(peerId)) {
            requestedMap.remove(peerId);
        }
        return null;
    }

    private ActualMessage handleUnchockMessage(byte[] payload, int peerId) {
        System.out.println("un " + peerId);
        logWriter.unchoking(myId, peerId);
        return requestPiece(peerId);
    }

    private ActualMessage handleInterestedMessage(byte[] payload, int peerId) {
        logWriter.receiveInterestedMessage(myId, peerId);
        interested.add(peerId);
        return null;
    }

    private ActualMessage handleUnInterestedMessage(byte[] payload, int peerId) {
        logWriter.receiveNotInterestedMessage(myId, peerId);
        if(interested.contains(peerId)) {
            interested.remove(peerId);
        }
        return null;
    }

    private ActualMessage handleHaveMessage(byte[] payload, int peerId) {
        int index = ByteBuffer.wrap(payload).getInt();
        logWriter.receiveHaveMessage(myId, peerId, index);
        bitMap.get(peerId)[index] = true;
        if(!myBitField[index]) {
            return MessageFactory.interestedMessage();
        }
        return null;
    }

    private ActualMessage handleBitfieldMessage(byte[] payload, int peerId) {
        bitMap.put(peerId, new boolean[(commonInfo.getFileSize() + commonInfo.getPieceSize() - 1)/commonInfo.getPieceSize()]);
        boolean interestFlag = false;
        byte[] array = new byte[payload.length * 8];
        for (int i = 0; i < payload.length; i++) {
            for (int k = 7; k >= 0; k--) {
                array[k + i * 8] = (byte) (payload[i] & 1);
                payload[i] = (byte) (payload[i] >> 1);
            }
        }
        for (int i = 0; i < bitMap.get(peerId).length; i++) {
            if (array[i] == 1) {
                bitMap.get(peerId)[i] = true;
                if (!myBitField[i]) interestFlag = true;
            }
        }
        if (interestFlag) return MessageFactory.interestedMessage();
        return MessageFactory.uninterestedMessage();
    }

    private ActualMessage handleRequestMessage(byte[] payload, int peerId) {
        return MessageFactory.pieceMessage(payload, myId, commonInfo);
    }

    private ActualMessage handlePieceMessage(byte[] payload, int peerId) {
        int pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).getInt();
        byte[] content = Arrays.copyOfRange(payload, 4, payload.length);
        FileHandler fh = new FileHandler(commonInfo);
        try {
            fh.writePiece(pieceIndex, myId, content);
        }catch (IOException e) {
            e.printStackTrace();
        }

        requestedMap.remove(peerId, pieceIndex);
        downloadRateMap.put(peerId, downloadRateMap.getOrDefault(peerId, 0) + 1);

        // update bitfield
        updateBitField(Arrays.copyOfRange(payload, 0, 4));

        // finally should send not interested to some nodes
        int len = preferredNeighbors.size();
        int currentPieceNum = countPiece();
        for(int i = 0; i < len; i++) {
            int preferredNeighborID = preferredNeighbors.get(i);
            if(currentPieceNum == myBitField.length || containAllNeighborFiles(preferredNeighborID)) {
                ActualMessage respMessage = MessageFactory.uninterestedMessage();
                clients.get(peerId).sendMessage(respMessage);
            }
        }

        if(!containAllNeighborFiles(peerId)) {
            if(preferredNeighbors.contains(peerId)) {
                return requestPiece(peerId);
            }
        }
        logWriter.downloadPiece(myId, peerId, pieceIndex, currentPieceNum);
        return MessageFactory.uninterestedMessage();
    }

    private void updateBitField(byte[] payload) {
        int pieceIndex = ByteBuffer.wrap(payload).getInt();
        myBitField[pieceIndex] = true;
    }

    private int countPiece() {
        int cnt = 0;
        for(boolean hasFile : myBitField) {
            if(hasFile) {
                ++cnt;
            }
        }
        return cnt;
    }

    private boolean containAllNeighborFiles(int neighborId) {
        boolean[] neighborBitField = bitMap.get(neighborId);
        int len = neighborBitField.length;
        for(int i = 0; i < len; i++) {
            if(neighborBitField[i]) {
                if(!myBitField[i]) {
                    return false;
                }
            }
        }
        return true;

    }

    private ActualMessage requestPiece(int peerId) {
        List<Integer> index = new ArrayList<>();
        for(int i = 0; i < myBitField.length; ++i) {
            if(!myBitField[i]) {
                index.add(i);
            }
        }
        int missingPieceSize = index.size();
        if(missingPieceSize == 0) {
            return null;
        }
        int i = new Random().nextInt(missingPieceSize);
        while(requestedMap.containsValue(index.get(i))) {
            i = new Random().nextInt(missingPieceSize);
        }
        requestedMap.put(peerId, index.get(i));
        return MessageFactory.requestMessage(index.get(i));
    }
}

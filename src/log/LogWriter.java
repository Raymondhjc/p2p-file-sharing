package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class LogWriter {
    // Create log: filename = 'log_peer_[peerID].log'
    public void createLogFile(int peerID) {
        String filename = "peer_" + peerID + "/log_peer_" + peerID + ".log";
        File output = new File("peer_" + peerID);
        output.mkdir();

        File file = new File(filename);

        try {
            if(!file.exists()) {
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("Log File for peer_" + peerID + ".");
                bw.newLine();
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get Time for log
    public String getTime() {
        Date date = new Date();
        return (date.toString());
    }

    public void tcpConnectionTo(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer" + peer_id_1 + " makes a connection to Peer" + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void tcpConnectionFrom(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " is connected to Peer " + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void changePreferredNeighbors(int peer_id, int[] neighbors_list) {
        String filename = "peer_" + peer_id + "/log_peer_" + peer_id + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id + " has the preferred neighbors ");
            for(int i = 0; i < neighbors_list.length-1; i++) {
                bw.write(neighbors_list[i] + ",");
            }
            bw.write(neighbors_list[neighbors_list.length-1] + ".");
            bw.newLine();
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void changeOptimisticallyUnchockedNeighbor(int peer_id, int opt_neighbor_id) {
        String filename = "peer_" + peer_id + "/log_peer_" + peer_id + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id + " has the optimistically unchoked neighbor " + opt_neighbor_id + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unchoking(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " is unchoked by " + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void choking(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " is choked by " + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveHaveMessage(int peer_id_1, int peer_id_2, int index) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " received the 'have' message from " + peer_id_2 + " for the piece " + index + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveInterestedMessage(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " received the 'interested' message from " + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveNotInterestedMessage(int peer_id_1, int peer_id_2) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " received the 'not interested' message from " + peer_id_2 + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadPiece(int peer_id_1, int peer_id_2, int index, int piece_num) {
        String filename = "peer_" + peer_id_1 + "/log_peer_" + peer_id_1 + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id_1);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id_1 + " has downloaded the piece " + index + " from " + peer_id_2 + ". Now the number of pieces it has is " + piece_num + ".");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void completionOfDownload(int peer_id) {
        String filename = "peer_" + peer_id + "/log_peer_" + peer_id + ".log";
        File file = new File(filename);
        if(!file.exists()) {
            createLogFile(peer_id);
        }

        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(getTime() + ": Peer " + peer_id + " has downloaded the complete file.");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

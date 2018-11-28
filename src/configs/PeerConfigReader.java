package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PeerConfigReader {
    public static List<PeerInfo> readPeerConfig(String peerConfigDir) {
        String st;
        List<PeerInfo> list = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(peerConfigDir));
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                int peerId = Integer.parseInt(tokens[0]);
                String peerAddress = tokens[1];
                int peerPort = Integer.parseInt(tokens[2]);
                boolean hasFile = Integer.parseInt(tokens[3]) == 1;
                list.add(new PeerInfo(peerId, peerAddress, peerPort, hasFile));
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return list;
    }
}

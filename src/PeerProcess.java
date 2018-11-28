import configs.CommonConfigReader;
import configs.CommonInfo;
import configs.PeerConfigReader;
import configs.PeerInfo;

import java.util.List;

public class PeerProcess {
    public static void main(String[] args) {
        if(args == null || args.length == 0) {
            System.out.println("expect argument peer ID");
        }
        CommonInfo commonInfo = CommonConfigReader.readCommonConfig("Common.cfg");
        List<PeerInfo> peerList = PeerConfigReader.readPeerConfig("PeerInfo.cfg");
        int peerId = Integer.parseInt(args[0]);;
        Peer peer = new Peer(peerId, commonInfo);
        try {
            peer.start(peerList);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class PingReply extends Packet implements Serializable {

    public PingReply(KadNode source, KadNode dest)
    {
        super(source, dest);
    }
}

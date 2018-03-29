package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class PingReply implements Serializable {

    private KadNode source;
    private KadNode dest;

    public PingReply(KadNode source, KadNode dest)
    {
        this.source = source;
        this.dest = dest;
    }

    public KadNode getSourceKadNode()
    {
        return source;
    }

    public KadNode getDestKadNode()
    {
        return dest;
    }
}

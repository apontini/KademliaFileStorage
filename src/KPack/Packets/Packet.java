package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class Packet implements Serializable{

    private final KadNode source;
    private final KadNode dest;

    public Packet(KadNode source, KadNode dest)
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

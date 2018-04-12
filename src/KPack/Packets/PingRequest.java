package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class PingRequest extends Packet implements Serializable {

    public PingRequest(KadNode source, KadNode dest)
    {
        super(source, dest);
    }
}

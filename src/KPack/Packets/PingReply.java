package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class PingReply implements Serializable
{
    private KadNode kn;

    public PingReply(KadNode kn)
    {
        this.kn=kn;
    }

    public KadNode getKadNode() {
        return kn;
    }
}



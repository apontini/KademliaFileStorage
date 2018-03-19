package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;

public class PingRequest implements Serializable
{
    private KadNode kn;

    public PingRequest(KadNode kn)
    {
       this.kn=kn;
    }

    public KadNode getKadNode() {
        return kn;
    }
}



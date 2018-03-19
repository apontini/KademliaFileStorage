package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;

public class FindNodeRequest implements Serializable
{
    private final KadNode kn;
    private BigInteger targetID;

    public FindNodeRequest (BigInteger targetID,KadNode kn)
    {
        this.targetID=targetID;
        this.kn=kn;
    }

    public KadNode getKadNode() {
        return kn;
    }

    public BigInteger getTargetID() {
        return targetID;
    }
}



package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;

public class FindNodeRequest implements Serializable
{
    private KadNode sourceKN;
    private KadNode destKN;
    private BigInteger targetID;

    public FindNodeRequest (BigInteger targetID,KadNode sourceKN,KadNode destKN)
    {
        this.targetID=targetID;
        this.sourceKN=sourceKN;
        this.destKN=destKN;
    }

    public KadNode getSourceKadNode() {
        return sourceKN;
    }

    public KadNode getDestKadNode() {
        return destKN;
    }

    public BigInteger getTargetID() {
        return targetID;
    }
}



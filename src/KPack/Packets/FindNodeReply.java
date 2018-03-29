package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class FindNodeReply implements Serializable {

    private BigInteger targetID;
    private KadNode sourceKN;
    private KadNode destKN;
    private List<KadNode> lkn;

    public FindNodeReply(BigInteger targetID, KadNode sourceKN, KadNode destKN, List<KadNode> lkn)
    {
        this.targetID = targetID;
        this.sourceKN = sourceKN;
        this.destKN = destKN;
        this.lkn = lkn;
    }

    public BigInteger getTargetID()
    {
        return targetID;
    }

    public KadNode getSourceKN()
    {
        return sourceKN;
    }

    public KadNode getDestKN()
    {
        return destKN;
    }

    public List<KadNode> getList()
    {
        return lkn;
    }
}

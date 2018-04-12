package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class FindNodeReply extends Packet implements Serializable {

    private BigInteger targetID;
    private List<KadNode> lkn;

    public FindNodeReply(BigInteger targetID, KadNode sourceKN, KadNode destKN, List<KadNode> lkn)
    {
        super(sourceKN, destKN);
        this.targetID = targetID;
        this.lkn = lkn;
    }

    public BigInteger getTargetID()
    {
        return targetID;
    }

    public List<KadNode> getList()
    {
        return lkn;
    }
}

package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class FindNodeReply implements Serializable
{
    private final short idCommand=8;
    private BigInteger targetID;
    private List<KadNode> lkn;

    public FindNodeReply(BigInteger targetID, List<KadNode> lkn)
    {
       this.targetID=targetID;
       this.lkn=lkn;
    }

    public short getIdCommand() {
        return idCommand;
    }

    public List<KadNode> getLkn() {
        return lkn;
    }

    public BigInteger getTargetID() {
        return targetID;
    }
}



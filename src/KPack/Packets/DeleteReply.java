package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class DeleteReply implements Serializable
{
    private BigInteger idFileDelete;
    private KadNode kn;

    public DeleteReply(BigInteger idFileDelete, KadNode kn)
    {
        this.idFileDelete=idFileDelete;
        this.kn=kn;
    }

    public BigInteger getIdFileDelete() {
        return idFileDelete;
    }

    public KadNode getKadNode() {
        return kn;
    }
}

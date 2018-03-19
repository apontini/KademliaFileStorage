package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class StoreReply implements Serializable
{
    private final KadNode kn;
    private BigInteger idFileStore;

    public StoreReply(BigInteger idFileStore, KadNode kn)
    {
        this.idFileStore=idFileStore;
        this.kn=kn;
    }

    public BigInteger getIdFileStore() {
        return idFileStore;
    }

    public KadNode getKadNode() {
        return kn;
    }
}

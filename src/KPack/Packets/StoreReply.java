package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class StoreReply implements Serializable
{
    private final KadNode source;
    private final KadNode dest;
    private BigInteger idFileStore;

    public StoreReply(BigInteger idFileStore, KadNode source, KadNode dest)
    {
        this.idFileStore=idFileStore;
        this.source = source;
        this.dest = dest;
    }

    public BigInteger getIdFileStore() {
        return idFileStore;
    }

    public KadNode getSourceKadNode() {
        return source;
    }

    public KadNode getDestKadNode() { return dest;}
}

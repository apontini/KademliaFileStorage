package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class FindValueRequest implements Serializable
{
    private final KadNode kn;
    private BigInteger fileID;

    public FindValueRequest(BigInteger fileID, KadNode kn)
    {
        this.fileID = fileID;
        this.kn=kn;
    }

    public KadNode getKadNode() {
        return kn;
    }

    public BigInteger getFileID() {
        return fileID;
    }
}



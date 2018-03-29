package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class FindValueRequest implements Serializable {

    private final KadNode kn;
    private BigInteger fileID;
    private boolean contentRequested;

    public FindValueRequest(BigInteger fileID, KadNode kn, boolean contentRequested)
    {
        this.fileID = fileID;
        this.kn = kn;
        this.contentRequested = contentRequested;
    }

    public KadNode getKadNode()
    {
        return kn;
    }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public boolean isContentRequested()
    {
        return contentRequested;
    }
}

package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class FindValueRequest implements Serializable {

    private final KadNode source;
    private final KadNode dest;
    private BigInteger fileID;
    private boolean contentRequested;

    public FindValueRequest(BigInteger fileID, KadNode source, KadNode dest, boolean contentRequested)
    {
        this.fileID = fileID;
        this.source = source;
        this.dest = dest;
        this.contentRequested = contentRequested;
    }

    public KadNode getSourceKadNode()
    {
        return source;
    }

    public KadNode getDestKadNode() { return dest; }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public boolean isContentRequested()
    {
        return contentRequested;
    }
}

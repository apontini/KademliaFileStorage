package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class FindValueRequest extends Packet implements Serializable {

    private BigInteger fileID;
    private boolean contentRequested;

    public FindValueRequest(BigInteger fileID, KadNode source, KadNode dest, boolean contentRequested)
    {
        super(source, dest);
        this.fileID = fileID;
        this.contentRequested = contentRequested;
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

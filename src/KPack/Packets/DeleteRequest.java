package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class DeleteRequest implements Serializable {

    private final KadNode source;
    private final KadNode dest;
    private BigInteger fileID;
    private String fileName;

    public DeleteRequest(BigInteger id, KadNode source, KadNode dest)
    {
        this.fileID = id;
        this.source = source;
        this.dest = dest;
    }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public String getFileName()
    {
        return fileName;
    }

    public KadNode getSourceKadNode()
    {
        return source;
    }

    public KadNode getDestKadNode()
    {
        return dest;
    }
}

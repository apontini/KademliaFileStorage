package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class DeleteRequest implements Serializable
{
    private final KadNode kn;
    private BigInteger fileID;
    private String fileName;

    public DeleteRequest(KadFileInterf kf, KadNode kn)
    {
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();
        this.kn=kn;
    }

    public BigInteger getFileID() {
        return fileID;
    }

    public String getFileName() {
        return fileName;
    }

    public KadNode getKadNode() {
        return kn;
    }
}



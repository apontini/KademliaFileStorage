package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FindValueReply implements Serializable
{
    private final String fileName;
    private BigInteger fileID;
    private byte[] content;
    private List<KadNode> lkn;
    private KadNode kn;

    public FindValueReply(BigInteger fileID, List<KadNode> lkn, KadFileInterf kf,KadNode kn)
    {
        this.fileID=fileID;
        this.kn=kn;
        this.lkn=lkn;
        if(kf==null) {
            content=null;
            fileName=null;
        }
        else {
            this.fileName = kf.getFileName();

            Path path= Paths.get(kf.getPath());
            try {
                content= Files.readAllBytes(path);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BigInteger getFileID() {
        return fileID;
    }

    public byte[] getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public List<KadNode> getListKadNode() {
        return lkn;
    }

    public KadNode getKadNode() {
        return kn;
    }
}


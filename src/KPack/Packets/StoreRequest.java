package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StoreRequest implements Serializable
{
    private BigInteger fileID;
    private String fileName;
    private KadNode kn;
    private byte[] content;

    public StoreRequest(KadFileInterf kf, KadNode kn)
    {
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();

        Path path= Paths.get(kf.getPath());
        try {
            content= Files.readAllBytes(path);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public KadNode getKadNode(){
        return kn;
    }

    public BigInteger getFileID() {
        return fileID;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent(){
        return content;
    }
}


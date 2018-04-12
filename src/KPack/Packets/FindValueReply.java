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

public class FindValueReply extends Packet implements Serializable {

    private final String fileName;
    private BigInteger fileID;
    private byte[] content;
    private List<KadNode> lkn;

    public FindValueReply(BigInteger fileID, KadFileInterf kf, KadNode source, KadNode dest)
    {
        super(source, dest);
        this.fileID = fileID;
        this.lkn = lkn;
        if (kf == null)
        {
            content = null;
            fileName = null;
        }
        else
        {
            this.fileName = kf.getFileName();

            Path path = Paths.get(kf.getPath());
            try
            {
                content = Files.readAllBytes(path);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public byte[] getContent()
    {
        return content;
    }

    public String getFileName()
    {
        return fileName;
    }

    public List<KadNode> getListKadNode()
    {
        return lkn;
    }
}

package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

public class StoreRequest extends Packet implements Serializable {

    private BigInteger fileID;
    private String fileName;
    private byte[] content;

    public StoreRequest(KadFileInterf kf, KadNode source, KadNode dest) throws IOException
    {
        super(source, dest);
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();
        //Orribile ma temporanea
        if (!((new File(kf.getPath())).isDirectory()))
        {
            throw new InvalidParameterException("Errore nella composizione del KadFile, nel path");
        }
        if ((new File(kf.getPath() + File.separator + kf.getFileName())).isDirectory())
        {
            throw new InvalidParameterException("Errore nella composizione del KadFile, il file è una directory");
        }
        Path path = Paths.get(kf.getPath() + File.separator + kf.getFileName());
        content = Files.readAllBytes(path);
    }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public String getFileName()
    {
        return fileName;
    }

    public byte[] getContent()
    {
        return content;
    }
}

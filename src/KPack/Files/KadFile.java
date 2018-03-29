package KPack.Files;

import java.io.Serializable;
import java.math.BigInteger;

public class KadFile implements KadFileInterf, Serializable {

    //deve mantenere singolo file
    private BigInteger fileID;
    private boolean redundant; //false se il file è mio o true se mi è stato dato grazie al protocollo della rete Kademlia
    private String fileName;
    private String path;

    public KadFile(BigInteger fileID, boolean redundant, String fileName, String path)
    {
        this.fileID = fileID;
        this.redundant = redundant;
        this.fileName = fileName;
        this.path = path;
    }

    public BigInteger getFileID()
    {
        return fileID;
    }

    public boolean isRedundant()
    {
        return redundant;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public String toString()
    {
        return "KadFile{"
                + "fileID=" + fileID
                + ", redundant=" + redundant
                + ", fileName='" + fileName + '\''
                + ", path='" + path + '\''
                + '}';
    }
}

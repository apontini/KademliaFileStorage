package KPack.Files;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class KadFile implements KadFileInterf, Serializable {

    //deve mantenere singolo file
    private BigInteger fileID;
    private boolean redundant; //false se il file è mio o true se mi è stato dato grazie al protocollo della rete Kademlia
    private String fileName;
    private String path;
    private long lastRefresh;

    public KadFile(BigInteger fileID, boolean redundant, String fileName, String path)
    {
        this.fileID = fileID;
        this.redundant = redundant;
        this.fileName = fileName;
        this.path = path;
        if(redundant) lastRefresh = System.currentTimeMillis();
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

    public long getLastRefresh() { return lastRefresh; }

    public void setLastRefresh(long lastRefresh) { this.lastRefresh = lastRefresh;}

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

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final KadFile other = (KadFile) obj;
        if (!Objects.equals(this.fileName, other.fileName))
        {
            return false;
        }
        if (!Objects.equals(this.fileID, other.fileID))
        {
            return false;
        }
        return true;
    }
    
    
}

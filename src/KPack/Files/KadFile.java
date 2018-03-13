package KPack.Files;

import java.math.BigInteger;

public class KadFile implements KadFileInterf
{
    //deve mantenere singolo file
    private BigInteger fileID;
    private boolean redundant;
    private String fileName;
    private String path;

    public KadFile(BigInteger fileID, boolean redundant, String fileName, String path)
    {
        this.fileID = fileID;
        this.redundant = redundant;
        this.fileName = fileName;
        this.path = path;
    }

    public BigInteger getFileID() {
        return fileID;
    }

    public boolean isRedundant() {
        return redundant;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }
}

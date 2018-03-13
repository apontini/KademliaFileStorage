package KPack.Files;

import java.math.BigInteger;

public class KadFile implements KadFileInterf
{
    //deve mantenere singolo file
    private BigInteger fileID;
    private boolean redundant;
    private String fileName;
    private String path;

    public KadFile()
    {

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

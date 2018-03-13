package KPack.Files;

import java.math.BigInteger;

public interface KadFileInterf
{
    public BigInteger getFileID();

    public boolean isRedundant();

    public String getFileName();

    public String getPath();

}

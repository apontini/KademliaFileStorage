package KPack.Files;

import java.math.BigInteger;

public interface KadFileInterf {

    BigInteger getFileID();

    boolean isRedundant();

    String getFileName();

    String getPath();

    long getLastRefresh();
}

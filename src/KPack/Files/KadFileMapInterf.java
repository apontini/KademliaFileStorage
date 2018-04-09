package KPack.Files;

import java.math.BigInteger;
import java.util.Iterator;

public interface KadFileMapInterf
{

    void add(KadFile file);

    void remove(KadFile file);

    KadFile get(BigInteger i);

    void clearRedundants();

    void clearAll();

    int size();
}

package KPack.Files;

import java.math.BigInteger;
import java.util.Iterator;

public interface KadFileMapInterf {

    void add(KadFile file);

    void remove(KadFile file);

    void remove(BigInteger ID, boolean redundant);
    
    void remove(BigInteger ID);

    KadFile get(BigInteger i, boolean redundant);

    void clearRedundants();

    void clearAll();

    int size();
}

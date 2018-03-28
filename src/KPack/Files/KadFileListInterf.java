package KPack.Files;

import java.util.Iterator;

public interface KadFileListInterf
{
    void add(KadFile file);

    void remove(KadFile file);

    KadFile get(int i);

    int contains(KadFile file);

    int size();

    void clear();

    Iterator<KadFile> getListIterator();
}

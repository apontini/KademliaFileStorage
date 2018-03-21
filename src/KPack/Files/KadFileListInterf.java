package KPack.Files;

public interface KadFileListInterf
{
    void add(KadFile file);

    void remove(KadFile file);

    KadFile get(int i);

    int contains(KadFile file);

    int size();
}

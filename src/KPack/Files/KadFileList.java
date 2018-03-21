package KPack.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KadFileList
{
    private List<KadFile> fileList;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();


    public KadFileList()
    {
        fileList = new ArrayList<KadFile>();
    }

    public void add(KadFile file)
    {
        writeLock.lock();
        fileList.add(file);
        //Aggiornare il file index
        writeLock.unlock();
    }

    public void remove(KadFile file)
    {
        writeLock.lock();
        fileList.remove(file);
        //Aggiornare il file index
        writeLock.unlock();
    }

    public KadFile get(int i)
    {
        readLock.lock();
        try
        {
            return fileList.get(i);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public int indexOf(KadFile file)
    {
        readLock.lock();
        try
        {
            return fileList.indexOf(file);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public int size() //e se size cambia mentre sto ciclando un for per esempio?
    {
        readLock.lock();
        try
        {
            return fileList.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    private void indexRefresh()
    {
        
    }
}

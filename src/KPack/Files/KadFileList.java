package KPack.Files;

import KPack.Kademlia;

import java.io.*;
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
    private Kademlia thisNode;

    public KadFileList(Kademlia thisNode)
    {
        fileList = loadListFromFile();
        this.thisNode = thisNode;
    }

    public void add(KadFile file)
    {
        writeLock.lock();
        fileList.add(file);
        indexRefresh();
        writeLock.unlock();
    }

    public void remove(KadFile file)
    {
        writeLock.lock();
        fileList.remove(file);
        indexRefresh();
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

    public int size() //e se size cambia mentre sto ciclando un for per esempio? Prendo il lock dell'oggetto KadFileList?
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
        try
        {
            File temp = new File(thisNode.FILESPATH);
            if(!(temp.exists())) temp.mkdir();
            File localFiles = new File(thisNode.FILESPATH + "index");
            if(!(localFiles.exists())) localFiles.createNewFile();

            FileOutputStream fout = new FileOutputStream(thisNode.FILESPATH + "index");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(fileList);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private ArrayList<KadFile> loadListFromFile()
    {

        ArrayList<KadFile> ret = new ArrayList<>();

        File temp = new File(thisNode.FILESPATH);
        if(!(temp.exists())) temp.mkdir();

        File localFiles = new File(thisNode.FILESPATH + "index");

        if(localFiles.exists())
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(thisNode.FILESPATH + "index");
                ObjectInputStream ois = new ObjectInputStream(fis);
                ret = new ArrayList<>();
                while(true)
                {
                    ret = ((ArrayList<KadFile>)ois.readObject());
                }
            }
            catch (EOFException | FileNotFoundException | ClassNotFoundException e)
            {
                //Aspettate o impossibili
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
            finally
            {
                try { if (fis != null) fis.close();}
                catch (IOException ioe) {} //Ignorata
                finally { return ret; }
            }
        }

        try
        {
            localFiles.createNewFile();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            return ret;
        }
    }
}

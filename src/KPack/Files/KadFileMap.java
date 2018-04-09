package KPack.Files;

import KPack.Kademlia;

import java.io.*;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class KadFileMap implements KadFileMapInterf {

    private ConcurrentHashMap<BigInteger,KadFile> fileMap;
    private Kademlia thisNode;

    public KadFileMap(Kademlia thisNode)
    {
        fileMap = loadListFromFile();
        this.thisNode = thisNode;
    }

    public void add(KadFile file)
    {
        fileMap.put(file.getFileID(),file);
        if (!file.isRedundant())
        {
            serializeList();
        }
    }

    public void remove(KadFile file)
    {
        KadFile temp = fileMap.get(file.getFileID());

        if (temp == null)
        {
            return;
        }

        fileMap.remove(temp);

        if (file.isRedundant())
        {
            new File(file.getPath() + File.pathSeparator + file.getFileName()).delete();
        }
        serializeList();
    }

    public void remove(BigInteger ID)
    {

        KadFile temp = fileMap.get(ID);

        if (temp == null)
        {
            return;
        }
        fileMap.remove(temp);
        if (temp.isRedundant())
        {
            new File(temp.getPath() + File.pathSeparator + temp.getFileName()).delete();
        }
        serializeList();
    }

    public void forEach(BiConsumer<BigInteger, KadFile> function)
    {
        fileMap.forEach(function);
    }

    public void clearAll()
    {
        fileMap.clear();
    }

    public void clearRedundants()
    {
        fileMap.forEach((k,v)->
                {
                    if (v.isRedundant())
                    {
                        this.remove(k);
                    }
                }
        );
    }

    public KadFile get(BigInteger i)
    {
        return fileMap.get(i);
    }

    public int size()
    {
        return fileMap.size();
    }

    private void serializeList()
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try
        {
            File temp = new File(thisNode.FILESPATH);
            if (!(temp.exists()))
            {
                temp.mkdir();
            }
            File localFiles = new File(thisNode.FILESPATH + "index");
            if (!(localFiles.exists()))
            {
                localFiles.createNewFile();
            }

            fout = new FileOutputStream(thisNode.FILESPATH + "index");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(fileMap);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fout != null)
                {
                    fout.close();
                }
                if (oos != null)
                {
                    oos.close();
                }
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    private ConcurrentHashMap<BigInteger, KadFile> loadListFromFile()
    {

        ConcurrentHashMap<BigInteger, KadFile> ret = new ConcurrentHashMap<>();
        File temp = new File(thisNode.FILESPATH);
        if (!(temp.exists()))
        {
            temp.mkdir();
        }

        File localFiles = new File(thisNode.FILESPATH + "index");
        if (localFiles.exists())
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(thisNode.FILESPATH + "index");
                ObjectInputStream ois = new ObjectInputStream(fis);
                while (true)
                {
                    ret = ((ConcurrentHashMap<BigInteger, KadFile>) ois.readObject());
                }
            }
            catch (EOFException | FileNotFoundException | ClassNotFoundException e)
            {
                //Aspettate o impossibili
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            finally
            {
                try
                {
                    if (fis != null)
                    {
                        fis.close();
                    }
                }
                catch (IOException ioe)
                {
                } //Ignorata
                finally
                {
                    return ret;
                }
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

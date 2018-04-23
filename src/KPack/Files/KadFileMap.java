package KPack.Files;

import KPack.Kademlia;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class KadFileMap implements KadFileMapInterf {

    private HashMap<BigInteger, KadFile> fileMap;
    private Kademlia thisNode;
    private boolean serialized;

    public KadFileMap(Kademlia thisNode, boolean serialized)
    {
        if(serialized) fileMap = loadListFromFile();
        this.thisNode = thisNode;
        this.serialized = serialized;
    }

    synchronized public void add(KadFile file)
    {
        fileMap.put(file.getFileID(), file);
        if(serialized) serializeMap();
    }

    synchronized public void remove(KadFile file)
    {
        remove(file.getFileID());
    }

    synchronized public void remove(BigInteger ID)
    {
        KadFile temp = fileMap.remove(ID);

        if(serialized)
        {
            serializeMap();
        }
        else
        {
            new File(temp.getPath() + File.separator + temp.getFileName()).delete();
        }
    }

    public void forEach(BiConsumer<BigInteger, KadFile> function) //Prendere il lock per usare questo metodo!
    {
        fileMap.forEach(function);
    }

    synchronized public void clearAll()
    {
        fileMap.clear();
    }

    synchronized public KadFile get(BigInteger i)
    {
        return fileMap.get(i);
    }

    synchronized public int size() //Prendere il lock per ciclare su size!
    {
        return fileMap.size();
    }

    private void serializeMap()
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

    private HashMap<BigInteger, KadFile> loadListFromFile()
    {

        HashMap<BigInteger, KadFile> ret = new HashMap<>();
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
                    ret = ((HashMap<BigInteger, KadFile>) ois.readObject());
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

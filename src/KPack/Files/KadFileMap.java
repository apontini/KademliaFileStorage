package KPack.Files;

import KPack.Kademlia;
import KPack.Tupla;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;

public class KadFileMap implements KadFileMapInterf {

    private HashMap<Tupla<BigInteger, Boolean>, KadFile> fileMap;
    private Kademlia thisNode;

    public KadFileMap(Kademlia thisNode)
    {
        fileMap = loadListFromFile();
        this.thisNode = thisNode;
    }

    synchronized public void add(KadFile file)
    {
        if (fileMap.containsKey(new Tupla<BigInteger, Boolean>(file.getFileID(), file.isRedundant())))
        {
            return;
        }
        fileMap.put(new Tupla<BigInteger, Boolean>(file.getFileID(), file.isRedundant()), file);

        serializeMap();
    }

    synchronized public void remove(KadFile file)
    {
        remove(file.getFileID());
    }

    synchronized public void remove(BigInteger ID, boolean redundant)
    {
        KadFile temp = fileMap.remove(new Tupla<BigInteger, Boolean>(ID, true));
        if (temp != null)
        {
            System.out.println("ELIMINO: " + temp.getPath() + File.separator + temp.getFileName());
            System.out.println();
            new File(temp.getPath() + File.separator + temp.getFileName()).delete();
        }
        if (!redundant)
        {
            fileMap.remove(new Tupla<BigInteger, Boolean>(ID, false));
        }

        serializeMap();
    }

    public void remove(BigInteger ID)
    {
        remove(ID, false);
    }

    public void forEach(BiConsumer<Tupla<BigInteger, Boolean>, KadFile> function) //Prendere il lock per usare questo metodo!
    {
        fileMap.forEach(function);
    }

    synchronized public void clearAll()
    {
        fileMap.clear();
    }

    synchronized public void clearRedundants()
    {
        fileMap.forEach((k, v) ->
        {
            if (k.getValue())
            {
                fileMap.remove(k);
            }
        }
        );
    }

    synchronized public KadFile get(BigInteger i, boolean redundant)
    {
        return fileMap.get(new Tupla<BigInteger, Boolean>(i, redundant));
    }

    synchronized public int size()
    {
        return fileMap.size();
    }

    private void serializeMap()
    {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        HashMap<Tupla<BigInteger, Boolean>, KadFile> toSave = new HashMap<>();
        fileMap.forEach((t, u) ->
        {
            if (!t.getValue())
            {
                toSave.put(t, u);
            }
        });
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
            oos.writeObject(toSave);
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

    private HashMap<Tupla<BigInteger, Boolean>, KadFile> loadListFromFile()
    {

        HashMap<Tupla<BigInteger, Boolean>, KadFile> ret = new HashMap<Tupla<BigInteger, Boolean>, KadFile>();
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
                    ret = ((HashMap<Tupla<BigInteger, Boolean>, KadFile>) ois.readObject());
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

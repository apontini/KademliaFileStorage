package KPack.Files;

import KPack.KadNode;
import KPack.Kademlia;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KadFileList implements Iterable<KadFile> {

    private List<KadFile> fileList;
    private Kademlia thisNode;

    public KadFileList(Kademlia thisNode)
    {
        fileList = loadListFromFile();
        this.thisNode = thisNode;
    }

    synchronized public void add(KadFile file)
    {
        fileList.add(file);
        if (!file.isRedundant())
        {
            serializeList();
        }
    }

    synchronized public void remove(KadFile file)
    {
        KadFile temp = null;

        for(KadFile i : fileList)
        {
            if (i.getFileID().equals(file.getFileID()))
            {
                temp = i;
                break;
            }
        }
        if(temp == null) return;

        fileList.remove(temp);

        if (file.isRedundant())
        {
            new File(file.getPath() + File.pathSeparator + file.getFileName()).delete();
        }
        serializeList();
    }

    synchronized public void remove(BigInteger ID)
    {

        KadFile temp = null;
        for(KadFile i : fileList)
        {
            if (i.getFileID().equals(ID))
            {
                temp = i;
                break;
            }
        }
        if(temp == null) return;
        fileList.remove(temp);
        if (temp.isRedundant())
        {
            new File(temp.getPath() + File.pathSeparator + temp.getFileName()).delete();
        }
        serializeList();
    }

    synchronized public void clearAll()
    {
        for (KadFile i : fileList)
        {
            this.remove(i);
        }
    }

    synchronized public void clearRedundants()
    {
        for (KadFile i : fileList)
        {
            if (i.isRedundant())
            {
                this.remove(i);
            }
        }
    }

    synchronized public KadFile get(int i)
    {
        return fileList.get(i);
    }

    synchronized public int indexOf(KadFile file)
    {
        return fileList.indexOf(file);
    }

    public Iterator<KadFile> iterator()
    {
        return fileList.listIterator();
    }

    synchronized public int size()
    {
        return fileList.size();
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
            oos.writeObject(fileList);
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

    private ArrayList<KadFile> loadListFromFile()
    {

        ArrayList<KadFile> ret = new ArrayList<>();

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
                ret = new ArrayList<>();
                while (true)
                {
                    ret = ((ArrayList<KadFile>) ois.readObject());
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

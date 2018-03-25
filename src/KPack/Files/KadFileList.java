package KPack.Files;

import KPack.Kademlia;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KadFileList implements Iterable<KadFile>
{
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
        serializeList();
    }

    synchronized public void remove(KadFile file)
    {
        fileList.remove(file); //override di equals() per eliminare il nodo solo usando l'ID?
        serializeList();
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

    synchronized public int size() //e se size cambia mentre sto ciclando un for per esempio? Prendo il lock dell'oggetto KadFileList?
    {
        return fileList.size();
    }

    private void serializeList()
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

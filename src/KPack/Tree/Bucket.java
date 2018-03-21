package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;
import java.util.Iterator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Bucket extends Node {

    //SOLO foglia
    private Node parent;
    private int dimensioneMax;
    private List<KadNode> listaNodi;
    private boolean splittable;
    private Kademlia thisKadNode;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public Bucket(Kademlia thisKadNode, boolean splittable)
    {
        listaNodi = new LinkedList<KadNode>();
        dimensioneMax = thisKadNode.K;
        this.splittable = splittable;
        this.thisKadNode = thisKadNode;
    }

    public boolean add(KadNode kn)
    {
        writeLock.lock();
        try
        {
            if (listaNodi.size() == dimensioneMax)
            {
                if (splittable)
                {
                    return false; //ci pensa l'albero
                }
                else
                {
                    //pingu i nodi, tutti quelli nella lista
                    for (KadNode node : listaNodi)
                    {
                        if (!thisKadNode.ping(node))
                        {
                            listaNodi.remove(node);
                            listaNodi.add(kn);
                            return true;
                        }
                    }
                    //Se non l'ho sostituito, scarto il nuovo nodo
                }
            }
            else
            {
                //C'è spazio, rimuoviamo il nodo (nel caso sia già presente) e lo riaggiungiamo
                //il nodo nuovo è in coda
                listaNodi.remove(kn);
                listaNodi.add(kn);
            }
            return true; //l'albero non deve gestire niente
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public Node getParent()
    {
        readLock.lock();
        try
        {
            return parent;
        }
        finally
        {
            readLock.unlock();
        }
    }

    public void setParent(Node parent)
    {
        writeLock.lock();
        try
        {
            this.parent = parent;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public int size()
    {
        readLock.lock();
        try
        {
            return listaNodi.size();
        }
        finally
        {
            readLock.unlock();
        }
    }

    public KadNode get(int i)
    {
        readLock.lock();
        try
        {
            return listaNodi.get(i);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public void setSplittable(boolean splittable)
    {
        writeLock.lock();
        try
        {
            this.splittable = splittable;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public Iterator<KadNode> getList()
    {
        return listaNodi.iterator();
    }

    public String toString()
    {
        String bu = "{\n";
        for (KadNode k : listaNodi)
        {
            bu += k.toString() + "\n";
        }
        bu += "}";
        return bu;
    }
}

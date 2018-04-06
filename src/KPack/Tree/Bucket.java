package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bucket extends Node implements Iterable {

    //SOLO foglia
    private int dimensioneMax;
    private List<KadNode> listaNodi;
    private boolean splittable;
    private Kademlia thisKadNode;

    public Bucket(Kademlia thisKadNode, boolean splittable)
    {
        listaNodi = new LinkedList<KadNode>();
        dimensioneMax = thisKadNode.K;
        this.splittable = splittable;
        this.thisKadNode = thisKadNode;
    }

    public synchronized boolean add(KadNode kn)
    {
        listaNodi.remove(kn);
        if (listaNodi.size() == dimensioneMax)
        {
            if (splittable)
            {
                return false; //ci pensa l'albero
            }
            else
            {
                //pingu 1 solo nodo (il più vecchio)
                KadNode node = listaNodi.get(0);
                if (node.equals(this.thisKadNode.getMyNode()))
                {
                    node = listaNodi.get(1);
                }
                if (!thisKadNode.ping(node))
                {
                    listaNodi.remove(node);
                    listaNodi.add(kn);
                    return true;
                }
                //Se non l'ho sostituito, scarto il nuovo nodo
            }
        }
        else
        {
            //C'è spazio, rimuoviamo il nodo (nel caso sia già presente) e lo riaggiungiamo
            //il nodo nuovo è in coda
            listaNodi.add(kn);
        }
        return true; //l'albero non deve gestire niente
    }

    public synchronized void removeFromBucket (KadNode kn) {  listaNodi.remove(kn); }

    public synchronized int size()
    {
        return listaNodi.size();
    }

    public synchronized KadNode get(int i)
    {
        return listaNodi.get(i);
    }

    public synchronized void setSplittable(boolean splittable)
    {
        this.splittable = splittable;
    }

    public synchronized Iterator<KadNode> iterator()
    {
        return listaNodi.iterator();
    }

    public synchronized String toString()
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

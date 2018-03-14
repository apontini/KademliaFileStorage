package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bucket extends Node
{
    //SOLO foglia
    private Node parent;
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

    public boolean add(KadNode kn)
    {
        if(listaNodi.size()==dimensioneMax)
        {
            if(splittable)
                return false; //ci pensa l'albero
            else
            {
                //pingu i nodi, tutti quelli nella lista
                for (KadNode node: listaNodi)
                {
                    if(!thisKadNode.ping(node))
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

    public Iterator<KadNode> getListIterator() {
        return listaNodi.iterator();
    }

    public void setSplittable(boolean splittable) {
        this.splittable = splittable;
    }
    
    public void clearBucket()
    {
        listaNodi.clear();
    }
}

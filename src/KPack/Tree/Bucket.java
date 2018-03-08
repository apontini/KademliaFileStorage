package KPack.Tree;

import KPack.KadNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bucket extends Node
{
    //SOLO foglia
    private Node parent;
    private int dimensioneMax;
    private List<KadNode> listaNodi;
    private int bucketNum; //TODO

    public Bucket(int dim)
    {
        listaNodi = new LinkedList<KadNode>();
        dimensioneMax = dim;
    }

    public boolean add(KadNode kn)
    {
        if(listaNodi.size()==dimensioneMax)
        {
            //Pinghiamo o torniamo false
            return false;
        }
        else
        {
            //C'è spazio, rimuoviamo il nodo (nel caso sia già presente) e lo riaggiungiamo
            listaNodi.remove(kn);
            listaNodi.add(kn);
        }
        return true;
    }

    public Iterator<KadNode> getListIterator() {
        return listaNodi.iterator();
    }
}

package KPack.Tree;

import KPack.KadNode;

import java.util.LinkedList;
import java.util.List;

public class Bucket extends Node
{
    //SOLO foglia
    private Node parent;
    private List<KadNode> listaNodi;

    public Bucket(int dim)
    {
        listaNodi = new LinkedList<KadNode>();
    }

    public Node getParent()
    {
        return parent;
    }

    public void setParent(Node parent)
    {
        this.parent = parent;
    }
}

package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;

public class RoutingTree
{
    private Node root;
    private static boolean instance = false;

    public RoutingTree(int bucketsDim)
    {
        if(instance) return; //TODO
        if(bucketsDim<=0) throw new java.lang.IllegalArgumentException("La dimensione deve essere maggiore di 0");
        root = new Bucket(bucketsDim);
    }

    public void add(KadNode nodo)
    {
        //TODO
    }

    public Bucket findNodesBucket(KadNode node)
    {
        //preghiamo gli dei del java e li ringraziamo per la loro benevolenza per la classe BigInteger

        Node curNode = root;
        int i = Kademlia.getK()-1;

        while(!(curNode instanceof Bucket))
        {
            if(node.getNodeID().testBit(i--))
                curNode = ((TreeNode)curNode).getLeft();
            else
                curNode = ((TreeNode)curNode).getRight();
        }
        return (Bucket)curNode;
    }
}

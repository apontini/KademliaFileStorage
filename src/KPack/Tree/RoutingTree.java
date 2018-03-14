package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;

import java.math.BigInteger;
import java.util.List;

public class RoutingTree
{
    private Node root;
    private static boolean instance = false;
    private Kademlia thisNode = null;
    private int bucketsDim;

    public RoutingTree(Kademlia thisNode)
    {
        if(instance) return;
        bucketsDim = Kademlia.K;
        if(bucketsDim<=0) throw new java.lang.IllegalArgumentException("La dimensione deve essere maggiore di 0");
        root = new Bucket(thisNode, true);
        this.thisNode = thisNode;
    }

    public void add(KadNode nodo)
    {
        Bucket tempBuck = findNodesBucket(nodo);
        if(!findNodesBucket(nodo).add(nodo))
        {
            TreeNode temp = new TreeNode();
            //TODO
        }
    }

    public Bucket findNodesBucket(KadNode node)
    {
        //preghiamo gli dei del java e li ringraziamo per la loro benevolenza per la classe BigInteger

        Node curNode = root;
        int i = Kademlia.K-1;

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

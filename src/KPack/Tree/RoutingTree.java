package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;

import java.math.BigInteger;
import java.util.Iterator;
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
        Bucket tempBuck;
        if(!(tempBuck = findNodesBucket(nodo)).add(nodo))
        {
            TreeNode temp = new TreeNode();
            Bucket bucketSx = new Bucket(thisNode, instance);   //SISTEMARE IL FLAG
            Bucket bucketDx = new Bucket(thisNode, instance);   //SISTEMARE IL FLAG
            
            temp.setLeft(bucketSx);
            temp.setRight(bucketDx);
            bucketSx.setParent(temp);
            bucketDx.setParent(temp);
            
            Node tempBuckParent = tempBuck.getParent();
            if(tempBuckParent==null)  //il genitore di tempBuck è null, quindi è tempBuck è la radice
                root=temp;
            else
            {
                temp.setParent(tempBuckParent);
                TreeNode tempBuckParentCast=(TreeNode)tempBuckParent;
                //temp diventa nodo destro o sinistro di tempBuckParent?
                if(tempBuckParentCast.getLeft().equals(tempBuck))
                    tempBuckParentCast.setLeft(temp);
                else
                    tempBuckParentCast.setRight(temp);
            }
            
            Iterator<KadNode> kadNodeIterator = tempBuck.getListIterator(); 
            while(kadNodeIterator.hasNext())
            {
                add(kadNodeIterator.next());
            }
            
            add(nodo);
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

package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;
import KPack.UserInterface.TreeUI;
import java.awt.HeadlessException;

import java.math.BigInteger;
import java.util.Iterator;

public class RoutingTree {

    private Node root;
    private static boolean instance = false;
    private Kademlia thisNode = null;
    private int bucketsDim;

    public RoutingTree(Kademlia thisNode)
    {
        if (instance)
        {
            return;
        }
        bucketsDim = Kademlia.K;
        if (bucketsDim <= 0)
        {
            throw new java.lang.IllegalArgumentException("La dimensione deve essere maggiore di 0");
        }
        root = new Bucket(thisNode, true);
        this.thisNode = thisNode;

        try
        {
            new TreeUI(this);
        }
        catch (HeadlessException he) //per dispositivi senza schermo
        {
        }
    }

    public synchronized void add(KadNode nodo)
    {
        Bucket tempBuck;
        while (!((tempBuck = findNodesBucket(nodo)).add(nodo)))
        {
            TreeNode temp = new TreeNode();
            Bucket bucketSx = new Bucket(thisNode, false);
            Bucket bucketDx = new Bucket(thisNode, false);

            temp.setLeft(bucketSx);
            temp.setRight(bucketDx);
            bucketSx.setParent(temp);
            bucketDx.setParent(temp);

            Node tempBuckParent = tempBuck.getParent();
            temp.setParent(tempBuckParent);
            //SOLO ORA vado a modificare il nodo che dev'essere splittato
            if (tempBuckParent == null)  //il genitore di tempBuck è null, quindi tempBuck è la radice
            {
                root = temp;
            }
            else
            {
                TreeNode tempBuckParentCast = (TreeNode) tempBuckParent;
                //Ora il nodo originale che devo sostituire
                //temp diventa nodo destro o sinistro di tempBuckParent?
                if (tempBuckParentCast.getLeft().equals(tempBuck))
                {
                    tempBuckParentCast.setLeft(temp);
                }
                else
                {
                    tempBuckParentCast.setRight(temp);
                }
            }

            int tempDepth = tempBuck.getDepth();
            for (int i = 0; i < tempBuck.size(); i++)
            {
                BigInteger tempNodeID = tempBuck.get(i).getNodeID();
                if (tempNodeID.testBit(Kademlia.BITID - tempDepth - 1))
                {
                    bucketSx.add(tempBuck.get(i));
                }
                else
                {
                    bucketDx.add(tempBuck.get(i));
                }
            }
            //aggiorno il flag splittable
            if (thisNode.getNodeID().testBit(Kademlia.BITID - tempDepth - 1))
            {
                bucketSx.setSplittable(true);
            }
            else
            {
                bucketDx.setSplittable(true);
            }
        }
    }

    public synchronized Bucket findNodesBucket(KadNode node)
    {
        //preghiamo gli dei del java e li ringraziamo per la loro benevolenza per la classe BigInteger
        Node curNode = root;
        int i = Kademlia.BITID - 1;

        while (!(curNode instanceof Bucket))
        {
            if (node.getNodeID().testBit(i--))
            {
                curNode = ((TreeNode) curNode).getLeft();
            }
            else
            {
                curNode = ((TreeNode) curNode).getRight();
            }
        }
        return (Bucket) curNode;
    }

    public Node getRoot()
    {
        return root;
    }

    private class RefreshThread implements Runnable
    {
        @Override
        public void run()
        {
            Node curNode = root;

           /* while (!(curNode instanceof Bucket))
            {
                if (node.getNodeID().testBit(i--))
                {
                    curNode = ((TreeNode) curNode).getLeft();
                }
                else
                {
                    curNode = ((TreeNode) curNode).getRight();
                }
            }
            */
        }
        private void refreshBucket(Node node)
        {
            if (node instanceof Bucket)
            {
                Bucket nodeBucket = (Bucket)node;
                synchronized (nodeBucket)
                {
                    Iterator<KadNode> nodeIterator= nodeBucket.iterator();
                }

            }
            else
            {
                TreeNode intNode = (TreeNode)node;
                refreshBucket(intNode.getLeft());
                refreshBucket(intNode.getRight());
            }
        }
    }
}

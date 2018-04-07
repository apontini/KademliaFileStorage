package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;
import KPack.UserInterface.TreeUI;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.math.BigInteger;
import java.util.Iterator;

public class RoutingTree {

    private Node root;
    private static boolean instance = false;
    private Kademlia thisNode = null;
    private int bucketsDim;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public RoutingTree(Kademlia thisNode)
    {
        if (instance)
        {
            return;
        }
        instance = true;
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

    public void add(KadNode nodo)
    {
        writeLock.lock();
        Bucket toSplitBucket;
        while (!((toSplitBucket = findNodesBucket(nodo)).add(nodo)))
        {
            TreeNode temp = new TreeNode();
            Bucket bucketSx = new Bucket(thisNode, false);
            Bucket bucketDx = new Bucket(thisNode, false);

            temp.setLeft(bucketSx);
            temp.setRight(bucketDx);
            bucketSx.setParent(temp);
            bucketDx.setParent(temp);

            int tempDepth = toSplitBucket.getDepth();
            synchronized (toSplitBucket)
            {
                for (int i = 0; i < toSplitBucket.size(); i++)
                {
                    BigInteger tempNodeID = toSplitBucket.get(i).getNodeID();
                    if (tempNodeID.testBit(Kademlia.BITID - tempDepth - 1))
                    {
                        bucketSx.add(toSplitBucket.get(i));
                    } else
                    {
                        bucketDx.add(toSplitBucket.get(i));
                    }
                }
                Node tempBuckParent = toSplitBucket.getParent();
                temp.setParent(tempBuckParent);
                //SOLO ORA vado a modificare il nodo che dev'essere splittato
                if (tempBuckParent == null)  //il genitore di toSplitBucket è null, quindi toSplitBucket è la radice
                {
                    root = temp;
                }
                else
                {
                    TreeNode tempBuckParentCast = (TreeNode) tempBuckParent;
                    //Ora il nodo originale che devo sostituire
                    //temp diventa nodo destro o sinistro di tempBuckParent?
                    if (tempBuckParentCast.getLeft().equals(toSplitBucket))
                    {
                        tempBuckParentCast.setLeft(temp);
                    } else
                    {
                        tempBuckParentCast.setRight(temp);
                    }
                }

                //aggiorno il flag splittable
                if (thisNode.getNodeID().testBit(Kademlia.BITID - tempDepth - 1))
                {
                    bucketSx.setSplittable(true);
                } else
                {
                    bucketDx.setSplittable(true);
                }
            }
        }
        writeLock.unlock();
    }

    public Bucket findNodesBucket(KadNode node)
    {
        readLock.lock();
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
        readLock.unlock();
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
            long totTime = 0;
            while(true)
            {
                Node curNode = root;
                try {
                    if (5*60*1000-totTime <= 0)
                    {
                        throw new java.lang.IllegalArgumentException("Attenzione: tempo per refreshBucket maggiore di 5 minuti!!");
                    }
                    Thread.sleep(5*60*1000-totTime);
                    long timeStart = System.currentTimeMillis();
                    refreshBucket(curNode);
                    long timeEnd = System.currentTimeMillis();
                    totTime = timeEnd - timeStart;
                }
                catch (InterruptedException ie)
                {
                    System.err.println("Il thread di refresh dei bucket è stato interrotto");
                }
            }
        }

        private void refreshBucket(Node node)
        {
            if (node instanceof Bucket)
            {
                Bucket nodeBucket = (Bucket)node;
                int sizeBucket = nodeBucket.size();
                boolean notDead = false;
                synchronized (nodeBucket)
                {
                    /*Iterator<KadNode> nodeIterator= nodeBucket.iterator();
                    while(nodeIterator.hasNext())
                    {
                        KadNode current = nodeIterator.next();
                        if(!thisNode.ping(current)) //nodo è morto
                        {
                            nodeBucket.removeFromBucket(current);
                        }
                        else
                        {   }
                    }
                    */

                    int randomIndex = (int )(Math.random() * sizeBucket + 1);
                    KadNode randomNode = nodeBucket.get(randomIndex);      //prendo un nodo random
                    List<KadNode> knowedNodes = thisNode.findNode(randomNode.getNodeID(),false);    //faccio il findNode e mi restituisce una lista
                    //devo controllare che current ci sia in lista
                    for (KadNode kn : knowedNodes)                            //cerco tra i nodi se ce n'è qualcuno con il mio stesso ID
                    {
                        if (kn.getNodeID().equals(randomNode.getNodeID()))              //ho trovato un nodo con il mio stesso ID
                        {
                            notDead = true;
                        }
                    }
                    //se nodo non torna sicuramente è morto (lo elimino), altrimenti non posso dire nulla
                    if(!notDead)
                    {
                        nodeBucket.removeFromBucket(randomNode);
                    }
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

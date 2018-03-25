package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;
import KPack.UserInterface.TreeUI;

import java.math.BigInteger;

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
        System.out.println(Kademlia.intToBinary(thisNode.getMyNode().getNodeID()));
        this.thisNode = thisNode;

        add(thisNode.getMyNode());

        for (int i = 0; i < 60; i++)
        {
            KadNode n = new KadNode("0.0.0.0", (short) 0, new BigInteger(Integer.toString((int) (Math.random() * 256))));
            if (n.equals(thisNode.getMyNode()))
            {
                i--;
                continue;
            }
            add(n);
        }
        new TreeUI(this);
    }

    public void add(KadNode nodo)
    {
        Bucket tempBuck;
        System.out.println("Aggiungo il nodo: " + Kademlia.intToBinary(nodo.getNodeID()));
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
                //findNodesBucket(tempBuck.get(i)).add(tempBuck.get(i));
                
                System.out.println(tempDepth);
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
            //findNodesBucket(thisNode.getMyNode()).setSplittable(true);

             if (thisNode.getNodeID().testBit(Kademlia.BITID - tempDepth - 1))
            {
                bucketSx.setSplittable(true);
            }
            else
            {
                bucketDx.setSplittable(true);
            }
            //chiamo ricorsivamente il metodo add per aggiungere il nodo e splittare di nuovo se neccesario
            // add(nodo);
        }
    }

    public Bucket findNodesBucket(KadNode node)
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
}

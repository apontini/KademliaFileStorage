package KPack;

import KPack.Files.KadFile;
import KPack.Tree.RoutingTree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Kademlia implements KademliaInterf
{
    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    private BigInteger nodeID;
    private List<KadFile> fileList;
    private RoutingTree routingTree;

    public Kademlia()
    {
        if(instance) return;

        fileList = new ArrayList<>();

        boolean exists = true;
        do
        {
            nodeID = new BigInteger(BITID, new Random());
            //Controllare se esiste
            //TODO
            exists = false;
        }
        while(exists);

        routingTree = new RoutingTree(this);

        instance = true;
    }

    public BigInteger getNodeID()
    {
        return nodeID;
    }

    public boolean ping(KadNode node)
    {
        return false;
    }

    public Object findValue(BigInteger fileID)
    {
        return null;
    }

    public List<KadNode> findNode(BigInteger nodeID)
    {
        return null;
    }

    public List<KadFile> getFileList()
    {
        return fileList;
    }

    public void store(KadNode node, KadFile file) //gestire eccezioni
    {

    }
}

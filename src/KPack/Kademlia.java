package KPack;

import KPack.Files.KadFile;
import KPack.Tree.RoutingTree;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Kademlia implements KademliaInterf
{
    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    private BigInteger nodeID;
    private List<KadFile> fileList;
    private RoutingTree routingTree;
    private KadNode thisNode;
    private short UDOPort = 1337;

    public Kademlia()
    {
        if(instance) return; //Aggiungere un'eccezione tipo AlreadyInstanced

        fileList = new ArrayList<>();

        String myIP = null; //TODO

        boolean exists = true;
        do
        {
            nodeID = new BigInteger(BITID, new Random());
            //Controllare se esiste
            //TODO
            exists = false;
        }
        while(exists);

        thisNode = new KadNode(myIP, UDOPort, nodeID);

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

    public KadNode getMyNode()
    {
        return thisNode;
    }
    
    public void store(KadNode node, KadFile file) //gestire eccezioni
    {

    }
}

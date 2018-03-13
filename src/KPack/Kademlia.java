package KPack;

import KPack.Files.KadFile;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Kademlia implements KademliaInterf
{
    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 20;
    BigInteger nodeID = null;
    private List<KadFile> fileList;

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
        instance = true;
    }

    public BigInteger getNodeID()
    {
        return null;
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

    public void store(KadNode node, KadFile file) //gestire eccezioni
    {

    }
}

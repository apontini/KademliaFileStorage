package KPack;

import java.math.BigInteger;
import java.util.Random;

public class Kademlia
{
    private static boolean instance = false;
    final private int BITID = 8;
    final private int K = 20;
    BigInteger nodeID = null;

    public Kademlia()
    {
        if(instance) return;
        boolean exists = true;
        do
        {
            nodeID = new BigInteger(BITID, new Random());
            //Controllare se esiste
            exists = false;
        }
        while(exists);
        instance = true;
    }

    public BigInteger getNodeID()
    {
        return nodeID;
    }

    public void ping(KadNode node)
    {

    }

    public void findValue()
    {

    }

    public void findNode()
    {

    }

    public void store()
    {

    }
}

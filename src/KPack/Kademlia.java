package KPack;

import java.math.BigInteger;
import java.util.Random;

public class Kademlia
{
    private static boolean instance = false;
    private final static int BITID = 8;
    private final static int K = 20;
    BigInteger nodeID = null;

    public Kademlia()
    {
        if(instance) return; //TODO
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

    public static int getBITID() {
        return BITID;
    }

    public static int getK() {
        return K;
    }
}

package KPack;

import java.math.BigInteger;
import java.util.Random;

public class Kademlia
{
    private boolean instance = false;
    private int bitID = 8;
    BigInteger nodeID = null;

    public Kademlia() {
        boolean exists = true;
        do
        {
            nodeID = new BigInteger(bitID, new Random());
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
}

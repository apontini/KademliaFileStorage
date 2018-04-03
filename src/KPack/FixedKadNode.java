package KPack;

import java.math.BigInteger;

public class FixedKadNode extends KadNode {

    private String name;

    public FixedKadNode(String ipString, short port, BigInteger ID, String name)
    {
        super(ipString, port, ID);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    
    public KadNode getKadNode()
    {
        return (KadNode)super.clone();
    }
}

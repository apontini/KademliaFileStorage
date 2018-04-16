package KPack;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FixedKadNode extends KadNode {

    private String name;
    private String indirizzo;

    public FixedKadNode(String indirizzo, int port, BigInteger ID, String name)
    {
        super(indirizzo, port, ID);
        this.name = name;
        this.indirizzo = indirizzo;
    }

    public String getName()
    {
        return name;
    }

    public KadNode getKadNode()
    {
        try
        {
            InetAddress inAddr = InetAddress.getByName(indirizzo);
            return new KadNode(inAddr.getHostAddress(), super.getPort(), super.getNodeID());
        }
        catch (UnknownHostException ex)
        {
            return null;
        }
        
    }
}

package KPack;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class KadNode implements Serializable {

    private InetAddress ip;
    private int port;
    private BigInteger nodeID;

    public KadNode(String ipString, int port, BigInteger ID)
    {
        try
        {
            ip = InetAddress.getByName(ipString);
        }
        catch (UnknownHostException uoe)
        {
            uoe.printStackTrace();
        }

        this.port = port;
        this.nodeID = ID;
    }

    public InetAddress getIp()
    {
        return ip;
    }

    public int getUDPPort()
    {
        return port;
    }

    public BigInteger getNodeID()
    {
        return nodeID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        KadNode kadNode = (KadNode) o;
        return Objects.equals(nodeID, kadNode.nodeID);
    }

    @Override
    public String toString()
    {
        return "KadNode{"
                + "ip=" + ip
                + ", UDPport=" + port
                + ", nodeID=" + nodeID
                + '}';
    }
}

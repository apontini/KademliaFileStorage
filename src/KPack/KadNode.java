package KPack;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class KadNode
{
    private InetAddress ip;
    private short UDPport;
    private BigInteger nodeID;

    public KadNode(String ipString, short port, BigInteger ID)
    {
        try
        {
            ip = InetAddress.getByName(ipString);
        }
        catch (UnknownHostException uoe)
        {
            uoe.printStackTrace();
        }

        UDPport = port;
        nodeID = ID;
    }

    public InetAddress getIp() {
        return ip;
    }

    public short getUDPPort() {
        return UDPport;
    }

    public BigInteger getNodeID() {
        return nodeID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KadNode kadNode = (KadNode) o;
        return Objects.equals(nodeID, kadNode.nodeID); //dubbio
    }
}

package KPack;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class KadNode
{
    private InetAddress ip;
    private short UDPport;
    private BigInteger nodeID;

    public KadNode(String ipString, short port, BigInteger ID) throws UnknownHostException
    {
        ip = InetAddress.getByName(ipString);
        UDPport = port;
        nodeID = ID;
    }

    public InetAddress getIp() {
        return ip;
    }

    public short getUDPport() {
        return UDPport;
    }

    public BigInteger getNodeID() {
        return nodeID;
    }
}

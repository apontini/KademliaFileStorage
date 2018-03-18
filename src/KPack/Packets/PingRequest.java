package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class PingRequest implements Serializable
{
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private final short idCommand=1;

    public PingRequest(KadNode kn)
    {
        this.ipKadNode=kn.getIp();
        this.UDPport=kn.getUDPPort();
        this.nodeID=kn.getNodeID();
    }

    public short getIdCommand() {
        return idCommand;
    }

    public InetAddress getIpKadNode() {
        return ipKadNode;
    }

    public short getUDPport() {
        return UDPport;
    }

    public BigInteger getNodeID() {
        return nodeID;
    }
}



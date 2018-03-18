package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class FindNodeRequest implements Serializable
{
    private final short idCommand=3;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private BigInteger targetID;

    public FindNodeRequest (BigInteger targetID,KadNode kn)
    {
        this.targetID=targetID;
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

    public BigInteger getTargetID() {
        return targetID;
    }
}



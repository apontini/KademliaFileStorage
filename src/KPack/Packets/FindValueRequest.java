package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class FindValueRequest implements Serializable
{
    private BigInteger fileID;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private final short idCommand=4;

    public FindValueRequest(BigInteger targetID, KadNode kn)
    {
        this.fileID = targetID;
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

    public BigInteger getFileID() {
        return fileID;
    }
}



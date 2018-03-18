package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class StoreReply implements Serializable
{
    private final short idCommand=7;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private BigInteger idFileStore;

    public StoreReply(BigInteger idFileStore, KadNode kn)
    {
        this.idFileStore=idFileStore;
        this.ipKadNode=kn.getIp();
        this.UDPport=kn.getUDPPort();
        this.nodeID=kn.getNodeID();
    }

    public BigInteger getIdFile() {
        return idFileStore;
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

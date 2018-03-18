package KPack.Packets;

import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class DeleteReply implements Serializable
{
    private final short idCommand=10;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private BigInteger idFileDelete;

    public DeleteReply(BigInteger idFileDelete, KadNode kn)
    {
        this.idFileDelete=idFileDelete;
        this.ipKadNode=kn.getIp();
        this.UDPport=kn.getUDPPort();
        this.nodeID=kn.getNodeID();
    }

    public BigInteger getIdFileDelete() {
        return idFileDelete;
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

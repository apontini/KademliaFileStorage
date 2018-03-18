package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;

public class DeleteRequest implements Serializable
{
    private BigInteger fileID;
    private String fileName;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private final short idCommand=5;

    public DeleteRequest(KadFileInterf kf, KadNode kn)
    {
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();
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

    public String getFileName() {
        return fileName;
    }
}



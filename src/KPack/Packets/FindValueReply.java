package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FindValueReply implements Serializable
{
    private final short idCommand=9;
    private final String fileName;
    private BigInteger fileID;
    private String content;
    private String path;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;

    public FindValueReply(BigInteger targetID, List<KadNode> lkn, KadFileInterf kf) throws FileNotFoundException {
        this.fileID = targetID;
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();
        this.path = kf.getPath();

        FileReader fr = new FileReader(path);;
        BufferedReader br = new BufferedReader(fr);
        content="";
        try {
            while(br.ready())
            {
                content+= br.readLine();
                content+="\n";
            }
        } catch (IOException ex) {
            Logger.getLogger(StoreRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
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


package KPack.Packets;

import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreRequest implements Serializable
{
    private BigInteger fileID;
    private String fileName;
    private String path;
    private String content;
    private InetAddress ipKadNode;
    private short UDPport;
    private BigInteger nodeID;
    private final short idCommand=2;

    public StoreRequest(KadFileInterf kf, KadNode kn) throws FileNotFoundException
    {
        this.fileID = kf.getFileID();
        this.fileName = kf.getFileName();
        this.path = kf.getPath();
        this.ipKadNode=kn.getIp();
        this.UDPport=kn.getUDPPort();
        this.nodeID=kn.getNodeID();

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

    public String getFileName() {
        return fileName;
    }

    public String getContent(){
        return content;
    }
}


package KPack;

import KPack.Exceptions.FileNotKnownException;
import KPack.Files.KadFile;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.List;

public interface KademliaInterf
{

    BigInteger getNodeID();

    List<KadFile> getFileList();

    boolean ping(KadNode node);

    Object findValue(BigInteger fileID, boolean returnContent);

    List<KadNode> findNode(BigInteger nodeID);

    void store(String filepath) throws FileNotFoundException, InvalidParameterException;

    void delete(BigInteger ID) throws FileNotKnownException;

    KadNode getMyNode();
}

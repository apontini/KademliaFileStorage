package KPack;

import KPack.Exceptions.FileNotKnown;
import KPack.Files.KadFileList;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.List;

public interface KademliaInterf {

    BigInteger getNodeID();

    KadFileList getFileList();

    boolean ping(KadNode node);

    Object findValue(BigInteger fileID);

    List<KadNode> findNode(BigInteger nodeID, boolean doNotTrack);

    void store(String filepath) throws FileNotFoundException, InvalidParameterException;

    void delete(BigInteger ID) throws FileNotKnown;

    KadNode getMyNode();
}

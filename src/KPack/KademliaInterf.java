package KPack;

import KPack.Files.KadFile;
import KPack.Files.KadFileList;

import java.math.BigInteger;
import java.util.List;

public interface KademliaInterf
{
    BigInteger getNodeID();

    KadFileList getFileList();

    boolean ping(KadNode node);

    Object findValue(BigInteger fileID);

    List<KadNode> findNode(BigInteger nodeID);

    void store(KadNode node, KadFile file); //gestire eccezioni

    KadNode getMyNode();
}

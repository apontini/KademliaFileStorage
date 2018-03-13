package KPack;

import KPack.Files.KadFile;

import java.math.BigInteger;
import java.util.List;

public interface KademliaInterf
{
    BigInteger getNodeID();

    List<KadFile> getFileList();

    boolean ping(KadNode node);

    Object findValue(BigInteger fileID);

    List<KadNode> findNode(BigInteger nodeID);

    void store(KadNode node, KadFile file); //gestire eccezioni
}

package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;

public class FindNodeRequest implements Serializable {

    private KadNode sourceKN;
    private KadNode destKN;
    private BigInteger targetID;
    private boolean doNotTrack; //se è vera, il nodo che riceve la richiesta non aggiorna il bucket relativo all'ID

    public FindNodeRequest(BigInteger targetID, KadNode sourceKN, KadNode destKN, boolean doNotTrack)
    {
        this.targetID = targetID;
        this.sourceKN = sourceKN;
        this.destKN = destKN;
        this.doNotTrack = doNotTrack;
    }

    public KadNode getSourceKadNode()
    {
        return sourceKN;
    }

    public KadNode getDestKadNode()
    {
        return destKN;
    }

    public BigInteger getTargetID()
    {
        return targetID;
    }

    public boolean toTrack(){ return doNotTrack; }
}

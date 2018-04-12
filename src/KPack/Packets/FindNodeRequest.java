package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;

public class FindNodeRequest extends Packet implements Serializable {

    private BigInteger targetID;
    private boolean doNotTrack; //se è vera, il nodo che riceve la richiesta non aggiorna il bucket relativo all'ID

    public FindNodeRequest(BigInteger targetID, KadNode sourceKN, KadNode destKN, boolean doNotTrack)
    {
        super(sourceKN, destKN);
        this.targetID = targetID;
        this.doNotTrack = doNotTrack;
    }

    public BigInteger getTargetID()
    {
        return targetID;
    }

    public boolean toTrack()
    {
        return doNotTrack;
    }
}

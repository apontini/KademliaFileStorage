package KPack.Packets;

import KPack.KadNode;

import java.io.Serializable;
import java.math.BigInteger;

public class FindNodeRequest extends Packet implements Serializable {

    private BigInteger targetID;
    private boolean track; //se è falsa, il nodo che riceve la richiesta non aggiorna il bucket relativo all'ID

    public FindNodeRequest(BigInteger targetID, KadNode sourceKN, KadNode destKN, boolean track)
    {
        super(sourceKN, destKN);
        this.targetID = targetID;
        this.track = track;
    }

    public BigInteger getTargetID()
    {
        return targetID;
    }

    public boolean isTracked()
    {
        return track;
    }
}

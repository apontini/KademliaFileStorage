package KPack.Packets;

import KPack.Files.KadFile;
import KPack.KadNode;
import java.io.Serializable;

public class DeleteRequest extends Packet implements Serializable {

    private KadFile kf;

    public DeleteRequest(KadFile kf, KadNode source, KadNode dest)
    {
        super(source, dest);
        this.kf = kf;
    }

    public KadFile getFile()
    {
        return kf;
    }
}

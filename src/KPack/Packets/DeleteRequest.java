package KPack.Packets;

import KPack.Files.KadFile;
import KPack.Files.KadFileInterf;
import KPack.KadNode;
import java.io.Serializable;
import java.math.BigInteger;

public class DeleteRequest implements Serializable {

    private final KadNode source;
    private final KadNode dest;
    private KadFile kf;

    public DeleteRequest(KadFile kf, KadNode source, KadNode dest)
    {
        this.kf = kf;
        this.source = source;
        this.dest = dest;
    }

    public KadFile getFile()
    {
        return kf;
    }

    public KadNode getSourceKadNode()
    {
        return source;
    }

    public KadNode getDestKadNode()
    {
        return dest;
    }
}

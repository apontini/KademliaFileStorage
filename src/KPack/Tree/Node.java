package KPack.Tree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public abstract class Node
{
    private Node parent;
    private final ReadWriteLock readWriteLock = null;
    private final Lock readLock = null;
    private final Lock writeLock = null;


    public Node getParent()
    {
        readLock.lock();
        try
        {
            return parent;
        }
        finally
        {
            readLock.unlock();
        }
    }

    public void setParent(Node parent)
    {
        writeLock.lock();
        try
        {
            this.parent = parent;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public int getDepth()
    {
        if(parent == null) return 0;
        return parent.getDepth()+1;
    }
}

package KPack.Tree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TreeNode extends Node
{
    //SOLO nodo interno
    private Node left;
    private Node right;
    private Node parent;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

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

    public Node getLeft()
    {
        readLock.lock();
        try
        {
            return left;
        }
        finally
        {
            readLock.unlock();
        }
    }

    public Node getRight()
    {
        readLock.lock();
        try
        {
            return right;
        }
        finally
        {
            readLock.unlock();
        }
    }

    public void setLeft(Node left)
    {
        writeLock.lock();
        try
        {
            this.left = left;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void setRight(Node right)
    {
        writeLock.lock();
        try
        {
            this.right = right;
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
package KPack.Tree;

public abstract class Node {

    private Node parent;

    public synchronized Node getParent()
    {
        return parent;
    }

    public synchronized void setParent(Node parent)
    {
        this.parent = parent;
    }

    public synchronized int getDepth()
    {
        if (parent == null)
        {
            return 0;
        }
        return parent.getDepth() + 1;
    }
}

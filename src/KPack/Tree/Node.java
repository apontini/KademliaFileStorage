package KPack.Tree;

public abstract class Node
{
    private Node parent;

    public Node getParent()
    {
        return parent;
    }

    public void setParent(Node parent)
    {
            this.parent = parent;
    }
}

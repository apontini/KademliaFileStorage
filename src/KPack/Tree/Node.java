package KPack.Tree;

public abstract class Node
{
    private Node parent;

    public Node getParent()
    {
        return parent;
    }

    public void setParent(Node parent) throws NotAValidParentException
    {
        if(!(parent instanceof Bucket))
            this.parent = parent;
        else
            throw new NotAValidParentException();
    }
}

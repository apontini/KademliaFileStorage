package KPack.Tree;

public class TreeNode extends Node {

    //SOLO nodo interno
    private Node parent;
    private Node left;
    private Node right;

    public TreeNode() {}

    public synchronized Node getParent()
    {
        return parent;
    }

    public synchronized void setParent(Node parent)
    {
        this.parent=parent;
    }

    public synchronized Node getLeft()
    {
        return left;
    }

    public synchronized Node getRight()
    {
        return right;
    }

    public synchronized void setLeft(Node left)
    {
        this.left = left;
    }

    public synchronized void setRight(Node right)
    {
        this.right = right;
    }
    
    public synchronized int getDepth()
    {
        if(parent == null) return 0;
        return parent.getDepth()+1;
    }
}

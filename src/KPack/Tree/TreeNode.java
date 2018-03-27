package KPack.Tree;

public class TreeNode extends Node {

    //SOLO nodo interno
    private Node left;
    private Node right;

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
}

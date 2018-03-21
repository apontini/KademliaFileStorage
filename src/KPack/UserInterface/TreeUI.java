package KPack.UserInterface;

import KPack.KadNode;
import KPack.Tree.Bucket;
import KPack.Tree.Node;
import KPack.Tree.RoutingTree;
import KPack.Tree.TreeNode;
import java.math.BigInteger;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeUI extends javax.swing.JFrame {

    private JTree tree;
    private RoutingTree routingTree;

    public TreeUI(RoutingTree rt)
    {
        routingTree = rt;
        
        for(int i=0;i<5;i++)
        {
            rt.add(new KadNode("1.1.1.1",(short) 0, new BigInteger(((int)(Math.random()*100))+""))); //for testing tree
        }

        Node rootNode = routingTree.getRoot();
        DefaultMutableTreeNode rootTree = new DefaultMutableTreeNode("");

        recursiveTree(rootNode, rootTree);

        tree = new JTree(rootTree);
        add(tree);

        this.pack();
        this.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Routing Tree");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 855, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 629, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void recursiveTree(Node n, DefaultMutableTreeNode dmt)
    {
        if (n instanceof Bucket)
        {
            Bucket b = (Bucket) n;
            Iterator<KadNode> ikn = b.getList();
            while (ikn.hasNext())
            {
                KadNode kn = ikn.next();
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(kn.getNodeID());
                dmt.add(treeNode);
            }
        }
        else
        {
            TreeNode tn = (TreeNode) n;
            DefaultMutableTreeNode sx = new DefaultMutableTreeNode("1");
            DefaultMutableTreeNode dx = new DefaultMutableTreeNode("0");
            dmt.add(sx);
            dmt.add(dx);

            recursiveTree(tn.getLeft(), sx);
            recursiveTree(tn.getRight(), dx);

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

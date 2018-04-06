package KPack.UserInterface;

import KPack.KadNode;
import KPack.Kademlia;
import KPack.Tree.Bucket;
import KPack.Tree.Node;
import KPack.Tree.RoutingTree;
import KPack.Tree.TreeNode;
import java.awt.Dimension;
import static java.lang.Thread.sleep;
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeUI extends javax.swing.JFrame {

    private JTree tree;
    private RoutingTree routingTree;

    public TreeUI(RoutingTree rt)
    {
        routingTree = rt;

        Node rootNode = routingTree.getRoot();
        DefaultMutableTreeNode rootTree = new DefaultMutableTreeNode("");
        recursiveTree(rootNode, rootTree);

        tree = new JTree(rootTree);
        add(tree);

        this.setPreferredSize(new Dimension(500, 500));
        this.setSize(new Dimension(500, 500));
        this.setTitle("Routing Tree");
        this.pack();
        this.setVisible(true);

        new Thread(() ->
        {
            while (true)
            {
                try
                {
                    sleep(750);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }

                updateTree();
            }
        }).start();
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

    private void updateTree()
    {
        remove(tree);
        Node rootNode = routingTree.getRoot();
        DefaultMutableTreeNode rootTree = new DefaultMutableTreeNode("");

        recursiveTree(rootNode, rootTree);

        tree = new JTree(rootTree);
        add(tree);

        for (int i = 0; i < tree.getRowCount(); i++)
        {
            tree.expandRow(i);
        }
        this.pack();

    }

    private void recursiveTree(Node n, DefaultMutableTreeNode dmt)
    {
        if (n instanceof Bucket)
        {
            Bucket b = (Bucket) n;
            //System.out.println(b.toString());
            synchronized (b)
            {
                Iterator<KadNode> ikn = b.iterator();
                while (ikn.hasNext())
                {
                    KadNode kn = ikn.next();
                    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(Kademlia.intToBinary(kn.getNodeID()) + " (" +kn.getNodeID() +") from: " + kn.getIp().toString());
                    dmt.add(treeNode);
                }
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

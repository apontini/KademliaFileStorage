package KPack.Tree;

import KPack.KadNode;
import KPack.Kademlia;
import static java.lang.Thread.sleep;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bucket extends Node implements Iterable {

    //SOLO foglia
    private int dimensioneMax;
    private List<KadNode> listaNodi;
    private boolean splittable;
    private Kademlia thisKadNode;
    //private long timeVisited;

    private AtomicLong lastUse;
    private AtomicBoolean isActive;
    private Thread refresher;

    public Bucket(Kademlia thisKadNode, boolean splittable)
    {
        listaNodi = new LinkedList<KadNode>();
        dimensioneMax = thisKadNode.K;
        this.splittable = splittable;
        this.thisKadNode = thisKadNode;

        lastUse = new AtomicLong(System.currentTimeMillis());
        isActive = new AtomicBoolean(true);
        refresher = null;
    }

    public synchronized boolean add(KadNode kn)
    {
        lastUse.getAndSet(System.currentTimeMillis());
        listaNodi.remove(kn);
        if (listaNodi.size() == dimensioneMax)
        {
            if (splittable)
            {
                return false; //ci pensa l'albero
            }
            else
            {
                //pingu 1 solo nodo (il più vecchio)
                KadNode node = listaNodi.get(0);
                if (node.equals(this.thisKadNode.getMyNode()))
                {
                    node = listaNodi.get(1);
                }
                if (!thisKadNode.ping(node))
                {
                    listaNodi.remove(node);
                    listaNodi.add(kn);
                    return true;
                }
                //Se non l'ho sostituito, scarto il nuovo nodo
            }
        }
        else
        {
            //C'è spazio, rimuoviamo il nodo (nel caso sia già presente) e lo riaggiungiamo
            //il nodo nuovo è in coda
            listaNodi.add(kn);
        }
        return true; //l'albero non deve gestire niente
    }

    public synchronized void removeFromBucket(KadNode kn)
    {
        listaNodi.remove(kn);
    }

    public synchronized int size()
    {
        return listaNodi.size();
    }

    public synchronized KadNode get(int i)
    {
        return listaNodi.get(i);
    }

    public synchronized void setSplittable(boolean splittable)
    {
        this.splittable = splittable;
    }

    public synchronized Iterator<KadNode> iterator()
    {
        return listaNodi.iterator();
    }

    /* public synchronized void setTimeVisited(long now)
    {
        timeVisited = now;
    }*/
 /* public synchronized long getTimeVisited()
    {
        return timeVisited;
    }*/
    public synchronized String toString()
    {
        String bu = "{\n";
        for (KadNode k : listaNodi)
        {
            bu += k.toString() + "\n";
        }
        bu += "}";
        return bu;
    }

    public void refreshStop()
    {
        isActive.set(false);
    }

    public void refreshStart()
    {
        if (refresher == null)
        {
            refresher = new Thread(new BucketRefresher());
            refresher.start();
        }
    }

    private class BucketRefresher implements Runnable {

        @Override
        public void run()
        {
            while (isActive.get())
            {
                if (!(System.currentTimeMillis() - lastUse.get() >= 20 * 1000))
                {
                    try
                    {
                        sleep(20 * 1000 - (System.currentTimeMillis() - lastUse.get()) + 1);
                    }
                    catch (InterruptedException ex)
                    {
                        break;
                    }
                }
                synchronized (Bucket.this)
                {
                    if(size()==0)
                    {
                        lastUse.set(System.currentTimeMillis());
                        continue;
                    }
                    System.out.println("Eseguo il refresh del bucket" + hashCode());
                    boolean notDead = false;
                    int randomIndex = (int) (Math.random() * size());
                    KadNode randomNode = get(randomIndex);      //prendo un nodo random
                    List<KadNode> knowedNodes = thisKadNode.findNode(randomNode.getNodeID());    //faccio il findNode e mi restituisce una lista
                    //devo controllare che current ci sia in lista
                    for (KadNode kn : knowedNodes)                            //cerco tra i nodi se ce n'è qualcuno con il mio stesso ID
                    {
                        if (kn.getNodeID().equals(randomNode.getNodeID()))              //ho trovato un nodo con il mio stesso ID
                        {
                            notDead = true;
                        }
                    }
                    //se nodo non torna sicuramente è morto (lo elimino), altrimenti non posso dire nulla
                    if (!notDead)
                    {
                        removeFromBucket(randomNode);
                    }
                    /*boolean isAlive=thisKadNode.ping(randomNode);
                    removeFromBucket(randomNode);
                    if (isAlive)                                        //lo aggiungo in coda ad indicare che l'ho visto vivo di recente
                    {
                        listaNodi.add(randomNode);
                    }*/
                    System.out.println("Refresh del bucket" + hashCode() + " completato");
                    lastUse.set(System.currentTimeMillis());
                }
            }
            System.out.println("Bucket Refresher Thread Morto");
        }

    }
}

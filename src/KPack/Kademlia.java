package KPack;

import KPack.Exceptions.AlreadyInstancedException;
import KPack.Exceptions.FileNotKnownException;
import KPack.Exceptions.InvalidSettingsException;
import KPack.Files.KadFile;
import KPack.Files.KadFileMap;
import KPack.Packets.*;
import KPack.Tree.Bucket;
import KPack.Tree.Node;
import KPack.Tree.RoutingTree;
import KPack.Tree.TreeNode;
import KPack.UserInterface.TreeUI;
import KPack.UserInterface.UserInterface;

import java.awt.*;
import java.io.*;
import static java.lang.Thread.sleep;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Kademlia implements KademliaInterf {

    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    public final static int ALPHA = 2;
    public final static String FILESPATH = "." + File.separator + "storedFiles" + File.separator;
    private KadFileMap fileMap;
    private KadFileMap localFileMap;
    private BigInteger nodeID;
    private RoutingTree routingTree;
    private KadNode thisNode;
    private ArrayList<FixedKadNode> fixedNodesList = new ArrayList<>();
    private Set<Tupla<BigInteger, String>> recentlyRefreshed;

    //Variabili lette dalle impostazioni
    public int port = 1337;
    public int fileRefreshWait = 100000;
    public int fileRefreshThreadSleep = 20000;
    public int bucketRefreshWait = 20000;
    private int timeout = 10000;

    private final ReadWriteLock localFileReadWriteLock;
    private final Lock localFileReadLock;
    private final Lock localFileWriteLock;

    private final ReadWriteLock globalFileReadWriteLock;
    private final Lock globalFileReadLock;
    private final Lock globalFileWriteLock;

    public Kademlia() throws AlreadyInstancedException
    {
        if (instance)
        {
            throw new AlreadyInstancedException();
        }
        instance = true;
        Runtime.getRuntime().addShutdownHook(new Thread() { //Hook per eliminare i file ridondanti allo spegnimento
            @Override
            public void run()
            {
                File temp = new File(FILESPATH);
                if (temp.listFiles() != null)
                {
                    for (File i : temp.listFiles())
                    {
                        if (i.getName().endsWith(".kad"))
                        {
                            i.delete();
                        }
                    }
                }
            }
        });

        System.out.println("WorkingDir: " + System.getProperty("user.dir"));

        loadSettings();

        //Lo rieseguo, potrebbe non essere stato eseguito in seguito ad un crash della JVM
        File temp = new File(FILESPATH);
        if (temp.listFiles() != null)
        {
            for (File i : temp.listFiles())
            {
                if (i.getName().endsWith(".kad"))
                {
                    i.delete();
                }
            }
        }

        localFileReadWriteLock = new ReentrantReadWriteLock(true);
        localFileReadLock = localFileReadWriteLock.readLock();
        localFileWriteLock = localFileReadWriteLock.writeLock();

        globalFileReadWriteLock = new ReentrantReadWriteLock(true);
        globalFileReadLock = globalFileReadWriteLock.readLock();
        globalFileWriteLock = globalFileReadWriteLock.writeLock();

        fileMap = new KadFileMap(this, false);
        localFileMap = new KadFileMap(this, true);
        String myIP = getIP().getHostAddress().toString();

        routingTree = new RoutingTree(this);

        fixedNodesList = loadFixedNodesFromFile();
        recentlyRefreshed = new HashSet<>();

        //Aggiungo all'alberto i nodi noti
        for (FixedKadNode fkn : fixedNodesList)
        {
            routingTree.add(fkn.getKadNode());
        }

        if (!isFixedNode())
        {
            do
            {
                nodeID = new BigInteger(BITID, new Random());
                System.out.println("ID generato: " + nodeID);
            }
            while (!isUniqueID());
        }
        System.out.println("ID ottenuto: " + nodeID);

        thisNode = new KadNode(myIP, port, nodeID);

        routingTree.add(thisNode); //Mi aggiungo

        try
        {
            new TreeUI(routingTree, this);
            new UserInterface(this, fileMap, localFileMap);
        }
        catch (HeadlessException he) //per dispositivi senza schermo
        {
        }

        new Thread(new ListenerThread(), "Listener").start();

        networkJoin();

        new Thread(new FileRefresh(fileRefreshWait, fileRefreshThreadSleep), "FileRefresh").start();

        synchronized (routingTree.getRoot())
        {
            if (routingTree.getRoot() instanceof Bucket)
            {
                ((Bucket) routingTree.getRoot()).refreshStart();
            }
        }
    }

    private boolean isUniqueID()
    {
        List<KadNode> nodes = findNode(nodeID, false);       //richiamo il findnode utilizzando il mio ID
        for (KadNode kn : nodes)                            //cerco tra i nodi se ce n'è qualcuno con il mio stesso ID
        {
            if (kn.getNodeID().equals(nodeID))              //ho trovato un nodo con il mio stesso ID
            {
                return false;
            }
        }
        return true;                                        //non ci sono altri nodi con il mio ID, quindi è unico!
    }

    private void loadSettings()
    {
        File temp = new File("./settings.properties");
        try
        {
            if (!temp.exists())
            {
                throw new InvalidSettingsException("File di configurazione settings.properties non trovato");
            }
            for (String i : Files.readAllLines(temp.toPath(), StandardCharsets.UTF_8))
            {
                String sub = i.substring(0, 1);
                if (sub.equals("#"))
                {
                    continue;
                }
                else
                {
                    String[] split = i.split("=");
                    switch (split[0])
                    {
                        case "port":
                            if (!split[1].isEmpty())
                            {
                                if (Integer.valueOf(split[1]) > 1024 && Integer.valueOf(split[1]) < 65535)
                                {
                                    port = Integer.valueOf(split[1]);
                                }
                                else
                                {
                                    throw new InvalidSettingsException("Porta non valida");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Porta non valida");
                            }
                            break;
                        case "bucketRefreshWait":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    bucketRefreshWait = Integer.parseInt(split[1]);
                                    System.out.println("\u001B[31mBucketRefreshWait: " + bucketRefreshWait + "\u001B[0m");
                                }
                                catch (NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Tempo di refresh dei bucket non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Tempo di refresh dei bucket non valido");
                            }
                            break;
                        case "fileRefreshWait":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    fileRefreshWait = Integer.parseInt(split[1]);
                                    System.out.println("FileRefreshWait: " + fileRefreshWait);
                                }
                                catch (NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Tempo di refresh dei file non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Tempo di refresh dei file non valido");
                            }
                            break;
                        case "socketTimeout":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    timeout = Integer.parseInt(split[1]);
                                    System.out.println("Timeout Socket: " + timeout);
                                }
                                catch (NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Timeout non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Timeout non valido");
                            }
                            break;
                        case "fileRefreshThreadSleep":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    fileRefreshThreadSleep = Integer.parseInt(split[1]);
                                    System.out.println("fileResfreshThreadSleep: " + fileRefreshThreadSleep);
                                }
                                catch (NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Tempo di sleep del thread FileRefresh non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Tempo di sleep del thread FileRefresh non valido");
                            }
                            break;
                        default:
                            throw new InvalidSettingsException("Parametro non valido: " + split[0]);
                    }
                }
                //Check di altri vincoli
                if (fileRefreshThreadSleep >= fileRefreshWait)
                {
                    throw new InvalidSettingsException("Il tempo di sleep del thread dev'essere minore del tempo di refresh dei file");
                }
            }
        }
        catch (InvalidSettingsException ise)
        {
            System.err.println("\u001B[31mErrore nelle impostazioni" + ise.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            System.exit(1);
        }
        catch (IOException ioe)
        {
            System.err.println("\u001B[31mErrore nelle impostazioni" + ioe.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    private boolean isFixedNode()
    {
        String hostname;
        InetAddress addr;
        try
        {
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();

            for (FixedKadNode fkn : fixedNodesList)
            {
                if (fkn.getName().toLowerCase().equals(hostname.toLowerCase()))
                {
                    nodeID = fkn.getNodeID();
                    port = fkn.getPort();
                    return true;
                }
            }
        }
        catch (UnknownHostException uhe)
        {
            System.err.println("\u001B[31mErrore isFixedNode: " + uhe.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            System.exit(1);
        }

        return false;
    }

    private void networkJoin()
    {
        //Faccio il findNode su me stesso
        List<KadNode> nearestNodes = findNode(nodeID, true);
        for (KadNode kn : nearestNodes)
        {
            routingTree.add(kn);
        }
    }

    public void writeFixedList()    //poi questo sarà da chiamare da qualche parte una sola volta e poi da commentare
    {
        ArrayList<FixedKadNode> fixNodes = new ArrayList<>();
        try
        {
            InetAddress inAddrPunto = InetAddress.getByName("pintini.ddns.net");
            String addressPunto = inAddrPunto.getHostAddress();

            InetAddress inAddrTavo = InetAddress.getByName("tavolino.ddns.net");
            String addressTavo = inAddrTavo.getHostAddress();

            FixedKadNode Punto = new FixedKadNode("pintini.ddns.net", (int) 1336, BigInteger.ONE, "pintini");
            FixedKadNode Tavolino = new FixedKadNode("tavolino.ddns.net", (int) 1336, BigInteger.valueOf(2), "tavolino");

            fixNodes.add(Punto);
            fixNodes.add(Tavolino);
        }
        catch (UnknownHostException uhe)
        {
            System.err.println("\u001B[31mErrore nella scrittura dei nodi fissi: " + uhe.getMessage() + "\u001B[0m");
        }
        //Scrive file "nodes", inserendoci la lista fixNodes e serializza il file
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try
        {
            fout = new FileOutputStream("." + File.separator + "nodes");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(fixNodes);
        }
        catch (IOException ioe)
        {
            System.err.println("\u001B[31mErrore nella scrittura dei nodi fissi: " + ioe.getMessage() + "\u001B[0m");
            ioe.printStackTrace();
        }
        finally
        {
            try
            {
                if (fout != null)
                {
                    fout.close();
                }
                if (oos != null)
                {
                    oos.close();
                }
            }
            catch (IOException ioe)
            {
                System.err.println("\u001B[31mErrore nella scrittura dei nodi fissi: " + ioe.getMessage() + "\u001B[0m");
                ioe.printStackTrace();
            }
        }
    }

    private ArrayList<FixedKadNode> loadFixedNodesFromFile() //va a leggere il file serializzato e restituisce la lista di KadNodes fissi.
    {
        ArrayList<FixedKadNode> retFixNodes = new ArrayList<>();

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream("." + File.separator + "nodes");
            ObjectInputStream ois = new ObjectInputStream(fis);
            //retFixNodes = new ArrayList<>();      ??
            while (true)
            {
                retFixNodes = ((ArrayList<FixedKadNode>) ois.readObject());
            }
        }
        catch (EOFException e)
        {

        }
        catch (IOException | ClassNotFoundException ioe)
        {
            System.err.println("\u001B[31mErrore nella lettura dei nodi fissi: " + ioe.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            ioe.printStackTrace();
            System.exit(1);
        }
        finally
        {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
            catch (IOException ioe)
            {
            } //Ignorata
            finally
            {
                return retFixNodes;
            }
        }
    }

    public InetAddress getIP()
    {
        String publicIP = null;
        try
        {
            URL urlForIP = new URL("https://api.ipify.org/");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlForIP.openStream()));

            publicIP = in.readLine(); //IP as a String
        }
        catch (MalformedURLException mue)
        {
            System.err.println("\u001B[31mErrore nell'URL per l'IP: " + mue.getMessage() + "\u001B[31m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            System.exit(1);
        }
        catch (IOException ioe)
        {
            System.err.println("\u001B[31mEccezione generale nel trovare l'IP del nodo: " + ioe.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            ioe.printStackTrace();
            System.exit(1);
        }
        try
        {
            return InetAddress.getByName(publicIP);
        }
        catch (UnknownHostException e)
        {
            System.err.println("\u001B[31mHost sconosciuto nel trovare l'IP: " + e.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
            System.exit(1);
            return null;
        }
    }

    public BigInteger getNodeID()
    {
        return nodeID;
    }

    public boolean ping(KadNode node)
    {
        PingRequest pr = new PingRequest(thisNode, node);
        try
        {
            Socket s = new Socket();
            s.setSoTimeout(timeout);
            s.connect(new InetSocketAddress(node.getIp(), node.getPort()), timeout);

            OutputStream os = s.getOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(pr);
            outputStream.flush();

            InputStream is = s.getInputStream();
            ObjectInputStream inputStream = new ObjectInputStream(is);

            long timeInit = System.currentTimeMillis();
            while (true)
            {
                try
                {
                    Object preply = inputStream.readObject();
                    if (preply instanceof PingReply)
                    {
                        if (((PingReply) preply).getSourceKadNode().equals(pr.getDestKadNode()))
                        {
                            is.close();
                            s.close();
                            return true;
                        }
                    }
                    s.setSoTimeout(((int) (timeout - (System.currentTimeMillis() - timeInit))));
                }
                catch (ClassNotFoundException e)
                {
                    System.err.println("\u001B[31mIl nodo pingato mi ha risposto con qualcosa che non conosco\u001B[0m");
                }
            }
        }
        catch (SocketTimeoutException soe)
        {
            System.err.println("\u001B[31mTimeout exception: " + soe.getMessage() + "\u001B[0m");
            return false;
        }
        catch (ConnectException soe)
        {
            System.err.println("\u001B[31mConnect Exception: " + soe.getMessage() + "\u001B[0m");
            return false;
        }
        catch (EOFException e)
        {
            return false;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    private Object findValue_lookup(BigInteger fileID)    //Object può essere o una List<KadNode> oppure di tipo KadFile
    {
        /*Verifico se il file richiesto è contenuto nel nodo
        Controllo solo sui file ridondanti, questo per evitare che il possessore del file lo abbia modificato
        rispetto alla versione presente nella rete*/
        KadFile temp = fileMap.get(fileID);
        if (temp != null)
        {
            return temp;
        }
        else
        {
            return findNode_lookup(fileID); //se non lo è procedo come per il findnode_lookup
        }
    }

    public Object findValue(BigInteger fileID, boolean returnContent) //Object può essere di tipo List<KadNode> oppure di tipo byte[] (null se returnContent=false)
    {
        Bucket bucket = routingTree.findNodesBucket(thisNode);
        KadNode target = new KadNode("", (short) 0, fileID);
        int depth = ((Node) bucket).getDepth();
        BigInteger prefix = thisNode.getNodeID().shiftRight(BITID - depth); // prendo il prefisso relativo al bucket
        List<KadNode> lkn = new ArrayList<>();  // lista di tutti i nodi conosciuti
        List<KadNode> alphaNode;
        AbstractQueue<KadNode> queriedNode = new PriorityQueue<>((o1, o2)
                -> distanza(o1, target).compareTo(distanza(o2, target))); // lista dei nodi interrogati
        Iterator<KadNode> it = null;
        //System.out.println("****Prendo il lock del bucket " + bucket.getDepth());
        synchronized (bucket)
        {
            it = bucket.iterator();
            while (it.hasNext()) // inserisco l'intero bucket nella lista lkn
            {
                lkn.add(it.next());
            }
            //System.out.println("<<<<Rilascio il lock del bucket " + bucket.getDepth());
        }
        TreeNode node = (TreeNode) bucket.getParent();
        int count = depth;
        //sfrutto il fatto che solo il bucket contente this node viene splittato, quindi risalendo l'albero ogni fratello è un bucket
        while (count > 0 && lkn.size() < K)  // se il bucket non contiene K nodi, mi sposto negli altri bucket vicini per prendere i loro nodi fino a raggiungere K
        {
            if (prefix.testBit(depth - count))
            {
                bucket = (Bucket) node.getRight();
            }
            else
            {
                bucket = (Bucket) node.getLeft();
            }
            List<KadNode> list = new ArrayList<>();
            //System.out.println("****Prendo il lock del bucket " + bucket.getDepth());
            synchronized (bucket)
            {
                it = bucket.iterator();

                while (it.hasNext())
                {
                    list.add(it.next());
                }
                //System.out.println("<<<<Lascio il lock del bucket " + bucket.getDepth());
            }
            list.sort((o1, o2)
                    -> distanza(o1, thisNode).compareTo(distanza(o2, thisNode)));
            if (list.size() >= K - lkn.size())
            {
                for (KadNode kn : list.subList(0, K - lkn.size()))
                {
                    lkn.add(kn);
                }
            }
            else
            {
                lkn.addAll(list);
            }
            node = (TreeNode) node.getParent();
            count--;
        }
        lkn.sort((o1, o2)
                -> distanza(o1, target).compareTo(distanza(o2, target)));
        if (lkn.size() >= ALPHA)
        {
            alphaNode = new ArrayList<>();
            for (KadNode kn : lkn.subList(0, ALPHA))
            {
                alphaNode.add(kn);
            }

        }
        else
        {
            alphaNode = lkn;
        }
        //chiedo anche a me stesso
        //uso una coppia boolean,byte[] perchè se returnContent è false allora anche se il file è stato trovato comunque il contentuo sarà null,
        // boolean mi permette di capire se qualche thread ha trovato il file o meno.
        AtomicReference<Tupla<Boolean, byte[]>> content = new AtomicReference<>();
        content.set(new Tupla<Boolean, byte[]>(false, null));
        while (true)
        {
            int size = lkn.size(); // per capire se il round di find nodes è fallito o meno
            //ad ognuno degli alpha node vado a inviargli un findNode
            // for (int i = 0; i < alphaNode.size(); i++)
            Thread[] threads = new Thread[alphaNode.size()];
            for (int i = 0; i < alphaNode.size(); i++)
            {
                if (content.get().getKey())
                {
                    return content.get().getValue();
                }
                KadNode kadNode = alphaNode.get(i);
                FindValueRequest fvr = new FindValueRequest(fileID, thisNode, kadNode, returnContent);
                threads[i] = new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Socket s = new Socket();
                            s.setSoTimeout(timeout);
                            s.connect(new InetSocketAddress(kadNode.getIp(), kadNode.getPort()), timeout);

                            OutputStream os = s.getOutputStream();
                            ObjectOutputStream outputStream = new ObjectOutputStream(os);
                            outputStream.writeObject(fvr);
                            outputStream.flush();

                            InputStream is = s.getInputStream();
                            ObjectInputStream inputStream = new ObjectInputStream(is);

                            long timeInit = System.currentTimeMillis();
                            boolean state = true;
                            while (state)
                            {
                                if (content.get().getKey())
                                {
                                    is.close();
                                    s.close();
                                    return;
                                }
                                try
                                {
                                    Object fvreply = inputStream.readObject();
                                    if (fvreply instanceof FindValueReply)
                                    {
                                        if (((FindValueReply) fvreply).getSourceKadNode().equals(fvr.getDestKadNode()))
                                        {
                                            //termino
                                            is.close();
                                            s.close();
                                            content.set(new Tupla(true, ((FindValueReply) fvreply).getContent()));
                                            return;
                                        }
                                    }
                                    else
                                    {
                                        if ((fvreply instanceof FindNodeReply) && ((FindNodeReply) fvreply).getSourceKadNode().equals(fvr.getDestKadNode()))
                                        {
                                            routingTree.add(((FindNodeReply) fvreply).getSourceKadNode());
                                            Iterator<KadNode> it1 = ((FindNodeReply) fvreply).getList().iterator();
                                            while (it1.hasNext())
                                            {
                                                KadNode k = it1.next();
                                                //System.out.println("****Prendo il lock nella lista del findNode ");
                                                synchronized ((lkn))
                                                {
                                                    if (!(lkn.contains(k)))  // se mi da un nodo che conosco gia, non lo inserisco
                                                    {
                                                        lkn.add(k);
                                                    }
                                                    //System.out.println("<<<<Lascio il lock nella lista del findNode ");
                                                }
                                            }
                                            is.close();
                                            s.close();
                                            //inputStream.close();
                                            //ds.close();
                                            state = false;
                                        }
                                    }
                                    if (state)
                                    {
                                        System.out.println("******Aggiorno il timeout");
                                        s.setSoTimeout(((int) (timeout - (System.currentTimeMillis() - timeInit))));
                                    }
                                }
                                catch (ClassNotFoundException e)
                                {
                                    System.err.println("\u001B[31mErrore nella risposta ricevuta: " + e.getMessage() + "\u001B[0m");
                                }
                            }
                        }
                        catch (SocketTimeoutException soe)
                        {
                            //Un nodo interrogato non ha risposto in tempo pazienza
                        }
                        catch (ConnectException soe)
                        {
                            //System.err.println("Connect Exception: " + soe.getMessage());
                        }
                        catch (EOFException e)
                        {
                            //impossibile
                        }
                        catch (IOException ex)
                        {
                            System.err.println("\u001B[31mIOException nel FindValue " + ex.getMessage() + "\u001B[0m");
                            ex.printStackTrace();
                        }
                    }
                });
                threads[i].start();
            }
            boolean state = true;
            while (state)
            {
                int i = 0;
                state = false;
                if (content.get().getKey())
                {
                    return content.get().getValue();
                }
                while (!state && i < alphaNode.size())
                {
                    if (!(threads[i].getState().equals(threads[i].getState().TERMINATED)))
                    {
                        state = true;
                    }
                    i++;
                }
            }
            if (content.get().getKey())
            {
                return content.get().getValue();
            }
            queriedNode.addAll(alphaNode);
            lkn.sort((o1, o2)
                    -> distanza(o1, target).compareTo(distanza(o2, target)));
            if (lkn.size() < K)
            {
                if (queriedNode.containsAll(lkn))
                {
                    return lkn;
                }
            }
            else
            {
                if (queriedNode.containsAll(lkn.subList(0, K)))
                {
                    List<KadNode> toRet = new ArrayList<>();
                    for (KadNode kn : lkn.subList(0, K))
                    {
                        toRet.add(kn);
                    }
                    return toRet;
                }
            }
            alphaNode.clear();
            int alphaSize;
            if (size == lkn.size()) //caso in cui il round di find nodes fallisce, cioè nessuno degli alpha node mi da nuovi nodi
            {
                alphaSize = K;
            }
            else
            {
                alphaSize = ALPHA;
            }
            int i = 0;
            while (i < lkn.size() && alphaNode.size() < alphaSize)
            {
                if (!(queriedNode.contains(lkn.get(i))))
                {
                    alphaNode.add(lkn.get(i));
                }
                i++;
            }
        }
    }

    private List<KadNode> findNode_lookup(BigInteger targetID)
    {
        Bucket bucket = routingTree.findNodesBucket(new KadNode("", (short) 0, targetID));
        KadNode targetKN = new KadNode("", (short) 0, targetID);
        BigInteger currentID = targetID;                      //mi serve per tenere traccia del percorso che ho fatto nell'albero
        int depth = ((Node) bucket).getDepth();
        List<KadNode> lkn = new ArrayList<>();
        Iterator<KadNode> it = null;
        //System.out.println("****Prendo il lock del bucket " + bucket.getDepth());
        synchronized (bucket)//lista dei K nodi conosciuti più vicini al target
        {
            it = bucket.iterator();
            while (it.hasNext())                                 //inserisco l'intero bucket nella lista lkn (lkn conterrà i nodi (<=K) più vicini a targetID che conosco)
            {
                lkn.add(it.next());
            }
            //System.out.println("<<<<Lascio il lock del bucket " + bucket.getDepth());
        }
        TreeNode node = (TreeNode) bucket.getParent();
        int count = depth - 1;                                    //count rappresenta la profondità del nodo in cui sono ad ogni istante.
        while (count >= 0 && lkn.size() < K)                      //ricerco altri nodi vicini al targetID finche non arrivo a K o non ho guardato tutti i nodi nell'albero
        {
            //sono ad un certo TreeNode node dell'albero, se seguo il targetID, partendo da node, e vado nello stesso ramo che raggiungo seguendo il currentID (partendo da node),
            // allora vuol dire che il sottoalbero relativo a quel ramo l'ho già visitato e passo a visitare il sottoalbero fratello. Altrimenti vuol dire che ho già
            // visitato entrambi i sottoalberi (sinistro e destro) di node e mi sposto più in su al nodo padre di node.
            //Sfrutto il fatto che due ID sono tanto più distanti quanto più verso sinistra è il primo bit diverso. 10111 è più distante da 11111 piuttosto che da 10000
            if (!(targetID.testBit((BITID - count) - 1) && currentID.testBit((BITID - count) - 1)
                    || (!(targetID.testBit((BITID - count) - 1)) && !(currentID.testBit((BITID - count) - 1)))))
            {
                //qui ho già visitato entrambi i sottoalberi
                node = (TreeNode) node.getParent();
                count--;
            }
            else
            {
                //qui il sottoalbero fratello non l'ho ancora visitato
                Node n;
                if (targetID.testBit((BITID - count) - 1))    //individuo se sono figlio destro o sinistro di node, poi mi sposto nel fratello per visitarlo
                {
                    n = node.getRight();
                }
                else
                {
                    n = node.getLeft();
                }
                //aggiorno currentID perchè il bit alla profondità del nodo fratello è diverso da quello del targetID, questo mi permette, quando risalgo,
                //di ricordarmi se ho già visitato o meno quel sottoalbero
                currentID = currentID.flipBit((BITID - count) - 1);
                if (n instanceof Bucket)
                {
                    //System.out.println("****Prendo il lock del bucket " + n.getDepth());
                    synchronized (n)
                    {
                        it = ((Bucket) n).iterator();
                        while (it.hasNext())
                        {
                            lkn.add(it.next());
                        }
                        //System.out.println("<<<<Lascio il lock del bucket " + n.getDepth());
                    }
                    node = (TreeNode) node.getParent();
                    count--;
                }
                else
                {
                    //seguo il percorso del targetID a partire da n fino ad arrivare ad un bucket. Questo conterrà i nodi più vicini al target
                    //tra quelli non ancora visitati
                    while (!(n instanceof Bucket))
                    {
                        count++;
                        node = (TreeNode) n;
                        if (targetID.testBit((BITID - count) - 1))
                        {
                            n = node.getLeft();
                        }
                        else
                        {
                            n = node.getRight();
                        }
                    }
                    //System.out.println("****Prendo il lock del bucket " + n.getDepth());
                    synchronized (n)
                    {
                        it = ((Bucket) n).iterator();
                        while (it.hasNext())
                        {
                            lkn.add(it.next());
                        }
                        //System.out.println("<<<<Lascio il lock del bucket " + n.getDepth());
                    }
                }
            }
        }
        if (lkn.size() > K)
        {
            lkn.sort((o1, o2)
                    -> distanza(o1, targetKN).compareTo(distanza(o2, targetKN)));
            lkn.removeAll(lkn.subList(K, lkn.size()));
        }
        return lkn;
    }

    public List<KadNode> findNode(BigInteger targetID)
    {
        return findNodeMethod(targetID, true);
    }

    private List<KadNode> findNode(BigInteger targetID, boolean track)
    {
        return findNodeMethod(targetID, track);
    }

    private List<KadNode> findNodeMethod(BigInteger targetID, boolean track)
    {
        Bucket bucket = routingTree.findNodesBucket(thisNode);
        KadNode targetKN = new KadNode("", (short) 0, targetID);
        int depth = ((Node) bucket).getDepth();
        BigInteger prefix = getNodeID().shiftRight(BITID - depth); // prendo il prefisso relativo al bucket
        List<KadNode> lkn = new ArrayList<>();  // lista di tutti i nodi conosciuti
        List<KadNode> alphaNode;
        AbstractQueue<KadNode> queriedNode = new PriorityQueue<>((o1, o2)
                -> distanza(o1, targetKN).compareTo(distanza(o2, targetKN))); // lista dei nodi interrogati
        Iterator<KadNode> it = null;
        //System.out.println("****Prendo il lock del bucket " + bucket.getDepth());
        synchronized (bucket)
        {
            it = bucket.iterator();
            while (it.hasNext()) // inserisco l'intero bucket nella lista lkn
            {
                lkn.add(it.next());
            }
            //System.out.println("<<<<Lascio il lock del bucket " + bucket.getDepth());
        }
        TreeNode node = (TreeNode) bucket.getParent();
        int count = depth;
        //sfrutto il fatto che solo il bucket contente this node viene splittato, quindi risalendo l'albero ogni fratello è un bucket
        while (count > 0 && lkn.size() < K)  // se il bucket non contiene K nodi, mi sposto negli altri bucket vicini per prendere i loro nodi fino a raggiungere K
        {
            if (prefix.testBit(depth - count))
            {
                bucket = (Bucket) node.getRight();
            }
            else
            {
                bucket = (Bucket) node.getLeft();
            }
            List<KadNode> list = new ArrayList<>();
            //System.out.println("****Prendo il lock del bucket " + bucket.getDepth());
            synchronized (bucket)
            {
                it = bucket.iterator();

                while (it.hasNext())
                {
                    list.add(it.next());
                }
                //System.out.println("<<<<Lascio il lock del bucket " + bucket.getDepth());
            }
            list.sort((o1, o2)
                    -> distanza(o1, thisNode).compareTo(distanza(o2, thisNode)));
            if (list.size() >= K - lkn.size())
            {
                for (KadNode kn : list.subList(0, K - lkn.size()))
                {
                    lkn.add(kn);
                }
            }
            else
            {
                lkn.addAll(list);
            }
            node = (TreeNode) node.getParent();
            count--;
        }
        lkn.sort((o1, o2)
                -> distanza(o1, targetKN).compareTo(distanza(o2, targetKN)));
        if (lkn.size() >= ALPHA)
        {
            alphaNode = new ArrayList<>();
            for (KadNode kn : lkn.subList(0, ALPHA))
            {
                alphaNode.add(kn);
            }

        }
        else
        {
            alphaNode = lkn;
        }
        //chiedo anche a me stesso
        while (true)
        {
            int size = lkn.size(); // per capire se il round di find nodes è fallito o meno
            //ad ognuno degli alpha node vado a inviargli un findNode
            // for (int i = 0; i < alphaNode.size(); i++)
            Thread[] threads = new Thread[alphaNode.size()];
            for (int i = 0; i < alphaNode.size(); i++)
            {
                KadNode kadNode = alphaNode.get(i);
                FindNodeRequest fnr = new FindNodeRequest(targetID, thisNode, kadNode, track);
                threads[i] = new Thread(() ->
                {
                    try
                    {
                        Socket s = new Socket();
                        s.setSoTimeout(timeout);
                        s.connect(new InetSocketAddress(kadNode.getIp(), kadNode.getPort()), timeout);

                        OutputStream os = s.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(fnr);
                        outputStream.flush();

                        InputStream is = s.getInputStream();
                        ObjectInputStream inputStream = new ObjectInputStream(is);

                        long timeInit = System.currentTimeMillis();
                        boolean state = true;
                        while (state)
                        {
                            try
                            {
                                Object fnreply = inputStream.readObject();
                                if (fnreply instanceof FindNodeReply)
                                {
                                    if (((FindNodeReply) fnreply).getSourceKadNode().equals(fnr.getDestKadNode()))
                                    {
                                        routingTree.add(((FindNodeReply) fnreply).getSourceKadNode());
                                        Iterator<KadNode> it1 = ((FindNodeReply) fnreply).getList().iterator();
                                        while (it1.hasNext())
                                        {
                                            KadNode k = it1.next();
                                            //System.out.println("****Prendo il lock nella lista del findNode ");
                                            synchronized ((lkn))
                                            {
                                                if (!(lkn.contains(k)))  // se mi da un nodo che conosco gia, non lo inserisco
                                                {
                                                    lkn.add(k);
                                                }
                                                //System.out.println("<<<<Lascio il lock nella lista del findNode ");
                                            }
                                        }
                                        is.close();
                                        s.close();
                                        //inputStream.close();
                                        //ds.close();
                                        state = false;
                                    }
                                }
                                if (state)
                                {
                                    System.out.println("******Aggiorno il timeout");
                                    s.setSoTimeout(((int) (timeout - (System.currentTimeMillis() - timeInit))));
                                    //ds.setSoTimeout(((int) (timeout - (System.currentTimeMillis() - timeInit))));
                                }
                            }
                            catch (ClassNotFoundException e)
                            {
                                System.err.println("\u001B[31mErrore nella risposta ricevuta: " + e.getMessage() + "\u001B[0m");
                            }
                        }
                    }
                    catch (SocketTimeoutException soe)
                    {
                        //Un nodo interrogato non ha risposto in tempo pazienza
                    }
                    catch (ConnectException soe)
                    {
                        //System.err.println("Connect Exception: " + soe.getMessage());
                    }
                    catch (EOFException e)
                    {
                        //impossibile
                    }
                    catch (IOException ex)
                    {
                        System.err.println("\u001B[31mIOException nel FindNode: " + ex.getMessage() + "\u001B[0m");
                        ex.printStackTrace();
                    }
                });
                threads[i].start();
            }
            boolean state = true;
            while (state)
            {
                int i = 0;
                state = false;
                while (!state && i < alphaNode.size())
                {
                    if (!(threads[i].getState().equals(threads[i].getState().TERMINATED)))
                    {
                        state = true;
                    }
                    i++;
                }
            }
            queriedNode.addAll(alphaNode);
            lkn.sort((o1, o2)
                    -> distanza(o1, targetKN).compareTo(distanza(o2, targetKN)));
            if (lkn.size() < K)
            {
                if (queriedNode.containsAll(lkn))
                {
                    return lkn;
                }
            }
            else
            {
                if (queriedNode.containsAll(lkn.subList(0, K)))
                {
                    List<KadNode> toRet = new ArrayList<>();
                    for (KadNode kn : lkn.subList(0, K))
                    {
                        toRet.add(kn);
                    }
                    return toRet;
                }
            }
            alphaNode.clear();
            int alphaSize;
            if (size == lkn.size()) //caso in cui il round di find nodes fallisce, cioè nessuno dei alpha node mi da nuovi nodi
            {
                alphaSize = K;
            }
            else
            {
                alphaSize = ALPHA;
            }
            int i = 0;
            while (i < lkn.size() && alphaNode.size() < alphaSize)
            {
                if (!(queriedNode.contains(lkn.get(i))))
                {
                    alphaNode.add(lkn.get(i));
                }
                i++;
            }
        }
    }

    public List<KadFile> getFileList()
    {
        List<KadFile> temp = new ArrayList<>();
        System.out.println("[" + Thread.currentThread().getName() + "] Chiedo il lock della mappa");
        globalFileReadLock.lock();
        localFileReadLock.lock();
        System.out.println("[" + Thread.currentThread().getName() + "] Prendo il lock della mappa");
        fileMap.forEach((k, v) -> temp.add(v));
        System.out.println(fileMap.size());
        localFileMap.forEach((k, v) -> temp.add(v));
        System.out.println(localFileMap.size());
        localFileReadLock.unlock();
        globalFileReadLock.unlock();
        System.out.println("[" + Thread.currentThread().getName() + "] Lascio il lock della mappa");
        return temp;
    }

    public KadNode getMyNode()
    {
        return thisNode;
    }

    public int getPort()
    {
        return port;
    }

    public void store(String filepath) throws FileNotFoundException, InvalidParameterException
    {
        File temp = new File(filepath);
        if (!temp.exists())
        {
            throw new FileNotFoundException();
        }
        if (temp.isDirectory())
        {
            throw new InvalidParameterException("Non posso memorizzare una directory");
        }

        //Genero un ID per il file e controllo se esiste
        BigInteger fileID = null;
        do
        {
            fileID = new BigInteger(BITID, new Random());
            System.out.println("Cerco se esistono file con ID: " + fileID + "...");
        }
        while ((findValue(fileID, false)) instanceof byte[]);

        List<KadNode> closestK = findNode(fileID);

        localFileWriteLock.lock();
        System.out.println("Il file avrà ID: " + fileID);
        KadFile tempfile = new KadFile(fileID, false, temp.getName(), temp.getParent());
        localFileMap.add(tempfile);
        System.out.println("Invio il file a: ");
        for (KadNode i : closestK)
        {
            System.out.println(i.getNodeID() + " (Distanza: " + distanza(new KadNode("0.0.0.0", 0, fileID), i) + ")");
        }
        // List<KadNode> closestK = findNode_lookup(fileID); togliere il commento per i test veri
        for (KadNode i : closestK)
        {
            try
            {
                StoreRequest sr = new StoreRequest(new KadFile(fileID, true, temp.getName(), temp.getParent()), thisNode, i);
                new Thread(() ->
                {
                    try
                    {
                        System.out.println("Contatto " + i.getNodeID() + "(" + i.getIp() + ") per lo store");
                        Socket s = new Socket(i.getIp(), i.getPort());
                        OutputStream os = s.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(sr);
                        outputStream.flush();
                    }
                    catch (IOException ex)
                    {
                        System.err.println("\u001B[31mErrore nell'invio dello store: " + ex.getMessage() + "\u001B[0m");
                        //ex.printStackTrace();
                    }
                }).start();

            }
            catch (IOException ioe)
            {
                System.err.println("\u001B[31mErrore nell'eseguire lo store: " + ioe.getMessage() + "\u001B[0m");
                ioe.printStackTrace();
            }
        }
        localFileWriteLock.unlock();

    }

    public void delete(BigInteger id) throws FileNotKnownException
    {
        localFileWriteLock.lock();
        KadFile temp = localFileMap.get(id); //Non si possono eliminare file ridondanti dalla rete, solo il proprietario può

        if (temp != null)
        {
            List<KadNode> closestK = findNode(temp.getFileID());
            System.out.println("Elimino il file " + temp.getFileName() + " (" + temp.getFileID() + ") da: ");
            for (KadNode i : closestK)
            {
                System.out.println(i.getNodeID() + " (Distanza: " + distanza(thisNode, i) + ")");
            }
            for (KadNode k : closestK)
            {
                DeleteRequest dr = new DeleteRequest(temp, thisNode, k);
                new Thread(() ->
                {
                    try
                    {
                        System.out.println("Contatto " + k.getNodeID() + "(" + k.getIp() + ") per il delete");
                        Socket s = new Socket();
                        s.setSoTimeout(timeout);
                        s.connect(new InetSocketAddress(k.getIp(), k.getPort()), timeout);

                        OutputStream os = s.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(dr);
                        outputStream.flush();
                    }
                    catch (IOException ioe)
                    {
                        System.err.println("\u001B[31mErrore generale nell'eseguire il delete per " + k.getNodeID() + ": " + ioe.getMessage() + "\u001B[0m");
                        ioe.printStackTrace();
                    }
                }).start();

            }
            localFileMap.remove(temp);
        }
        else
        {
            throw new FileNotKnownException();
        }
        localFileWriteLock.unlock();
    }

    private class ListenerThread implements Runnable {

        private ServerSocket listener;

        @Override
        public void run()
        {
            try
            {
                listener = new ServerSocket(port);
                System.out.println("Thread Server avviato\n" + "IP: " + getIP() + "\nPorta: " + port);
            }
            catch (IOException ex)
            {
                System.err.println("\u001B[31mErrore nell'apertura del socket del Thread Server: " + ex.getMessage() + "\u001B[0m");
                System.err.println("\u001B[31mABORT! ABORT!\u001B[0m");
                ex.printStackTrace();
                System.exit(1);
            }

            while (true)
            {
                Socket connection = null;
                try
                {
                    connection = listener.accept();

                    connection.setSoTimeout(timeout);

                    Object responseObject = null;

                    //Analizzo la richiesta ricevuta
                    InputStream is = connection.getInputStream();
                    ObjectInputStream inStream = new ObjectInputStream(is);

                    Object received = inStream.readObject();
                    System.out.println(received.getClass() + " received from " + connection.getInetAddress().getHostAddress());

                    //se non ho ricevuto un pacchetto o non sono io il destinatario, chiudo la connessione
                    if (!(received instanceof Packet) || !((Packet) received).getDestKadNode().equals(thisNode))
                    {
                        connection.close();
                        continue;
                    }

                    Packet p = (Packet) received;
                    //aggiungo il nodo sorgente all'albero di routing
                    if (!(received instanceof FindNodeRequest && !((FindNodeRequest) received).isTracked()))
                    {
                        new Thread(() ->
                        {
                            routingTree.add(p.getSourceKadNode());
                        }).start();
                    }

                    //Elaboro la risposta
                    if (received instanceof FindNodeRequest)
                    {
                        FindNodeRequest fnr = (FindNodeRequest) received;

                        System.out.println("Received FindNodeRequest from: " + fnr.getSourceKadNode());

                        List<KadNode> lkn = findNode_lookup(fnr.getTargetID());

                        FindNodeReply fnrep = new FindNodeReply(fnr.getTargetID(), thisNode, fnr.getSourceKadNode(), lkn);

                        responseObject = fnrep;
                    }
                    else if (received instanceof FindValueRequest)
                    {
                        FindValueRequest fvr = (FindValueRequest) received;

                        globalFileReadLock.lock();
                        Object value = findValue_lookup(fvr.getFileID());
                        globalFileReadLock.unlock();

                        FindValueReply fvrep = null;
                        if (value instanceof KadFile)
                        {
                            if (fvr.isContentRequested())
                            {
                                fvrep = new FindValueReply(fvr.getFileID(), (KadFile) value, thisNode, fvr.getSourceKadNode());
                            }
                            else
                            {
                                fvrep = new FindValueReply(fvr.getFileID(), null, thisNode, fvr.getSourceKadNode());
                            }
                            responseObject = fvrep;
                        }
                        else        //È una lista di KadNode tornata da findnode_lookup
                        {
                            responseObject = new FindNodeReply(fvr.getFileID(), thisNode, fvr.getSourceKadNode(), (List<KadNode>) value);
                        }

                    }
                    else if (received instanceof StoreRequest)
                    {
                        System.out.println("[" + Thread.currentThread().getName() + "] Chiedo il lock della mappa");
                        globalFileWriteLock.lock();

                        System.out.println("[" + Thread.currentThread().getName() + "] Prendo il lock della mappa");

                        StoreRequest rq = (StoreRequest) received;

                        //i file ridondanti vengono salvati con estensione .FILEID.kad
                        System.out.println("Ho ricevuto uno store di " + rq.getFileName() + " da  " + rq.getSourceKadNode().getIp());
                        String extension = rq.getFileName().contains("." + rq.getFileID() + ".kad") ? "" : "." + rq.getFileID() + ".kad";
                        File toStore = new File(FILESPATH + rq.getFileName() + extension);
                        toStore.delete();
                        toStore.createNewFile();
                        Files.write(toStore.toPath(), rq.getContent());
                        fileMap.add(new KadFile(rq.getFileID(), true, rq.getFileName() + extension, FILESPATH));
                        System.out.println("[" + Thread.currentThread().getName() + "] Lascio il lock della mappa");
                        globalFileWriteLock.unlock();
                    }
                    else if (received instanceof DeleteRequest)
                    {
                        System.out.println("[" + Thread.currentThread().getName() + "] Chiedo il lock della mappa");
                        globalFileWriteLock.lock();
                        System.out.println("[" + Thread.currentThread().getName() + "] Prendo il lock della mappa");

                        DeleteRequest dr = (DeleteRequest) received;

                        System.out.println("Ho ricevuto un delete di " + dr.getFile().getFileName() + " (" + dr.getFile().getFileID() + ") da " + dr.getSourceKadNode().getIp());

                        try
                        {
                            fileMap.remove(dr.getFile().getFileID());
                        }
                        catch (NullPointerException npe)
                        {
                            System.out.println("..Ma non ce l'ho!");
                        }
                        //Controllo di non averlo refreshato di recente
                        if (recentlyRefreshed.contains(new Tupla<>(dr.getFile().getFileID(), dr.getFile().getFileName())))
                        {
                            List<KadNode> temp = findNode(dr.getFile().getFileID(), true);
                            for (KadNode i : temp)
                            {
                                //DA PARALLELIZZARE
                                new Thread(() ->
                                {
                                    try
                                    {
                                        Socket tempS = new Socket();
                                        tempS.setSoTimeout(timeout);
                                        tempS.connect(new InetSocketAddress(i.getIp(), i.getPort()), timeout);
                                        OutputStream os = tempS.getOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                                        outputStream.writeObject(new DeleteRequest(dr.getFile(), thisNode, i));
                                        outputStream.flush();
                                    }
                                    catch (IOException ex)
                                    {
                                        Logger.getLogger(Kademlia.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }).start();
                                recentlyRefreshed.remove(new Tupla<>(dr.getFile().getFileID(), dr.getFile().getFileName()));
                            }
                        }
                        System.out.println("[" + Thread.currentThread().getName() + "] Lascio il lock della mappa");
                        globalFileWriteLock.unlock();
                    }
                    else if (received instanceof PingRequest)
                    {
                        PingRequest pr = (PingRequest) received;

                        KadNode sourceKadNode = pr.getSourceKadNode();

                        System.out.println("Received PingRequest from: " + pr.getSourceKadNode().toString());

                        PingReply reply = new PingReply(thisNode, sourceKadNode);

                        responseObject = reply;
                    }

                    if (responseObject != null)
                    {
                        OutputStream os = connection.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        try
                        {
                            System.out.println("Rispondo a " + connection.getInetAddress().getHostAddress() + " (" + ((Packet) responseObject).getDestKadNode().getNodeID() + ") con una " + responseObject.getClass());
                        }
                        catch (NullPointerException npe)
                        {
                            System.out.println("Rispondo a " + connection.getInetAddress().getHostAddress() + " con una " + responseObject.getClass());
                        }
                        outputStream.writeObject(responseObject);
                        outputStream.flush();
                        os.close();
                    }
                }
                catch (ClassNotFoundException ex)
                {
                    System.err.println("\u001B[31mClassNotFound nel thread server: " + ex.getMessage() + "\u001B[0m");
                    ex.printStackTrace();
                }
                catch (IOException ex)
                {
                    System.err.println("\u001B[31mIOException nel thread server: " + ex.getMessage() + "\u001B[0m");
                    ex.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (connection != null)
                        {
                            connection.close();
                        }
                    }
                    catch (IOException ex)
                    {
                        System.err.println("\u001B[31mIOException nel thread server: " + ex.getMessage() + "\u001B[0m");
                        ex.printStackTrace();
                    }
                }
                System.out.println("Connessione conclusa");
            }
        }
    }

    private class FileRefresh implements Runnable {

        private int refreshWait;
        private int refreshThreadSleep;

        public FileRefresh(int refreshWait, int refreshThreadSleep)
        {
            this.refreshWait = refreshWait;
            this.refreshThreadSleep = refreshThreadSleep;
        }

        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(refreshThreadSleep);
                    System.out.println("Inizio il refresh dei file..");
                    System.out.println("[" + Thread.currentThread().getName() + "] Chiedo il lock della mappa");
                    List<KadFile> toBeDeleted = new ArrayList<>();

                    System.out.println("[" + Thread.currentThread().getName() + "] Prendo il lock della mappa");
                    //si refreshano solo i file ridondanti
                    globalFileReadLock.lock();
                    recentlyRefreshed.clear();
                    List<KadFile> copyList = new ArrayList<>();
                    fileMap.forEach((k, v) ->
                    {
                        copyList.add(v);
                    });
                    globalFileReadLock.unlock();
                    copyList.forEach((v) ->
                    {
                        try
                        {
                            sleep(5000);  //aspetto 5 secondi tra un file e il successivo per lasciare libero il lock per la write
                        }
                        catch (InterruptedException ex)
                        {
                        }
                        globalFileReadLock.lock();
                        if (fileMap.get(v.getFileID()) == null)
                        {
                            globalFileReadLock.unlock();
                            return;
                        }
                        globalFileReadLock.unlock();
                        if ((System.currentTimeMillis() - v.getLastRefresh()) >= refreshWait)
                        {
                            System.out.println("++++Refresho " + v.getFileName());
                            List<KadNode> temp = findNode(v.getFileID(), true);
                            //Se non sono tra i K nodi più vicini a quell'ID, elimino il file da me e passo al prossimo file
                            boolean state = true;
                            for (KadNode n : temp)
                            {
                                if (thisNode.getNodeID().equals(n.getNodeID()))
                                {
                                    state = false;
                                    break;
                                }
                            }
                            if (state)
                            {
                                System.out.println("+++Non sono tra i nodi più vicini! Elimino il file");
                                toBeDeleted.add(v);
                                return;
                            }

                            recentlyRefreshed.add(new Tupla<>(v.getFileID(), v.getFileName()));
                            //Ora passo al refresh vero e proprio
                            for (KadNode n : temp)
                            {
                                System.out.println("++++Tento di contattare " + n.getNodeID() + "(" + n.getIp() + ":" + n.getPort() + ")");
                                if (n.getNodeID().equals(thisNode.getNodeID()))
                                {
                                    System.out.println("Ma sono io, dunque proseguo");
                                    continue;
                                }
                                Socket tempS = null;
                                try
                                {
                                    tempS = new Socket();
                                    tempS.setSoTimeout(timeout);
                                    tempS.connect(new InetSocketAddress(n.getIp(), n.getPort()), timeout);
                                    OutputStream os = tempS.getOutputStream();
                                    ObjectOutputStream outputStream = new ObjectOutputStream(os);
                                    outputStream.writeObject(new FindValueRequest(v.getFileID(), thisNode, n, false));
                                    outputStream.flush();

                                    InputStream is = tempS.getInputStream();
                                    ObjectInputStream ois = new ObjectInputStream(is);
                                    try
                                    {
                                        Object resp = ois.readObject();
                                        if (resp instanceof FindNodeReply)
                                        {
                                            KadFile toSend = new KadFile(v.getFileID(), true, v.getFileName(), v.getPath());
                                            System.out.println("\u001B[32m ++++Invio a " + n.getNodeID() + "(" + n.getIp() + ":" + n.getPort() + ") \u001B[0m");

                                            try
                                            {
                                                tempS.close();
                                            }
                                            catch (IOException e)
                                            {
                                                e.printStackTrace();
                                            }
                                            tempS = new Socket();
                                            tempS.setSoTimeout(timeout);
                                            tempS.connect(new InetSocketAddress(n.getIp(), n.getPort()), timeout);
                                            os = tempS.getOutputStream();
                                            outputStream = new ObjectOutputStream(os);
                                            outputStream.writeObject(new StoreRequest(toSend, thisNode, n));
                                            outputStream.flush();

                                            System.out.println("\u001B[32m ++++Invio a " + n.getNodeID() + "(" + n.getIp() + ":" + n.getPort() + ") completato \u001B[0m");
                                        }
                                    }
                                    catch (EOFException eofe)
                                    {
                                        //Aspettata, ignoro
                                    }
                                    catch (ClassNotFoundException cnfe)
                                    {
                                        System.err.println("\u001B[31mErrore nella risposta durante il refresh: " + cnfe.getMessage() + "\u001B[0m");
                                        //Gli invio comunque il file
                                        KadFile toSend = new KadFile(v.getFileID(), true, v.getFileName(), v.getPath());
                                        outputStream.writeObject(new StoreRequest(toSend, thisNode, n));
                                    }
                                }
                                catch (SocketException se)
                                {
                                    System.err.println("\u001B[31mImpossibile aprire il socket verso " + n.getIp().toString() + ": " + se.getMessage() + "\u001B[0m");
                                }
                                catch (EOFException eofe)
                                {
                                    System.err.println("\u001B[31mEOF di: " + eofe.getCause() + "\u001B[0m");
                                }
                                catch (IOException ioe)
                                {
                                    System.err.println("\u001B[31mErrore nel refresh: " + ioe.getMessage() + "\u001B[0m");
                                    ioe.printStackTrace();
                                }
                                finally
                                {
                                    try
                                    {
                                        if (tempS != null)
                                        {
                                            tempS.close();
                                        }
                                    }
                                    catch (IOException ioe)
                                    {
                                        System.err.println("\u001B[31mErrore generale nel chiudere il socket del refresh: " + ioe.getMessage() + "\u001B[0m");
                                        ioe.printStackTrace();
                                    }
                                }
                            }
                            v.setLastRefresh(System.currentTimeMillis());
                        }
                    });

                    System.out.println("[" + Thread.currentThread().getName() + "] Lascio il lock della mappa");
                    System.out.println("[" + Thread.currentThread().getName() + "] Chiedo il lock della mappa");
                    globalFileWriteLock.lock();
                    System.out.println("[" + Thread.currentThread().getName() + "] Prendo il lock della mappa");
                    //Elimino qua i file per evitare ConcurrentModificationExceptions
                    for (KadFile i : toBeDeleted)
                    {
                        fileMap.remove(i.getFileID());
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "] Lascio il lock della mappa");
                    globalFileWriteLock.unlock();
                    System.out.println("Refresh dei file finito");
                }
                catch (InterruptedException ie)
                {
                    System.err.println("\u001B[31mThread di refresh dei file interrotto\u001B[0m");
                }
            }
        }
    }

    public static BigInteger distanza(KadNode o1, KadNode o2)
    {
        return o1.getNodeID().xor(o2.getNodeID());
    }

    public static String intToBinary(BigInteger n)
    {
        String num = "";
        for (int i = 0; i < Kademlia.BITID; i++)
        {
            num = (n.testBit(i) ? 1 : 0) + num;
        }
        return num;
    }

    public String printTree()
    {
        StringBuilder sb = new StringBuilder();
        printTree(routingTree.getRoot(), 0, sb);
        return sb.toString();
    }

    private void printTree(Node n, int indent, StringBuilder sb)
    {
        if (n instanceof Bucket)
        {
            Bucket b = (Bucket) n;
            Iterator<KadNode> ikn = b.iterator();
            while (ikn.hasNext())
            {
                StringBuilder s1 = new StringBuilder();
                for (int i = 0; i < indent; i++)
                {
                    s1.append("|  ");
                }
                sb.append(s1);

                sb.append("+  ");
                KadNode kn = ikn.next();
                sb.append(Kademlia.intToBinary(kn.getNodeID()) + " (" + kn.getNodeID() + ") from: " + kn.getIp().toString() + ":" + kn.getPort() + "(Dist: " + distanza(thisNode, kn) + ")");
                sb.append("\n");
            }
            return;
        }
        else
        {
            TreeNode tn = (TreeNode) n;

            StringBuilder s1 = new StringBuilder();
            for (int i = 0; i < indent; i++)
            {
                s1.append("|  ");
            }
            sb.append(s1);
            sb.append("+--");
            sb.append("1");
            sb.append("\n");

            printTree(tn.getLeft(), indent + 1, sb);
            s1 = new StringBuilder();
            for (int i = 0; i < indent; i++)
            {
                s1.append("|  ");
            }
            sb.append(s1);
            sb.append("+--");
            sb.append("0");
            sb.append("\n");
            printTree(tn.getRight(), indent + 1, sb);
        }
    }
}

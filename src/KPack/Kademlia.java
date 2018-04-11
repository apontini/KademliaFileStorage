package KPack;

import KPack.Files.KadFile;
import KPack.Files.KadFileMap;
import KPack.Packets.*;
import KPack.Tree.Bucket;
import KPack.Tree.Node;
import KPack.Tree.RoutingTree;
import KPack.Tree.TreeNode;
import KPack.Exceptions.*;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.*;

public class Kademlia implements KademliaInterf {

    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    public final static int ALPHA = 2;
    public final static String FILESPATH = "." + File.separator + "storedFiles" + File.separator;
    private KadFileMap fileMap;
    private BigInteger nodeID;
    private RoutingTree routingTree;
    private KadNode thisNode;
    private short UDPPort=1337; // default 1337
    private int fileRefreshWait=10000;
    private ArrayList<FixedKadNode> fixedNodesList = new ArrayList<>();
    private int timeout=10000;

    public Kademlia()
    {
        if (instance)
        {
            return; //Aggiungere un'eccezione tipo AlreadyInstanced
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
        System.out.println("Timeout socket: " + timeout);

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

        fileMap = new KadFileMap(this);
        String myIP = getIP().getHostAddress().toString();

        routingTree = new RoutingTree(this);

        fixedNodesList = loadFixedNodesFromFile();

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

        thisNode = new KadNode(myIP, UDPPort, nodeID);

        routingTree.add(thisNode); //Mi aggiungo

        new Thread(new ListenerThread()).start();

        networkJoin();

        new Thread(new FileRefresh(fileRefreshWait)).start();

    }



    private boolean isUniqueID()
    {
        List<KadNode> nodes = findNode(nodeID, true);       //richiamo il findnode utilizzando il mio ID
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
                                if (Short.valueOf(split[1]) > 1024 && Short.valueOf(split[1]) < 65535)
                                {
                                    UDPPort = Short.valueOf(split[1]);
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
                        case "fileRefreshWait":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    fileRefreshWait = Integer.parseInt(split[1]);
                                }
                                catch(NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Tempo di refresh non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Tempo di refresh non valido");
                            }
                            break;
                        case "socketTimeout":
                            if (!split[1].isEmpty())
                            {
                                try
                                {
                                    timeout = Integer.parseInt(split[1]);
                                }
                                catch(NumberFormatException | NullPointerException e)
                                {
                                    throw new InvalidSettingsException("Timeout non valido");
                                }
                            }
                            else
                            {
                                throw new InvalidSettingsException("Timeout non valido");
                            }
                            break;
                        default:
                            throw new InvalidSettingsException("Parametro non valido: " + split[0]);
                    }
                }
            }
        }
        catch (InvalidSettingsException ise)
        {
            System.err.println("Errore nelle impostazioni" + ise.getMessage());
            System.err.println("ABORT! ABORT!");
            System.exit(1);
        }
        catch (IOException ioe)
        {
            System.err.println("Errore nelle impostazioni" + ioe.getMessage());
            System.err.println("ABORT! ABORT!");
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
                    UDPPort = fkn.getUDPPort();
                    return true;
                }
            }
        }
        catch (UnknownHostException uhe)
        {
            System.err.println("Errore isFixedNode: " + uhe.getMessage());
            System.err.println("ABORT! ABORT!");
            System.exit(1);
        }

        return false;
    }

    private void networkJoin()
    {
        //Faccio il findNode su me stesso
        List<KadNode> nearestNodes = findNode(nodeID, false);
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

            FixedKadNode Punto = new FixedKadNode("pintini.ddns.net", (short) 1336, BigInteger.ONE, "pintini");
            FixedKadNode Tavolino = new FixedKadNode("tavolino.ddns.net", (short) 1336, BigInteger.valueOf(2), "tavolino");

            fixNodes.add(Punto);
            fixNodes.add(Tavolino);
        }
        catch (UnknownHostException uhe)
        {
            System.err.println("Errore nella scrittura dei nodi fissi: " + uhe.getMessage());
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
            System.err.println("Errore nella scrittura dei nodi fissi: " + ioe.getMessage());
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
                System.err.println("Errore nella scrittura dei nodi fissi: " + ioe.getMessage());
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
            System.err.println("Errore nella lettura dei nodi fissi: " + ioe.getMessage());
            System.err.println("ABORT! ABORT!");
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
            System.err.println("Errore nell'URL per l'IP: " + mue.getMessage());
            System.err.println("ABORT! ABORT!");
            System.exit(1);
        }
        catch (IOException ioe)
        {
            System.err.println("Eccezione generale nel trovare l'IP del nodo: " + ioe.getMessage());
            System.err.println("ABORT! ABORT!");
            System.exit(1);
        }
        try
        {
            return InetAddress.getByName(publicIP);
        }
        catch (UnknownHostException e)
        {
            System.err.println("Host sconosciuto nel trovare l'IP: ");
            System.err.println("ABORT! ABORT!");
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
            s.connect(new InetSocketAddress(node.getIp(), node.getUDPPort()));

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
                    //TODO
                    e.printStackTrace();
                }
            }
        }
        catch (SocketTimeoutException soe)
        {
            System.err.println("Timeout exception: " + soe.getMessage());
            return false;
        }
        catch (ConnectException soe)
        {
            System.err.println("Connect Exception: " + soe.getMessage());
            return false;
        }
        catch (EOFException e)
        {
            return false;
        }
        catch (IOException ex)
        {
            return false;
        }
    }

    private Object findValue_lookup(BigInteger fileID)    //Object può essere o una List<KadNode> oppure di tipo KadFile
    {
        //verifico se il file richiesto è contenuto nel nodo
        KadFile temp = fileMap.get(fileID);
        if(temp != null && temp.isRedundant())
            return temp;
        else
            return findNode_lookup(fileID); //se non lo è procedo come per il findnode_lookup
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
        while (true)
        {
            int size = lkn.size(); // per capire se il round di find nodes è fallito o meno
            //ad ognuno degli alpha node vado a inviargli un findNode
            // for (int i = 0; i < alphaNode.size(); i++)
            for (KadNode akn : alphaNode)
            {
                KadNode kadNode = akn;
                FindValueRequest fvr = new FindValueRequest(fileID, thisNode, kadNode, returnContent);
                try
                {
                    Socket s = new Socket();
                    s.setSoTimeout(timeout);
                    s.connect(new InetSocketAddress(kadNode.getIp(), kadNode.getUDPPort()),timeout);

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
                        try
                        {
                            Object fvreply = inputStream.readObject();
                            if (fvreply instanceof FindValueReply)
                            {
                                if (((FindValueReply) fvreply).getSourceKadNode().equals(fvr.getDestKadNode()))
                                {
                                    //restituisco il contenuto
                                    is.close();
                                    s.close();
                                    return ((FindValueReply) fvreply).getContent();
                                }
                            }
                            else
                            {
                                if ((fvreply instanceof FindNodeReply) && ((FindNodeReply) fvreply).getSourceKN().equals(fvr.getDestKadNode()))
                                {
                                    it = ((FindNodeReply) fvreply).getList().iterator();
                                    while (it.hasNext())
                                    {
                                        KadNode k = it.next();
                                        if (!(lkn.contains(k)))  // se mi da un nodo che conosco gia, non lo inserisco
                                        {
                                            lkn.add(k);
                                        }
                                    }
                                    is.close();
                                    s.close();
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
                            System.err.println("Errore nella risposta ricevuta: " + e.getMessage());
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
                    System.err.println("IOException " + ex.getMessage());
                }
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
                        node=(TreeNode)n;
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
        return findNodeMethod(targetID, false);
    }

    private List<KadNode> findNode(BigInteger targetID, boolean doNotTrack)
    {
        return findNodeMethod(targetID, doNotTrack);
    }

    private List<KadNode> findNodeMethod(BigInteger targetID, boolean doNotTrack)
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
                FindNodeRequest fnr = new FindNodeRequest(targetID, thisNode, kadNode, doNotTrack);
                threads[i] = new Thread(() ->
                {
                    try
                    {
                        Socket s = new Socket();
                        s.setSoTimeout(timeout);
                        s.connect(new InetSocketAddress(kadNode.getIp(), kadNode.getUDPPort()),timeout);

                        OutputStream os = s.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(fnr);
                        outputStream.flush();

                        InputStream is = s.getInputStream();
                        ObjectInputStream inputStream = new ObjectInputStream(is);

                        /*DatagramSocket ds=new DatagramSocket();
                        ds.setSoTimeout(timeout);
                        ds.connect(new InetSocketAddress(kadNode.getIp(), kadNode.getUDPPort()));

                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(baos);
                        outputStream.writeObject(fnr);
                        outputStream.flush();
                        byte[] buffer=baos.toByteArray();
                        DatagramPacket packet=new DatagramPacket(buffer,buffer.length,kadNode.getIp(), kadNode.getUDPPort());
                        ds.send(packet);
                        */
                        long timeInit = System.currentTimeMillis();
                        boolean state = true;
                        while (state)
                        {
                            try
                            {
                                Object fnreply = inputStream.readObject();
                                /*buffer=new byte[65536];
                                DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
                                ds.receive(recv);

                                ByteArrayInputStream bais=new ByteArrayInputStream(buffer);
                                ObjectInputStream inputStream=new ObjectInputStream(bais);
                                Object fnreply = inputStream.readObject();*/
                                if (fnreply instanceof FindNodeReply)
                                {
                                    if (((FindNodeReply) fnreply).getSourceKN().equals(fnr.getDestKadNode()))
                                    {
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
                                System.err.println("Errore nella risposta ricevuta: " + e.getMessage());
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
                        System.err.println("IOException " + ex.getMessage());
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
        System.out.println("****Prendo il lock della mappa");
        synchronized (fileMap)
        {
            fileMap.forEach((k, v) -> temp.add(v));
            System.out.println("<<<<Lascio il lock della mappa");
        }
        return temp;
    }

    public KadNode getMyNode()
    {
        return thisNode;
    }

    public short getUDPPort()
    {
        return UDPPort;
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

        System.out.println("Il file avrà ID: " + fileID);
        KadFile tempfile = new KadFile(fileID, false, temp.getName(), temp.getParent());
        fileMap.add(tempfile);

        StoreRequest sr = null;

        List<KadNode> closestK = findNode_lookup(fileID);

        // List<KadNode> closestK = findNode_lookup(fileID); togliere il commento per i test veri
        for (KadNode i : closestK)
        {
            sr = new StoreRequest(new KadFile(fileID, false, temp.getName(), filepath), thisNode, i);
            try
            {
                Socket s = new Socket(i.getIp(), i.getUDPPort());

                OutputStream os = s.getOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(os);
                outputStream.writeObject(sr);
                outputStream.flush();
            }
            catch (IOException ioe)
            {
                System.err.println("Errore generale nell'eseguire lo store: " + ioe.getMessage());
            }
        }
    }

    public void delete(BigInteger id) throws FileNotKnownException
    {

        KadFile temp = fileMap.get(id);

        if(temp != null && !(temp.isRedundant()))
        {
            DeleteRequest dr = null;
            List<KadNode> closestK = findNode_lookup(temp.getFileID());
            for (KadNode k : closestK)
            {
                dr = new DeleteRequest(temp.getFileID(), thisNode, k);
                try
                {
                    Socket s = new Socket();
                    s.setSoTimeout(timeout);
                    s.connect(new InetSocketAddress(k.getIp(), k.getUDPPort()));

                    OutputStream os = s.getOutputStream();
                    ObjectOutputStream outputStream = new ObjectOutputStream(os);
                    outputStream.writeObject(dr);
                    outputStream.flush();

                    /*DatagramSocket ds=new DatagramSocket();
                    ds.setSoTimeout(timeout);
                    ds.connect(new InetSocketAddress(k.getIp(), k.getUDPPort()));

                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    ObjectOutputStream outputStream = new ObjectOutputStream(baos);
                    outputStream.writeObject(dr);
                    outputStream.flush();
                    byte[] buffer=baos.toByteArray();
                    DatagramPacket packet=new DatagramPacket(buffer,buffer.length,k.getIp(), k.getUDPPort());
                    ds.send(packet);*/
                }
                catch (IOException ioe)
                {
                    System.err.println("Errore generale nell'eseguire il delete: " + ioe.getMessage());
                }
            }
            fileMap.remove(temp);
        }
        else
        {
            throw new FileNotKnownException();
        }
    }

    private class ListenerThread implements Runnable {

        private ServerSocket listener;

        @Override
        public void run()
        {
            try
            {
                listener = new ServerSocket(UDPPort);
                System.out.println("Thread Server avviato\n" + "IP: " + getIP() + "\nPorta: " + UDPPort);
            }
            catch (IOException ex)
            {
                System.err.println("Errore nell'apertura del socket del Thread Server: " + ex.getMessage());
                System.err.println("ABORT! ABORT!");
                System.exit(1);
            }

            while (true)
            {
                Socket connection = null;
                try
                {
                    System.out.println("Waiting for connection");

                    connection = listener.accept();
                    connection.setSoTimeout(timeout);
                    
                    System.out.println("Connection received from " + connection.getInetAddress().getHostAddress());

                    Object responseObject = null;

                    //Analizzo la richiesta ricevuta
                    InputStream is = connection.getInputStream();
                    ObjectInputStream inStream = new ObjectInputStream(is);

                    Object received = inStream.readObject();

                    //Elaboro la risposta
                    if (received instanceof FindNodeRequest)
                    {
                        FindNodeRequest fnr = (FindNodeRequest) received;
                        if (!fnr.toTrack())
                        {
                            new Thread(() ->
                            {
                                routingTree.add(fnr.getSourceKadNode());
                            }).start();
                        }

                        //System.out.println("Received FindNodeRequest from: " + fnr.getSourceKadNode().toString());

                        List<KadNode> lkn = findNode_lookup(fnr.getTargetID());

                        FindNodeReply fnrep = new FindNodeReply(fnr.getTargetID(), thisNode, fnr.getSourceKadNode(), lkn);

                        responseObject = fnrep;

                    }
                    else if (received instanceof FindValueRequest)
                    {
                        FindValueRequest fvr = (FindValueRequest) received;
                        new Thread(() ->
                        {
                            routingTree.add(fvr.getSourceKadNode());
                        }).start();
                        if (fvr.getDestKadNode().equals(thisNode))
                        {

                            Object value = findValue_lookup(fvr.getFileID());
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
                                responseObject = new FindNodeReply(fvr.getFileID(),thisNode,fvr.getSourceKadNode(),(List<KadNode>)value);
                            }
                        }
                    }
                    else if (received instanceof StoreRequest)
                    {
                        System.out.println("****Prendo il lock della mappa");

                        synchronized (fileMap)
                        {
                            StoreRequest rq = (StoreRequest) received;
                            new Thread(() ->
                            {
                                routingTree.add(rq.getSourceKadNode());
                            }).start();
                            //i file ridondanti vengono salvati con estensione .FILEID.kad
                            System.out.println("Ho ricevuto uno store di " + rq.getFileName() + " da  " + rq.getSourceKadNode().getIp());
                            File toStore = new File(FILESPATH + rq.getFileName() + "." + rq.getFileID() + ".kad");
                            toStore.createNewFile();
                            Files.write(toStore.toPath(), rq.getContent());
                            fileMap.add(new KadFile(rq.getFileID(), true, rq.getFileName() + "." + rq.getFileID() + ".kad", FILESPATH));
                            System.out.println("<<<<Lascio il lock della mappa");

                        }
                    }
                    else if (received instanceof DeleteRequest)
                    {
                        System.out.println("****Prendo il lock della mappa");
                        synchronized (fileMap)
                        {
                            DeleteRequest dr = (DeleteRequest) received;
                            new Thread(() ->
                            {
                                routingTree.add(dr.getSourceKadNode());
                            }).start();
                            System.out.println("Ho ricevuto un delete di " + dr.getFileName() + " da  " + dr.getSourceKadNode().getIp());
                            fileMap.remove(new KadFile(dr.getFileID(), true, dr.getFileName(), ""));
                            System.out.println("<<<<Prendo il lock della mappa");
                        }
                    }
                    else if (received instanceof PingRequest)
                    {
                        PingRequest pr = (PingRequest) received;
                        if (!(pr.getDestKadNode().equals(thisNode)))
                        {
                            connection.close();
                            continue;
                        }
                        KadNode sourceKadNode = pr.getSourceKadNode();

                        new Thread(() ->
                        {
                            routingTree.add(sourceKadNode);
                        }).start();

                        System.out.println("Received PingRequest from: " + pr.getSourceKadNode().toString());

                        PingReply reply = new PingReply(thisNode, sourceKadNode);

                        responseObject = reply;
                    }

                    if (responseObject != null)
                    {
                        OutputStream os = connection.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(responseObject);
                        outputStream.flush();
                        os.close();
                    }
                    connection.close();

                }
                catch (ClassNotFoundException | IOException ex)
                {
                    System.err.println("Errore nel thread server: " + ex.getMessage());
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
                        System.err.println("Errore nel thread server: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private class FileRefresh implements Runnable
    {
        private int sleep;

        public FileRefresh(int sleep)
        {
            this.sleep = sleep;
        }

        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(sleep);
                    System.out.println("Inizio il refresh dei file..");
                    System.out.println("****Prendo il lock della mappa");
                    synchronized (fileMap)
                    {
                        fileMap.forEach((k, v) ->
                        {
                            if (v.isRedundant())
                            {
                                System.out.println("++++Refresho " + v.getFileName());
                                List<KadNode> temp = findNode(v.getFileID(), false);
                                for (KadNode n : temp)
                                {
                                    Socket tempS = null;
                                    try
                                    {
                                        tempS = new Socket();
                                        tempS.setSoTimeout(timeout);
                                        System.out.println("++++Tento di contattare " + n.getNodeID() + "(" + n.getIp() + ":"+ n.getUDPPort() +")");
                                        tempS.connect(new InetSocketAddress(n.getIp(), n.getUDPPort()), timeout);
                                        OutputStream os = tempS.getOutputStream();
                                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                                        outputStream.writeObject(new FindValueRequest(k, thisNode, n, false));
                                        outputStream.flush();

                                        InputStream is = tempS.getInputStream();
                                        ObjectInputStream ois = new ObjectInputStream(is);
                                        try
                                        {
                                            while (true)
                                            {
                                                Object resp = ois.readObject();
                                                if (resp instanceof FindNodeReply)
                                                {
                                                    KadFile toSend = new KadFile(k, true, v.getFileName(), v.getPath());
                                                    System.out.println("++++Invio a " + n.getNodeID() + "(" + n.getIp() + ":"+ n.getUDPPort() +")");
                                                    outputStream.writeObject(new StoreRequest(toSend, thisNode, n));
                                                }
                                            }
                                        } catch (EOFException eofe)
                                        {
                                            //Aspettata, ignoro
                                        } catch (ClassNotFoundException cnfe)
                                        {
                                            System.err.println("Errore nella risposta durante il refresh: " + cnfe.getMessage());
                                            //Gli invio comunque il file
                                            KadFile toSend = new KadFile(k, true, v.getFileName(), v.getPath());
                                            outputStream.writeObject(new StoreRequest(toSend, thisNode, n));
                                        }
                                    }
                                    catch (SocketException se)
                                    {
                                        System.err.println("Impossibile aprire il socket verso " + n.getIp().toString());
                                    }
                                    catch (IOException ioe)
                                    {
                                        System.err.println("Errore generale nel refresh: " + ioe.getMessage());
                                    }
                                    finally
                                    {
                                        try
                                        {
                                            if (tempS != null)
                                            {
                                                tempS.close();
                                            }
                                        } catch (IOException ioe)
                                        {
                                            System.err.println("Errore generale nel chiudere il socket del refresh: " + ioe.getMessage());
                                        }
                                    }
                                }
                                //Se non sono tra i K nodi, elimino il file da me
                                boolean state = false;
                                for (KadNode i : temp)
                                {
                                    if (thisNode.getNodeID().equals(i.getNodeID()))
                                    {
                                        state = true;
                                        break;
                                    }
                                }
                                if (!state) fileMap.remove(v.getFileID());
                            }
                        });
                        System.out.println("<<<<Lascio il lock della mappa");
                    }
                    System.out.println("Refresh dei file finito");
                }
                catch (InterruptedException ie)
                {
                    System.err.println("Thread di refresh dei file interrotto");
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
}

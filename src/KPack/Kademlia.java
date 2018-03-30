package KPack;

import KPack.Files.KadFile;
import KPack.Files.KadFileList;
import KPack.Packets.*;
import KPack.Tree.Bucket;
import KPack.Tree.Node;
import KPack.Tree.RoutingTree;
import KPack.Tree.TreeNode;
import KPack.Exceptions.*;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Kademlia implements KademliaInterf {

    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    public final static int ALPHA = 2;
    public final static String FILESPATH = "." + File.separator + "storedFiles" + File.separator;
    public KadFileList fileList;
    private BigInteger nodeID;
    private RoutingTree routingTree;
    private KadNode thisNode;
    public short UDPPort = 1337;
    private ArrayList<KadNode> fixedNodesList = new ArrayList<>();

    //public HashMap<BigInteger, String> fixedNodes = new HashMap<>();
    private final int pingTimeout = 10000;

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

        //Lo rieseguo, potrebbe non essere stato eseguito in seguito ad un crash
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

        fileList = new KadFileList(this);
        String myIP = getIP().getHostAddress().toString();
        boolean first = true;
        if(first)
        {
            writeFixedList();
            first = false;
        }

        fixedNodesList = loadFixedNodesFromFile();

        boolean exists = true;
        do
        {
            if (fixedNode())
            {
                break;
            }

            nodeID = new BigInteger(BITID, new Random());
            exists = false;

            //Controllare se esiste
            //TODO
        }
        while (exists);

        thisNode = new KadNode(myIP, UDPPort, nodeID);

        routingTree = new RoutingTree(this);
        routingTree.add(thisNode); //Mi aggiungo

        new Thread(new ListenerThread()).start();

        networkJoin();

        new Thread(new FileRefresh()).start();

        //aggiungo nodi random, solo per testare il find node
        /*for (int i = 1; i < 6; i++)
        {
            routingTree.add(new KadNode("192.168.1.20", (short) 1337, new BigInteger("" + i * 30)));
        }*/
    }

    private boolean fixedNode()
    {
        String hostname;
        InetAddress addr;
        try
        {
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
            if (hostname.toLowerCase().equals("tavolino"))
            {
                nodeID = new BigInteger("2");
                UDPPort = 1336;
                System.out.println(nodeID);
                return true;
            }
            else if (hostname.toLowerCase().equals("SERVER LUCA"))
            {
                nodeID = new BigInteger("0");
                return true;
            }
            else if (hostname.toLowerCase().equals("pintini"))
            {
                nodeID = new BigInteger("1");
                return true;
            }
        }
        catch (UnknownHostException uhe)
        {
        }
        return false;
    }

    private void networkJoin()
    {
        // try
        //{
        //Aggiungo all'alberto i nodi noti
        try
        {
            KadNode tavolino = new KadNode(InetAddress.getByName("tavolino.ddns.net").getHostAddress(), (short) 1336, BigInteger.valueOf(2));
            routingTree.add(tavolino);
            KadNode pintini = new KadNode(InetAddress.getByName("pintini.ddns.net").getHostAddress(), (short) 1337, BigInteger.valueOf(1));
            routingTree.add(pintini);
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(Kademlia.class.getName()).log(Level.SEVERE, null, ex);
        }

        // }
        /*catch (UnknownHostException ex)
        {
            ex.printStackTrace();
        }*/
        //Faccio il findNode su me stesso
        List<KadNode> nearestNodes = findNode(nodeID);
        for (KadNode kn : nearestNodes)
        {
            routingTree.add(kn);
        }
    }

    public void writeFixedList()    //poi questo sarà da chiamare da qualche parte una sola volta e poi da commentare
    {
        ArrayList<KadNode> fixNodes = new ArrayList<>();
        try
        {
            InetAddress inAddrPunto = InetAddress.getByName("pintini.ddns.net");
            String addressPunto = inAddrPunto.getHostAddress();

            InetAddress inAddrTavo = InetAddress.getByName("tavolino.ddns.net");
            String addressTavo = inAddrTavo.getHostAddress();

            KadNode Punto = new KadNode(addressPunto, (short) 1337, BigInteger.ONE);
            KadNode Tavolino = new KadNode(addressTavo, (short) 1336, BigInteger.valueOf(2));

            fixNodes.add(Punto);
            fixNodes.add(Tavolino);
        }
        catch (UnknownHostException uhe)
        {
            uhe.printStackTrace();
        }
        //Scrive file "nodes", inserendoci la lista fixNodes e serializza il file
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try
        {
            fout = new FileOutputStream( "." + File.separator + "nodes");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(fixNodes);
        }
        catch (IOException ioe)
        {
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
                ioe.printStackTrace();
            }
        }
    }

    private ArrayList<KadNode> loadFixedNodesFromFile() //va a leggere il file serializzato e restituisce la lista di KadNodes fissi.
    {
        ArrayList<KadNode> retFixNodes = new ArrayList<>();

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream("." + File.separator + "nodes");
            ObjectInputStream ois = new ObjectInputStream(fis);
            //retFixNodes = new ArrayList<>();      ??
            while (true)
            {
                retFixNodes = ((ArrayList<KadNode>) ois.readObject());
            }
        }
        catch (EOFException | FileNotFoundException | ClassNotFoundException e)
        {
            //Aspettate o impossibili
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
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

    /*  public InetAddress getIP()   //per il momento restituisce l'ip locale.
    {
        try
        {
            return InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException ex)
        {
            ex.printStackTrace();
            /////////////// DA GESTIRE
        }
        return null;
    }*/
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
            mue.printStackTrace();
            /////////////// DA GESTIRE
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        try
        {
            return InetAddress.getByName(publicIP);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            //DA GESTIRE
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
            Socket s = new Socket(node.getIp(), node.getUDPPort());
            s.setSoTimeout(pingTimeout);

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
                    s.setSoTimeout(((int) (pingTimeout - (System.currentTimeMillis() - timeInit))));
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (SocketTimeoutException soe)
        {
            System.out.println("Timeout");
            return false;
        }
        catch (ConnectException soe)
        {
            System.out.println("Non c'è risposta");
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

    public Object findValue(BigInteger fileID)
    {
        return null;
    }

    private List<KadNode> findNode_lookup(BigInteger targetID)
    {
        Bucket bucket = routingTree.findNodesBucket(new KadNode("", (short) 0, targetID));
        BigInteger currentID = targetID;                      //mi serve per tenere traccia del percorso che ho fatto nell'albero
        int depth = ((Node) bucket).getDepth();
        List<KadNode> lkn = new ArrayList<>();
        Iterator<KadNode> it = null;
        synchronized (bucket)//lista dei K nodi conosciuti più vicini al target
        {
            it = bucket.iterator();
            while (it.hasNext())                                 //inserisco l'intero bucket nella lista lkn (lkn conterrà i nodi (<=K) più vicini a targetID che conosco)
            {
                lkn.add(it.next());
            }
        }
        TreeNode node = (TreeNode) bucket.getParent();
        int count = depth;                                    //count rappresenta la profondità del nodo in cui sono ad ogni istante.
        while (count > 0 && lkn.size() < K)                      //ricerco altri nodi vicini al targetID finche non arrivo a K o non ho guardato tutti i nodi nell'albero
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
                    synchronized (n)
                    {
                        it = ((Bucket) n).iterator();
                        while (it.hasNext())
                        {
                            lkn.add(it.next());
                        }
                    }
                    node = (TreeNode) node.getParent();
                    count--;
                }
                else
                {
                    //seguo il percorso del targetID a partire da n fino ad arrivare ad un bucket. Questo conterrà i nodi più vicini al target
                    //tra quelli non ancora visitati
                    while (n instanceof Bucket)
                    {
                        count++;
                        if (targetID.testBit((BITID - count) - 1))
                        {
                            n = node.getLeft();
                        }
                        else
                        {
                            n = node.getRight();
                        }
                    }
                    node = (TreeNode) n.getParent();
                    synchronized (n)
                    {
                        it = ((Bucket) n).iterator();
                        while (it.hasNext())
                        {
                            lkn.add(it.next());
                        }
                    }
                }
            }
        }
        return lkn;
    }

    public List<KadNode> findNode(BigInteger targetID)
    {
        Bucket bucket = routingTree.findNodesBucket(thisNode);
        KadNode targetKN = new KadNode("", (short) 0, targetID);
        int depth = ((Node) bucket).getDepth();
        BigInteger prefix = thisNode.getNodeID().shiftRight(BITID - depth); // prendo il prefisso relativo al bucket
        List<KadNode> lkn = new ArrayList<>();  // lista di tutti i nodi conosciuti
        List<KadNode> alphaNode;
        AbstractQueue<KadNode> queriedNode = new PriorityQueue<>((o1, o2)
                -> distanza(o1, targetKN).compareTo(distanza(o2, targetKN))); // lista dei nodi interrogati
        Iterator<KadNode> it = null;
        synchronized (bucket)
        {
            it = bucket.iterator();
            while (it.hasNext()) // inserisco l'intero bucket nella lista lkn
            {
                lkn.add(it.next());
            }
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
            synchronized (bucket)
            {
                it = bucket.iterator();

                while (it.hasNext())
                {
                    list.add(it.next());
                }
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
            for (KadNode akn : alphaNode)
            {
                KadNode kadNode = akn;
                FindNodeRequest fnr = new FindNodeRequest(targetID, thisNode, kadNode);
                try
                {
                    Socket s = new Socket(kadNode.getIp(), kadNode.getUDPPort());
                    s.setSoTimeout(pingTimeout);

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
                                if (((FindNodeReply) fnreply).getSourceKN().equals(fnr.getDestKadNode()))
                                {
                                    it = ((FindNodeReply) fnreply).getList().iterator();
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
                                s.setSoTimeout(((int) (pingTimeout - (System.currentTimeMillis() - timeInit))));
                            }
                        }
                        catch (ClassNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (SocketTimeoutException soe)
                {
                    //soe.printStackTrace();
                }
                catch (ConnectException soe)
                {
                    //soe.printStackTrace();
                }
                catch (EOFException e)
                {
                    // e.printStackTrace();
                }
                catch (IOException ex)
                {
                    //ex.printStackTrace();
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

    public KadFileList getFileList()
    {
        return fileList;
    }

    public KadNode getMyNode()
    {
        return thisNode;
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
        boolean exists = true;
        do
        {
            fileID = new BigInteger(BITID, new Random());
            //Controllare se esiste
            //TODO
            exists = false;
        }
        while (exists);

        KadFile tempfile = new KadFile(fileID, false, temp.getName(), temp.getParent());
        fileList.add(tempfile);

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
                ioe.printStackTrace(); //Gestire
            }
        }
    }

    public void delete(BigInteger id) throws FileNotKnown
    {
        //Funzione temporanea, non completa
        for (KadFile i : fileList)
        {
            if (i.getFileID().equals(id))
            {


                DeleteRequest dr = null;
                List<KadNode> closestK = findNode_lookup(i.getFileID());

                fileList.remove(i);

                // List<KadNode> closestK = findNode_lookup(fileID); togliere il commento per i test veri
                for (KadNode k : closestK)
                {
                    dr = new DeleteRequest(i.getFileID(), thisNode, k);
                    try
                    {
                        Socket s = new Socket(k.getIp(), k.getUDPPort());
                        s.setSoTimeout(pingTimeout);

                        OutputStream os = s.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(dr);
                        outputStream.flush();

                        InputStream is = s.getInputStream();
                        ObjectInputStream inputStream = new ObjectInputStream(is);

                        if (inputStream.readObject() instanceof FindNodeReply)
                        {
                            //Invio il file al nodo
                        }
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace(); //Gestire
                    }
                    catch (ClassNotFoundException cnfe)
                    {
                        System.out.println("Risposta sconosciuta");
                    }
                }
            }
        }
        throw new FileNotKnown();
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
                ex.printStackTrace();
                ////////// DA GESTIRE
            }

            Socket connection;
            while (true)
            {

                try
                {
                    System.out.println("Waiting for connection");

                    connection = listener.accept();
                    System.out.println("Connection received from " + connection.getInetAddress().getHostAddress());

                    //Analizzo la richiesta ricevuta
                    InputStream is = connection.getInputStream();
                    ObjectInputStream inStream = new ObjectInputStream(is);

                    Object received = inStream.readObject();

                    //Elaboro la risposta
                    if (received instanceof FindNodeRequest)
                    {
                        FindNodeRequest fnr = (FindNodeRequest) received;
                        new Thread(() ->
                        {
                            routingTree.add(fnr.getSourceKadNode());
                        }).start();

                        List<KadNode> lkn = findNode_lookup(fnr.getTargetID());

                        FindNodeReply fnrep = new FindNodeReply(fnr.getTargetID(), thisNode, fnr.getSourceKadNode(), lkn);

                        OutputStream os = connection.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(fnrep);
                        outputStream.flush();

                        os.close();

                    }
                    else if (received instanceof FindValueRequest)
                    {
                        FindValueRequest fvr = (FindValueRequest) received;
                        new Thread(() ->
                        {
                            routingTree.add(fvr.getKadNode());
                        }).start();
                    }
                    else if (received instanceof StoreRequest)
                    {
                        StoreRequest rq = (StoreRequest) received;
                        //i file ridondanti vengono salvati con estensione .FILEID.kad
                        System.out.println("Ho ricevuto uno store di " +rq.getFileName() + " da  " + rq.getSourceKadNode().getIp());
                        File toStore = new File(FILESPATH+rq.getFileName()+"." + rq.getFileID() +".kad");
                        toStore.createNewFile();
                        Files.write(toStore.toPath(), rq.getContent());
                        fileList.add(new KadFile(rq.getFileID(),true,rq.getFileName()+"." + rq.getFileID() +".kad", FILESPATH));
                    }
                    else if (received instanceof DeleteRequest)
                    {
                        DeleteRequest dr = (DeleteRequest) received;
                        System.out.println("Ho ricevuto un delete di " +dr.getFileName() + " da  " + dr.getSourceKadNode().getIp());
                        fileList.remove(new KadFile(dr.getFileID(),true,dr.getFileName(),""));
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

                        OutputStream os = connection.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(reply);
                        outputStream.flush();

                        os.close();
                    }

                    connection.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    ////// GESTIREE
                }
                catch (ClassNotFoundException ex)
                {
                    ex.printStackTrace();
                    //// GESTIREEEEE
                }
            }
        }
    }

    private class FileRefresh implements Runnable {

        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(300000); //5 minuti
                    for (KadFile i : fileList)
                    {
                        if (i.isRedundant())
                        {
                            List<KadNode> temp = findNode(i.getFileID());
                            for (KadNode n : temp)
                            {
                                Socket tempS = new Socket();
                                OutputStream os = tempS.getOutputStream();
                                ObjectOutputStream outputStream = new ObjectOutputStream(os);
                                outputStream.writeObject(new FindValueRequest(i.getFileID(), n, false));
                                outputStream.flush();
                            }
                        }
                    }

                }
                catch (InterruptedException ie)
                {
                    System.out.println("Thread di refresh dei file interrotto");
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
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

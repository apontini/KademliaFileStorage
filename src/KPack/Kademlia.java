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
import java.util.*;

public class Kademlia implements KademliaInterf {

    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    public final static int ALPHA = 2;
    public final static String FILESPATH = "./storedFiles/";
    public KadFileList fileList;
    private BigInteger nodeID;
    private RoutingTree routingTree;
    private KadNode thisNode;
    public short UDPPort = 1337;
    public HashMap<BigInteger, String> fixedNodes = new HashMap<>();

    private final int pingTimeout = 15000;

    public Kademlia()
    {
        if (instance) return; //Aggiungere un'eccezione tipo AlreadyInstanced
        instance = true;

        fileList = new KadFileList(this);
        String myIP = getIP().getHostAddress().toString();
        fixedNodes.put(BigInteger.ONE,"79.6.223.119");   //aggiungo ID,IP alla mappa
        //fixedNodes.put(BigInteger.valueOf(2),"x.x.x.x");   //aggiungo ID,IP alla mappa
        //fixedNodes.put(BigInteger.valueOf(3),"x.x.x.x");   //aggiungo ID,IP alla mappa
        //fixedNodes.put(BigInteger.valueOf(4),"x.x.x.x");   //aggiungo ID,IP alla mappa
        boolean exists = true;
        do
        {
            nodeID = new BigInteger(BITID, new Random());
            if(!fixedNodes.containsKey(nodeID))     //controlla che non sia ID fisso
                exists = false;
            //Controllare se esiste
            //TODO
        }
        while (exists);

        thisNode = new KadNode(myIP, UDPPort, nodeID);
        routingTree = new RoutingTree(this);
        routingTree.add(thisNode); //Mi aggiungo


        new Thread(new ListenerThread()).start();
    }
    /*
    public InetAddress getIP()   //per il momento restituisce l'ip locale.
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
    }
    */

    public InetAddress getIP()
    {
        String publicIP = null;
        try {
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
        catch(UnknownHostException e)
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
        PingRequest pr = new PingRequest(thisNode,node);
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
            boolean state = true;
            while(true)
            {
                try
                {
                    Object preply = inputStream.readObject();
                    if(preply instanceof PingReply)
                    {
                        if(((PingReply)preply).getSourceKadNode().equals(pr.getDestKadNode()))
                        {
                            is.close();
                            s.close();
                            return true;
                        }
                    }
                    s.setSoTimeout(((int)(pingTimeout-(System.currentTimeMillis()-timeInit))));
                }
                catch(ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch(SocketTimeoutException soe)
        {
            System.out.println("Timeout");
            return false;
        }
        catch(ConnectException soe)
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
            ex.printStackTrace();
            return false;
        }
    }

    public Object findValue(BigInteger fileID)
    {
        return null;
    }

    public List<KadNode> findNode(BigInteger targetID)
    {
        Bucket bucket=routingTree.findNodesBucket(new KadNode("",(short)0,targetID));
        BigInteger currentID=targetID;                      //mi serve per tenere traccia del percorso che ho fatto nell'albero
        int depth=((Node)bucket).getDepth();
        List<KadNode> lkn=new ArrayList<>();                //lista dei K nodi conosciuti più vicini al target
        Iterator<KadNode> it=bucket.getList();
        while(it.hasNext())                                 //inserisco l'intero bucket nella lista lkn (lkn conterrà i nodi (<=K) più vicini a targetID che conosco)
        {
            lkn.add(it.next());
        }
        TreeNode node=(TreeNode)bucket.getParent();
        int count=depth;                                    //count rappresenta la profondità del nodo in cui sono ad ogni istante.
        while(count>0 && lkn.size()<K)                      //ricerco altri nodi vicini al targetID finche non arrivo a K o non ho guardato tutti i nodi nell'albero
        {
            //sono ad un certo TreeNode node dell'albero, se seguo il targetID, partendo da node, e vado nello stesso ramo che raggiungo seguendo il currentID (partendo da node),
            // allora vuol dire che il sottoalbero relativo a quel ramo l'ho già visitato e passo a visitare il sottoalbero fratello. Altrimenti vuol dire che ho già
            // visitato entrambi i sottoalberi (sinistro e destro) di node e mi sposto più in su al nodo padre di node.
            //Sfrutto il fatto che due ID sono tanto più distanti quanto più verso sinistra è il primo bit diverso. 10111 è più distante da 11111 piuttosto che da 10000
            if(!(targetID.testBit((BITID-count)-1) && currentID.testBit((BITID-count)-1) ||
                    (!(targetID.testBit((BITID-count)-1)) && !(currentID.testBit((BITID-count)-1)))))
            {
                //qui ho già visitato entrambi i sottoalberi
                node=(TreeNode)node.getParent();
                count--;
            }
            else
            {
                //qui il sottoalbero fratello non l'ho ancora visitato
                Node n;
                if(targetID.testBit((BITID-count)-1))    //individuo se sono figlio destro o sinistro di node, poi mi sposto nel fratello per visitarlo
                    n=node.getRight();
                else
                    n=node.getLeft();
                //aggiorno currentID perchè il bit alla profondità del nodo fratello è diverso da quello del targetID, questo mi permette, quando risalgo,
                //di ricordarmi se ho già visitato o meno quel sottoalbero
                currentID=currentID.flipBit((BITID-count)-1);
                if(n instanceof Bucket)
                {
                    it=((Bucket) n).getList();
                    while(it.hasNext())
                        lkn.add(it.next());
                    node=(TreeNode) node.getParent();
                    count--;
                }
                else
                {
                    //seguo il percorso del targetID a partire da n fino ad arrivare ad un bucket. Questo conterrà i nodi più vicini al target
                    //tra quelli non ancora visitati
                    while(n instanceof Bucket)
                    {
                        count++;
                        if(targetID.testBit((BITID-count)-1))
                            n=node.getLeft();
                        else
                            n=node.getRight();
                    }
                    node=(TreeNode) n.getParent();
                    it=((Bucket) n).getList();
                    while(it.hasNext())
                        lkn.add(it.next());
                }
            }
        }
        return lkn;
    }

    public List<KadNode> findNode_lookup(BigInteger targetID)
    {
        Bucket bucket=routingTree.findNodesBucket(thisNode);
        KadNode targetKN=new KadNode("",(short)0,targetID);
        int depth=((Node)bucket).getDepth();
        BigInteger prefix=thisNode.getNodeID().shiftRight(BITID-depth); // prendo il prefisso relativo al bucket
        List<KadNode> lkn=new ArrayList<>();  // lista di tutti i nodi conosciuti
        List<KadNode> alphaNode;
        AbstractQueue<KadNode> queryNode=new PriorityQueue<>(); // lista dei nodi interrogati
        Iterator<KadNode> it=bucket.getList();
        while(it.hasNext()) // inserisco l'intero bucket nella lista lkn
        {
            lkn.add(it.next());
        }
        TreeNode node=(TreeNode)bucket.getParent();
        int count=depth;
        //sfrutto il fatto che solo il bucket contente this node viene splittato, quindi risalendo l'albero ogni fratello è un bucket
        while(count>0 && lkn.size()<K)  // se il bucket non contiene K nodi, mi sposto negli altri bucket vicini per prendere i loro nodi fino a raggiungere K
        {
            if(prefix.testBit(depth-count))
                bucket=(Bucket)node.getRight();
            else
                bucket=(Bucket)node.getLeft();
            it=bucket.getList();
            List<KadNode> list=new ArrayList<>();
            while(it.hasNext())
            {
                list.add(it.next());
            }
            list.sort((o1, o2) ->
                    distanza(o1, thisNode).compareTo(distanza(o2,thisNode)));
            if(list.size()>=K-lkn.size())
                lkn.addAll(list.subList(0,K-lkn.size()));
            else
                lkn.addAll(list);
            node=(TreeNode)node.getParent();
            count--;
        }
        lkn.sort((o1, o2) ->
                distanza(o1, targetKN).compareTo(distanza(o2,targetKN)));
        if(lkn.size()>=ALPHA)
            alphaNode=lkn.subList(0,ALPHA);
        else
            alphaNode=lkn;
        //chiedo anche a me stesso
        while(true)
        {
            int size=lkn.size(); // per capire se il round di find nodes è fallito o meno
            //ad ognuno degli alpha node vado a inviargli un findNode
            for(int i=0;i<alphaNode.size();i++)
            {
                KadNode kadNode=alphaNode.get(i);
                FindNodeRequest fnr = new FindNodeRequest(targetID, thisNode, kadNode);
                try {
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
                    while (state) {
                        try {
                            Object fnreply = inputStream.readObject();
                            if (fnreply instanceof FindNodeReply) {
                                if (((FindNodeReply) fnreply).getSourceKN().equals(fnr.getDestKadNode()))
                                {
                                    it=((FindNodeReply) fnreply).getList().iterator();
                                    while(it.hasNext())
                                    {
                                        KadNode k=it.next();
                                        if(!(lkn.contains(k)))  // se mi da un nodo che conosco gia, non lo inserisco
                                            lkn.add(k);
                                    }
                                    is.close();
                                    s.close();
                                    state = false;
                                }
                            }
                            if (state)
                                s.setSoTimeout(((int) (pingTimeout - (System.currentTimeMillis() - timeInit))));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SocketTimeoutException soe) {
                    soe.printStackTrace();
                } catch (ConnectException soe) {
                    soe.printStackTrace();
                } catch (EOFException e) {
                    e.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            queryNode.addAll(alphaNode);
            lkn.sort((o1, o2) ->
                    distanza(o1, targetKN).compareTo(distanza(o2, targetKN)));
            if(lkn.size()<K)
            {
                if (queryNode.containsAll(lkn))
                    return lkn;
            }
            else
            {
                if (queryNode.containsAll(lkn.subList(0, K)))
                    return lkn.subList(0, K);
            }
            alphaNode.clear();
            int alphaSize;
            if(size==lkn.size()) //caso in cui il round di find nodes fallisce, cioè nessuno dei alpha node mi da nuovi nodi
                alphaSize=K;
            else
                alphaSize=ALPHA;
            int i=0;
            while(i<lkn.size() && alphaNode.size()<alphaSize)
            {
                if(!(queryNode.contains(lkn.get(i))))
                    alphaNode.add(lkn.get(i));
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

    public void store(String filepath) throws FileNotFoundException //gestire eccezioni
    {
        //Funzione temporanea, non completa
        File temp = new File(filepath);
        if(!temp.exists()) throw new FileNotFoundException();

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

        KadFile tempfile = new KadFile(fileID, false, temp.getName(), filepath);
        fileList.add(tempfile);
        //Manca la parte di invio agli altri nodi
    }

    public void delete(BigInteger id) throws FileNotKnown
    {
        //Funzione temporanea, non completa
        for (KadFile i : fileList)
        {
            if(i.getFileID().equals(id))
            {
                fileList.remove(i);
                //Manca l'invio agli altri nodi
                System.out.println("Eliminato");
                return;
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
                    /*
                    if (received instanceof PingReply)
                    {
                        PingReply pr = (PingReply) received;
                        System.out.println("Received PingReply from: " + pr.toString());
                        KadNode kn = pr.getKadNode();

                        synchronized (pendentPing)
                        {
                            if (pendentPing.contains(kn));
                            {
                                pendentPing.remove(kn);
                                notifyAll();
                            }
                        }
                    }
                    */
                    if(received instanceof FindNodeRequest)
                    {

                    }
                    else if(received instanceof FindValueRequest)
                    {

                    }
                    else if (received instanceof StoreRequest)
                    {
                        StoreRequest rq = (StoreRequest) received;

                    }
                    else if(received instanceof DeleteRequest)
                    {

                    }
                    else if (received instanceof PingRequest)
                    {
                        PingRequest pr = (PingRequest) received;
                        if(!(pr.getDestKadNode().equals(thisNode)))
                        {
                            connection.close();
                            continue;
                        }
                        KadNode sourceKadNode = pr.getSourceKadNode();

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

    public static BigInteger distanza(KadNode o1,KadNode o2)
    {
        return o1.getNodeID().xor(o2.getNodeID());
    }
    
    public static String intToBinary(BigInteger n)
    {
        String num="";
        for(int i=0;i<Kademlia.BITID;i++)
        {
            num=(n.testBit(i)?1:0)+num;
        }
        return num;
    }
}

package KPack;

import KPack.Files.KadFile;
import KPack.Packets.PingReply;
import KPack.Packets.PingRequest;
import KPack.Tree.RoutingTree;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Kademlia implements KademliaInterf {

    private static boolean instance = false;
    public final static int BITID = 8;
    public final static int K = 4;
    private BigInteger nodeID;
    private List<KadFile> fileList;
    private RoutingTree routingTree;
    private KadNode thisNode;
    private short UDOPort = 1337;

    private List<KadNode> pendentPing;
    private final long pingTimeout=60000; //1 minuto

    public Kademlia()
    {
        if (instance)
        {
            return; //Aggiungere un'eccezione tipo AlreadyInstanced
        }
        fileList = new ArrayList<>();

        String myIP = getIP(); //TODO

        boolean exists = true;
        do
        {
            nodeID = new BigInteger(BITID, new Random());
            //Controllare se esiste
            //TODO
            exists = false;
        }
        while (exists);

        thisNode = new KadNode(myIP, UDOPort, nodeID);

        routingTree = new RoutingTree(this);

        instance = true;

        pendentPing = new ArrayList<>();

        new Thread(new ListenerThread()).start();
    }

    private String getIP()   //per il momento restituisce l'ip locale.
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress().toString();
        }
        catch (UnknownHostException ex)
        {
            ex.printStackTrace();
            /////////////// DA GESTIRE
        }
        return null;
    }

    public BigInteger getNodeID()
    {
        return nodeID;
    }

    public boolean ping(KadNode node)
    {
        PingRequest pr = new PingRequest(thisNode);

        try
        {
            Socket s = new Socket(node.getIp(), node.getUDPPort());

            synchronized (pendentPing)
            {
                pendentPing.add(node);
            }

            OutputStream os = s.getOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(pr);

            s.close();
            
            try 
            {
                wait(pingTimeout);
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            
            synchronized (pendentPing)
            {
                if(pendentPing.contains(node))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
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

    public List<KadNode> findNode(BigInteger nodeID)
    {
        return null;
    }

    public List<KadFile> getFileList()
    {
        return fileList;
    }

    public KadNode getMyNode()
    {
        return thisNode;
    }

    public void store(KadNode node, KadFile file) //gestire eccezioni
    {

    }

    private class ListenerThread implements Runnable {

        private ServerSocket listener;

        @Override
        public void run()
        {
            try
            {
                listener = new ServerSocket(UDOPort);
                System.out.println("Thread Server avviato\n" + "IP: " + getIP() + "\nPorta: " + UDOPort);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                ////////// DA GESTIRE
            }

            while (true)
            {
                Socket connection;
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
                    if (received instanceof PingReply)
                    {
                        PingReply pr = (PingReply) received;
                        System.out.println("Received PingReply from: " + pr.toString());
                        //KadNode kn=pr.getKadNode();
                        KadNode kn=new KadNode(pr.getIpKadNode().getHostAddress(), pr.getUDPport(), pr.getNodeID());
                        
                        synchronized(pendentPing)
                        {
                            if(pendentPing.contains(kn));
                            {
                                pendentPing.remove(kn);
                                notifyAll();
                            }
                        }
                    }

                    if (received instanceof PingRequest)
                    {
                        PingRequest pr = (PingRequest) received;
                        System.out.println("Received PingRequest from: " + pr.toString());
                        
                        PingReply reply=new PingReply(thisNode);
                        
                        connection = new Socket(pr.getIpKadNode(),pr.getUDPport());
                        OutputStream os = connection.getOutputStream();
                        ObjectOutputStream outputStream = new ObjectOutputStream(os);
                        outputStream.writeObject(reply);
                        
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
}

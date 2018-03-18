package KPack;

import KPack.Files.KadFile;
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
        return false;
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

                    System.out.println(inStream.readObject());
                    
                    //Elaboro la risposta
                    
                    
                    
                    //Mando la risposta
                    OutputStream os = connection.getOutputStream();
                    ObjectOutputStream outputStream = new ObjectOutputStream(os);
                    outputStream.writeObject(os);

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

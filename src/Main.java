
import KPack.Exceptions.FileNotKnown;
import KPack.Files.KadFile;
import KPack.KadNode;
import KPack.Kademlia;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        System.out.println("Cerco un ID valido..");
        Kademlia myNode = new Kademlia();
        boolean keep = true;
        String in = null;
        Scanner reader = new Scanner(System.in);

        System.out.println("I file conosciuti nella lista sono: ");
        for (KadFile i : myNode.getFileList())
        {
            System.out.println(i.toString());
        }

        while (keep)
        {
            System.out.print("KadNode@ " + myNode.getNodeID() + "-: ");
            in = reader.nextLine();
            String[] split = in.split(" ");
            switch (split[0])
            {
                case "get":
                    //Cerca di ottenere un file di cui conosce l'esistenza, se è possibile
                    if (split.length > 1)
                    {
                        synchronized (myNode.getFileList())
                        {
                            for (KadFile i : myNode.getFileList())
                            {
                                if (i.getFileName().equals(split[1]))
                                {
                                    myNode.findValue(i.getFileID());
                                    break;
                                }
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Please define a file to get");
                    }
                    break;
                case "ls":
                    //Lista tutti i file di cui conosco l'esistenza
                    synchronized (myNode.getFileList())
                    {
                        for (KadFile i : myNode.getFileList())
                        {
                            if (!(i.isRedundant()))
                            {
                                System.out.println(i.toString());
                            }
                        }
                    }
                    break;
                case "ip":
                    System.out.println(myNode.getIP().getHostAddress());
                    break;
                case "store":
                    if (split.length > 1)
                    {
                        try
                        {
                            myNode.store(split[1]);
                        }
                        catch (IOException ioe)
                        {
                            ioe.printStackTrace();
                        }
                    }
                    else
                    {
                        System.out.println("Inserisci il percorso del file");
                    }
                    break;
                case "findnode":
                    if (split.length > 1)
                    {
                        List<KadNode> l = myNode.findNode(new BigInteger(split[1]));
                        for (int i = 0; i < l.size(); i++)
                        {
                            System.out.println("******* " + l.get(i));
                        }
                    }
                    else
                    {
                        System.out.println("Inserisci l'ID del nodo");
                    }
                    break;
                case "delete":
                    if (split.length > 1)
                    {
                        try
                        {
                            myNode.delete(new BigInteger(split[1]));
                        }
                        catch (FileNotKnown fnk)
                        {
                            System.out.println("Il file non esiste o non ne sei il proprietario");
                        }
                    }
                    else
                    {
                        System.out.println("Inserisci L'ID del file");
                    }
                    break;
                case "ping":
                    //Funzione di test
                    if (split.length > 1)
                    {
                        try
                        {
                            if (myNode.ping(new KadNode(split[1], Short.parseShort(split[2]), new BigInteger(split[3]))))
                            {
                                System.out.println("VIVO");
                            }
                        }
                        catch (NumberFormatException nfe)
                        {
                            if (myNode.ping(new KadNode(split[1], myNode.UDPPort, new BigInteger(split[2]))))
                            {
                                System.out.println("VIVO");
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Please define an IP address and ID or IP,Port and ID");
                    }
                    break;
                case "findvalue":  //TODO
                    if (split.length > 1)
                    {
                        Object value= myNode.findValue(new BigInteger(split[1]));
                        if(value instanceof List)
                        {
                            for (int i = 0; i < ((List)value).size(); i++)
                            {
                                System.out.println("******* " + ((List)value).get(i));
                            }
                        }
                        else
                        {
                            //TODO
                        }

                    }
                    else
                    {
                        System.out.println("Inserisci l'ID del nodo");
                    }
                    break;

                case "exit":
                    break;
                default:
                    System.out.println("Say what?");
                    break;
            }
        }
    }
}

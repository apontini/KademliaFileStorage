import KPack.Exceptions.FileNotKnown;
import KPack.Files.KadFile;
import KPack.KadNode;
import KPack.Kademlia;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Cerco un ID valido..");
        Kademlia myNode = new Kademlia();
        boolean keep = true;
        String in = null;
        Scanner reader = new Scanner(System.in);

        System.out.println("I file conosciuti nella lista sono: ");
        for(KadFile i : myNode.getFileList())
            System.out.println(i.toString());

        while(keep)
        {
            System.out.print("KadNode@ " + myNode.getNodeID() + "-: ");
            in = reader.nextLine();
            String[] split = in.split(" ");
            switch(split[0])
            {
                case "get":
                    //Cerca di ottenere un file di cui conosce l'esistenza, se è possibile
                    if(split.length > 1)
                    {
                        synchronized(myNode.getFileList())
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
                    synchronized(myNode.getFileList())
                    {
                        for (KadFile i : myNode.getFileList())
                        {
                            if (!(i.isRedundant()))
                            {
                                System.out.println(i.getFileName() + " ID: " + i.getFileID());
                            }
                        }
                    }
                    break;
                case "ip":
                    System.out.println(myNode.getIP().getHostAddress());
                    break;
                case "store":
                    if(split.length > 1)
                    {
                        try
                        {
                            myNode.store(split[1]);
                        }
                        catch(IOException ioe)
                        {
                            ioe. printStackTrace();
                        }
                    }
                    else
                    {
                        System.out.println("Inserisci il percorso del file");
                    }
                    break;
                case "delete":
                    if(split.length > 1)
                    {
                        try
                        {
                            myNode.delete(new BigInteger(split[1]));
                        }
                        catch(FileNotKnown fnk)
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
                    if(split.length > 2)
                    {
                        if(myNode.ping(new KadNode(split[1],myNode.UDPPort,new BigInteger(split[2]))))
                        {
                            System.out.println("VIVO");
                        }
                    }
                    else
                    {
                        System.out.println("Please define an IP address and ID");
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

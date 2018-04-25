
import KPack.Exceptions.AlreadyInstancedException;
import KPack.Exceptions.FileNotKnownException;
import KPack.Files.KadFile;
import KPack.KadNode;
import KPack.Kademlia;
import java.io.File;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        Kademlia myNode = null;
        try
        {
            myNode = new Kademlia();
        }
        catch (AlreadyInstancedException aie)
        {
            System.exit(1);
        }
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
            try
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
                            try
                            {
                                final Kademlia myNodeCopy=myNode;
                                BigInteger id = new BigInteger(split[1]);
                                new Thread(() ->
                                {
                                    KadFile kf=null;
                                    for(KadFile fil:myNodeCopy.getFileList())
                                    {
                                        if(fil.getFileID().equals(id))
                                        {
                                            kf=fil;
                                            break;
                                        }
                                    }
                                    if (kf != null)
                                    {
                                        Object result = myNodeCopy.findValue(id, true);
                                        if (result instanceof byte[])
                                        {
                                            try
                                            {
                                                String path = "." + File.separator + "myDownloadedFile" + File.separator;
                                                File directory = new File(path);
                                                if (!directory.exists())
                                                {
                                                    directory.mkdir();
                                                }
                                                Files.write(new File(path + kf.getFileName()).toPath(), (byte[]) result);
                                            }
                                            catch (IOException ex)
                                            {
                                            }
                                        }
                                    }
                                }).start();
                            }
                            catch (Exception e)
                            {
                                System.out.println("ID non valido");
                            }
                        }
                        else
                        {
                            System.out.println("Please define a file to get");
                        }
                        break;
                    case "ls":
                        //Lista tutti i file di cui conosco l'esistenza
                        for (KadFile i : myNode.getFileList())
                        {
                            System.out.println(i.toString());
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
                            catch (IOException | InvalidParameterException ioe)
                            {
                                System.out.println("File specificato non valido");
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
                            catch (FileNotKnownException fnk)
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
                                if (myNode.ping(new KadNode(split[1], myNode.getPort(), new BigInteger(split[2]))))
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
                    case "findvalue":
                        if (split.length > 1)
                        {
                            Object value = myNode.findValue(new BigInteger(split[1]), false);
                            if (value instanceof List)
                            {
                                for (int i = 0; i < ((List<KadNode>) value).size(); i++)
                                {
                                    System.out.println("******* " + ((List<KadNode>) value).get(i));
                                }
                            }
                            else
                            {
                                System.out.println("Il contenuto del file richiesto è stato trovato");
                            }

                        }
                        else
                        {
                            System.out.println("Inserisci l'ID del nodo");
                        }
                        break;
                    case "printtree":
                        System.out.println(myNode.printTree());
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Say what?");
                        break;
                }
            }

            catch (ArrayIndexOutOfBoundsException aioobe)
            {
                System.out.println("Parametri non validi");
            }
        }
    }
}

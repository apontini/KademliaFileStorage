import KPack.Files.KadFile;
import KPack.KadNode;
import KPack.Kademlia;

import java.math.BigInteger;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Cerco un ID valido..");
        Kademlia myNode = new Kademlia();
        System.out.println(myNode.getNodeID());
        boolean keep = true;
        String in = null;
        Scanner reader = new Scanner(System.in);

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
                        for (KadFile i: myNode.getFileList())
                        {
                            if(i.getFileName().equals(split[1]))
                            {
                                myNode.findValue(i.getFileID());
                                break;
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
                    for (KadFile i: myNode.getFileList())
                    {
                        if(!(i.isRedundant()))
                        {
                            System.out.println(i.getFileName() + " ID: " + i.getFileID());
                        }
                    }
                    break;
                case "ip":
                    System.out.println(myNode.getIP().getHostAddress());
                    break;
                case "ping":
                    if(split.length > 1)
                    {
                        myNode.ping(new KadNode(split[1],myNode.UDPPort, new BigInteger("123")));
                    }
                    else
                    {
                        System.out.println("Please define an IP address");
                    }
                    break;
                default:
                    System.out.println("Say what?");
                    break;
            }
        }
    }
}

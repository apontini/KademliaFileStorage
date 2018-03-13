import KPack.Files.KadFile;
import KPack.Kademlia;

import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("HELLO WORLDDDDD");
        Kademlia myNode = new Kademlia();
        System.out.println(myNode.getNodeID());
        boolean keep = true;
        String in = null;
        Scanner reader = new Scanner(System.in);

        while(keep)
        {
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
                default:
                    System.out.println("Say what?");
                    break;
            }
        }
    }
}

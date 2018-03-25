package KPack.Exceptions;

public class FileNotKnown extends Exception
{
    // Parameterless Constructor
    public FileNotKnown() {}

    // Constructor that accepts a message
    public FileNotKnown(String message)
    {
        super(message);
    }
}
package KPack.Exceptions;

public class FileNotKnownException extends Exception {

    // Parameterless Constructor
    public FileNotKnownException()
    {
    }

    // Constructor that accepts a message
    public FileNotKnownException(String message)
    {
        super(message);
    }
}

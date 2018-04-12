package KPack.Exceptions;

public class AlreadyInstancedException extends Exception {

    // Parameterless Constructor
    public AlreadyInstancedException()
    {
    }

    // Constructor that accepts a message
    public AlreadyInstancedException(String message)
    {
        super(message);
    }
}

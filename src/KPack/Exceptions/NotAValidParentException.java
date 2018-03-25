package KPack.Exceptions;

public class NotAValidParentException extends Exception
{
    public NotAValidParentException() {}

    public NotAValidParentException(String message)
    {
        super(message);
    }
}

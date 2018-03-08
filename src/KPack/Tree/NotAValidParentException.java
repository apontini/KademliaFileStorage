package KPack.Tree;

public class NotAValidParentException extends Exception
{
    public NotAValidParentException() {}

    public NotAValidParentException(String message)
    {
        super(message);
    }
}

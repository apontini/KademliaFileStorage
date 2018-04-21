package KPack;

public class Tupla {
    boolean key;
    byte[] value;

    public Tupla(boolean key, byte[] value)
    {
        this.key = key;
        this.value=value;
    }

    public boolean getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}

package KPack;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class Tupla<K, V> implements Serializable {

    private K key;
    private V value;

    public Tupla(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }

    public void setKey(K key)
    {
        this.key = key;
    }

    public void setValue(V value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof Tupla)
        {
            if (key instanceof BigInteger && ((Tupla) obj).getKey() instanceof BigInteger)
            {
                if (!((BigInteger) key).equals((BigInteger) (((Tupla) obj).getKey())))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
            if (value instanceof Boolean && ((Tupla) obj).getValue() instanceof Boolean)
            {
                if (!((Boolean) value).equals((Boolean) (((Tupla) obj).getValue())))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
            return true;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Tupla<?, ?> other = (Tupla<?, ?>) obj;
        if (!Objects.equals(this.key, other.key))
        {
            return false;
        }
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        if (key instanceof BigInteger)
        {
            hash += ((BigInteger) key).hashCode();
        }
        else
        {
            hash += key.hashCode();
        }
        if (value instanceof Boolean)
        {
            hash += ((Boolean) value).hashCode();
        }
        else
        {
            hash += value.hashCode();
        }
        return hash;
    }
}

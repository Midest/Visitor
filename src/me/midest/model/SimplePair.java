package me.midest.model;

/**
 * Класс, представляющий пару значений.
 * @param <K> тип первого значения
 * @param <V> тип второго значения
 * @version 20150902
 */
public class SimplePair<K, V> {

    private K first;
    private V second;

    /**
     * Конструктор, принимающий оба значения.
     * @param first первое
     * @param second второе
     */
    public SimplePair( K first, V second ){
        set( first, second );
    }

    public K getFirst() {
        return first;
    }

    public void setFirst( K first ) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond( V second ) {
        this.second = second;
    }

    public void set( K first, V second ){
        setFirst( first );
        setSecond( second );
    }

    /**
     * @return пара значений в скобках через запятую с пробелом
     */
    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ")";
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) return true;
        if( obj instanceof SimplePair ) {
            SimplePair that = (SimplePair) obj;
            if( this.first != null ? this.first.equals( that.first ) : that.first != null )
                return false;
            if( this.second != null ? !this.second.equals( that.second ) : that.second != null)
                return false;
            return true;
        }
        return false;
    }
}

package divvyhost.utils;

/**
 * A simple Generic Pair Class
 * @author scopeinfinity
 * @param <T1>
 * @param <T2> 
 */
public class Pair<T1,T2> {
    T1 first;
    T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }
    
}

package lunar.tinkerer.util;

public class Tuple<A, B> {

    private final A first;
    private final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getA() {
        return first;
    }

    public B getB() {
        return second;
    }
}
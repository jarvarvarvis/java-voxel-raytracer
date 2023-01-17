package me.jarvis.util;

public class Function {

    public interface Unary<P1, O> {
        O apply(P1 param1);
    }

    public interface Binary<P1, P2, O> {
        O apply(P1 param1, P2 param2);
    }

    public interface Ternary<P1, P2, P3, O> {
        O apply(P1 param1, P2 param2, P3 param3);
    }
}

package tj.util.predicates;

@FunctionalInterface
public interface IntBiPredicate<T> {

    boolean test(int u, T t);
}

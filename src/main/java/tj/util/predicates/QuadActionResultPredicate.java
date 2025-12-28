package tj.util.predicates;

import net.minecraft.util.EnumActionResult;

@FunctionalInterface
public interface QuadActionResultPredicate<T, U, V, X> {
    EnumActionResult test(T t, U u, V v, X x);
}

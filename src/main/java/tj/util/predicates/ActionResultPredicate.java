package tj.util.predicates;

import net.minecraft.util.EnumActionResult;

@FunctionalInterface
public interface ActionResultPredicate<T> {

    EnumActionResult test(T t);
}

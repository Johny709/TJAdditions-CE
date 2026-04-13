package tj.mixin.gregtech;

import gregtech.api.recipes.Recipe;
import gregtech.api.util.RecipeLRUCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.LinkedList;

@Mixin(value = RecipeLRUCache.class, remap = false)
public abstract class RecipeLRUCacheMixin implements Iterable<Recipe> {

    @Shadow
    @Final
    private LinkedList<Recipe> recipeCaches;

    @Override
    public Iterator<Recipe> iterator() {
        return this.recipeCaches.iterator();
    }
}

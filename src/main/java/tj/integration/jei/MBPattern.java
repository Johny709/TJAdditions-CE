package tj.integration.jei;

import gregtech.api.render.scene.WorldSceneRenderer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MBPattern {
    public final WorldSceneRenderer sceneRenderer;
    public final List<ItemStack> parts;

    public MBPattern(final WorldSceneRenderer sceneRenderer, final List<ItemStack> parts) {
        this.sceneRenderer = sceneRenderer;
        this.parts = parts;
    }
}

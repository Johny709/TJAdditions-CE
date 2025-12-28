package tj.mixin.gregtech;

import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;


@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public interface IMultiblockInfoRecipeWrapperMixin {

    @Accessor("MAX_PARTS")
    int GET_MAX_PARTS();

    @Accessor("PARTS_HEIGHT")
    int GET_PARTS_HEIGHT();

    @Accessor("SLOT_SIZE")
    int GET_SLOT_SIZE();

    @Accessor("SLOTS_PER_ROW")
    int GET_SLOTS_PER_ROW();

    @Accessor("ICON_SIZE")
    int GET_ICON_SIZE();

    @Accessor("RIGHT_PADDING")
    int GET_RIGHT_PADDING();

    @Accessor("currentRendererPage")
    void setCurrentRenderPage(int currentRenderPage);

    @Accessor("currentRendererPage")
    int getCurrentRenderPage();

    @Accessor("buttonPreviousPattern")
    GuiButton getButtonPreviousPattern();

    @Accessor("buttonNextPattern")
    GuiButton getButtonNextPattern();

    @Accessor("buttons")
    Map<GuiButton, Runnable> getButtons();

    @Accessor("recipeLayout")
    RecipeLayout getRecipeLayout();

    @Invoker("shouldDisplayBlock")
    boolean shouldDisplayBlock2(BlockPos pos);
}

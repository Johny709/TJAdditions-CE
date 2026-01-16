package tj.mixin.gregicality;

import gregicadditions.Gregicality;
import gregicadditions.machines.multi.simple.MultiRecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

@Mixin(value = MultiRecipeMapMultiblockController.class, remap = false)
public abstract class MultiRecipeMapMultiblockControllerMixin extends LargeSimpleRecipeMapMultiblockControllerMixin {

    @Shadow
    protected RecipeMap<?>[] recipeMaps;

    public MultiRecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract int getRecipeMapIndex();

    @Shadow
    public abstract RecipeMap<?>[] getRecipeMaps();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.recipe", new TextComponentTranslation("recipemap." + this.recipeMaps[this.getRecipeMapIndex()].getUnlocalizedName() + ".name")));
    }

    @Override
    public String getJEIRecipeUid() {
        return Gregicality.MODID + ":" + this.getRecipeMaps()[this.getRecipeMapIndex()].getUnlocalizedName();
    }
}

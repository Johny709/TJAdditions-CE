package tj.mixin.gregicality;

import gregicadditions.capabilities.impl.RecipeMapSteamMultiblockController;
import gregicadditions.capabilities.impl.SteamMultiblockRecipeLogic;
import gregtech.common.ConfigHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.IFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

@Mixin(value = RecipeMapSteamMultiblockController.class, remap = false)
public abstract class RecipeMapSteamMultiblockControllerMixin extends MultiblockWithDisplayBaseMixin {

    @Shadow
    protected SteamMultiblockRecipeLogic recipeMapWorkable;

    public RecipeMapSteamMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.customLine(text -> {
                    IFluidTank steamFluidTank = this.recipeMapWorkable.getSteamFluidTankCombined();
                    if (steamFluidTank != null && steamFluidTank.getCapacity() > 0) {
                        int steamStored = steamFluidTank.getFluidAmount();
                        text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.steam.steam_stored", steamStored, steamFluidTank.getCapacity()));
                    }
                    if (this.recipeMapWorkable.isHasNotEnoughEnergy()) {
                        text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.steam.low_steam").setStyle(new Style().setColor(TextFormatting.RED)));
                    }
                    if (ConfigHolder.debug_options_for_caching) {
                        text.addTextComponent(new TextComponentString(String.format("Cache size (%s) hit (%s) miss (%s)", this.recipeMapWorkable.previousRecipe.getCachedRecipeCount(), this.recipeMapWorkable.previousRecipe.getCacheHit(), this.recipeMapWorkable.previousRecipe.getCacheMiss()))
                                .setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }
                }).isWorkingLine(this.recipeMapWorkable.isWorkingEnabled(), this.recipeMapWorkable.isActive(), this.recipeMapWorkable.getProgress(), this.recipeMapWorkable.getMaxProgress(), 999)
                .addRecipeOutputLine(this.recipeMapWorkable, 1000);
    }
}

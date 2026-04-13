package tj.mixin.gregicality;

import gregicadditions.capabilities.impl.GARecipeMapMultiblockController;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.mixin.gregtech.RecipeMapMultiblockControllerMixin;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;
import static tj.gui.TJGuiTextures.*;

@Mixin(value = GARecipeMapMultiblockController.class, remap = false)
public abstract class GARecipeMapMultiblockControllerMixin extends RecipeMapMultiblockControllerMixin {

    @Shadow
    @Final
    protected boolean canDistinct;

    @Shadow
    protected boolean isDistinct;

    @Shadow
    public abstract boolean hasMufflerHatch();

    @Shadow
    public abstract boolean isMufflerFaceFree();

    @Shadow
    public abstract byte getProblems();

    @Shadow
    public abstract boolean hasProblems();

    @Unique
    protected Instant placedDown = Instant.now();

    public GARecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void injectWriteToNBT(NBTTagCompound data, CallbackInfoReturnable<NBTTagCompound> cir) {
        data.setLong("placedDownDate", this.placedDown.getEpochSecond());
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void injectReadFromNBT(NBTTagCompound data, CallbackInfo ci) {
        if (data.hasKey("placedDownDate"))
            this.placedDown = Instant.ofEpochSecond(data.getLong("placedDownDate"));
    }

    @Override
    protected void addNewTabs(WidgetTabBuilder tabBuilder) {
        super.addNewTabs(tabBuilder);
        tabBuilder.addTab("tj.multiblock.tab.debug", MetaItems.WRENCH.getStackForm(), debugTab -> {
            debugTab.add(new ToggleButtonWidget(175, 133, 18, 18, RESET_BUTTON, () -> false, b -> this.recipeMapWorkable.previousRecipe.clear())
                    .setTooltipText("tj.multiblock.parallel.recipe.clear"));
            debugTab.add(new ScrollableDisplayWidget(10, -11, 187, 140)
                    .addDisplayWidget(new AdvancedDisplayWidget(0, 0, this::configureDebugDisplayText, 0xFFFFFF)
                            .setMaxWidthLimit(180))
                    .setScrollPanelWidth(3));
        });
    }

    @Override
    protected void addMainDisplayTab(List<Widget> widgetGroup) {
        super.addMainDisplayTab(widgetGroup);
        if (this.canDistinct)
            widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, () -> this.isDistinct, this::setDistinctMode)
                    .setTooltipText("machine.universal.toggle.distinct.mode"));
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed() || !this.canDistinct) return;
        final ITextComponent buttonText = new TextComponentTranslation("gtadditions.multiblock.universal.distinct");
        buttonText.appendText(" ");
        final ITextComponent button = withButton((this.isDistinct ? new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes")
                : new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no")), "distinct");
        withHoverTextTranslate(button, "gtadditions.multiblock.universal.distinct.info");
        buttonText.appendSibling(button);
        builder.addTextComponent(buttonText);
    }

    @Override
    protected void configureMaintenanceDisplayText(GUIDisplayBuilder builder) {
        final Instant now = Instant.now();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMMM d, yyyy hh:mm:ss aa");
        final long timeElapsed = now.getEpochSecond() - this.placedDown.getEpochSecond();
        builder.addTranslationLine("tj.multiblock.date.placed_down", dateFormat.format(Date.from(this.placedDown)))
                .addTranslationLine("tj.multiblock.date.ago", timeElapsed / 3600, (timeElapsed % 3600) / 60, timeElapsed % 60)
                .addEmptyLine()
                .addMufflerDisplayLine(!this.hasMufflerHatch() || this.isMufflerFaceFree(), 999)
                .addMaintenanceDisplayLines(this.getProblems(), this.hasProblems(), 1000);
    }

    @Unique
    protected void configureDebugDisplayText(GUIDisplayBuilder builder) {
        builder.addTranslationLine("tj.multiblock.parallel.debug.cache.capacity", this.recipeMapWorkable.previousRecipe.getCachedRecipeCount())
                .addTranslationLine(text -> text.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.hit.info")))),
                        "tj.multiblock.parallel.debug.cache.hit", this.recipeMapWorkable.previousRecipe.getCacheHit())
                .addTranslationLine(text -> text.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.miss.info")))),
                        "tj.multiblock.parallel.debug.cache.miss", this.recipeMapWorkable.previousRecipe.getCacheMiss())
                .addEmptyLine();
        int i = 1;
        for (Recipe recipe : ((Iterable<Recipe>) this.recipeMapWorkable.previousRecipe)) {
            builder.addTranslationLine("tj.multiblock.recipe_cache.slot", i++)
                    .addTranslationLine("tj.multiblock.recipe_cache.inputs");
            for (CountableIngredient ingredient : recipe.getInputs())
                builder.addIngredient(ingredient);
            for (FluidStack stack : recipe.getFluidInputs())
                builder.addFluidStack(stack);
            if (!recipe.getOutputs().isEmpty() || !recipe.getFluidOutputs().isEmpty())
                builder.addTranslationLine("tj.multiblock.recipe_cache.outputs");
            for (ItemStack stack : recipe.getOutputs())
                builder.addItemStack(stack);
            for (FluidStack stack : recipe.getFluidOutputs())
                builder.addFluidStack(stack);
            if (!recipe.getChancedOutputs().isEmpty())
                builder.addTranslationLine("tj.multiblock.recipe_cache.chanced_outputs");
            for (Recipe.ChanceEntry entry : recipe.getChancedOutputs())
                builder.addItemStack(entry.getItemStack());
        }
    }

    @Unique
    protected void setDistinctMode(boolean distinct) {
        final List<IItemHandlerModifiable> itemInputs = this.getAbilities(MultiblockAbility.IMPORT_ITEMS);
        if (itemInputs != null && !itemInputs.isEmpty()) {
            this.isDistinct = distinct;
            this.markDirty();
        }
    }
}

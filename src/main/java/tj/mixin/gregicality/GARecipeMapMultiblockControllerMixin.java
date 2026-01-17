package tj.mixin.gregicality;

import gregicadditions.capabilities.impl.GARecipeMapMultiblockController;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.gui.TJGuiTextures;
import tj.mixin.gregtech.RecipeMapMultiblockControllerMixin;

import java.util.List;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;

@Mixin(value = GARecipeMapMultiblockController.class, remap = false)
public abstract class GARecipeMapMultiblockControllerMixin extends RecipeMapMultiblockControllerMixin {

    @Shadow
    @Final
    protected boolean canDistinct;

    @Shadow
    protected boolean isDistinct;

    public GARecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void addMainDisplayTab(List<Widget> widgetGroup) {
        super.addMainDisplayTab(widgetGroup);
        if (this.canDistinct)
            widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, () -> this.isDistinct, this::setDistinctMode)
                    .setTooltipText("machine.universal.toggle.distinct.mode"));
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed() || !this.canDistinct) return;
        ITextComponent buttonText = new TextComponentTranslation("gtadditions.multiblock.universal.distinct");
        buttonText.appendText(" ");
        ITextComponent button = withButton((this.isDistinct ? new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes")
                : new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no")), "distinct");
        withHoverTextTranslate(button, "gtadditions.multiblock.universal.distinct.info");
        buttonText.appendSibling(button);
        builder.addTextComponent(buttonText);
    }

    @Unique
    protected void setDistinctMode(boolean distinct) {
        List<IItemHandlerModifiable> itemInputs = this.getAbilities(MultiblockAbility.IMPORT_ITEMS);
        if (itemInputs != null && !itemInputs.isEmpty()) {
            this.isDistinct = distinct;
            this.markDirty();
        }
    }
}

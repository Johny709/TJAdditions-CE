package tj.mixin.gregtech;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;

@Mixin(value = MetaTileEntityLargeBoiler.class, remap = false)
public abstract class MetaTileEntityLargeBoilerMixin extends MultiblockWithDisplayBaseMixin {

    @Shadow
    private int currentTemperature;

    @Shadow
    @Final
    public MetaTileEntityLargeBoiler.BoilerType boilerType;

    @Shadow
    private int lastTickSteamOutput;

    @Shadow
    private int throttlePercentage;

    public MetaTileEntityLargeBoilerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    protected abstract double getThrottleEfficiency();

    @Shadow
    protected abstract double getHeatEfficiencyMultiplier();

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_boiler.temperature", this.currentTemperature, this.boilerType.maxTemperature))
                .addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", this.lastTickSteamOutput, boilerType.baseSteamOutput));

        ITextComponent heatEffText = new TextComponentTranslation("gregtech.multiblock.large_boiler.heat_efficiency", (int) (this.getHeatEfficiencyMultiplier() * 100));
        withHoverTextTranslate(heatEffText, "gregtech.multiblock.large_boiler.heat_efficiency.tooltip");
        builder.addTextComponent(heatEffText);

        ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle", this.throttlePercentage, (int)(this.getThrottleEfficiency() * 100));
        withHoverTextTranslate(throttleText, "gregtech.multiblock.large_boiler.throttle.tooltip");
        builder.addTextComponent(throttleText);

        ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle_modify");
        buttonText.appendText(" ");
        buttonText.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
        buttonText.appendText(" ");
        buttonText.appendSibling(withButton(new TextComponentString("[+]"), "add"));
        builder.addTextComponent(buttonText);
    }

    @Override
    public String getJEIRecipeUid() {
        return GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
    }
}

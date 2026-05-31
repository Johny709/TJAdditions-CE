package tj.mixin.gregicality;

import gregicadditions.machines.multi.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

@Mixin(value = MetaTileEntityCentralMonitor.class, remap = false)
public abstract class MetaTIleEntityCentralMonitorMixin extends MultiblockWithDisplayBaseMixin {

    @Shadow
    public int height;

    @Shadow
    public int width;

    public MetaTIleEntityCentralMonitorMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void configureDisplayText(GUIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        builder.addTranslationLine("gtadditions.multiblock.central_monitor.height", this.height);
        if (!this.isStructureFormed()) {
            final ITextComponent buttonText = new TextComponentTranslation("gtadditions.multiblock.central_monitor.height_modify");
            buttonText.appendText(" ");
            buttonText.appendSibling(AdvancedTextWidget.withButton(new TextComponentString("[-]"), "sub"));
            buttonText.appendText(" ");
            buttonText.appendSibling(AdvancedTextWidget.withButton(new TextComponentString("[+]"), "add"));
            builder.addTextComponent(buttonText);
        } else {
            builder.addTranslationLine("gtadditions.multiblock.central_monitor.width", this.width)
                    .addTranslationLine("metaitem.tool.prospect.low_power");
        }
    }
}

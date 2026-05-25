package tj.items.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.textures.TJTextures;

import java.util.regex.Pattern;

import static tj.gui.TJGuiTextures.MINUS_BUTTON;
import static tj.gui.TJGuiTextures.PLUS_BUTTON;

public class VoidAdvancedEnergyCover extends VoidEnergyCover {

    private VoidMode voidMode = VoidMode.NORMAL;
    private boolean isWorking;
    private long throughput;
    private int ticks = 1;

    public VoidAdvancedEnergyCover(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        super.renderCover(ccRenderState, matrix4, iVertexOperations, cuboid6, blockRenderLayer);
        final int oldBaseColor = ccRenderState.baseColour;
        final int oldAlphaOverride = ccRenderState.alphaOverride;

        ccRenderState.baseColour = 0x27FF00 << 8;
        ccRenderState.alphaOverride = 0xFF;

        TJTextures.INSIDE_OVERLAY_BASE.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);

        ccRenderState.baseColour = oldBaseColor;
        ccRenderState.alphaOverride = oldAlphaOverride;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        if (compound.hasKey("voidMode"))
            this.voidMode = VoidMode.values()[compound.getInteger("voidMode")];
        if (compound.hasKey("ticks"))
            this.ticks = compound.getInteger("ticks");
        if (compound.hasKey("throughput"))
            this.throughput = compound.getLong("throughput");
        if (compound.hasKey("isWorking"))
            this.isWorking = compound.getBoolean("isWorking");
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setItemLabel(this.getPickItem()).setLocale("void_advanced_energy_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(this.ticks), this::setTicks)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ToggleButtonWidget(7, 7, 18, 18, MINUS_BUTTON, () -> false, data -> this.setTicks(String.valueOf((long) this.ticks / 2), "")))
                .widget(new ToggleButtonWidget(151, 7, 18, 18, PLUS_BUTTON, () -> false, data -> this.setTicks(String.valueOf((long) this.ticks * 2), "")))
                .widget(new ToggleButtonWidget(7, 30, 18, 18, MINUS_BUTTON, () -> false, b -> this.setThroughput(String.valueOf((double) this.throughput / 2), "")))
                .widget(new ToggleButtonWidget(151, 30, 18, 18, PLUS_BUTTON, () -> false, b -> this.setThroughput(String.valueOf((double) this.throughput * 2), "")))
                .widget(new NewTextFieldWidget<>(26, 30, 124, 18, true, () -> String.valueOf(this.throughput), this::setThroughput)
                        .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                        .setUpdateOnTyping(true))
                .widget(new CycleButtonWidget(26, 50, 124, 18, VoidMode.class, () -> this.voidMode, this::setVoidMode))
                .widget(new ToggleButtonWidget(151, 106, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isWorking, this::setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        if (this.isWorking && this.coverHolder.getOffsetTimer() % this.ticks == 0) {
            if (this.voidMode == VoidMode.EXACT) {
                if (this.energyContainer.getEnergyStored() > this.throughput)
                    this.energyContainer.removeEnergy(this.energyContainer.getEnergyStored() - this.throughput);
            } else this.energyContainer.removeEnergy(Long.MAX_VALUE);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("voidMode", this.voidMode.ordinal());
        tagCompound.setInteger("ticks", this.ticks);
        tagCompound.setLong("throughput", this.throughput);
        tagCompound.setBoolean("isWorking", this.isWorking);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidMode = VoidMode.values()[tagCompound.getInteger("voidMode")];
        this.ticks = Math.max(1, tagCompound.getInteger("ticks"));
        this.throughput = tagCompound.getLong("throughput");
        this.isWorking = tagCompound.getBoolean("isWorking");
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
        this.markAsDirty();
    }

    public void setTicks(String text, String id) {
        this.ticks = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setThroughput(String text, String id) {
        this.throughput = (long) Math.max(0, Math.min(Long.MAX_VALUE, Double.parseDouble(text)));
        this.markAsDirty();
    }

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}

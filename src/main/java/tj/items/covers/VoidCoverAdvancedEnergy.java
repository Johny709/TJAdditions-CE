package tj.items.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJCycleButtonWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.textures.TJTextures;

import static tj.gui.TJGuiTextures.MINUS_BUTTON;
import static tj.gui.TJGuiTextures.PLUS_BUTTON;

public class VoidCoverAdvancedEnergy extends VoidCoverEnergy {

    private VoidMode voidMode = VoidMode.NORMAL;
    private long throughput;

    public VoidCoverAdvancedEnergy(ICoverable coverHolder, EnumFacing attachedSide) {
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
    public ModularUI createUI(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new ToggleButtonWidget(7, 30, 18, 18, PLUS_BUTTON, () -> false, b -> this.setThroughput(this.throughput * 2)))
                .widget(new ToggleButtonWidget(151, 30, 18, 18, MINUS_BUTTON, () -> false, b -> this.setThroughput(this.throughput / 2D)))
                .widget(new NewTextFieldWidget<>(27, 30, 119, 18, true, () -> String.valueOf(this.throughput), this::setThroughput))
                .widget(new TJCycleButtonWidget(27, 48, 119, 18, VoidMode.class, () -> this.voidMode, this::setVoidMode))
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale("metaitem.void_energy_cover.name"))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        switch (this.voidMode) {
            case NORMAL: this.energyContainer.removeEnergy(Long.MAX_VALUE);
                break;
            case SUPPLY: this.energyContainer.removeEnergy(this.throughput);
                break;
            case EXACT:
                if (this.energyContainer.getEnergyStored() > this.throughput)
                    this.energyContainer.removeEnergy(this.energyContainer.getEnergyStored() - this.throughput);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("voidMode", this.voidMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidMode = VoidMode.values()[tagCompound.getInteger("voidMode")];
    }

    public void setThroughput(double throughput) {
        this.throughput = (long) Math.max(1, Math.min(Long.MAX_VALUE, throughput));
        this.markAsDirty();
    }

    public void setThroughput(String text, String id) {
        this.throughput = (long) Math.max(1, Math.min(Long.MAX_VALUE, Double.parseDouble(text)));
        this.markAsDirty();
    }

    public void setVoidMode(VoidMode voidMode) {
        this.voidMode = voidMode;
        this.markAsDirty();
    }
}

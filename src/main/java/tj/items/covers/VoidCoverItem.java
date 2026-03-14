package tj.items.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.common.covers.filter.SimpleItemFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.textures.TJTextures;

public class VoidCoverItem extends CoverBehavior implements CoverWithUI, ITickable {

    private final SimpleItemFilter itemFilter = new SimpleItemFilter();
    private final IItemHandlerModifiable itemHandler;

    public VoidCoverItem(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.itemHandler = (IItemHandlerModifiable) this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        TJTextures.COVER_CREATIVE_FLUID.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!playerIn.world.isRemote)
            this.openUI((EntityPlayerMP) playerIn);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup();
        this.itemFilter.initUI(widgetGroup::addWidget);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 105)
                .build(this, player);
    }

    @Override
    public void update() {
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            if (this.itemFilter.matchItemStack(this.itemHandler.getStackInSlot(i)) != null)
                this.itemHandler.extractItem(i, Integer.MAX_VALUE, false);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        this.itemFilter.writeToNBT(tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.itemFilter.readFromNBT(tagCompound);
    }
}

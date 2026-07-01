package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.fluids.parts.PartFluidInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.TJ;
import tj.integration.ae2.ISuperFluidInterface;
import tj.integration.ae2.blocks.BlockSuperFluidInterface;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.items.item.TJItems;

import javax.annotation.Nonnull;


public class PartSuperFluidInterface extends PartFluidInterface implements ITileEntityUI, ISuperFluidInterface {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_fluid_interface_has_channel"));

    public PartSuperFluidInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this, 18), "duality");
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
        }
        return true;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return BlockSuperFluidInterface.createFluidInterfaceGUI(holder, player, this);
    }

    @Override
    public void setPriority(String text, String id) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        // No such feature
    }
}

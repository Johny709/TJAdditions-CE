package tj.integration.ae2.part;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPartDisplay;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import tj.TJ;
import tj.integration.ae2.items.ItemWirelessCellTerminal;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;

import javax.annotation.Nonnull;


public class PartCellTerminal extends AbstractPartDisplay implements ITileEntityUI {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(TJ.MODID, "part/me.part.cell_terminal_off");

    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(TJ.MODID, "part/me.part.cell_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public PartCellTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            TileEntityHolder holder = new TileEntityHolder(tileCableBus);
            holder.setFacing(this.getSide().getFacing());
            holder.openUI((EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return ItemWirelessCellTerminal.createWirelessCellTerminal(holder, player, this.getGridNode());
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}

package tj.mixin.ae2;

import appeng.api.parts.IPart;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;

@Mixin(value = TileCableBus.class, remap = false)
public abstract class TileCableBusMixin implements ITileEntityUI {

    @Shadow
    public abstract IPart getPart(EnumFacing side);

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        IPart part = this.getPart(holder.getFacing());
        return part instanceof ITileEntityUI ? ((ITileEntityUI) part).createUI(holder, player) : null;
    }
}

package tj.gui;

import appeng.client.gui.implementations.GuiInterface;
import appeng.container.implementations.ContainerInterface;
import appeng.helpers.IInterfaceHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static int PATTERN_INTERFACE = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        System.out.println(ID);
        final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == PATTERN_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new ContainerInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        System.out.println(ID);
        final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == PATTERN_INTERFACE && tileEntity instanceof IInterfaceHost) {
            return new GuiInterface(player.inventory, (IInterfaceHost) tileEntity);
        } else return null;
    }
}

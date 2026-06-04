package tj.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import tj.gui.container.ContainerPatternInterface;
import tj.mui.TJGuiUtils;

import javax.annotation.Nonnull;

public class PacketToggleButtonPress implements Packet<INetHandler> {

    public int id;
    public int x;
    public int y;
    public int z;
    public int world;
    public boolean value;

    public PacketToggleButtonPress(int id, int x, int y, int z, int world, boolean value) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.value = value;
    }

    @Override
    public void readPacketData(@Nonnull PacketBuffer buf) {
        this.id = buf.readInt();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.world = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void writePacketData(@Nonnull PacketBuffer buf) {
        buf.writeInt(this.id);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.world);
        buf.writeBoolean(this.value);
    }

    @Override
    public void processPacket(@Nonnull INetHandler handler) {
        final World world = DimensionManager.getWorld(this.world);
        final EntityPlayer player = world.getClosestPlayer(this.x, this.y, this.z, 3, false);
        if (player == null) return;
        if (player.openContainer instanceof ContainerPatternInterface) {
            if (TJGuiUtils.isServer()) {
                FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> ((ContainerPatternInterface) player.openContainer).readClientPacket(this));
            } else Minecraft.getMinecraft().addScheduledTask(() -> ((ContainerPatternInterface) player.openContainer).readServerPacket(this));
        }
    }
}

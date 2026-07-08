package tj.network;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.net.NetworkHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import tj.capability.IJEIExtentSync;

public class CPacketSyncParallelLayer implements NetworkHandler.Packet {
    public BlockPos multiBlockControllerPos;
    public int parallelLayer;
    public int dimension;

    public CPacketSyncParallelLayer(BlockPos multiBlockControllerPos, int parallelLayer, int dimension) {
        this.multiBlockControllerPos = multiBlockControllerPos;
        this.parallelLayer = parallelLayer;
        this.dimension = dimension;
    }

    public static void registerPacket(int packetId) {
        NetworkHandler.registerPacket(packetId, CPacketSyncParallelLayer.class, new NetworkHandler.PacketCodec<>(
                (packet, buf) -> {
                    buf.writeVarInt(packet.dimension);
                    buf.writeBlockPos(packet.multiBlockControllerPos);
                    buf.writeVarInt(packet.parallelLayer);
                },
                (buf) -> {
                    int dim = buf.readVarInt();
                    BlockPos pos = buf.readBlockPos();
                    int layer = buf.readVarInt();
                    return new CPacketSyncParallelLayer(pos, layer, dim);
                }
        ));
    }

    public static void registerExecutor() {
        NetworkHandler.registerServerExecutor(CPacketSyncParallelLayer.class, (packet, handler) -> {
            TileEntity te = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(packet.dimension).getTileEntity(packet.multiBlockControllerPos);

            if (!(te instanceof MetaTileEntityHolder)) {
                return;
            }

            MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
            if (mte instanceof IJEIExtentSync) {
                ((IJEIExtentSync) mte).setJEIPreviewLayer(packet.parallelLayer);
            }
        });
    }
}
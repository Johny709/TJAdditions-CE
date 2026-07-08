package tj.network;

public class TJNetworkHandler {

    public static void preInit() {
        CPacketSyncParallelLayer.registerPacket(102);
        CPacketSyncParallelLayer.registerExecutor();
    }
}

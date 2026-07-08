package tj.mixin.gregicality;

import gregicadditions.item.behaviors.FreedomWrenchBehaviour;
import gregicadditions.jei.JEIOptional;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.net.NetworkHandler;
import gregtech.api.render.scene.WorldSceneRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.capability.IJEIExtentSync;
import tj.network.CPacketSyncParallelLayer;


import java.util.List;

@Mixin(value = FreedomWrenchBehaviour.class, remap = false)
public abstract class MixinGAFreedomWrenchBehaviour {

    @Inject(method = "build", at = @At("HEAD"))
    private void tj_syncParallelLayer(MultiblockControllerBase controller, World world, CallbackInfo ci) {
        if (!(controller instanceof IJEIExtentSync)) {
            return;
        }

        WorldSceneRenderer renderer = JEIOptional.getWorldSceneRenderer(controller);
        if (renderer == null){
            return;
        }

        List<BlockPos> renderedBlocks = ObfuscationReflectionHelper.getPrivateValue(WorldSceneRenderer.class, renderer, "renderedBlocks");
        if (renderedBlocks == null) {
            return;
        }

        for (BlockPos blockPos : renderedBlocks) {
            MetaTileEntity metaTE = BlockMachine.getMetaTileEntity(renderer.world, blockPos);
            if (metaTE instanceof MultiblockControllerBase && metaTE.metaTileEntityId.equals(controller.metaTileEntityId)
                    && metaTE instanceof IJEIExtentSync) {

                int parallelLayer = ((IJEIExtentSync) metaTE).getJEIPreviewLayer();
                if (parallelLayer > 1) {
                    NetworkHandler.channel.sendToServer(new CPacketSyncParallelLayer(controller.getPos(),
                            parallelLayer, world.provider.getDimension()).toFMLPacket());
                }
                return;
            }
        }
    }
}
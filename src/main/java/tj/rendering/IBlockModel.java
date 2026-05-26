package tj.rendering;

import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBlockModel {

    @SideOnly(Side.CLIENT)
    StateMapperBase getStateMapper(ResourceLocation resourceLocation);
}

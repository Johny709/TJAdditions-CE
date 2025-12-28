package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class MetaTileEntitySolarBoiler extends MetaTileEntityCoalBoiler {

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public MetaTileEntitySolarBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId, boilerType);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntitySolarBoiler(this.metaTileEntityId, this.boilerType);
    }

    @Override
    protected boolean canBurn() {
        return this.getWorld().isDaytime() && this.canSeeSky() && !this.getWorld().isRaining();
    }

    private boolean canSeeSky() {
        this.pos.setPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ());
        for (int i = this.pos.getY(); i <= this.getWorld().getHeight(); this.pos.setPos(this.pos.getX(), ++i, this.pos.getZ())) {
            if (this.getWorld().getBlockState(this.pos).isFullCube())
                return false;
        }
        return true;
    }

    @Override
    protected void addWidgets(Consumer<Widget> widget) {
        widget.accept(new ImageWidget(120, 15, 40, 40, this.isActive() ? this.boilerType.getSun() : this.boilerType.getMoon()));
    }

    @Override
    public int getProgress() {
        return this.canBurn() ? (int) this.getWorld().getWorldTime() % 24000 : 0;
    }

    @Override
    public int getMaxProgress() {
        return 12540;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SOLAR_BOILER_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive());
    }
}

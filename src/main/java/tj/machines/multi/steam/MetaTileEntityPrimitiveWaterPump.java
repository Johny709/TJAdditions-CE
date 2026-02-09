package tj.machines.multi.steam;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.blocks.AbilityBlocks;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.textures.TJTextures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.material.Materials.Wood;

public class MetaTileEntityPrimitiveWaterPump extends TJMultiblockControllerBase {

    private final PrimitivePumpWorkableHandler workableHandler = new PrimitivePumpWorkableHandler(this);
    private boolean otherMode;

    public MetaTileEntityPrimitiveWaterPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maintenance_problems = 0b111111;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveWaterPump(this.metaTileEntityId);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        this.workableHandler.update();
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return true;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle(this.otherMode ? new String[]{"CCCCC", "~~F~~", "~~F~~"} : new String[]{"CCCC", "~~F~", "~~F~"})
                .aisle(this.otherMode ? new String[]{"CCXCC", "F~~~F", "FFFFF"} : new String[]{"CCXC", "F~~F", "FFFF"})
                .aisle(this.otherMode ? new String[]{"SCCCC", "~~F~~", "~~F~~"} : new String[]{"SCCC", "~~F~", "~~F~"})
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Wood).getDefaultState()))
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.PRIMITIVE_PUMP_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.PRIMITIVE_PUMP;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PUMP_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.otherMode);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.otherMode = buf.readBoolean();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("otherMode", this.otherMode);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.otherMode = data.getBoolean("otherMode");
    }

    private static class PrimitivePumpWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

        private final List<FluidStack> fluidOutputs = new ArrayList<>();
        private int lastAmount;
        private int amount = 100;

        public PrimitivePumpWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected boolean startRecipe() {
            if (this.lastAmount != this.amount) {
                this.lastAmount = amount;
                this.fluidOutputs.clear();
                this.fluidOutputs.add(Water.getFluid(this.amount));
            }
            return true;
        }

        @Override
        protected void progressRecipe(int progress) {
            this.progress++;
        }

        @Override
        protected boolean completeRecipe() {
            this.handler.getExportFluidTank().fill(this.fluidOutputs.get(0), false);
            return true;
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
                return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
            return super.getCapability(capability);
        }
    }
}

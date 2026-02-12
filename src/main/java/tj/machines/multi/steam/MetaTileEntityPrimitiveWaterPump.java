package tj.machines.multi.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.item.components.PumpCasing;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.blocks.AbilityBlocks;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.material.Materials.Wood;

public class MetaTileEntityPrimitiveWaterPump extends TJMultiblockControllerBase {

    private final PrimitivePumpWorkableHandler workableHandler = new PrimitivePumpWorkableHandler(this);
    private boolean otherMode;

    public MetaTileEntityPrimitiveWaterPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, false);
        this.maintenance_problems = 0b111111;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveWaterPump(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.primitive_water_pump.description", 500, 1280));
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
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle(!this.otherMode ? new String[]{"CCCC", "~~F~", "~~F~"} : new String[]{"CCCCC", "~~F~~", "~~F~~"})
                .aisle(!this.otherMode ? new String[]{"CPXC", "F~~F", "FFFF"} : new String[]{"CPXPC", "F~~~F", "FFFFF"})
                .aisle(!this.otherMode ? new String[]{"SCCC", "~~F~", "~~F~"} : new String[]{"SCCCC", "~~F~~", "~~F~~"})
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS)))
                .where('P', statePredicate(this.getCasingState()).or(pumpPredicate()))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Wood).getDefaultState()))
                .where('~', tile -> true)
                .build();
    }

    public static Predicate<BlockWorldState> pumpPredicate() {
        return blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof PumpCasing))
                return false;
            List<PumpCasing.CasingType> casingType = blockWorldState.getMatchContext().getOrCreate("Pumps", ArrayList::new);
            PumpCasing.CasingType currentCasingType = ((PumpCasing) state.getBlock()).getState(state);
            casingType.add(currentCasingType);
            return casingType.get(0).getTier() == currentCasingType.getTier();
        };
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.PRIMITIVE_PUMP_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.PRIMITIVE_PUMP;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<PumpCasing.CasingType> casingTypes = context.getOrDefault("Pumps", new ArrayList<>());
        this.workableHandler.initialize2(500 + casingTypes.stream()
                .mapToLong(pump -> 1280L << (pump.getTier() - 1) * 2)
                .sum());
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getEntityWorld().isRemote) {
            this.otherMode = !this.otherMode;
            this.writeCustomData(1, buffer -> buffer.writeBoolean(this.otherMode));
            this.invalidateStructure();
            this.structurePattern = this.createStructurePattern();
            this.markDirty();
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PUMP_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.otherMode = buf.readBoolean();
            this.structurePattern = this.createStructurePattern();
        }
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
        this.structurePattern = this.createStructurePattern();
    }

    private static class PrimitivePumpWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

        private final List<FluidStack> fluidOutputs = new ArrayList<>();
        private long lastAmount;

        public PrimitivePumpWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
            this.maxProgress = 20;
        }

        public void initialize2(long amount) {
            if (this.lastAmount != amount) {
                this.fluidOutputs.clear();
                long amountLeft = this.lastAmount = amount;
                while (amountLeft > 0) {
                    FluidStack water = Water.getFluid((int) Math.min(amountLeft, Integer.MAX_VALUE));
                    this.fluidOutputs.add(water);
                    amountLeft -= water.amount;
                }
            }
        }

        @Override
        protected boolean startRecipe() {
            return true;
        }

        @Override
        protected void progressRecipe(int progress) {
            this.progress++;
        }

        @Override
        protected boolean completeRecipe() {
            for (FluidStack stack : this.fluidOutputs)
                this.handler.getExportFluidTank().fill(stack, true);
            return true;
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
                return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
            return super.getCapability(capability);
        }

        @Override
        public List<FluidStack> getFluidOutputs() {
            return this.fluidOutputs;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = super.serializeNBT();
            NBTTagList fluidOutputsList = new NBTTagList();
            for (FluidStack stack : this.fluidOutputs)
                fluidOutputsList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            compound.setTag("fluidOutputs", fluidOutputsList);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            NBTTagList fluidOutputsList = compound.getTagList("fluidOutputs", 10);
            for (int i = 0; i < fluidOutputsList.tagCount(); i++) {
                this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
            }
        }
    }
}

package tj.machines.multi.electric;

import gregicadditions.GAValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.TJRecipeMapMultiblockController;
import tj.capability.OverclockManager;
import tj.capability.impl.handler.IDistillationHandler;
import tj.capability.impl.workable.MegaRecipeLogic;
import tj.util.TJItemUtils;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.machines.multi.electric.MetaTileEntityTJDistillationTower.outputHatchPredicate;
import static tj.util.TJFluidUtils.VOID_TANK;

public class MetaTileEntityTJMegaDistillationTower extends TJRecipeMapMultiblockController implements IDistillationHandler {

    private final List<BlockPos> outputHatchPos = new ArrayList<>();

    public MetaTileEntityTJMegaDistillationTower(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.DISTILLATION_RECIPES, true, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMegaDistillationTower(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
        tooltip.add(I18n.format("tj.multiblock.mega"));
        tooltip.add(I18n.format("tj.multiblock.distillation_tower.layers", 3, 14));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        overclockManager.setDuration(overclockManager.getDuration() * 2);
        overclockManager.setParallel((int) Math.min(Integer.MAX_VALUE, 1L << overclockManager.getParallel() * 2));
    }

    @Override
    protected MegaDistillationRecipeLogic createRecipeLogic() {
        return new MegaDistillationRecipeLogic(this);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("~ZZZ~", "ZZZZZ", "ZZZZZ", "ZZZZZ", "~ZZZ~")
                .aisle("~XXX~", "XCPCX", "XPFPX", "XCPCX", "~XXX~").setRepeatable(1, 12)
                .aisle("~ZSZ~", "ZZZZZ", "ZZZZZ", "ZZZZZ", "~ZZZ~")
                .where('S', this.selfPredicate())
                .where('F', frameworkPredicate())
                .where('C', statePredicate(MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.NICHROME)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('Z', statePredicate(this.getCasingState()).or(abilityPartPredicate(MetaTileEntityTJDistillationTower.ALLOWED_ABILITIES)))
                .where('X', this.countMatch("outputHatchCount", tilePredicate(outputHatchPredicate())).or(statePredicate(this.getCasingState())).or(abilityPartPredicate(GregicAdditionsCapabilities.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY)))
                .where('~', tile -> true)
                .validateLayer(1, context -> context.getOrDefault("outputHatchCount", 0) == 1)
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        BlockPos offsetPos = this.getPos().offset(this.getFrontFacing().getOpposite(), 2);
        this.outputHatchPos.addAll(context.getOrDefault("outputHatches", new HashSet<>()));
        this.outputHatchPos.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() - offsetPos.getX()) + Math.abs(pos.getY() - offsetPos.getY()) + Math.abs(pos.getZ() - offsetPos.getZ())));
        int tier = context.getOrDefault("frameworkTier", 0);
        if (tier < GAValues.MAX) {
            this.maxVoltage = 8L << tier * 2;
            this.tier = tier;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.outputHatchPos.clear();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public int getDurationMultiplier() {
        return 200;
    }

    @Override
    public int getParallel() {
        return Math.max(0, this.getTier() - GTValues.LuV);
    }

    @Override
    public boolean renderTJLogoOverlay() {
        return true;
    }

    @Override
    public IMultipleTankHandler getOutputHatchAt(int index) {
        if (index >= this.outputHatchPos.size())
            return null;
        TileEntity tileEntity = this.getWorld().getTileEntity(this.outputHatchPos.get(index));
        if (!(tileEntity instanceof MetaTileEntityHolder))
            return null;
        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        return metaTileEntity != null ? metaTileEntity.getExportFluids() : null;
    }

    public static class MegaDistillationRecipeLogic extends MegaRecipeLogic<IDistillationHandler> {

        private final Int2LongMap fluidOutputAmount = new Int2LongOpenHashMap();
        private final List<FluidStack> fluidOutputsOrdered = new ArrayList<>();

        public MegaDistillationRecipeLogic(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        @Override
        protected void addFluidOutputs(int parallels, Recipe recipe) {
            for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
                FluidStack stack = recipe.getFluidOutputs().get(i);
                long amount = (long) stack.amount * parallels;
                this.fluidOutputAmount.put(i, amount);
                this.fluidOutputsOrdered.add(stack);
                for (; amount > 0; amount -= Integer.MAX_VALUE) {
                    stack = stack.copy();
                    stack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
                    this.fluidOutputs.add(stack);
                }
            }
        }

        @Override
        protected boolean completeRecipe() {
            for (int i = this.itemOutputIndex; i < this.itemOutputs.size(); i++) {
                final ItemStack stack = this.itemOutputs.get(i);
                if (this.voidingItems || TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                    TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                    this.itemOutputIndex++;
                } else return false;
            }
            for (int i = this.fluidOutputIndex; i < this.fluidOutputsOrdered.size(); i++) {
                final long amount = this.fluidOutputAmount.get(i);
                final FluidStack stack = this.fluidOutputsOrdered.get(i);
                IMultipleTankHandler fluidTank = this.handler.getOutputHatchAt(i);
                if (fluidTank == null)
                    fluidTank = VOID_TANK;
                if (this.voidingFluids || TJFluidUtils.fillIntoTanksLong(fluidTank, stack, amount, false) == amount) {
                    TJFluidUtils.fillIntoTanksLong(fluidTank, stack, amount, true);
                    this.fluidOutputIndex++;
                } else return false;
            }
            this.recipeRecheck = true;
            this.itemOutputIndex = 0;
            this.fluidOutputIndex = 0;
            this.itemInputs.clear();
            this.itemOutputs.clear();
            this.fluidInputs.clear();
            this.fluidOutputs.clear();
            this.fluidOutputsOrdered.clear();
            return true;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            final NBTTagCompound compound = super.serializeNBT();
            final NBTTagList fluidOutputOrderedList = new NBTTagList(), fluidOutputIndexList = new NBTTagList();
            for (FluidStack stack : this.fluidOutputsOrdered)
                fluidOutputOrderedList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            for (Int2LongMap.Entry entry : this.fluidOutputAmount.int2LongEntrySet()) {
                final NBTTagCompound subCompound = new NBTTagCompound();
                subCompound.setInteger("i", entry.getIntKey());
                subCompound.setLong("amount", entry.getIntKey());
                fluidOutputIndexList.appendTag(subCompound);
            }
            compound.setTag("fluidOutputsOrdered", fluidOutputOrderedList);
            compound.setTag("fluidOutputsDT", fluidOutputIndexList);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            final NBTTagList fluidOutputOrderedList = compound.getTagList("fluidOutputsOrdered", 10), fluidOutputIndexList = compound.getTagList("fluidOutputsDT", 10);
            for (int i = 0; i < fluidOutputOrderedList.tagCount(); i++)
                this.fluidOutputsOrdered.add(FluidStack.loadFluidStackFromNBT(fluidOutputOrderedList.getCompoundTagAt(i)));
            for (int i = 0; i < fluidOutputIndexList.tagCount(); i++) {
                final NBTTagCompound subCompound = fluidOutputIndexList.getCompoundTagAt(i);
                this.fluidOutputAmount.put(subCompound.getInteger("i"), subCompound.getLong("amount"));
            }
        }
    }
}

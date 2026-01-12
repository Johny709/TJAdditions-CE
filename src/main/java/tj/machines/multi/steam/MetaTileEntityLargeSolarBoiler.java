package tj.machines.multi.steam;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import tj.blocks.AbilityBlocks;
import tj.blocks.TJMetaBlocks;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IHeatInfo;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.TJCapabilities;
import tj.gui.TJGuiTextures;
import tj.multiblockpart.TJMultiblockAbility;
import tj.util.Color;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.IMPORT_FLUIDS;
import static gregtech.api.unification.material.Materials.*;

public class MetaTileEntityLargeSolarBoiler extends TJMultiblockDisplayBase implements IWorkable, IHeatInfo, IProgressBar {

    private static final FluidStack WATER = Water.getFluid(1);
    private static final FluidStack DISTILLED_WATER = DistilledWater.getFluid(1);
    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_FLUIDS, GregicAdditionsCapabilities.MAINTENANCE_HATCH, GregicAdditionsCapabilities.MUFFLER_HATCH};
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final Set<BlockPos> solarCollectorPos = new HashSet<>();
    private final Set<BlockPos> activeStates = new HashSet<>();
    private final boolean mega;
    private IMultipleTankHandler waterTank;
    private IMultipleTankHandler steamTank;
    private BlockPos offSetPos;
    private boolean isActive;
    private boolean hadWater;
    private int steamProduction;
    private int waterConsumption;
    private int calcification;
    private int temp;

    public MetaTileEntityLargeSolarBoiler(ResourceLocation metaTileEntityId, boolean mega) {
        super(metaTileEntityId);
        this.mega = mega;
        if (this.mega)
            this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeSolarBoiler(this.metaTileEntityId, this.mega);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.multiblock.large_solar_boiler.description"));
        if (!this.mega) return;
        tooltip.add(net.minecraft.client.resources.I18n.format("tj.multiblock.mega_boiler.parallel.description", this.getMaxParallel()).replace("§r", "§7"));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tj.multiblock.mega_boiler.warning"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        boolean hasInputFluid = abilities.containsKey(IMPORT_FLUIDS);
        boolean hasSteamOutput = abilities.containsKey(TJMultiblockAbility.STEAM_OUTPUT);
        boolean hasOutputFluid = abilities.containsKey(MultiblockAbility.EXPORT_FLUIDS);
        int mufflerCount = abilities.getOrDefault(MUFFLER_HATCH, Collections.emptyList()).size();

        return mufflerCount == 1 && hasInputFluid && (hasOutputFluid || hasSteamOutput) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) == 0) return;
        if (this.getOffsetTimer() % 20 == 0) {
            if (this.isWorkingEnabled && this.canBurn() && this.areSolarCollectorsValid()) {
                if (!this.isActive)
                    this.setActive(true);
                this.temp = MathHelper.clamp(this.temp + 20, 0, 12000);
            } else {
                if (this.isActive)
                    this.setActive(false);
                this.temp = MathHelper.clamp(this.temp - 10, 0, 12000);
            }
        }
        if (!this.canGenerateSteam() || this.getOffsetTimer() < 20 || this.calcification > 239999) {
            this.hadWater = false;
            this.steamProduction = 0;
            this.waterConsumption = 0;
            return;
        }
        int waterToConsume = Math.round((900 * this.getMaxParallel() * this.getTempPercent()) / 160);
        FluidStack waterStack = this.waterTank.drain(waterToConsume, false);
        boolean hasEnoughWater = waterStack != null && (waterStack.isFluidEqual(WATER) || waterStack.isFluidEqual(DISTILLED_WATER)) && waterStack.amount == waterToConsume || waterToConsume == 0;
        if (hasEnoughWater && this.hadWater) {
            this.getWorld().setBlockToAir(this.getPos());
            this.getWorld().createExplosion(null,
                    this.getPos().getX() + 0.5, this.getPos().getY() + 0.5, this.getPos().getZ() + 0.5,
                    2.0f, true);
        } else if (hasEnoughWater) {
            this.steamProduction = this.steamTank.fill(Steam.getFluid(waterToConsume * 160), true);
            this.waterConsumption = this.waterTank.drain(waterToConsume, true).amount;
            if (!waterStack.isFluidEqual(DISTILLED_WATER))
                this.calcification = MathHelper.clamp(this.calcification + this.waterConsumption, 0, 240000);
        } else this.hadWater = true;
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        FluidStack water = Water.getFluid(this.waterConsumption), distilledWater = DistilledWater.getFluid(this.waterConsumption);
        if (this.hasEnoughWater(water, this.waterConsumption)) {
        } else if (this.hasEnoughWater(distilledWater, this.waterConsumption)) {
            water = distilledWater;
        }
        builder.temperatureLine(this.heat(), this.maxHeat())
                .fluidInputLine(this.waterTank, water)
                .customLine(text -> {
                    text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", this.steamProduction, 900));

                    ITextComponent heatEffText = new TextComponentTranslation("gregtech.multiblock.large_boiler.heat_efficiency",100);
                    withHoverTextTranslate(heatEffText, "gregtech.multiblock.large_boiler.heat_efficiency.tooltip");
                    text.addTextComponent(heatEffText);
                    if (this.calcification > 0)
                        text.addTextComponent(new TextComponentString(I18n.translateToLocalFormatted("tj.multiblock.large_solar_boiler.calcification", (this.calcification == 240000 ? Color.RED : Color.DARK_AQUA) + TJValues.thousandTwoPlaceFormat.format(this.getCalcificationPercent() * 100))));
                    if (!this.canBurn())
                        text.addTextComponent(new TextComponentTranslation("tj.multiblock.large_solar_boiler.obstructed").setStyle(new Style().setColor(TextFormatting.RED)));
                    if (!this.areSolarCollectorsValid())
                        text.addTextComponent(new TextComponentTranslation("tj.multiblock.large_solar_boiler.invalid").setStyle(new Style().setColor(TextFormatting.RED)));
                }).isWorkingLine(this.isWorkingEnabled(), this.isActive(), this.getProgress(), this.getMaxProgress());
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start();
        if (!this.mega) {
            factoryPattern.aisle("XXX", "CCC", "CCC", "sss");
            factoryPattern.aisle("XXX", "CPC", "CPC", "sss");
            factoryPattern.aisle("XXX", "CSC", "CCC", "sss");
        } else {
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "sssssssssssssss");
            factoryPattern.aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCSCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "sssssssssssssss");
        }
        return factoryPattern
                .where('S', this.selfPredicate())
                .where('X', this.fireboxStatePredicate(GTUtility.getAllPropertyValues(MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX), BlockFireboxCasing.ACTIVE))
                        .or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS, TJMultiblockAbility.STEAM_OUTPUT)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('s', solarCollectorPredicate())
                .build();
    }

    public Predicate<BlockWorldState> fireboxStatePredicate(IBlockState... allowedStates) {
        return (blockWorldState) -> {
            IBlockState state = blockWorldState.getBlockState();
            if (ArrayUtils.contains(allowedStates, state)) {
                if (blockWorldState.getWorld() != null)
                    this.activeStates.add(blockWorldState.getPos());
                return true;
            }
            return false;
        };
    }

    public static Predicate<BlockWorldState> solarCollectorPredicate() {
        return (blockWorldState) -> {
            if (blockWorldState.getBlockState() != TJMetaBlocks.ABILITY_BLOCKS.getState(AbilityBlocks.AbilityType.SOLAR_COLLECTOR))
                return false;
            if (blockWorldState.getWorld() != null) {
                Set<BlockPos> posList = blockWorldState.getMatchContext().getOrCreate("solarCollectors", HashSet::new);
                posList.add(blockWorldState.getPos());
            }
            return true;
        };
    }

    private void replaceFireboxAsActive(boolean isActive) {
        this.activeStates.forEach(pos -> {
            IBlockState state = this.getWorld().getBlockState(pos);
            if (state.getBlock() instanceof BlockFireboxCasing) {
                state = state.withProperty(BlockFireboxCasing.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        });
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IFluidTank> fluidTanks = new ArrayList<>();
        fluidTanks.addAll(this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        fluidTanks.addAll(this.getAbilities(TJMultiblockAbility.STEAM_OUTPUT));

        this.waterTank = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.steamTank = new FluidTankList(true, fluidTanks);
        this.offSetPos = this.getPos().offset(this.getFrontFacing().getOpposite(), this.mega ? 7 : 1);
        this.solarCollectorPos.addAll(context.getOrDefault("solarCollectors", new HashSet<>()));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.waterTank = new FluidTankList(true);
        this.steamTank = new FluidTankList(true);
        this.solarCollectorPos.clear();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceFireboxAsActive(false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.isActive());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability == MultiblockAbility.EXPORT_FLUIDS || ability == TJMultiblockAbility.STEAM_OUTPUT)
                return Textures.SOLID_STEEL_CASING;
        }
        return sourcePart == null ? Textures.SOLID_STEEL_CASING : this.isActive ? Textures.STEEL_FIREBOX_ACTIVE : Textures.STEEL_FIREBOX;
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability == MultiblockAbility.EXPORT_FLUIDS || ability == TJMultiblockAbility.STEAM_OUTPUT)
                return 0;
        }
        return sourcePart == null ? 0 : this.isActive ? 15 : 0;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            this.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        if (!this.getWorld().isRemote) {
            this.replaceFireboxAsActive(active);
            this.writeCustomData(1, buf -> buf.writeBoolean(active));
            this.markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("temp", this.temp);
        data.setInteger("calcification", this.calcification);
        data.setBoolean("hadWater", this.hadWater);
        data.setBoolean("isActive", this.isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.temp = data.getInteger("temp");
        this.calcification = data.getInteger("calcification");
        this.hadWater = data.getBoolean("hadWater");
        this.isActive = data.getBoolean("isActive");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == TJCapabilities.CAPABILITY_HEAT)
            return TJCapabilities.CAPABILITY_HEAT.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::heat).setMaxProgress(this::maxHeat)
                .setBarTexture(TJGuiTextures.BAR_RED)
                .setLocale("tj.multiblock.bars.heat"));
    }

    public boolean hasEnoughWater(FluidStack fluid, int amount) {
        FluidStack fluidStack = this.waterTank.drain(fluid, false);
        return fluidStack != null && fluidStack.amount == amount || amount == 0;
    }

    private boolean canBurn() {
        return this.getWorld().isDaytime() && this.canSeeSky() && !this.getWorld().isRaining();
    }

    private boolean canSeeSky() {
        int start = this.mega ? -7 : -1;
        int end = this.mega ? 8 : 2;
        int startY = this.offSetPos.getY() + (this.mega ? 18 : 3);
        for (int x = start; x < end; x++) {
            for (int y = startY; y <= this.getWorld().getHeight(); y++) {
                for (int z = start; z < end; z++) {
                    this.pos.setPos(this.offSetPos.getX() + x, y, this.offSetPos.getZ() + z);
                    if (this.getWorld().getBlockState(this.pos).isFullCube())
                        return false;
                }
            }
        }
        return true;
    }

    private boolean areSolarCollectorsValid() {
        int startY = this.offSetPos.getY() + (this.mega ? 17 : 2);
        for (BlockPos pos : this.solarCollectorPos)
            if (pos.getY() != startY)
                return false;
        return true;
    }

    public float getCalcificationPercent() {
        return this.calcification / (240000 * 1.00F);
    }

    public float getTempPercent() {
        return this.temp / (12000 * 1.00F);
    }

    public int getMaxParallel() {
        return this.mega ? 512 : 1;
    }

    @Override
    public int getProgress() {
        return this.canBurn() && this.areSolarCollectorsValid() ? (int) this.getWorld().getWorldTime() % 24000 : 0;
    }

    @Override
    public int getMaxProgress() {
        return 12540;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    public boolean canGenerateSteam() {
        return this.temp >= 2400;
    }

    @Override
    public long heat() {
        return (long) this.temp / 24;
    }

    @Override
    public long maxHeat() {
        return 500;
    }
}

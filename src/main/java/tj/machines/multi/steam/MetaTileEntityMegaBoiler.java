package tj.machines.multi.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.multiblock.BlockWorldState;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.lang3.ArrayUtils;
import tj.capability.impl.handler.IBoilerHandler;
import tj.capability.impl.workable.MegaBoilerRecipeLogic;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.multiblockpart.TJMultiblockAbility;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.impl.*;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.textures.TJTextures;
import tj.util.Color;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.unification.material.Materials.Water;

public class MetaTileEntityMegaBoiler extends TJMultiblockControllerBase implements IProgressBar, IBoilerHandler {

    private static final MultiblockAbility<?>[] OUTPUT_ABILITIES = {MultiblockAbility.EXPORT_FLUIDS, TJMultiblockAbility.STEAM_OUTPUT};
    private final MegaBoilerRecipeLogic boilerRecipeLogic = new MegaBoilerRecipeLogic(this);
    private final Set<BlockPos> activeStates = new HashSet<>();
    private final int parallel;
    private final MetaTileEntityLargeBoiler.BoilerType boilerType;

    public MetaTileEntityMegaBoiler(ResourceLocation metaTileEntityId, MetaTileEntityLargeBoiler.BoilerType boilerType, int parallel) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.parallel = parallel;
        this.boilerRecipeLogic.setActive(this::replaceFireboxAsActive);
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMegaBoiler(this.metaTileEntityId, this.boilerType, this.parallel);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format(this.boilerType == MetaTileEntityLargeBoiler.BoilerType.BRONZE ? "tj.multiblock.mega_bronze_boiler.description"
                : this.boilerType == MetaTileEntityLargeBoiler.BoilerType.STEEL ? "tj.multiblock.mega_steel_boiler.description"
                : this.boilerType == MetaTileEntityLargeBoiler.BoilerType.TITANIUM ? "tj.multiblock.mega_titanium_boiler.description"
                : "tj.multiblock.mega_tungstensteel_boiler.description"));
        tooltip.add(I18n.format("tj.multiblock.mega_boiler.parallel.description", this.getParallel()));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW, 20, "tj.multiblock.mega_boiler.warning"));
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.universal.tooltip.1", RecipeMaps.DIESEL_GENERATOR_FUELS.getLocalizedName() + ", " + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getLocalizedName()));
            tip.add(I18n.format("tj.multiblock.universal.tooltip.2", this.parallel));
        });
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
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IFluidTank> fluidTanks = new ArrayList<>();
        fluidTanks.addAll(this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        fluidTanks.addAll(this.getAbilities(TJMultiblockAbility.STEAM_OUTPUT));

        this.exportFluidTank = new FluidTankList(true, fluidTanks);
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        int amount = (int) this.boilerRecipeLogic.getConsumption();
        FluidStack water = Water.getFluid(amount);
        builder.temperatureLine(this.boilerRecipeLogic.heat(), this.boilerType.maxTemperature)
                .fluidInputLine(this.importFluidTank, water)
                .customLine(text -> {
                    text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", this.boilerRecipeLogic.getProduction(), this.boilerType.baseSteamOutput));

                    ITextComponent heatEffText = new TextComponentTranslation("gregtech.multiblock.large_boiler.heat_efficiency", (int) (this.getHeatEfficiencyMultiplier() * 100));
                    withHoverTextTranslate(heatEffText, "gregtech.multiblock.large_boiler.heat_efficiency.tooltip");
                    text.addTextComponent(heatEffText);

                    ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle", this.boilerRecipeLogic.getThrottlePercentage(), (int) (this.boilerRecipeLogic.getThrottleEfficiency() * 100));
                    withHoverTextTranslate(throttleText, "gregtech.multiblock.large_boiler.throttle.tooltip");
                    text.addTextComponent(throttleText);

                    ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle_modify");
                    buttonText.appendText(" ");
                    buttonText.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
                    buttonText.appendText(" ");
                    buttonText.appendSibling(withButton(new TextComponentString("[+]"), "add"));
                    text.addTextComponent(buttonText);
                }).isWorkingLine(this.boilerRecipeLogic.isWorkingEnabled(), this.boilerRecipeLogic.isActive(), this.boilerRecipeLogic.getProgress(), this.boilerRecipeLogic.getMaxProgress())
                .addRecipeInputLine(this.boilerRecipeLogic)
                .addRecipeOutputLine(this.boilerRecipeLogic);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int modifier = componentData.equals("add") ? 1 : -1;
        int result = (clickData.isShiftClick ? 1 : 5) * modifier;
        this.boilerRecipeLogic.setThrottlePercentage(MathHelper.clamp(this.boilerRecipeLogic.getThrottlePercentage() + result, 20, 100));
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (this.getOffsetTimer() > 40 && ((this.getProblems() >> 5) & 1) != 0)
            this.boilerRecipeLogic.update();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return this.boilerType == null ? null : FactoryBlockPattern.start()
                .aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CPPPPPPPPPPPPPC", "CCCCCCCCCCCCCCC")
                .aisle("XXXXXXXXXXXXXXX", "CCCCCCCCCCCCCCC", "CCCCCCCSCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC", "CCCCCCCCCCCCCCC")
                .setAmountAtLeast('L', 200)
                .setAmountAtLeast('l', 200)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.boilerType.casingState))
                .where('l', statePredicate(GTUtility.getAllPropertyValues(this.boilerType.fireboxState, BlockFireboxCasing.ACTIVE)))
                .where('P', statePredicate(this.boilerType.pipeState))
                .where('X', state -> this.fireboxStatePredicate(GTUtility.getAllPropertyValues(this.boilerType.fireboxState, BlockFireboxCasing.ACTIVE))
                        .or(abilityPartPredicate(IMPORT_FLUIDS, IMPORT_ITEMS, MAINTENANCE_HATCH, EXPORT_ITEMS, MUFFLER_HATCH)).test(state))
                .where('C', statePredicate(this.boilerType.casingState).or(abilityPartPredicate(OUTPUT_ABILITIES)))
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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.boilerRecipeLogic.isActive(), this.boilerRecipeLogic.hasProblem(), this.boilerRecipeLogic.isWorkingEnabled());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability == MultiblockAbility.EXPORT_FLUIDS || ability == TJMultiblockAbility.STEAM_OUTPUT)
                return this.boilerType.solidCasingRenderer;
        }
        return sourcePart == null ? this.boilerType.solidCasingRenderer : this.boilerRecipeLogic.isActive() ? this.boilerType.firefoxActiveRenderer : this.boilerType.fireboxIdleRenderer;
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability == MultiblockAbility.EXPORT_FLUIDS || ability == TJMultiblockAbility.STEAM_OUTPUT)
                return 0;
        }
        return sourcePart == null ? 0 : this.boilerRecipeLogic.isActive() ? 15 : 0;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if(!itemStack.isEmpty() && itemStack.hasCapability(GregtechCapabilities.CAPABILITY_MALLET, null)) {
            ISoftHammerItem softHammerItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_MALLET, null);

            if (this.getWorld().isRemote) {
                return true;
            }
            if (!softHammerItem.damageItem(DamageValues.DAMAGE_FOR_SOFT_HAMMER, false)) {
                return false;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.writeActiveBlockPacket(buf, this.boilerRecipeLogic.isActive());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.readActiveBlockPacket(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 128) {
            this.readActiveBlockPacket(buf);
        }
    }

    private void writeActiveBlockPacket(PacketBuffer buffer, boolean isActive) {
        buffer.writeBoolean(isActive);
        buffer.writeInt(this.activeStates.size());
        for (BlockPos pos : this.activeStates) {
            buffer.writeBlockPos(pos);
        }
    }

    private void readActiveBlockPacket(PacketBuffer buffer) {
        boolean isActive = buffer.readBoolean();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            IBlockState state = this.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockFireboxCasing) {
                state = state.withProperty(BlockFireboxCasing.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    private void replaceFireboxAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceFireboxAsActive(false);
            this.markDirty();
        }
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this.boilerRecipeLogic::heat).setMaxProgress(this.boilerRecipeLogic::maxHeat)
                .setBarTexture(TJGuiTextures.BAR_RED)
                .setLocale("tj.multiblock.bars.heat"));
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public boolean isActive() {
        return this.boilerRecipeLogic.isActive();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.boilerRecipeLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.boilerRecipeLogic.isWorkingEnabled();
    }

    @Override
    public String getRecipeUid() {
        return GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();
    }

    @Override
    public double getHeatEfficiencyMultiplier() {
        double temperature = this.boilerRecipeLogic.heat() / (this.boilerType.maxTemperature * 1.0);
        return 1.0 + Math.round(this.boilerType.temperatureEffBuff * temperature) / 100.0;
    }

    @Override
    public double getFuelConsumptionMultiplier() {
        return this.boilerType.fuelConsumptionMultiplier;
    }

    @Override
    public int getBaseSteamOutput() {
        return this.boilerType.baseSteamOutput;
    }

    @Override
    public int getMaxTemperature() {
        return this.boilerType.maxTemperature;
    }
}

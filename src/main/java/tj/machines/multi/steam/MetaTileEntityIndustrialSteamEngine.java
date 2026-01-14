package tj.machines.multi.steam;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidFuelInfo;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.render.Textures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.TJValues;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GAMultiblockCasing2;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IGeneratorInfo;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.TJCapabilities;
import tj.capability.impl.AbstractWorkableHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.UnaryOperator;

import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate;
import static gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController.frameworkPredicate2;
import static gregtech.api.unification.material.Materials.DistilledWater;
import static net.minecraft.util.text.TextFormatting.AQUA;
import static net.minecraft.util.text.TextFormatting.RED;

public class MetaTileEntityIndustrialSteamEngine extends TJMultiblockDisplayBase implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS,
            GregicAdditionsCapabilities.STEAM, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final SteamEngineWorkableHandler workableHandler = new SteamEngineWorkableHandler(this, RecipeMaps.STEAM_TURBINE_FUELS, this::getEfficiency)
            .setImportFluidsSupplier(this::getImportFluidHandler)
            .setExportFluidsSupplier(this::getExportFluidHandler)
            .setExportEnergySupplier(this::getEnergyContainer)
            .setMaxVoltageSupplier(this::getMaxVoltage)
            .setTierSupplier(this::getTier);
    private IMultipleTankHandler importFluidHandler;
    private IMultipleTankHandler exportFluidHandler;
    private IEnergyContainer energyContainer;
    private double efficiency;
    private long maxVoltage;
    private int tier;

    public MetaTileEntityIndustrialSteamEngine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityIndustrialSteamEngine(metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.industrial_steam_engine.description"));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!isStructureFormed()) return;
        builder.customLine(text -> {
                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("machine.universal.consuming.seconds", this.workableHandler.getConsumption(),
                            net.minecraft.util.text.translation.I18n.translateToLocal(this.workableHandler.getFuelName()),
                            this.workableHandler.getMaxProgress() / 20)));
                    FluidStack fuelStack = this.workableHandler.getFuelStack();
                    int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

                    ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.fuel_amount", fuelAmount, fuelName.getUnformattedText())));

                    text.addTextComponent(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.extreme_turbine.energy", this.workableHandler.getProduction())));

                    text.addTextComponent(new TextComponentTranslation("gregtech.universal.tooltip.efficiency", TJValues.thousandFormat.format(this.efficiency * 100)).setStyle(new Style().setColor(AQUA)));

                    if (!this.workableHandler.isVoidEnergy() && this.energyContainer.getEnergyCanBeInserted() < this.workableHandler.getProduction())
                        text.addTextComponent(new TextComponentTranslation("machine.universal.output.full").setStyle(new Style().setColor(RED)));
                }).isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJToggleButtonWidget(175, 151, 18, 18)
                .setToggleButtonResponder(this.workableHandler::setVoidEnergy)
                .setButtonSupplier(this.workableHandler::isVoidEnergy)
                .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                .setBackgroundTextures(TJGuiTextures.ENERGY_VOID)
                .useToggleTexture(true));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        boolean hasOutputEnergy = abilities.containsKey(MultiblockAbility.OUTPUT_ENERGY);
        boolean hasInputFluid = abilities.containsKey(MultiblockAbility.IMPORT_FLUIDS);
        boolean hasSteamInput = abilities.containsKey(GregicAdditionsCapabilities.STEAM);

        return super.checkStructureComponents(parts, abilities) && hasOutputEnergy && (hasInputFluid || hasSteamInput);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0) {
            this.workableHandler.update();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IFluidTank> fluidTanks = new ArrayList<>();
        fluidTanks.addAll(this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        fluidTanks.addAll(this.getAbilities(GregicAdditionsCapabilities.STEAM));

        int framework = 0, framework2 = 0;
        if (context.get("framework") instanceof GAMultiblockCasing.CasingType) {
            framework = ((GAMultiblockCasing.CasingType) context.get("framework")).getTier();
        }
        if (context.get("framework2") instanceof GAMultiblockCasing2.CasingType) {
            framework2 = ((GAMultiblockCasing2.CasingType) context.get("framework2")).getTier();
        }
        int motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        this.importFluidHandler = new FluidTankList(true, fluidTanks);
        this.exportFluidHandler = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        this.tier = Math.min(motor, Math.max(framework, framework2));
        this.maxVoltage = (long) (Math.pow(4, this.tier) * 8);
        this.efficiency = Math.max(0.1F, (1.0F - ((this.tier - 1) / 10.0F)));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importFluidHandler = new FluidTankList(true);
        this.exportFluidHandler = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Collections.emptyList());
        this.tier = 0;
        this.maxVoltage = 0;
        this.efficiency = 0;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~CC", "CEC", "~CC")
                .aisle("CCC", "CRC", "CCC")
                .aisle("~CC", "CFC", "~CC")
                .aisle("~CC", "~SC", "~CC")
                .setAmountAtLeast('L', 8)
                .where('S', selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('E', abilityPartPredicate(MultiblockAbility.OUTPUT_ENERGY))
                .where('F', frameworkPredicate().or(frameworkPredicate2()))
                .where('R', LargeSimpleRecipeMapMultiblockController.motorPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TUMBAGA);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.TUMBAGA_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive());
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getFuelAmount).setMaxProgress(this::getFuelCapacity)
                .setLocale("tj.multiblock.bars.fuel").setParams(() -> new Object[]{this.workableHandler.getFuelName()})
                .setFluidStackSupplier(this.workableHandler::getFuelStack));
    }

    private long getFuelAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(this.workableHandler.getFuelStack(), this.getImportFluidHandler());
    }

    private long getFuelCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(this.workableHandler.getFuelStack(), this.getImportFluidHandler());
    }

    private IMultipleTankHandler getImportFluidHandler() {
        return this.importFluidHandler;
    }

    public IMultipleTankHandler getExportFluidHandler() {
        return this.exportFluidHandler;
    }

    private IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    public double getEfficiency() {
        return this.efficiency;
    }

    public int getTier() {
        return this.tier;
    }

    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    private static class SteamEngineWorkableHandler extends AbstractWorkableHandler<SteamEngineWorkableHandler> implements IGeneratorInfo, IFuelable {

        private final Set<FluidStack> lastSearchedFluid = new HashSet<>();
        private final Set<FluidStack> blacklistFluid = new HashSet<>();
        private final FuelRecipeMap recipeMap;
        private final DoubleSupplier efficiencySupplier;
        private FuelRecipe previousRecipe;
        private String fuelName;
        private boolean voidEnergy;
        private int consumption;
        private int searchCount;

        public SteamEngineWorkableHandler(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, DoubleSupplier efficiencySupplier) {
            super(metaTileEntity);
            this.recipeMap = recipeMap;
            this.efficiencySupplier = efficiencySupplier;
        }

        @Override
        protected boolean startRecipe() {
            FluidStack fuelStack = null;
            for (int i = 0; i < ((IMultipleTankHandler) this.importFluidsSupplier.get()).getTanks(); i++) {
                IFluidTank tank = ((IMultipleTankHandler) this.importFluidsSupplier.get()).getTankAt(i);
                FluidStack stack = tank.getFluid();
                if (stack == null) continue;
                if (fuelStack == null) {
                    if (this.blacklistFluid.contains(stack) || this.lastSearchedFluid.contains(stack)) continue;
                    fuelStack = stack.copy();
                    this.lastSearchedFluid.add(fuelStack);
                } else if (fuelStack.isFluidEqual(stack)) {
                    long amount = fuelStack.amount + stack.amount;
                    fuelStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
                }
            }
            fuelStack = this.tryAcquireNewRecipe(fuelStack);
            if (fuelStack != null && fuelStack.isFluidStackIdentical(this.importFluidsSupplier.get().drain(fuelStack, false))) {
                FluidStack fluidStack = this.importFluidsSupplier.get().drain(fuelStack, true);
                this.exportFluidsSupplier.get().fill(DistilledWater.getFluid(this.consumption / 160), true);
                this.fuelName = fluidStack.getUnlocalizedName();
                this.lastSearchedFluid.remove(fuelStack);
                this.consumption = fluidStack.amount;
                return true; //recipe is found and ready to use
            }
            if (++this.searchCount >= ((IMultipleTankHandler) this.importFluidsSupplier.get()).getTanks()) {
                this.lastSearchedFluid.clear();
                this.searchCount = 0;
            }
            return false;
        }

        @Override
        protected void progressRecipe(int progress) {
            if (this.voidEnergy || this.exportEnergySupplier.get().getEnergyCanBeInserted() >= this.energyPerTick) {
                this.exportEnergySupplier.get().addEnergy(this.energyPerTick);
                if (this.hasProblem)
                    this.setProblem(false);
                this.progress++;
            } else if (!this.hasProblem)
                this.setProblem(true);
        }

        @Override
        protected boolean completeRecipe() {
            this.lastSearchedFluid.clear();
            return true;
        }

        public FluidStack getFuelStack() {
            if (this.previousRecipe == null)
                return null;
            FluidStack fuelStack = this.previousRecipe.getRecipeFluid();
            return this.importFluidsSupplier.get().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
        }

        protected FluidStack tryAcquireNewRecipe(FluidStack fuelStack) {
            FuelRecipe currentRecipe;
            if (this.previousRecipe != null && this.previousRecipe.matches(this.maxVoltageSupplier.getAsLong(), fuelStack)) {
                //if previous recipe still matches inputs, try to use it
                currentRecipe = this.previousRecipe;
            } else {
                //else, try searching new recipe for given inputs
                currentRecipe = this.recipeMap.findRecipe(this.maxVoltageSupplier.getAsLong(), fuelStack);
                //if we found recipe that can be buffered, buffer it
                if (currentRecipe != null) {
                    this.previousRecipe = currentRecipe;
                } else this.blacklistFluid.add(fuelStack); // blacklist fluid not found in recipe map to prevent search slowdown.
            }
            if (currentRecipe != null && checkRecipe(currentRecipe)) {
                int fuelAmountToUse = this.calculateFuelAmount(currentRecipe);
                if (fuelStack.amount >= fuelAmountToUse) {
                    this.maxProgress = this.calculateRecipeDuration(currentRecipe);
                    this.energyPerTick = this.startRecipe(currentRecipe, fuelAmountToUse, this.maxProgress);
                    FluidStack recipeFluid = currentRecipe.getRecipeFluid();
                    recipeFluid.amount = fuelAmountToUse;
                    return recipeFluid;
                }
            }
            return null;
        }

        protected int calculateRecipeDuration(FuelRecipe currentRecipe) {
            return currentRecipe.getDuration() * 2;
        }

        protected int calculateFuelAmount(FuelRecipe currentRecipe) {
            return (int) Math.round((currentRecipe.getRecipeFluid().amount * 2 * getVoltageMultiplier(this.maxVoltageSupplier.getAsLong(), currentRecipe.getMinVoltage())) / this.efficiencySupplier.getAsDouble());
        }

        public static int getVoltageMultiplier(long maxVoltage, long minVoltage) {
            return (int) (maxVoltage / minVoltage);
        }

        protected long startRecipe(FuelRecipe currentRecipe, int fuelAmountUsed, int recipeDuration) {
            return this.maxVoltageSupplier.getAsLong();
        }

        protected boolean checkRecipe(FuelRecipe recipe) {
            return true;
        }

        @Override
        public Collection<IFuelInfo> getFuels() {
            final IMultipleTankHandler fluidTanks = (IMultipleTankHandler) this.importFluidsSupplier.get();
            if (fluidTanks == null)
                return Collections.emptySet();

            final LinkedHashMap<String, IFuelInfo> fuels = new LinkedHashMap<>();
            // Fuel capacity is all tanks
            int fuelCapacity = 0;
            for (IFluidTank fluidTank : fluidTanks) {
                fuelCapacity += fluidTank.getCapacity();
            }

            for (IFluidTank fluidTank : fluidTanks) {
                final FluidStack tankContents = fluidTank.drain(Integer.MAX_VALUE, false);
                if (tankContents == null || tankContents.amount <= 0) {
                    fuelCapacity -= fluidTank.getCapacity();
                    continue;
                }
                int fuelRemaining = tankContents.amount;
                FuelRecipe recipe = this.recipeMap.findRecipe(this.maxVoltageSupplier.getAsLong(), tankContents);
                if (recipe == null || this.lastSearchedFluid.contains(recipe.getRecipeFluid())) {
                    fuelCapacity -= fluidTank.getCapacity();
                    continue;
                }
                int amountPerRecipe = this.calculateFuelAmount(recipe);
                int duration = this.calculateRecipeDuration(recipe);
                long fuelBurnTime = ((long) duration * fuelRemaining) / amountPerRecipe;

                FluidFuelInfo fuelInfo = (FluidFuelInfo) fuels.get(tankContents.getUnlocalizedName());
                if (fuelInfo == null) {
                    fuelInfo = new FluidFuelInfo(tankContents, fuelRemaining, fuelCapacity, amountPerRecipe, fuelBurnTime);
                    fuels.put(tankContents.getUnlocalizedName(), fuelInfo);
                } else {
                    fuelInfo.addFuelRemaining(fuelRemaining);
                    fuelInfo.addFuelBurnTime(fuelBurnTime);
                }
            }
            return fuels.values();
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tagCompound = super.serializeNBT();
            tagCompound.setInteger("consumption", this.consumption);
            tagCompound.setBoolean("voidEnergy", this.voidEnergy);
            if (this.fuelName != null)
                tagCompound.setString("fuelName", this.fuelName);
            return tagCompound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            super.deserializeNBT(compound);
            this.consumption = compound.getInteger("consumption");
            this.voidEnergy = compound.getBoolean("voidEnergy");
            if (compound.hasKey("fuelName"))
                this.fuelName = compound.getString("fuelName");
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == TJCapabilities.CAPABILITY_GENERATOR)
                return TJCapabilities.CAPABILITY_GENERATOR.cast(this);
            if (capability == GregtechCapabilities.CAPABILITY_FUELABLE)
                return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
            return super.getCapability(capability);
        }

        public void setVoidEnergy(boolean voidEnergy, String id) {
            this.voidEnergy = voidEnergy;
            this.metaTileEntity.markDirty();
        }

        public boolean isVoidEnergy() {
            return this.voidEnergy;
        }

        public String getFuelName() {
            return this.fuelName;
        }

        @Override
        public long getProduction() {
            return this.energyPerTick;
        }

        @Override
        public long getConsumption() {
            return this.consumption;
        }

        @Override
        public String[] consumptionInfo() {
            int seconds = this.maxProgress / 20;
            String amount = String.valueOf(seconds);
            String s = seconds < 2 ? "second" : "seconds";
            return ArrayUtils.toArray("machine.universal.consumption", "§7 ", "suffix", "machine.universal.liters.short",  "§r§7 (§b", this.fuelName, "§7)§r ", "every", "§b ", amount, "§r ", s);
        }

        @Override
        public String[] productionInfo() {
            int tier = GAUtility.getTierByVoltage(this.energyPerTick);
            String voltage = GAValues.VN[tier];
            String color = TJValues.VCC[tier];
            return ArrayUtils.toArray("machine.universal.producing", "§e ", "suffix", "§r ", "machine.universal.eu.tick",
                    " ", "§7(§6", color, voltage, "§7)");
        }
    }
}

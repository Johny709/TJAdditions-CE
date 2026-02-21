package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.impl.handler.INameHandler;
import tj.capability.impl.workable.NamingMachineWorkableHandler;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityLargeNamingMachine extends TJMultiblockControllerBase implements INameHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final NamingMachineWorkableHandler workableHandler = new NamingMachineWorkableHandler(this);
    private int maxVoltage;
    private int parallel;
    private String name;

    public MetaTileEntityLargeNamingMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeNamingMachine(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeNamingMachine.stack));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.getInputEnergyContainer())
                .voltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                .energyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick())
                .addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallel)
                .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~XXX~", "XXXXX", "~XXX~", "~~C~~", "~~~~~")
                .aisle("XXXXX", "X###X", "X###X", "~CCC~", "~~C~~")
                .aisle("XXXXX", "X###X", "X###X", "CC#CC", "~CPC~")
                .aisle("XXXXX", "X###X", "X###X", "~CCC~", "~~C~~")
                .aisle("~XXX~", "XXSXX", "~XXX~", "~~C~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', LargeSimpleRecipeMapMultiblockController.pistonPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.IRON);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        this.maxVoltage = 8 << tier * 2;
        this.parallel = TJConfig.largeNamingMachine.stack * 16;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("tagName", this.name);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.name = data.getString("tagName");
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.IRON_CASING;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

package tj.machines.multi.steam;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
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
import gregtech.common.blocks.wood.BlockGregLog;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.ICharcoalHandler;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;
import tj.gui.widgets.impl.WindowsWidgetGroup;

import java.util.*;

import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Charcoal;

public class MetaTileEntityCharcoalPit extends TJMultiblockControllerBase implements ICharcoalHandler {

    private final boolean advanced;
    private final CharcoalPitWorkableHandler workableHandler = new CharcoalPitWorkableHandler(this);
    private final Set<BlockPos> charcoalPos = new HashSet<>();
    private int widthHeight = 1;
    private int depth = 3;

    public MetaTileEntityCharcoalPit(ResourceLocation metaTileEntityId, boolean advanced) {
        super(metaTileEntityId);
        this.advanced = advanced;
        this.maintenance_problems = 0b111111;
        this.reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCharcoalPit(this.metaTileEntityId, this.advanced);
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
                .addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ButtonPopUpWidget<>(0, 0, 0, 0)
                .addPopup(widgetGroup1 -> true)
                .addPopup(new TJToggleButtonWidget(175, 152, 18, 18)
                        .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .useToggleTexture(true), widgetGroup1 -> {
                    widgetGroup1.addWidget(new WindowsWidgetGroup(12, 60, 160, 100, GuiTextures.BORDERED_BACKGROUND)
                            .addSubWidget(new TJLabelWidget(0, -1, 160, 18, null)
                                    .setLocale("tj.multiblock.charcoal_pit.window_settings")
                                    .setCanSlide(false))
                            .addSubWidget(new TJLabelWidget(35, 12, 99, 18, null)
                                    .setLocale("tj.multiblock.charcoal_pit.set_width_length")
                                    .setCanSlide(false))
                            .addSubWidget(new TJLabelWidget(35, 47, 99, 18, null)
                                    .setLocale("tj.multiblock.charcoal_pit.set_depth")
                                    .setCanSlide(false))
                            .addSubWidget(new ImageWidget(47, 30, 63, 18, GuiTextures.DISPLAY))
                            .addSubWidget(new ImageWidget(47, 65, 63, 18, GuiTextures.DISPLAY))
                            .addSubWidget(new TJToggleButtonWidget(35, 30, 18, 18)
                                    .setToggleDisplayText("§c-", "§c-")
                                    .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addSubWidget(new TJToggleButtonWidget(35, 65, 18, 18)
                                    .setToggleDisplayText("§c-", "§c-")
                                    .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addSubWidget(new TJToggleButtonWidget(110, 30, 18, 18)
                                    .setToggleDisplayText("§9+", "§9+")
                                    .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true))
                            .addSubWidget(new TJToggleButtonWidget(110, 65, 18, 18)
                                    .setToggleDisplayText("§9+", "§9+")
                                    .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                                    .setButtonSupplier(() -> false)
                                    .useToggleTexture(true)));
                    return false;
                }));
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!playerIn.getEntityWorld().isRemote && playerIn.getHeldItem(hand).getItem() instanceof ItemFlintAndSteel) {
            playerIn.getHeldItem(hand).damageItem(1, playerIn);
            this.workableHandler.setCanStart(true);
            return true;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        int size = this.widthHeight + this.widthHeight + 1;
        String[] controllerAisle = new String[size + 2];
        String[] charcoalAisle = new String[size + 2];
        Arrays.fill(controllerAisle, '~' + this.getAisleWidthHeight('G', size) + '~');
        Arrays.fill(charcoalAisle, 'G' + this.getAisleWidthHeight('C', size) + 'G');
        charcoalAisle[0] = charcoalAisle[charcoalAisle.length - 1] = '~' + this.getAisleWidthHeight('G', size) + '~';
        controllerAisle[0] = controllerAisle[controllerAisle.length - 1] = this.getAisleWidthHeight('~', size + 2);
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle(controllerAisle);
        for (int i = 0; i < this.depth; i++) {
            factoryPattern.aisle(charcoalAisle);
        }
        controllerAisle = Arrays.copyOf(controllerAisle, controllerAisle.length);
        controllerAisle[this.widthHeight + 1] = '~' + this.getAisleWidthHeight('G', this.widthHeight) + 'S' + this.getAisleWidthHeight('G', this.widthHeight) + '~';
        return factoryPattern.aisle(controllerAisle)
                .where('S', this.selfPredicate())
                .where('C', blockWorldState -> {
                    Block block = blockWorldState.getBlockState().getBlock();
                    if (block instanceof BlockLog || block instanceof BlockGregLog) {
                        if (blockWorldState.getWorld() != null)
                            this.charcoalPos.add(blockWorldState.getPos());
                        return true;
                    }
                    return false;
                }).where('G', blockWorldState -> {
                    Block block = blockWorldState.getBlockState().getBlock();
                    return block instanceof BlockDirt || block instanceof BlockGrass || block instanceof BlockSand;
                }).where('~', tile -> true)
                .build();
    }

    private String getAisleWidthHeight(char symbol, int widthHeight) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < widthHeight; i++) {
            builder.append(symbol);
        }
        return builder.toString();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return this.advanced ? Textures.SOLID_STEEL_CASING : Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_CRUSHER_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        if (this.workableHandler.isActive())
            Textures.ROCK_CRUSHER_ACTIVE_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.widthHeight);
        buf.writeInt(this.depth);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.widthHeight = buf.readInt();
        this.depth = buf.readInt();
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("widthHeight", this.widthHeight);
        data.setInteger("depth", this.depth);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("widthHeight"))
            this.widthHeight = data.getInteger("widthHeight");
        if (data.hasKey("depth"))
            this.depth = data.getInteger("depth");
        this.structurePattern = this.createStructurePattern();
    }

    @Override
    public boolean isAdvanced() {
        return this.advanced;
    }

    @Override
    public Set<BlockPos> getCharcoalPos() {
        return this.charcoalPos;
    }

    private static class CharcoalPitWorkableHandler extends AbstractWorkableHandler<ICharcoalHandler> implements IItemFluidHandlerInfo {

        private final List<ItemStack> itemInputs = new ArrayList<>();
        private final List<ItemStack> itemOutputs = new ArrayList<>();
        private boolean canStart;

        public CharcoalPitWorkableHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity);
        }

        public void setCanStart(boolean canStart) {
            this.canStart = canStart;
        }

        private boolean checkForFire() {
            if (!this.handler.isAdvanced())
                return false;
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos pos = this.metaTileEntity.getPos().offset(facing);
                if (this.metaTileEntity.getWorld().getBlockState(pos).getBlock() instanceof BlockFire)
                    return true;
            }
            return false;
        }

        @Override
        protected boolean startRecipe() {
            this.setMaxProgress(this.handler.getCharcoalPos().size() * 1200);
            if (this.canStart || this.checkForFire()) {
                ItemStack charcoalBlocks = MetaBlocks.COMPRESSED.get(Charcoal).getItem(Charcoal);
                charcoalBlocks.setCount(this.handler.getCharcoalPos().size());
                this.itemOutputs.add(charcoalBlocks);
                return true;
            }
            return false;
        }

        @Override
        protected void progressRecipe(int progress) {
            this.progress++;
        }

        @Override
        protected boolean completeRecipe() {
            Block charcoalBlock = MetaBlocks.COMPRESSED.get(Charcoal);
            for (BlockPos pos : this.handler.getCharcoalPos()) {
               this.metaTileEntity.getWorld().setBlockState(pos, charcoalBlock.getStateFromMeta(5));
            }
            this.canStart = false;
            this.itemInputs.clear();
            this.itemOutputs.clear();
            return true;
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
                return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
            return super.getCapability(capability);
        }

        @Override
        public List<ItemStack> getItemInputs() {
            return this.itemInputs;
        }

        @Override
        public List<ItemStack> getItemOutputs() {
            return this.itemOutputs;
        }
    }
}

package tj.mixin.gregtech.client;

import gregicadditions.GAValues;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.ItemStackKey;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.TJValues;
import tj.integration.jei.MBPattern;
import tj.integration.jei.PartInfo;
import tj.integration.jei.TJMultiblockShapeInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;

import java.util.*;


@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public abstract class MultiblockInfoRecipeWrapperMixin {

    @Shadow
    protected abstract void updateParts();

    @Shadow
    @Final
    private int ICON_SIZE;

    @Shadow
    @Final
    private int RIGHT_PADDING;

    @Shadow
    private Map<GuiButton, Runnable> buttons;

    @Shadow
    private GuiButton buttonNextPattern;

    @Shadow
    private int currentRendererPage;

    @Shadow
    private RecipeLayout recipeLayout;

    @Shadow
    @Final
    private static int MAX_PARTS;

    @Shadow
    private GuiButton buttonPreviousPattern;

    @Shadow
    protected abstract boolean shouldDisplayBlock(BlockPos pos);

    @Unique
    private GuiButton buttonVoltage;

    @Unique
    private MBPattern[][] mbPatterns;

    @Unique
    private boolean multiLayer;

    @Unique
    private int currentVoltagePage;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectMultiblockInfoRecipeWrapper_Init(MultiblockInfoPage infoPage, CallbackInfo ci, HashSet<ItemStackKey> drops) {
        if (infoPage instanceof IParallelMultiblockInfoPage) {
            final List<TJMultiblockShapeInfo[]> shapeInfos = ((IParallelMultiblockInfoPage) infoPage).getMatchingShapes(new TJMultiblockShapeInfo[15]);
            this.mbPatterns = new MBPattern[shapeInfos.size()][TJConfig.machines.disableLayersInJEI ? 1 : 15];
            this.multiLayer = true;
            for (int i = 0; i < shapeInfos.size(); i++) {
                final TJMultiblockShapeInfo[] infos = shapeInfos.get(i);
                if (!TJConfig.machines.disableLayersInJEI) {
                    for (int j = 0; j < infos.length; j++) {
                        this.mbPatterns[i][j] = this.initializePattern_2(infos[j], drops);
                    }
                } else if (infos != null && infos.length > 2 && infos[2] != null)
                    this.mbPatterns[i][0] = this.initializePattern_2(infos[2], drops);
            }
        }
    }

    @Inject(method = "setRecipeLayout", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectSetRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper, CallbackInfo ci, IDrawable border, boolean isPagesDisabled) {
        if (this.multiLayer) {
            this.buttonVoltage = new GuiButton(0, border.getWidth() - ((2 * ICON_SIZE) + RIGHT_PADDING + 1), 110, ICON_SIZE + 21, ICON_SIZE, TJValues.VCC[this.currentVoltagePage] + GAValues.VN[this.currentVoltagePage]);
            this.buttons.put(this.buttonVoltage, () -> this.switchVoltagePage(Mouse.isButtonDown(0) ? 1 : Mouse.isButtonDown(1) ? -1 : 0));
            this.buttonNextPattern.enabled = this.mbPatterns.length > 1;
        }
    }

    @Inject(method = "getCurrentRenderer", at = @At("HEAD"), cancellable = true)
    private void injectGetCurrentRenderer(CallbackInfoReturnable<WorldSceneRenderer> cir) {
        if (this.multiLayer) {
            cir.setReturnValue(this.mbPatterns[this.currentRendererPage][this.currentVoltagePage].sceneRenderer);
        }
    }

    @Inject(method = "updateParts", at = @At("HEAD"), cancellable = true)
    private void injectUpdateParts(CallbackInfo ci) {
        if (this.multiLayer) {
            final IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
            final List<ItemStack> parts = this.mbPatterns[currentRendererPage][this.currentVoltagePage].parts;
            final int limit = Math.min(parts.size(), MAX_PARTS);
            for (int i = 0; i < limit; ++i)
                itemStackGroup.set(i, parts.get(i));
            for (int i = parts.size(); i < limit; ++i)
                itemStackGroup.set(i, (ItemStack) null);
            ci.cancel();
        }
    }

    @Inject(method = "switchRenderPage", at = @At("HEAD"), cancellable = true)
    private void injectSwitchRenderPage(int amount, CallbackInfo ci) {
        if (this.multiLayer) {
            final int maxIndex = this.mbPatterns.length - 1;
            final int index = Math.max(0, Math.min(maxIndex, currentRendererPage + amount));
            if (index != currentRendererPage) {
                this.currentRendererPage = index;
                this.buttonPreviousPattern.enabled = index > 0;
                this.buttonNextPattern.enabled = index < maxIndex;
                updateParts();
            }
            ci.cancel();
        }
    }

    @Unique
    private void switchVoltagePage(int amount) {
        final int maxIndex = this.mbPatterns[currentRendererPage].length - 1;
        final int index = Math.max(0, Math.min(maxIndex, this.currentVoltagePage + amount));
        if (index != this.currentVoltagePage) {
            this.currentVoltagePage = index;
            this.buttonVoltage.displayString = TJValues.VCC[index] + GAValues.VN[index];
            updateParts();
        }
    }

    @Unique
    private MBPattern initializePattern_2(TJMultiblockShapeInfo shapeInfo, Set<ItemStackKey> blockDrops) {
        final MultiblockInfoRecipeWrapper wrapper = (MultiblockInfoRecipeWrapper)(Object)this;
        final Object2ObjectMap<BlockPos, BlockInfo> blockMap = new Object2ObjectOpenHashMap<>();
        final BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int z = 0; z < blocks.length; z++) {
            final BlockInfo[][] aisle = blocks[z];
            for (int y = 0; y < aisle.length; y++) {
                final BlockInfo[] column = aisle[y];
                for (int x = 0; x < column.length; x++) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final BlockInfo blockInfo = column[x];
                    blockMap.put(blockPos, blockInfo);
                }
            }
        }
        WorldSceneRenderer worldSceneRenderer = new WorldSceneRenderer(blockMap);
        worldSceneRenderer.world.updateEntities();
        final Object2ObjectMap<ItemStackKey, PartInfo> partsMap = new Object2ObjectOpenHashMap<>();
        gatherBlockDrops_2(worldSceneRenderer.world, blockMap, blockDrops, partsMap);
        worldSceneRenderer.setRenderCallback(wrapper);
        worldSceneRenderer.setRenderFilter(this::shouldDisplayBlock);
        final ArrayList<PartInfo> partInfos = new ArrayList<>(partsMap.values());
        partInfos.sort((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        });
        final ArrayList<ItemStack> parts = new ArrayList<>();
        for (PartInfo partInfo : partInfos) {
            parts.add(partInfo.getItemStack());
        }
        return new MBPattern(worldSceneRenderer, parts);
    }

    @Unique
    private void gatherBlockDrops_2(World world, Object2ObjectMap<BlockPos, BlockInfo> blocks, Set<ItemStackKey> drops, Object2ObjectMap<ItemStackKey, PartInfo> partsMap) {
        final NonNullList<ItemStack> dropsList = NonNullList.create();
        for (Object2ObjectMap.Entry<BlockPos, BlockInfo> entry : blocks.object2ObjectEntrySet()) {
            final BlockPos pos = entry.getKey();
            final IBlockState blockState = world.getBlockState(pos);
            final NonNullList<ItemStack> blockDrops = NonNullList.create();
            blockState.getBlock().getDrops(blockDrops, world, pos, blockState, 0);
            dropsList.addAll(blockDrops);

            for (ItemStack itemStack : blockDrops) {
                final ItemStackKey itemStackKey = new ItemStackKey(itemStack);
                PartInfo partInfo = partsMap.get(itemStackKey);
                if (partInfo == null) {
                    partInfo = new PartInfo(itemStackKey, entry.getValue());
                    partsMap.put(itemStackKey, partInfo);
                }
                ++partInfo.amount;
            }
        }
        for (ItemStack itemStack : dropsList) {
            drops.add(new ItemStackKey(itemStack));
        }
    }
}

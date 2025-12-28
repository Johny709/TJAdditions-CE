package tj.mixin.gregtech;

import gregicadditions.GAValues;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.ItemStackKey;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJValues;
import tj.integration.jei.MBPattern;
import tj.integration.jei.PartInfo;
import tj.integration.jei.multi.parallel.IParallelMultiblockInfoPage;

import java.util.*;


@Mixin(value = MultiblockInfoRecipeWrapper.class, remap = false)
public abstract class MultiblockInfoRecipeWrapperMixin implements IMultiblockInfoRecipeWrapperMixin {

    @Shadow
    protected abstract void updateParts();

    @Unique
    private GuiButton buttonVoltage;

    @Unique
    private MBPattern[][] mbPatterns;

    @Unique
    private boolean multiLayer;

    @Unique
    private int currentVoltagePage;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectMultiblockInfoRecipeWrapper_init(MultiblockInfoPage infoPage, CallbackInfo ci, HashSet<ItemStackKey> drops) {
        if (infoPage instanceof IParallelMultiblockInfoPage) {
            List<MultiblockShapeInfo[]> shapeInfos = ((IParallelMultiblockInfoPage) infoPage).getMatchingShapes(new MultiblockShapeInfo[15]);
            this.mbPatterns = new MBPattern[shapeInfos.size()][15];
            this.multiLayer = true;
            for (int i = 0; i < shapeInfos.size(); i++) {
                MultiblockShapeInfo[] infos = shapeInfos.get(i);
                for (int j = 0; j < infos.length; j++) {
                    this.mbPatterns[i][j] = this.initializePattern_2(infos[j], drops);
                }
            }
        }
    }

    @Inject(method = "setRecipeLayout", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectSetRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper, CallbackInfo ci, IDrawable border, boolean isPagesDisabled) {
        if (this.multiLayer) {
            this.buttonVoltage = new GuiButton(0, border.getWidth() - ((2 * GET_ICON_SIZE()) + GET_RIGHT_PADDING() + 1), 110, GET_ICON_SIZE() + 21, GET_ICON_SIZE(), TJValues.VCC[this.currentVoltagePage] + GAValues.VN[this.currentVoltagePage]);
            getButtons().put(this.buttonVoltage, () -> this.switchVoltagePage(Mouse.isButtonDown(0) ? 1 : Mouse.isButtonDown(1) ? -1 : 0));
            getButtonNextPattern().enabled = this.mbPatterns.length > 1;
        }
    }

    @Inject(method = "getCurrentRenderer", at = @At("HEAD"), cancellable = true)
    private void injectGetCurrentRenderer(CallbackInfoReturnable<WorldSceneRenderer> cir) {
        if (this.multiLayer) {
            cir.setReturnValue(this.mbPatterns[getCurrentRenderPage()][this.currentVoltagePage].sceneRenderer);
            cir.cancel();
        }
    }

    @Inject(method = "updateParts", at = @At("HEAD"), cancellable = true)
    private void injectUpdateParts(CallbackInfo ci) {
        if (this.multiLayer) {
            IGuiItemStackGroup itemStackGroup = getRecipeLayout().getItemStacks();
            List<ItemStack> parts = this.mbPatterns[getCurrentRenderPage()][this.currentVoltagePage].parts;
            int limit = Math.min(parts.size(), GET_MAX_PARTS());
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
            int maxIndex = this.mbPatterns.length - 1;
            int index = Math.max(0, Math.min(maxIndex, getCurrentRenderPage() + amount));
            if (index != getCurrentRenderPage()) {
                setCurrentRenderPage(index);
                getButtonPreviousPattern().enabled = index > 0;
                getButtonNextPattern().enabled = index < maxIndex;
                updateParts();
            }
            ci.cancel();
        }
    }

    @Unique
    private void switchVoltagePage(int amount) {
        int maxIndex = this.mbPatterns[getCurrentRenderPage()].length - 1;
        int index = Math.max(0, Math.min(maxIndex, this.currentVoltagePage + amount));
        if (index != this.currentVoltagePage) {
            this.currentVoltagePage = index;
            this.buttonVoltage.displayString = TJValues.VCC[index] + GAValues.VN[index];
            updateParts();
        }
    }

    @Unique
    private MBPattern initializePattern_2(MultiblockShapeInfo shapeInfo, Set<ItemStackKey> blockDrops) {
        MultiblockInfoRecipeWrapper wrapper = (MultiblockInfoRecipeWrapper)(Object)this;
        Object2ObjectMap<BlockPos, BlockInfo> blockMap = new Object2ObjectOpenHashMap<>();
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int z = 0; z < blocks.length; z++) {
            BlockInfo[][] aisle = blocks[z];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int x = 0; x < column.length; x++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockInfo blockInfo = column[x];
                    blockMap.put(blockPos, blockInfo);
                }
            }
        }
        WorldSceneRenderer worldSceneRenderer = new WorldSceneRenderer(blockMap);
        worldSceneRenderer.world.updateEntities();
        Object2ObjectMap<ItemStackKey, PartInfo> partsMap = new Object2ObjectOpenHashMap<>();
        gatherBlockDrops_2(worldSceneRenderer.world, blockMap, blockDrops, partsMap);
        worldSceneRenderer.setRenderCallback(wrapper);
        worldSceneRenderer.setRenderFilter(this::shouldDisplayBlock2);
        ArrayList<PartInfo> partInfos = new ArrayList<>(partsMap.values());
        partInfos.sort((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        });
        ArrayList<ItemStack> parts = new ArrayList<>();
        for (PartInfo partInfo : partInfos) {
            parts.add(partInfo.getItemStack());
        }
        return new MBPattern(worldSceneRenderer, parts);
    }

    @Unique
    private void gatherBlockDrops_2(World world, Object2ObjectMap<BlockPos, BlockInfo> blocks, Set<ItemStackKey> drops, Object2ObjectMap<ItemStackKey, PartInfo> partsMap) {
        NonNullList<ItemStack> dropsList = NonNullList.create();
        for (Object2ObjectMap.Entry<BlockPos, BlockInfo> entry : blocks.object2ObjectEntrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState blockState = world.getBlockState(pos);
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            blockState.getBlock().getDrops(blockDrops, world, pos, blockState, 0);
            dropsList.addAll(blockDrops);

            for (ItemStack itemStack : blockDrops) {
                ItemStackKey itemStackKey = new ItemStackKey(itemStack);
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

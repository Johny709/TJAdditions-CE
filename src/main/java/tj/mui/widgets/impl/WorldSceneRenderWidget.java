package tj.mui.widgets.impl;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;
import tj.mui.widgets.TJWidget;

import javax.vecmath.Vector3f;

public class WorldSceneRenderWidget extends TJWidget<WorldSceneRenderWidget> {

    private final WorldSceneRenderer worldSceneRenderer;
    private ItemStack tooltipBlockStack = ItemStack.EMPTY;
    private TextureArea backgroundTexture;
    private boolean isCameraFree = true;
    private boolean clickedInside;
    private float rotationYaw = -45.0F;
    private float rotationPitch = 0.0F;
    private float panX = 0.0F;
    private float panY = 0.0F;
    private int lastMouseX;
    private int lastMouseY;
    private int zoom = 2;

    public WorldSceneRenderWidget(int x, int y, int width, int height, MetaTileEntity metaTileEntity) {
        super(new Position(x, y), new Size(width, height));
        final BlockPos pos = metaTileEntity.getPos();
        final MetaTileEntityHolder tileEntity = new MetaTileEntityHolder();
        final MetaTileEntity metaTileEntity1 = tileEntity.setMetaTileEntity(metaTileEntity);
        this.worldSceneRenderer = new WorldSceneRenderer(ImmutableMap.of(new BlockPos(0, 0, 0), new BlockInfo(metaTileEntity.getWorld().getBlockState(pos), tileEntity)));
        this.worldSceneRenderer.setRenderCallback(this::preRenderScene);
        metaTileEntity1.setFrontFacing(EnumFacing.WEST);
    }

    public WorldSceneRenderWidget setBackgroundTexture(TextureArea backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if (this.isActive)
            this.worldSceneRenderer.world.updateEntities();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (!this.isActive) return;
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        if (this.backgroundTexture != null)
            this.backgroundTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        this.worldSceneRenderer.render(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), 0);
    }

    @SideOnly(Side.CLIENT)
    public void preRenderScene(WorldSceneRenderer renderer) {
        Vector3f size = renderer.getSize();
        Vector3f minPos = renderer.world.getMinPos();
        minPos = new Vector3f(minPos);
        minPos.add(new Vector3f(0.0f, -1.0f, 0.5f));

        GlStateManager.scale(this.zoom, this.zoom, this.zoom);
        GlStateManager.translate(this.panX, this.panY, 0);
        GlStateManager.translate(-minPos.x, -minPos.y, -minPos.z);
        Vector3 centerPosition = new Vector3(size.x / 2.0f, size.y / 2.0f, size.z / 2.0f);
        GlStateManager.translate(centerPosition.x, centerPosition.y, centerPosition.z);
        GlStateManager.scale(2.0, 2.0, 2.0);
        GlStateManager.translate(-centerPosition.x, -centerPosition.y, -centerPosition.z);
        GlStateManager.translate(minPos.x, minPos.y, minPos.z);

        GlStateManager.translate(centerPosition.x, centerPosition.y, centerPosition.z);
        GlStateManager.rotate(this.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(this.rotationPitch, 0.0f, 0.0f, 1.0f);
        GlStateManager.translate(-centerPosition.x, -centerPosition.y, -centerPosition.z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.clickedInside = true;
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.clickedInside = false;
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isActive && this.clickedInside) {
            this.tooltipBlockStack = ItemStack.EMPTY;
            final BlockPos pos = this.worldSceneRenderer.getLastHitBlock();
            final boolean leftClickHeld = Mouse.isButtonDown(0);
            final boolean rightClickHeld = Mouse.isButtonDown(1);
            final boolean isHoldingShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            if (leftClickHeld) {
                final int mouseDeltaY = mouseY - this.lastMouseY;
                final int mouseDeltaX = mouseX - this.lastMouseX;
                if (this.isCameraFree) {
                    this.rotationPitch += mouseDeltaY / 0.5f;
                    this.rotationYaw += mouseDeltaX * 2.0f;
                } else if (isHoldingShift) {
                    this.rotationPitch += mouseDeltaY / 0.5f;
                } else this.rotationYaw += mouseDeltaX * 2.0f;
            } else if (rightClickHeld) {
                final int mouseDeltaY = mouseY - lastMouseY;
                if (isHoldingShift) {
                    this.zoom *= Math.pow(1.05d, -mouseDeltaY);
                } else {
                    int mouseDeltaX = mouseX - lastMouseX;
                    this.panX -= mouseDeltaX / 2.0f;
                    this.panY -= mouseDeltaY / 2.0f;
                }
            }

            if (!(leftClickHeld || rightClickHeld) && pos != null && !this.worldSceneRenderer.world.isAirBlock(pos)) {
                IBlockState blockState = this.worldSceneRenderer.world.getBlockState(pos);
                RayTraceResult result = new CuboidRayTraceResult(new Vector3(0.5, 0.5, 0.5).add(pos), pos, EnumFacing.UP, new IndexedCuboid6(null, Cuboid6.full), 1.0);
                ItemStack itemStack = blockState.getBlock().getPickBlock(blockState, result, this.worldSceneRenderer.world, pos, Minecraft.getMinecraft().player);
                if (itemStack != null && !itemStack.isEmpty()) {
                    this.tooltipBlockStack = itemStack;
                }
            }
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            return true;
        } else return false;
    }
}

package tj.rendering;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class TJRenderUtil {

    private TJRenderUtil() {}

    public static void drawCube(int radius, int bottom, int top) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        buffer.pos(-radius, bottom, -radius).endVertex();
        buffer.pos(radius + 1, bottom, -radius).endVertex();
        buffer.pos(radius + 1, bottom, radius + 1).endVertex();
        buffer.pos(-radius, bottom, radius + 1).endVertex();

        buffer.pos(-radius, bottom, radius + 1).endVertex();
        buffer.pos(radius + 1, bottom, radius + 1).endVertex();
        buffer.pos(radius + 1, top, radius + 1).endVertex();
        buffer.pos(-radius, top, radius + 1).endVertex();

        buffer.pos(-radius, bottom, radius + 1).endVertex();
        buffer.pos(-radius, top, radius + 1).endVertex();
        buffer.pos(-radius, top, -radius).endVertex();
        buffer.pos(-radius, bottom, -radius).endVertex();

        buffer.pos(-radius, bottom,bottom -radius).endVertex();
        buffer.pos(radius + 1, bottom, -radius).endVertex();
        buffer.pos(radius + 1, top, -radius).endVertex();
        buffer.pos(-radius, top, -radius).endVertex();

        buffer.pos(radius + 1, bottom, radius + 1).endVertex();
        buffer.pos(radius + 1, top, radius + 1).endVertex();
        buffer.pos(radius + 1, top, -radius).endVertex();
        buffer.pos(radius + 1, bottom, -radius).endVertex();

        buffer.pos(-radius, top, -radius).endVertex();
        buffer.pos(radius + 1, top, -radius).endVertex();
        buffer.pos(radius + 1, top, radius + 1).endVertex();
        buffer.pos(-radius, top, radius + 1).endVertex();
        tessellator.draw();
    }
}

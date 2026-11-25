package tj.mui;

import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.TextureArea;
import net.minecraft.util.ResourceLocation;
import tj.TJ;

public final class TJTextureArea {

    /**
     * @param imageLocation path of image under {@link TJ#MODID} namespace. No need to add .png extension in string
     */
    public static TextureArea fullImage(String imageLocation) {
        if (imageLocation.endsWith(".png"))
            throw new IllegalArgumentException(String.format(".png extension in %s string is not necessary.", imageLocation));
        return new TextureArea(new ResourceLocation(TJ.MODID, imageLocation + ".png"), 0, 0, 1, 1);
    }

    /**
     * @param imageLocation path of image under custom namespace. No need to add .png extension in string
     */
    public static TextureArea fullImage(String namespace, String imageLocation) {
        if (imageLocation.endsWith(".png"))
            throw new IllegalArgumentException(String.format(".png extension in %s string is not necessary.", imageLocation));
        return new TextureArea(new ResourceLocation(namespace, imageLocation + ".png"), 0, 0, 1, 1);
    }

    /**
     * @param imageLocation path of image under {@link TJ#MODID} namespace. No need to add .png extension in string
     */
    public static TextureArea adaptableImage(String imageLocation, int imageWidth, int imageHeight, int cornerWidth, int cornerHeight) {
        if (imageLocation.endsWith(".png"))
            throw new IllegalArgumentException(String.format(".png extension in %s string is not necessary.", imageLocation));
        return new AdoptableTextureArea(new ResourceLocation(TJ.MODID, imageLocation + ".png"), 0, 0, 1, 1, imageWidth,
                imageHeight, cornerWidth, cornerHeight);
    }
}

package dev.tr7zw.skinlayers;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * 1.7.10 uploads player skins as 64x32 textures, but 3D layer UVs are laid out for 64x64.
 * We upload the uncropped cached skin and bind that while drawing voxels.
 */
public final class FullSkinTextureManager {

    private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();

    private FullSkinTextureManager() {
    }

    public static boolean bindFullSkin(AbstractClientPlayer player) {
        String cacheKey = SkinUtil.getSkinCacheKey(player);
        BufferedImage image = SkinUtil.readFullSkinImage(player);
        if (image == null || cacheKey == null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(player.getLocationSkin());
            return false;
        }
        ResourceLocation location = TEXTURES.get(cacheKey);
        if (location == null) {
            DynamicTexture dynamicTexture = new DynamicTexture(toOpaqueSkin(image));
            location = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("skinlayers3d/" + cacheKey, dynamicTexture);
            TEXTURES.put(cacheKey, location);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        return true;
    }

    private static BufferedImage toOpaqueSkin(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage opaque = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                if (alpha == 0 && (red | green | blue) != 0) {
                    alpha = 255;
                }
                opaque.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
        return opaque;
    }

    public static void clearCache() {
        TEXTURES.clear();
    }

}

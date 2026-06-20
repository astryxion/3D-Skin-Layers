package dev.tr7zw.skinlayers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.accessor.SkullSettings;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import dev.tr7zw.skinlayers.opengl.NativeImage;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import dev.tr7zw.skinlayers.render.SolidPixelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class SkinUtil {

    private static Field skinCacheFileField;
    private static Field bufferedImageField;
    private static Field skinCacheDirField;
    private static Field fileAssetsField;

    static {
        skinCacheFileField = findField(ThreadDownloadImageData.class, "field_152434_e", "cacheFile");
        bufferedImageField = findField(ThreadDownloadImageData.class, "bufferedImage", "field_110560_d");
        skinCacheDirField = findField(SkinManager.class, "skinCacheDir", "field_152796_d");
        fileAssetsField = findField(Minecraft.class, "fileAssets", "field_110446_Y");
    }

    public static boolean hasCustomSkin(AbstractClientPlayer player) {
        return player.getLocationSkin() != null
                && !AbstractClientPlayer.locationStevePng.equals(player.getLocationSkin());
    }

    public static boolean hasThinArms(AbstractClientPlayer player) {
        try {
            Map<Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().getSessionService()
                    .getTextures(player.getGameProfile(), false);
            MinecraftProfileTexture texture = map.get(Type.SKIN);
            if (texture != null && texture.getMetadata("model") != null) {
                return "slim".equals(texture.getMetadata("model"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static double squareDistance(AbstractClientPlayer a, AbstractClientPlayer b) {
        double dx = a.posX - b.posX;
        double dy = a.posY - b.posY;
        double dz = a.posZ - b.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double squareDistance(EntityLivingBase entity, AbstractClientPlayer player) {
        double dx = entity.posX - player.posX;
        double dy = entity.posY - player.posY;
        double dz = entity.posZ - player.posZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private static NativeImage getSkinTexture(AbstractClientPlayer player) {
        BufferedImage image = readFullSkinImage(player);
        if (image != null) {
            return NativeImage.fromBufferedImage(image);
        }
        return getTexture(player.getLocationSkin());
    }

    public static BufferedImage readFullSkinImage(AbstractClientPlayer player) {
        if (player == null) {
            return null;
        }
        BufferedImage image = readFullSkinImageFromProfile(player.getGameProfile());
        if (image != null) {
            return image;
        }
        ResourceLocation skinLocation = player.getLocationSkin();
        if (skinLocation == null) {
            return null;
        }
        ITextureObject textureObject = Minecraft.getMinecraft().getTextureManager().getTexture(skinLocation);
        if (textureObject instanceof ThreadDownloadImageData) {
            ThreadDownloadImageData download = (ThreadDownloadImageData) textureObject;
            image = readCacheFile(download, skinLocation);
            if (image == null) {
                image = readBufferedImage(download);
            }
            if (image != null) {
                image = ensureFullSkinImage(image);
                if (image.getWidth() >= 64) {
                    return image;
                }
            }
        }
        return null;
    }

    public static String getSkinCacheKey(AbstractClientPlayer player) {
        if (player == null) {
            return null;
        }
        MinecraftProfileTexture texture = getProfileSkinTexture(player.getGameProfile());
        if (texture != null) {
            return texture.getHash();
        }
        return getSkinHash(player.getLocationSkin());
    }

    private static MinecraftProfileTexture getProfileSkinTexture(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        Map map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(profile);
        MinecraftProfileTexture texture = (MinecraftProfileTexture) map.get(Type.SKIN);
        if (texture != null) {
            return texture;
        }
        try {
            map = Minecraft.getMinecraft().getSessionService().getTextures(profile, false);
            return (MinecraftProfileTexture) map.get(Type.SKIN);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static BufferedImage readFullSkinImageFromProfile(GameProfile profile) {
        MinecraftProfileTexture texture = getProfileSkinTexture(profile);
        if (texture == null) {
            return null;
        }
        BufferedImage image = readCacheFileByHash(texture.getHash());
        if (image == null) {
            return null;
        }
        image = ensureFullSkinImage(image);
        if (image.getWidth() < 64) {
            return null;
        }
        return image;
    }

    private static NativeImage readSkinImageFromProfile(GameProfile profile) {
        BufferedImage image = readFullSkinImageFromProfile(profile);
        if (image == null) {
            return null;
        }
        return NativeImage.fromBufferedImage(image);
    }

    private static NativeImage getTexture(ResourceLocation resource) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(resource);
        ITextureObject textureObject = textureManager.getTexture(resource);
        if (textureObject == null) {
            return null;
        }
        if (textureObject instanceof ThreadDownloadImageData) {
            NativeImage skin = readDownloadedSkin((ThreadDownloadImageData) textureObject, resource);
            if (skin != null) {
                return skin;
            }
        }
        return readTextureFromGpu(textureObject);
    }

    private static NativeImage readDownloadedSkin(ThreadDownloadImageData texture, ResourceLocation resource) {
        BufferedImage image = readCacheFile(texture, resource);
        if (image == null) {
            image = readBufferedImage(texture);
        }
        if (image == null) {
            return null;
        }
        image = ensureFullSkinImage(image);
        if (image.getWidth() < 64) {
            return null;
        }
        return NativeImage.fromBufferedImage(image);
    }

    private static BufferedImage readCacheFile(ThreadDownloadImageData texture, ResourceLocation resource) {
        BufferedImage image = readDownloadCacheFile(texture);
        if (image != null) {
            return image;
        }
        return readSkinManagerCacheFile(resource);
    }

    private static BufferedImage readDownloadCacheFile(ThreadDownloadImageData texture) {
        if (skinCacheFileField == null) {
            return null;
        }
        try {
            File cacheFile = (File) skinCacheFileField.get(texture);
            if (cacheFile != null && cacheFile.isFile()) {
                return ImageIO.read(cacheFile);
            }
        } catch (IOException | IllegalAccessException ex) {
            return null;
        }
        return null;
    }

    private static BufferedImage readSkinManagerCacheFile(ResourceLocation resource) {
        String hash = getSkinHash(resource);
        if (hash == null || hash.length() < 2) {
            return null;
        }
        return readCacheFileByHash(hash);
    }

    private static BufferedImage readCacheFileByHash(String hash) {
        if (hash == null || hash.length() < 2) {
            return null;
        }
        File cacheDir = getSkinCacheDirectory();
        if (cacheDir == null) {
            return null;
        }
        try {
            File skinFile = new File(new File(cacheDir, hash.substring(0, 2)), hash);
            if (skinFile.isFile()) {
                return ImageIO.read(skinFile);
            }
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    private static File getSkinCacheDirectory() {
        if (skinCacheDirField != null) {
            try {
                File cacheDir = (File) skinCacheDirField.get(Minecraft.getMinecraft().getSkinManager());
                if (cacheDir != null) {
                    return cacheDir;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        if (fileAssetsField != null) {
            try {
                File assetsDir = (File) fileAssetsField.get(Minecraft.getMinecraft());
                if (assetsDir != null) {
                    return new File(assetsDir, "skins");
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return new File(new File(System.getenv("APPDATA"), ".minecraft"), "assets/skins");
    }

    private static String getSkinHash(ResourceLocation resource) {
        String path = resource.getResourcePath();
        if (path.startsWith("skins/")) {
            return path.substring("skins/".length());
        }
        return path;
    }

    private static BufferedImage readBufferedImage(ThreadDownloadImageData texture) {
        if (bufferedImageField == null) {
            return null;
        }
        try {
            return (BufferedImage) bufferedImageField.get(texture);
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    /**
     * 1.7.10 stores a cropped 64x32 image in memory. Modern overlay layers live in the lower half of a 64x64 skin.
     */
    private static BufferedImage ensureFullSkinImage(BufferedImage image) {
        if (image.getHeight() >= 64 && image.getWidth() >= 64) {
            return image;
        }
        if (image.getWidth() == 64 && image.getHeight() == 32) {
            BufferedImage expanded = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = expanded.getGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            return expanded;
        }
        return image;
    }

    private static NativeImage readTextureFromGpu(ITextureObject textureObject) {
        NativeImage skin = new NativeImage(64, 64, false);
        GlStateManager.bindTexture(textureObject.getGlTextureId());
        skin.downloadTexture(0, true);
        skin.flipY();
        return skin;
    }

    public static boolean setup3dLayers(AbstractClientPlayer abstractClientPlayerEntity, PlayerSettings settings,
            boolean thinArms, ModelBiped model) {
        if (!SkinUtil.hasCustomSkin(abstractClientPlayerEntity)) {
            settings.setupSkinLayers(null);
            settings.setupHeadLayers(null);
            return false;
        }
        settings.setupSkinLayers(null);
        settings.setupHeadLayers(null);
        NativeImage skin = SkinUtil.getSkinTexture(abstractClientPlayerEntity);
        if (skin == null) {
            settings.setupSkinLayers(null);
            settings.setupHeadLayers(null);
            return false;
        }
        boolean fullOverlay = skin.getHeight() >= 64;
        CustomizableModelPart headLayer = SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f);
        CustomizableModelPart[] layers = null;
        if (fullOverlay) {
            layers = new CustomizableModelPart[5];
            layers[0] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 48, true, 0f);
            layers[1] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 32, true, 0f);
            if (thinArms) {
                layers[2] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 48, 48, true, -2.5f);
                layers[3] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 40, 32, true, -2.5f);
            } else {
                layers[2] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 48, 48, true, -2.5f);
                layers[3] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 40, 32, true, -2.5f);
            }
            layers[4] = SolidPixelWrapper.wrapBox(skin, 8, 12, 4, 16, 32, true, -0.8f);
        }
        skin.close();
        if (headLayer == null && !hasAnyLayer(layers)) {
            settings.setupSkinLayers(null);
            settings.setupHeadLayers(null);
            return false;
        }
        if (hasAnyLayer(layers)) {
            settings.setupSkinLayers(layers);
        } else {
            settings.setupSkinLayers(null);
        }
        if (headLayer != null) {
            settings.setupHeadLayers(headLayer);
        }
        return headLayer != null || hasAnyLayer(layers);
    }

    private static boolean hasAnyLayer(CustomizableModelPart[] layers) {
        if (layers == null) {
            return false;
        }
        for (CustomizableModelPart layer : layers) {
            if (layer != null && !layer.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean setup3dLayers(GameProfile gameprofile, SkullSettings settings) {
        if (gameprofile == null) {
            return false;
        }
        NativeImage skin = readSkinImageFromProfile(gameprofile);
        if (skin == null) {
            Map map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(gameprofile);
            MinecraftProfileTexture texture = (MinecraftProfileTexture) map.get(MinecraftProfileTexture.Type.SKIN);
            if (texture == null) {
                return false;
            }
            skin = SkinUtil.getTexture(
                    Minecraft.getMinecraft().getSkinManager().loadSkin(texture, MinecraftProfileTexture.Type.SKIN));
        }
        if (skin == null) {
            return false;
        }
        CustomizableModelPart headLayer = SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f);
        skin.close();
        if (headLayer == null) {
            return false;
        }
        settings.setupHeadLayers(headLayer);
        return true;
    }

    public static GameProfile readSkullOwner(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound compoundTag = itemStack.getTagCompound();
        if (compoundTag.hasKey("SkullOwner", 10)) {
            return NBTUtil.readGameProfileFromNBT(compoundTag.getCompoundTag("SkullOwner"));
        }
        if (compoundTag.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(compoundTag.getString("SkullOwner"))) {
            GameProfile gameProfile = new GameProfile((UUID) null, compoundTag.getString("SkullOwner"));
            compoundTag.removeTag("SkullOwner");
            return gameProfile;
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

}

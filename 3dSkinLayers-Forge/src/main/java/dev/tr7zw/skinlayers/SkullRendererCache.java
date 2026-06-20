package dev.tr7zw.skinlayers;

import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.tr7zw.skinlayers.accessor.SkullSettings;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class SkullRendererCache {

    public static boolean renderNext = false;
    public static int pendingSkullType = -1;
    public static SkullSettings lastSkull = null;
    public static WeakHashMap<ItemStack, SkullSettings> itemCache = new WeakHashMap<>();
    
    public static class ItemSettings implements SkullSettings {

        private CustomizableModelPart hatModel = null;
        
        @Override
        public CustomizableModelPart getHeadLayers() {
            return hatModel;
        }

        @Override
        public void setupHeadLayers(CustomizableModelPart box) {
            this.hatModel = box;
        }
        
    }

    public static void prepareHelmetSkullEntity(EntityLivingBase livingEntity, ItemStack itemStack, GameProfile gameProfile) {
        if(!SkinLayersModBase.config.enableSkulls) {
            return;
        }
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if(player != null) {
            double dx = livingEntity.posX - player.posX;
            double dy = livingEntity.posY - player.posY;
            double dz = livingEntity.posZ - player.posZ;
            if(dx * dx + dy * dy + dz * dz > SkinLayersModBase.config.renderDistanceLOD*SkinLayersModBase.config.renderDistanceLOD) {
                return; // too far away
            }
        }
        prepareHelmetSkullItem(itemStack, gameProfile);
    }

    public static void prepareHelmetSkullItem(ItemStack itemStack, GameProfile gameProfile) {
        pendingSkullType = 3;
        if(!SkinLayersModBase.config.enableSkullsItems) {
            return;
        }
        if(gameProfile != null) {
            lastSkull = itemCache.computeIfAbsent(itemStack, it -> new ItemSettings());
            if(lastSkull.getHeadLayers() == null) {
                SkinUtil.setup3dLayers(gameProfile, lastSkull);
            }
            renderNext = lastSkull.getHeadLayers() != null;
        }
    }
    
}

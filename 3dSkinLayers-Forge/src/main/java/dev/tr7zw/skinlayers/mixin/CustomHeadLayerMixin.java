package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.SkullRendererCache;
import dev.tr7zw.skinlayers.SkullRendererCache.ItemSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

@Mixin(RenderBiped.class)
public class CustomHeadLayerMixin {

    @Inject(method = "renderEquippedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;func_152674_a(FFFIFILcom/mojang/authlib/GameProfile;)V", shift = At.Shift.BEFORE))
    public void beforeRenderSkullHelmet(EntityLiving livingEntity, float partialTicks, CallbackInfo info) {
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
        ItemStack itemStack = livingEntity.func_130225_q(3);
        if(itemStack == null || itemStack.getItem() != Items.skull) {
            return;
        }
        GameProfile gameProfile = null;
        if(itemStack.hasTagCompound()) {
            NBTTagCompound compoundTag = itemStack.getTagCompound();
            if(compoundTag.hasKey("SkullOwner", 10)) {
                gameProfile = NBTUtil.readGameProfileFromNBT(compoundTag.getCompoundTag("SkullOwner"));
            }
        }
        if(gameProfile != null) {
            SkullRendererCache.lastSkull = SkullRendererCache.itemCache.computeIfAbsent(itemStack, it -> new ItemSettings());
            if(SkullRendererCache.lastSkull.getHeadLayers() == null) {
                SkinUtil.setup3dLayers(gameProfile, SkullRendererCache.lastSkull);
            }
            SkullRendererCache.renderNext = SkullRendererCache.lastSkull.getHeadLayers() != null;
        }
    }
    
}

package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.SkullRendererCache;
import dev.tr7zw.skinlayers.SkullRendererCache.ItemSettings;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import dev.tr7zw.skinlayers.renderlayers.BodyLayerFeatureRenderer;
import dev.tr7zw.skinlayers.renderlayers.HeadLayerFeatureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

@Mixin(RenderPlayer.class)
public abstract class PlayerRendererMixin implements PlayerEntityModelAccessor {

    @Unique
    private boolean smallArms;
    @Unique
    private HeadLayerFeatureRenderer headLayer;
    @Unique
    private BodyLayerFeatureRenderer bodyLayer;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onCreate(CallbackInfo info) {
        headLayer = new HeadLayerFeatureRenderer((RenderPlayer)(Object)this);
        bodyLayer = new BodyLayerFeatureRenderer((RenderPlayer)(Object)this);
    }
    
    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    private void setModelProperties(AbstractClientPlayer abstractClientPlayer, double x, double y, double z, float yaw, float partialTicks, CallbackInfo info) {
        smallArms = SkinUtil.hasThinArms(abstractClientPlayer);
        ModelBiped playerModel = renderPlayer().modelBipedMain;
        playerModel.bipedHeadwear.isHidden = false;
        if (SkinUtil.squareDistance(Minecraft.getMinecraft().thePlayer, abstractClientPlayer) < SkinLayersModBase.config.renderDistanceLOD
                * SkinLayersModBase.config.renderDistanceLOD) {
            if (SkinLayersModBase.config.enableHat) {
                PlayerSettings settings = (PlayerSettings) abstractClientPlayer;
                CustomizableModelPart headLayers = settings.getHeadLayers();
                if (headLayers != null && headLayers.isEmpty()) {
                    settings.setupHeadLayers(null);
                    headLayers = null;
                }
                if (headLayers == null) {
                    SkinUtil.setup3dLayers(abstractClientPlayer, settings, smallArms, null);
                    headLayers = settings.getHeadLayers();
                }
                boolean hasFullSkin = SkinUtil.readFullSkinImage(abstractClientPlayer) != null;
                playerModel.bipedHeadwear.isHidden = hasFullSkin && headLayers != null && !headLayers.isEmpty();
            }
        }
    }
    
    
    
    @Override
    public HeadLayerFeatureRenderer getHeadLayer() {
        return headLayer;
    }

    @Override
    public BodyLayerFeatureRenderer getBodyLayer() {
        return bodyLayer;
    }

    @Override
    public boolean hasThinArms() {
        return smallArms;
    }
    
    @Inject(method = "renderEquippedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;func_152674_a(FFFIFILcom/mojang/authlib/GameProfile;)V", shift = At.Shift.BEFORE))
    private void beforeRenderSkullHelmet(AbstractClientPlayer livingEntity, float partialTicks, CallbackInfo info) {
        if(!SkinLayersModBase.config.enableSkulls) {
            return;
        }
        if(SkinUtil.squareDistance(Minecraft.getMinecraft().thePlayer, livingEntity) > SkinLayersModBase.config.renderDistanceLOD*SkinLayersModBase.config.renderDistanceLOD) {
            return; // too far away
        }
        ItemStack itemStack = livingEntity.inventory.armorItemInSlot(3);
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

    @Inject(method = "renderEquippedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;func_152674_a(FFFIFILcom/mojang/authlib/GameProfile;)V", shift = At.Shift.BEFORE))
    private void beforeRenderSkullItem(AbstractClientPlayer livingEntity, float partialTicks, CallbackInfo info) {
        ItemStack itemStack = livingEntity.inventory.armorItemInSlot(3);
        if(itemStack == null || itemStack.getItem() != Items.skull) {
            return;
        }
        GameProfile gameProfile = SkinUtil.readSkullOwner(itemStack);
        SkullRendererCache.prepareHelmetSkullItem(itemStack, gameProfile);
    }
    
    @Inject(method = "renderFirstPersonArm", at = @At("RETURN"))
    public void renderFirstPersonArm(EntityPlayer player, CallbackInfo info) {
        renderFirstPersonArmLayer((AbstractClientPlayer) player, 3);
    }
    
    private void renderFirstPersonArmLayer(AbstractClientPlayer player, int layerId) {
        ModelBiped modelplayer = renderPlayer().modelBipedMain;
        float pixelScaling = SkinLayersModBase.config.baseVoxelSize;
        PlayerSettings settings = (PlayerSettings) player;
        if(settings.getSkinLayers() == null && !setupModel(player, settings)) {
            return;
        }
        GlStateManager.pushMatrix();
        modelplayer.bipedRightArm.postRender(0.0625F);
        GlStateManager.scale(0.0625, 0.0625, 0.0625);
        GlStateManager.scale(pixelScaling, pixelScaling, pixelScaling);
        if(!smallArms) {
            settings.getSkinLayers()[layerId].x = -0.998f*16f;
        } else {
            settings.getSkinLayers()[layerId].x = -0.499f*16;
        }
        settings.getSkinLayers()[layerId].render(false);
        GlStateManager.popMatrix();
    }
    
    private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, PlayerSettings settings) {
        
        if(!SkinUtil.hasCustomSkin(abstractClientPlayerEntity)) {
            return false; // default skin
        }
        return SkinUtil.setup3dLayers(abstractClientPlayerEntity, settings, smallArms, null);
    }

    private RenderPlayer renderPlayer() {
        return (RenderPlayer)(Object)this;
    }
    
}

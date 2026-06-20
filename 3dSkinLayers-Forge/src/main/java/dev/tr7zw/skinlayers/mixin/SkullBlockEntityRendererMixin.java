package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.skinlayers.SkullRendererCache;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.SkullSettings;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.tileentity.TileEntitySkull;

@Mixin(TileEntitySkullRenderer.class)
public class SkullBlockEntityRendererMixin {

    @Inject(method = "renderTileEntityAt", at = @At("HEAD"))
    public void renderTileEntityAt(TileEntitySkull skullBlockEntity, double p_renderTileEntityAt_2_, double d1,
            double d2, float f1, CallbackInfo info) {
        SkullRendererCache.pendingSkullType = skullBlockEntity.getSkullType();
        if (!SkinLayersModBase.config.enableSkulls) {
            return;
        }
        if (skullBlockEntity.getSkullType() != 3) {
            return;
        }
        net.minecraft.client.entity.EntityClientPlayerMP player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }
        double dx = skullBlockEntity.xCoord + 0.5D - player.posX;
        double dy = skullBlockEntity.yCoord + 0.5D - player.posY;
        double dz = skullBlockEntity.zCoord + 0.5D - player.posZ;
        if (dx * dx + dy * dy + dz * dz < SkinLayersModBase.config.renderDistanceLOD * SkinLayersModBase.config.renderDistanceLOD) {
            SkullRendererCache.lastSkull = (SkullSettings) skullBlockEntity;
            if (SkullRendererCache.lastSkull.getHeadLayers() == null) {
                SkinUtil.setup3dLayers(skullBlockEntity.func_152108_a(), SkullRendererCache.lastSkull);
            }
            SkullRendererCache.renderNext = SkullRendererCache.lastSkull.getHeadLayers() != null;
        }
    }

}

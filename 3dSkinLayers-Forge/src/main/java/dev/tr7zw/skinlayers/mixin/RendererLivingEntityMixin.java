package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
public class RendererLivingEntityMixin {

    @Inject(method = "renderModel", at = @At("TAIL"))
    private void renderModelLayers(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        if (!(entity instanceof AbstractClientPlayer)) {
            return;
        }
        if (!(((Object) this) instanceof PlayerEntityModelAccessor)) {
            return;
        }
        PlayerEntityModelAccessor accessor = (PlayerEntityModelAccessor) (Object) this;
        if (accessor.getHeadLayer() == null || accessor.getBodyLayer() == null) {
            return;
        }
        AbstractClientPlayer player = (AbstractClientPlayer) entity;
        boolean visible = !player.isInvisible();
        boolean ghostVisible = !visible && !player.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
        if (!visible && !ghostVisible) {
            return;
        }
        if (ghostVisible) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.alphaFunc(516, 0.003921569F);
        }
        accessor.getHeadLayer().doRenderLayer(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                scale, scale);
        accessor.getBodyLayer().doRenderLayer(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                scale, scale);
        if (ghostVisible) {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
        }
    }

}

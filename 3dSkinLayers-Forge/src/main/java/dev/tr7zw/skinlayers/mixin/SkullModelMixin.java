package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tr7zw.skinlayers.SkullRendererCache;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.accessor.SkullModelAccessor;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.entity.Entity;

@Mixin(ModelSkeletonHead.class)
public class SkullModelMixin implements SkullModelAccessor {

    @Override
    public void showHat(boolean val) {
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderSkullLayers(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (SkullRendererCache.pendingSkullType != 3) {
            return;
        }
        SkullModelAccessor accessor = (SkullModelAccessor) (Object) this;
        if (!SkullRendererCache.renderNext) {
            accessor.showHat(true);
            SkullRendererCache.lastSkull = null;
            return;
        }
        accessor.showHat(false);

        if (SkullRendererCache.lastSkull == null || SkullRendererCache.lastSkull.getHeadLayers() == null) {
            SkullRendererCache.renderNext = false;
            SkullRendererCache.lastSkull = null;
            return;
        }
        float voxelSize = SkinLayersModBase.config.skullVoxelSize;
        GlStateManager.pushMatrix();
        GlStateManager.scale(voxelSize, voxelSize, voxelSize);
        SkullRendererCache.lastSkull.getHeadLayers().render(false);
        GlStateManager.popMatrix();
        SkullRendererCache.renderNext = false;
        SkullRendererCache.lastSkull = null;
    }

}

package dev.tr7zw.skinlayers.renderlayers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.tr7zw.skinlayers.FullSkinTextureManager;
import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import dev.tr7zw.skinlayers.opengl.RenderState;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;

public class BodyLayerFeatureRenderer {
    
    public enum PlayerModelPart {
        LEFT_PANTS_LEG, RIGHT_PANTS_LEG, LEFT_SLEEVE, RIGHT_SLEEVE, JACKET
    }
    
	private RenderPlayer playerRenderer;
	private static final Minecraft mc = Minecraft.getMinecraft();
	
    public BodyLayerFeatureRenderer(
            RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
            bodyLayers.add(new Layer(0, false, PlayerModelPart.LEFT_PANTS_LEG, Shape.LEGS, () -> playerRenderer.modelBipedMain.bipedLeftLeg, () -> SkinLayersModBase.config.enableLeftPants));
            bodyLayers.add(new Layer(1, false, PlayerModelPart.RIGHT_PANTS_LEG, Shape.LEGS, () -> playerRenderer.modelBipedMain.bipedRightLeg, () -> SkinLayersModBase.config.enableRightPants));
            bodyLayers.add(new Layer(2, false, PlayerModelPart.LEFT_SLEEVE, Shape.ARMS, () -> playerRenderer.modelBipedMain.bipedLeftArm, () -> SkinLayersModBase.config.enableLeftSleeve));
            bodyLayers.add(new Layer(3, true, PlayerModelPart.RIGHT_SLEEVE, Shape.ARMS, () -> playerRenderer.modelBipedMain.bipedRightArm, () -> SkinLayersModBase.config.enableRightSleeve));
            bodyLayers.add(new Layer(4, false, PlayerModelPart.JACKET, Shape.BODY, () -> playerRenderer.modelBipedMain.bipedBody, () -> SkinLayersModBase.config.enableJacket));
    }
    
    public void doRenderLayer(AbstractClientPlayer player, float paramFloat1, float paramFloat2, float paramFloat3,
            float deltaTick, float paramFloat5, float paramFloat6, float paramFloat7) {
        if (player.isInvisible() || !SkinUtil.hasCustomSkin(player)) {
            return;
        }
        if(mc.theWorld == null) {
            return; // in a menu or something and the model gets rendered
        }
        if(SkinUtil.squareDistance(mc.thePlayer, player) > SkinLayersModBase.config.renderDistanceLOD*SkinLayersModBase.config.renderDistanceLOD)return;
        
        PlayerSettings settings = (PlayerSettings) player;
        if (settings.getSkinLayers() != null && !hasBodyLayers(settings.getSkinLayers())) {
            settings.setupSkinLayers(null);
        }
        if (settings.getSkinLayers() == null || !hasBodyLayers(settings.getSkinLayers())) {
            if (!setupModel(player, settings)) {
                return;
            }
        }
        if (settings.getSkinLayers() == null || !hasBodyLayers(settings.getSkinLayers())) {
            return;
        }

        if (!FullSkinTextureManager.bindFullSkin(player)) {
            return;
        }
        RenderState.prepareTexturedModelDraw();
        renderLayers(player, settings.getSkinLayers(), deltaTick);
        RenderState.restoreTexturedModelDraw();
    }

    private boolean hasBodyLayers(CustomizableModelPart[] layers) {
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

    private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, PlayerSettings settings) {
        if(!SkinUtil.hasCustomSkin(abstractClientPlayerEntity)) {
            return false; // default skin
        }
        return SkinUtil.setup3dLayers(abstractClientPlayerEntity, settings, ((PlayerEntityModelAccessor) playerRenderer).hasThinArms(), null);
    }
    
    private final List<Layer> bodyLayers = new ArrayList<>();
    
    class Layer{
        int layersId;
        boolean mirrored;
        PlayerModelPart modelPart;
        Shape shape;
        Supplier<ModelRenderer> vanillaGetter;
        Supplier<Boolean> configGetter;
        public Layer(int layersId, boolean mirrored, PlayerModelPart modelPart, Shape shape,
                Supplier<ModelRenderer> vanillaGetter, Supplier<Boolean> configGetter) {
            this.layersId = layersId;
            this.mirrored = mirrored;
            this.modelPart = modelPart;
            this.shape = shape;
            this.vanillaGetter = vanillaGetter;
            this.configGetter = configGetter;
        }
        
    }
    
    
    private enum Shape {
        HEAD(0), BODY(0.6f), LEGS(-0.2f), ARMS(0.4f), ARMS_SLIM(0.4f)
        ;
        
        private final float yOffsetMagicValue;

        private Shape(float yOffsetMagicValue) {
            this.yOffsetMagicValue = yOffsetMagicValue;
        }

    }
    
    public void renderLayers(AbstractClientPlayer abstractClientPlayer, CustomizableModelPart[] layers, float deltaTick) {
        if(layers == null)return;
        float pixelScaling = SkinLayersModBase.config.baseVoxelSize;
        float heightScaling = 1.035f;
        float widthScaling = SkinLayersModBase.config.baseVoxelSize;
        // Overlay refuses to work correctly, this is a workaround for now
        boolean redTint = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0;
        boolean thinArms = ((PlayerEntityModelAccessor) playerRenderer).hasThinArms();
        for(Layer layer : bodyLayers) {
            if(isWearing(abstractClientPlayer, layer.modelPart) && !layer.vanillaGetter.get().isHidden && layer.configGetter.get()) {
                GlStateManager.pushMatrix();
                if(abstractClientPlayer.isSneaking()) {
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
                }
                layer.vanillaGetter.get().postRender(0.0625F);
                Shape effectiveShape = layer.shape;
                if (layer.modelPart == PlayerModelPart.LEFT_SLEEVE || layer.modelPart == PlayerModelPart.RIGHT_SLEEVE) {
                    effectiveShape = thinArms ? Shape.ARMS_SLIM : Shape.ARMS;
                }
                if (effectiveShape == Shape.ARMS) {
                    layers[layer.layersId].x = 0.998f * 16;
                } else if (effectiveShape == Shape.ARMS_SLIM) {
                    layers[layer.layersId].x = 0.499f * 16;
                }
                if (effectiveShape == Shape.BODY) {
                    widthScaling = SkinLayersModBase.config.bodyVoxelWidthSize;
                } else {
                    widthScaling = SkinLayersModBase.config.baseVoxelSize;
                }
                if (layer.mirrored) {
                    layers[layer.layersId].x *= -1;
                }
                GlStateManager.scale(0.0625, 0.0625, 0.0625);
                GlStateManager.scale(widthScaling, heightScaling, pixelScaling);
                layers[layer.layersId].y = effectiveShape.yOffsetMagicValue;
                
                layers[layer.layersId].render(redTint);
                GlStateManager.popMatrix();
            }
        }
        
    }

    public boolean shouldCombineTextures() {
        return false;
    }

    private boolean isWearing(AbstractClientPlayer player, PlayerModelPart modelPart) {
        return true;
    }
    
}

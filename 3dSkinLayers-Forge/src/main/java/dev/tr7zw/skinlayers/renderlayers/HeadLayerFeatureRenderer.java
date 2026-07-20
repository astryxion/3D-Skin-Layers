package dev.tr7zw.skinlayers.renderlayers;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.FullSkinTextureManager;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.compat.superhero.SuperheroArmorCompat;
import dev.tr7zw.skinlayers.opengl.GlStateManager;
import dev.tr7zw.skinlayers.opengl.RenderState;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HeadLayerFeatureRenderer {

	private Set<Item> hideHeadLayers = Sets.newHashSet(Items.skull);
	private static final Minecraft mc = Minecraft.getMinecraft();
	private RenderPlayer playerRenderer;
	
    public HeadLayerFeatureRenderer(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    public void doRenderLayer(AbstractClientPlayer player, float paramFloat1, float paramFloat2, float paramFloat3,
            float deltaTick, float paramFloat5, float paramFloat6, float paramFloat7) {
		if (player.isInvisible() || !SkinLayersModBase.config.enableHat || !SkinUtil.hasCustomSkin(player)) {
			return;
		}
		if (SuperheroArmorCompat.isWearingSuperheroSuit(player)) {
			return;
		}
		if(SkinUtil.squareDistance(mc.thePlayer, player) > SkinLayersModBase.config.renderDistanceLOD*SkinLayersModBase.config.renderDistanceLOD)return;
		
		ItemStack itemStack = player.inventory.armorItemInSlot(3);
		if (itemStack != null && hideHeadLayers.contains(itemStack.getItem())) {
			return;
		}
		
		PlayerSettings settings = (PlayerSettings) player;
		CustomizableModelPart headLayers = settings.getHeadLayers();
		if (headLayers != null && headLayers.isEmpty()) {
			settings.setupHeadLayers(null);
			headLayers = null;
		}
		if (headLayers == null) {
			if (!setupModel(player, settings)) {
				return;
			}
			headLayers = settings.getHeadLayers();
		}
		if (headLayers == null || headLayers.isEmpty()) {
			return;
		}

		renderCustomHelmet(settings, player, deltaTick);
	}

	private boolean setupModel(AbstractClientPlayer abstractClientPlayerEntity, PlayerSettings settings) {
		
		if(!SkinUtil.hasCustomSkin(abstractClientPlayerEntity)) {
			return false; // default skin
		}
		return SkinUtil.setup3dLayers(abstractClientPlayerEntity, settings, ((PlayerEntityModelAccessor) playerRenderer).hasThinArms(), null);
	}

	public void renderCustomHelmet(PlayerSettings settings, AbstractClientPlayer abstractClientPlayer, float deltaTick) {
		if(settings.getHeadLayers() == null || settings.getHeadLayers().isEmpty())return;
		if(playerRenderer.modelBipedMain.bipedHead.isHidden)return;
		if (!FullSkinTextureManager.bindFullSkin(abstractClientPlayer)) {
			return;
		}
		RenderState.prepareTexturedModelDraw();
		float voxelSize = SkinLayersModBase.config.headVoxelSize;
		GlStateManager.pushMatrix();
		if(abstractClientPlayer.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
		playerRenderer.modelBipedMain.bipedHead.postRender(0.0625F);
		//this.getParentModel().head.translateAndRotate(matrixStack);
	    GlStateManager.scale(0.0625, 0.0625, 0.0625);
		GlStateManager.scale(voxelSize, voxelSize, voxelSize);
		
		// Overlay refuses to work correctly, this is a workaround for now
		boolean tintRed = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0;
		settings.getHeadLayers().render(tintRed);
		RenderState.restoreTexturedModelDraw();
		GlStateManager.popMatrix();

	}

    public boolean shouldCombineTextures() {
        return false;
    }
	

}

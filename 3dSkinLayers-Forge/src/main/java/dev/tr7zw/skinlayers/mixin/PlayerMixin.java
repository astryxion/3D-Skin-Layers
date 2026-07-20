package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Keep player specific settings, data and modifies the eye location when enabled
 *
 */
@Mixin(EntityPlayer.class)
public abstract class PlayerMixin extends EntityLivingBase implements PlayerSettings {
    
	public PlayerMixin(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Unique
    private CustomizableModelPart headLayer;
    @Unique
	private CustomizableModelPart[] skinLayer;
    @Unique
	private Boolean thinArms;
	

	@Override
	public CustomizableModelPart[] getSkinLayers() {
		return skinLayer;
	}
	
	@Override
	public void setupSkinLayers(CustomizableModelPart[] box) {
		this.skinLayer = box;
		if (box == null) {
			this.thinArms = null;
		}
	}
	
	@Override
	public CustomizableModelPart getHeadLayers() {
		return headLayer;
	}
	
	@Override
	public void setupHeadLayers(CustomizableModelPart box) {
		this.headLayer = box;
	}

	@Override
	public Boolean getThinArms() {
		return thinArms;
	}

	@Override
	public void setThinArms(Boolean thinArms) {
		this.thinArms = thinArms;
	}
	
}

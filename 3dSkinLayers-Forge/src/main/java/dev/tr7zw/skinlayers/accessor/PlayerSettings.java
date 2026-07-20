package dev.tr7zw.skinlayers.accessor;

import dev.tr7zw.skinlayers.render.CustomizableModelPart;

public interface PlayerSettings {

	public CustomizableModelPart getHeadLayers();
	
	public void setupHeadLayers(CustomizableModelPart box);
	
	public CustomizableModelPart[] getSkinLayers();
	
	public void setupSkinLayers(CustomizableModelPart[] box);

	/**
	 * Arm model type used when the current body layers were built.
	 * {@code null} means layers have not been built yet / were cleared.
	 */
	public Boolean getThinArms();

	public void setThinArms(Boolean thinArms);

}

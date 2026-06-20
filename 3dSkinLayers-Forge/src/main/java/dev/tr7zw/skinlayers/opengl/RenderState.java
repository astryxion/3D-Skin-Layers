package dev.tr7zw.skinlayers.opengl;

import org.lwjgl.opengl.GL11;

public final class RenderState {

    private static boolean blendEnabled;

    private RenderState() {
    }

    public static void prepareTexturedModelDraw() {
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.enableTexture();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void restoreTexturedModelDraw() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (blendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
        }
    }

}

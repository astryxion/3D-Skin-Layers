package dev.tr7zw.skinlayers.opengl;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

public class GlStateManager {

    public static void _getTexImage(int i, int j, int k, int l, ByteBuffer m) {
        GL11.glGetTexImage(i, j, k, l, m);
    }
    
    public static void _pixelStore(int i, int j) {
        GL11.glPixelStorei(i, j);
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void translate(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    public static void scale(float x, float y, float z) {
        GL11.glScalef(x, y, z);
    }

    public static void scale(double x, double y, double z) {
        scale((float) x, (float) y, (float) z);
    }

    public static void color(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    public static void depthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void blendFunc(int srcFactor, int dstFactor) {
        GL11.glBlendFunc(srcFactor, dstFactor);
    }

    public static void alphaFunc(int func, float ref) {
        GL11.glAlphaFunc(func, ref);
    }

    public static void enableDepth() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void enableTexture() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

}

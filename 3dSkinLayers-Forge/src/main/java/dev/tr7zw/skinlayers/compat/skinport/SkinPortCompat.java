package dev.tr7zw.skinlayers.compat.skinport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;

/**
 * Soft compatibility with SkinPort, which selects slim/default player models
 * via its own skin providers instead of Mojang texture metadata.
 */
@SideOnly(Side.CLIENT)
public final class SkinPortCompat {

    private static final boolean LOADED;
    private static Method getSkinType;
    private static boolean resolved;

    static {
        LOADED = Loader.isModLoaded("skinport");
    }

    private SkinPortCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }

    /**
     * @return {@code true} for slim, {@code false} for classic, {@code null} if SkinPort
     *         is absent or has no answer yet
     */
    public static Boolean isSlimSkin(AbstractClientPlayer player) {
        if (!LOADED || player == null) {
            return null;
        }
        resolve();
        if (getSkinType == null) {
            return null;
        }
        try {
            String type = (String) getSkinType.invoke(null, player);
            if ("slim".equals(type)) {
                return Boolean.TRUE;
            }
            if ("default".equals(type) || "legacy".equals(type)) {
                return Boolean.FALSE;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Read {@code smallArms} from SkinPort's player model when the active renderer uses it.
     */
    public static Boolean getModelSmallArms(ModelBiped model) {
        if (!LOADED || model == null) {
            return null;
        }
        String name = model.getClass().getName();
        if (!name.contains("SkinPortModelPlayer") && !name.contains("skinport")) {
            return null;
        }
        try {
            Field field = model.getClass().getField("smallArms");
            return field.getBoolean(model);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> clientProxy = Class.forName("lain.mods.skinport.init.forge.ClientProxy");
            getSkinType = clientProxy.getMethod("getSkinType", AbstractClientPlayer.class);
        } catch (Throwable ignored) {
            getSkinType = null;
        }
    }
}

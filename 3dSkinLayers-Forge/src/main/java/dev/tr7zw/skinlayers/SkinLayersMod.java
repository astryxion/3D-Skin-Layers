package dev.tr7zw.skinlayers;


import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = "skinlayers3d", name = "3dSkinLayers", version = "@VER@", guiFactory = "dev.tr7zw.skinlayers.config.GuiFactory")
public class SkinLayersMod extends SkinLayersModBase {

    //Forge only
    private boolean onServer = false;
    
    public SkinLayersMod() {
        try {
            Class clientClass = net.minecraft.client.Minecraft.class;
        }catch(Throwable ex) {
            System.out.println("EntityCulling Mod installed on a Server. Going to sleep.");
            onServer = true;
            return;
        }
        onInitialize();
    }
    
    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    
}

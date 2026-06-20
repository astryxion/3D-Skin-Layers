package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import dev.tr7zw.skinlayers.SkullRendererCache;
import dev.tr7zw.skinlayers.SkinUtil;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@Mixin(RenderBiped.class)
public class BlockEntityWithoutLevelRendererMixin {

    @Inject(method = "renderEquippedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;func_152674_a(FFFIFILcom/mojang/authlib/GameProfile;)V", shift = At.Shift.BEFORE))
    public void beforeRenderSkullItem(EntityLiving livingEntity, float partialTicks, CallbackInfo info) {
        ItemStack itemStack = livingEntity.func_130225_q(3);
        if (itemStack == null || itemStack.getItem() != Items.skull) {
            return;
        }
        GameProfile gameProfile = SkinUtil.readSkullOwner(itemStack);
        SkullRendererCache.prepareHelmetSkullItem(itemStack, gameProfile);
    }
    
}

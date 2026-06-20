package dev.tr7zw.skinlayers.config;

import java.util.List;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;

@SideOnly(Side.CLIENT)
public class GuiButtonRowList extends GuiListExtended {
    private final List<Row> field_148184_k = Lists.newArrayList();

    public GuiButtonRowList(Minecraft p_i45015_1_, int p_i45015_2_, int p_i45015_3_, int p_i45015_4_, int p_i45015_5_,
            int p_i45015_6_, List<GuiButton> buttons) {
        super(p_i45015_1_, p_i45015_2_, p_i45015_3_, p_i45015_4_, p_i45015_5_, p_i45015_6_);
        this.field_148163_i = false;
        for (int lvt_8_1_ = 0; lvt_8_1_ < buttons.size(); lvt_8_1_ += 2) {
            buttons.get(lvt_8_1_).xPosition = p_i45015_2_ / 2 - 155;
            if(lvt_8_1_ < buttons.size() - 1) {
                buttons.get(lvt_8_1_+1).xPosition = p_i45015_2_ / 2 - 155 + 160;
            }
            this.field_148184_k.add(new Row(buttons.get(lvt_8_1_), (lvt_8_1_ < buttons.size() - 1) ? buttons.get(lvt_8_1_+1) : null));
        }
    }

    public Row getListEntry(int p_getListEntry_1_) {
        return this.field_148184_k.get(p_getListEntry_1_);
    }

    protected int getSize() {
        return this.field_148184_k.size();
    }

    public int getListWidth() {
        return 400;
    }

    protected int getScrollBarX() {
        return super.getScrollBarX() + 32;
    }
    
    public static class Row implements GuiListExtended.IGuiListEntry {
        private final Minecraft field_148325_a;

        private final GuiButton field_148323_b;

        private final GuiButton field_148324_c;

        public Row(GuiButton p_i45014_1_, GuiButton p_i45014_2_) {
            this.field_148325_a = Minecraft.getMinecraft();
            this.field_148323_b = p_i45014_1_;
            this.field_148324_c = p_i45014_2_;
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
            if (this.field_148323_b != null) {
                this.field_148323_b.yPosition = y;
                this.field_148323_b.drawButton(this.field_148325_a, mouseX, mouseY);
            }
            if (this.field_148324_c != null) {
                this.field_148324_c.yPosition = y;
                this.field_148324_c.drawButton(this.field_148325_a, mouseX, mouseY);
            }
        }

        public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (this.field_148323_b.mousePressed(this.field_148325_a, x, y)) {
                if (this.field_148323_b instanceof GuiOptionButton) {
                    this.field_148325_a.gameSettings
                            .setOptionValue(((GuiOptionButton) this.field_148323_b).returnEnumOptions(), 1);
                    this.field_148323_b.displayString = this.field_148325_a.gameSettings
                            .getKeyBinding(GameSettings.Options.getEnumOptions(this.field_148323_b.id));
                }
                return true;
            }
            if (this.field_148324_c != null
                    && this.field_148324_c.mousePressed(this.field_148325_a, x, y)) {
                return true;
            }
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (this.field_148323_b != null)
                this.field_148323_b.mouseReleased(x, y);
            if (this.field_148324_c != null)
                this.field_148324_c.mouseReleased(x, y);
        }

        public void setSelected(int p_setSelected_1_, int p_setSelected_2_, int p_setSelected_3_) {
        }
    }
}

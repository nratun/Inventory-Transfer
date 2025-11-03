package net.vyltx.inventory_transfer.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TargetOverlay extends AbstractWidget {
    private final Minecraft mc = Minecraft.getInstance();
    private PlayerInfo targetInfo;
    private final int leftPos, topPos;

    public TargetOverlay(PlayerInfo targetInfo, int leftPos, int topPos) {
        super(leftPos, topPos, 60, 14, Component.nullToEmpty(""));
        this.targetInfo = targetInfo;
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    public void setTarget(PlayerInfo targetInfo) {
        this.targetInfo = targetInfo;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (targetInfo == null) return;

        ResourceLocation skin = targetInfo.getSkinLocation();
        String name = targetInfo.getProfile().getName();

        // Draw face (base + overlay)
        graphics.blit(skin, this.leftPos, this.topPos, 8, 8, 8, 8, 64, 64);   // Base
        graphics.blit(skin, this.leftPos, this.topPos, 40, 8, 8, 8, 64, 64);  // Overlay
        // Draw name next to face
        graphics.drawString(mc.font, name, this.leftPos + 14, this.topPos, 0xFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationOutput) {
        if (targetInfo != null) {
            String name = targetInfo.getProfile().getName();
            narrationOutput.add(NarratedElementType.TITLE, Component.literal("Target: " + name));
        }
    }
}

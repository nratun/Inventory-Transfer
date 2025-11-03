package net.vyltx.inventory_transfer.client.screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.vyltx.inventory_transfer.InventoryTransfer;
import net.vyltx.inventory_transfer.event.ClientEvents;
import net.vyltx.inventory_transfer.networking.ModNetworking;
import net.vyltx.inventory_transfer.networking.packets.TargetSelectPacket;
import org.jetbrains.annotations.NotNull;

public class PlayerSelectScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen");
    private static final Component SELECT_BUTTON = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.button.select_button");
    private static final Component RESET_BUTTON = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.button.reset_button");
    private static final Component INPUT = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.editbox.input");

    // Used to render textures, currently using default minecraft texture
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(InventoryTransfer.MOD_ID, "textures/gui/player_select_screen.png");

    private final int imgWidth, imgHeight;
    private int leftPos, topPos;

    private Button selectButton;
    private Button resetButton;
    private EditBox textbox;

    public PlayerSelectScreen() {
        super(TITLE);
        this.imgWidth = 190;
        this.imgHeight = 85;
    }

    @Override
    protected void init() {
        super.init();

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        this.leftPos = (this.width - this.imgWidth) / 2;
        this.topPos = (this.height - this.imgHeight) / 2;

        this.selectButton = addRenderableWidget(
                Button.builder(
                                SELECT_BUTTON,
                                this::handleSelectButton
                        )
                        .bounds(this.leftPos + 130, this.topPos + 27, 48, 21)
                        .tooltip(Tooltip.create(SELECT_BUTTON))
                        .build()
        );
        this.resetButton = addRenderableWidget(
                Button.builder(
                                RESET_BUTTON,
                                this::handleResetButton
                        )
                        .bounds(this.leftPos + 12, this.topPos + 53, 166, 22)
                        .tooltip(Tooltip.create(RESET_BUTTON))
                        .build()
        );
        this.textbox = addRenderableWidget(
                new EditBox(this.font, this.leftPos + 12, this.topPos + 28, 110, 20, INPUT)
        );
        this.textbox.setMaxLength(16); // Names are not longer than 16 chars
        // Maybe add ScrollPanel later

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC key closes the screen even if textbox is focused
        if (keyCode == 256) { // 256 = GLFW.GLFW_KEY_ESCAPE
            this.onClose();
            return true;
        }

        if (keyCode == 257 || keyCode == 335) { // Enter / Numpad Enter
            this.handleSelectButton(this.selectButton);
            return true;
        }

        if (this.textbox.keyPressed(keyCode, scanCode, modifiers) || this.textbox.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.textbox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void handleSelectButton(Button button) {
        // Function that would deal with what happens when button is clicked
        String playerName = textbox.getValue().trim();
        if (playerName.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Please enter a player name."), true);
            return;
        }

        String callerName = Minecraft.getInstance().player.getGameProfile().getName();
        if (callerName.equalsIgnoreCase(playerName)) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Target can not be yourself, must be another player."), true);
            return;
        }

        // Send name to server for validation
        ModNetworking.sendToServer(new TargetSelectPacket(playerName));

        InventoryTransfer.LOGGER.info("Sent target selection request for '{}'", playerName);
        Minecraft.getInstance().setScreen(null); // close the GUI
    }

    private void handleResetButton(Button button) {
        // Function that would deal with what happens when you want to remove target
        ClientEvents.targetUUID = null;
        Minecraft.getInstance().setScreen(null); // close the GUI
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Render any backgrounds and stuff beforehand, because later lines layer on top of prev
        renderBackground(graphics); // Darkens background when GUI opened
        //renders from top-left first, unless you use diff rendering methods
        // Using TEXTURE allows you to put in a custom texture file for the menu
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imgWidth, this.imgHeight);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.textbox.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 12, 0x404040, false);
    }

    @Override
    public void tick() {
        super.tick();

        // Add ticking logic for EditBox in editBox
        this.textbox.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }
}

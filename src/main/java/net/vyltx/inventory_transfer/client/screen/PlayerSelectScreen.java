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

/**
 * PlayerSelectScreen
 * Creates a GUI where players can assign/modify their target player
 */
public class PlayerSelectScreen extends Screen {
    // Creating translatable components for multilingual support
    private static final Component TITLE = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen");
    private static final Component SELECT_BUTTON = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.button.select_button");
    private static final Component RESET_BUTTON = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.button.reset_button");
    private static final Component INPUT = Component.translatable("gui." + InventoryTransfer.MOD_ID + ".player_select_screen.editbox.input");

    // Used to render texture for menu
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

    /**
     * Adds various elements to the screen (Ex. buttons, textboxes)
     * <p>
     * Elements include:
     * selectButton - Button players press when they have selected their target
     * resetButton  - Button players press when they would like to remove their current target
     * textbox      - Text box players use to type in their desired target's name
     */
    @Override
    protected void init() {
        super.init();

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        // Used to place the GUI in the center of the display
        this.leftPos = (this.width - this.imgWidth) / 2;
        this.topPos = (this.height - this.imgHeight) / 2;

        // Create the components, assign dimensions & positions on the screen
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

    /**
     * Uses keyboard inputs as alternatives/shortcuts to navigating the screen
     * <p>
     * Registered inputs include:
     * ESC      - Closes the GUI
     * ENTER    - Performs same functionality as clicking the selectButton
     *
     * @return  boolean value indicating if one of the specified keys is pressed
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC key closes the screen even if textbox is focused
        if (keyCode == 256) { // Escape key
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

    /* Don't think I need this at the moment
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.textbox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    */

    /**
     * Determines if a target is valid after the selectButton is clicked (or if the ENTER key is pressed)
     * <p>
     * Validity is checked using the following methods:
     * Check to see if the textbox contains any text
     * Ensure the target is not the player themselves
     * Sending a packet to ensure the target player exists and is currently online
     *
     * @param button  assigns functionality to a button
     */
    private void handleSelectButton(Button button) {
        // Check if textbox is empty
        String playerName = textbox.getValue().trim();
        if (playerName.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Please enter a player name."), true);
            return;
        }

        // Check if the target is yourself
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

    /**
     * Removes the player's target after the resetButton is clicked
     *
     * @param button  assigns functionality to a button
     */
    private void handleResetButton(Button button) {
        ClientEvents.targetUUID = null;
        Minecraft.getInstance().setScreen(null); // close the GUI
    }

    //FIXME finish documentation for render
    /**
     * Determines if a target is valid after the selectButton is clicked (or if the ENTER key is pressed)
     * <p>
     * Validity is checked using the following methods:
     * Check to see if the textbox contains any text
     * Ensure the target is not the player themselves
     * Sending a packet to ensure the target player exists and is currently online
     *
     * @param graphics      The
     * @param mouseX        The x position of the mouse
     * @param mouseY        The y position of the mouse
     * @param partialTicks  The
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Render any backgrounds and stuff beforehand, because later lines layer on top of prev
        renderBackground(graphics); // Darkens background when GUI opened
        //renders from top-left first, unless you use diff rendering methods
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imgWidth, this.imgHeight);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.textbox.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 12, 0x404040, false);
    }

    /**
     * Enables blinking on the textbox cursor
     */
    @Override
    public void tick() {
        super.tick();
        this.textbox.tick();
    }

    /**
     * Ensures the screen is not able to pause the game
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Removes the screen once it is closed
     */
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }
}

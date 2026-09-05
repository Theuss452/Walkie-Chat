package com.Theus452.walkietalkie.client;

import com.Theus452.walkietalkie.networking.packet.PacketSetBlockFrequency;
import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class WalkieTalkieBlockScreen extends Screen {
    private EditBox frequencyBox;
    private final BlockPos pos;
    private final String initialFrequency;

    public WalkieTalkieBlockScreen(BlockPos pos, String initialFrequency) {
        super(Component.translatable("message.walkietalkie.title.screen"));
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.initialFrequency = initialFrequency == null ? "" : initialFrequency;
    }

    @Override
    protected void init() {
        super.init();
        int boxWidth = 150;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = this.height / 2 - 10;

        this.frequencyBox = new EditBox(this.font, boxX, boxY, boxWidth, 20, Component.empty());
        this.frequencyBox.setFilter(text -> text.isEmpty() || text.matches("[0-9]*"));
        this.frequencyBox.setValue(this.initialFrequency);
        this.addRenderableWidget(this.frequencyBox);

        this.addRenderableWidget(Button.builder(Component.translatable("message.walkietalkie.save.frequency"), this::onSave)
                .bounds(boxX, boxY + 25, boxWidth, 20)
                .build());

        this.setInitialFocus(this.frequencyBox);
        this.frequencyBox.setFocused(true);
    }

    private void onSave(Button button) {
        String text = this.frequencyBox.getValue();

        if (text.isEmpty()) {
            if (this.minecraft != null) this.minecraft.setScreen(null);
            return;
        }

        int frequency;
        try {
            frequency = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            frequency = 1000;
        }

        if (frequency > 1000) {
            frequency = 1000;
        } else if (frequency < 1) {
            frequency = 1;
        }

        Platform.getHelper().sendToServer(new PacketSetBlockFrequency(String.valueOf(frequency), this.pos));

        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        if (this.frequencyBox.getValue().isEmpty() && !this.frequencyBox.isFocused()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.walkietalkie.frequency.suggestion"),
                    this.frequencyBox.getX() + 5,
                    this.frequencyBox.getY() + (this.frequencyBox.getHeight() - 8) / 2,
                    ChatFormatting.DARK_GRAY.getColor());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

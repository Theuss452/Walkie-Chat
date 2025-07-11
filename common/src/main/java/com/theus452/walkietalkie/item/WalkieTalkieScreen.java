package com.theus452.walkietalkie.item;

import com.theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.theus452.walkietalkie.platform.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class WalkieTalkieScreen extends Screen {

    private EditBox frequencyBox;
    private final InteractionHand hand;

    public WalkieTalkieScreen(InteractionHand hand) {
        super(Component.translatable("message.walkietalkie.title.screen"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();
        int boxWidth = 150;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = this.height / 2 - 10;

        this.frequencyBox = new EditBox(this.font, boxX, boxY, boxWidth, 20, Component.empty());
        this.frequencyBox.setFilter(text -> text.isEmpty() || text.matches("[0-9]*"));
        this.addWidget(this.frequencyBox);


        this.addRenderableWidget(new Button(boxX, boxY + 25, boxWidth, 20,
                Component.translatable("message.walkietalkie.save.frequency"), this::onSave));

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack heldItem = this.minecraft.player.getItemInHand(this.hand);
            if (heldItem.getItem() instanceof WalkieTalkieItem) {
                this.frequencyBox.setValue(WalkieTalkieItem.getFrequency(heldItem));
            }
        }
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

        Platform.HELPER.sendToServer(new PacketSetFrequency(String.valueOf(frequency), this.hand));

        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);

        this.frequencyBox.render(poseStack, mouseX, mouseY, partialTick);

        if (this.frequencyBox.getValue().isEmpty() && !this.frequencyBox.isFocused()) {
            int color = Objects.requireNonNull(ChatFormatting.DARK_GRAY.getColor());
            drawString(poseStack, this.font,
                    Component.translatable("gui.walkietalkie.frequency.suggestion"),
                    this.frequencyBox.x + 5,
                    this.frequencyBox.y + (this.frequencyBox.getHeight() - 8) / 2,
                    color);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
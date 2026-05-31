package com.Theus452.walkietalkie.item;

import com.Theus452.walkietalkie.client.ChannelCache;
import com.Theus452.walkietalkie.client.ChannelMessageCache;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.platform.Platform;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WalkieTalkieScreen extends Screen {

    private static final int PANEL_W  = 320;
    private static final int PANEL_H  = 200;
    private static final int HEADER_H = 34;
    private static final int TAB_H    = 20;
    private static final int FOOTER_H = 26;

    private static final int C_BG           = 0xFF080E09;
    private static final int C_PANEL        = 0xFF0D1510;
    private static final int C_DEEP         = 0xFF050A06;
    private static final int C_BORDER       = 0xFF1E3D20;
    private static final int C_BORDER_GLOW  = 0xFF2A5A2D;
    private static final int C_ACCENT       = 0xFF1E9940;
    private static final int C_TAB_ACTIVE   = 0xFF132418;
    private static final int C_TAB_BG       = 0xFF080E09;
    private static final int C_TEXT         = 0xFFB8EEBB;
    private static final int C_TEXT_DIM     = 0xFF517A53;

    private enum Tab { FREQUENCY, CHANNELS, CHAT }
    private Tab currentTab = Tab.FREQUENCY;
    private final InteractionHand hand;
    private String freqInput = "";
    private EditBox chatInput;
    private int scrollOffset = 0;
    private long openTime;
    private long txFlashEnd = 0L;
    private String myName = "";
    private boolean active = false;

    private final List<FormattedCharSequence> cachedLines = new ArrayList<>();
    private final List<Boolean> cachedIsMine = new ArrayList<>();
    private String cachedFreq = "";

    public WalkieTalkieScreen(InteractionHand hand) {
        super(Component.translatable("gui.walkietalkie.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        this.openTime = System.currentTimeMillis();
        if (minecraft != null && minecraft.player != null) {
            myName = minecraft.player.getName().getString();
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if (stack.getItem() instanceof WalkieTalkieItem) {
                String cur = WalkieTalkieItem.getFrequency(stack);
                freqInput = cur;
                active = !cur.isEmpty();
                if (active) {
                    currentTab = Tab.CHAT;
                }
            }
        }

        int px = panelX(), py = panelY();
        chatInput = new EditBox(font, px + 10, py + PANEL_H - FOOTER_H + 6, PANEL_W - 20, 14, Component.literal(""));
        chatInput.setMaxLength(100);
        chatInput.setBordered(false);
        chatInput.setTextColor(0xFFAAFFBB);
        chatInput.setResponder(s -> {});
        addRenderableWidget(chatInput);
        
        chatInput.visible = (currentTab == Tab.CHAT && active);
        setInitialFocus(chatInput);
    }

    private int panelX() { return this.width  / 2 - PANEL_W / 2; }
    private int panelY() { return this.height / 2 - PANEL_H / 2; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int px = panelX(), py = panelY();

        drawPanel(g, px, py);
        drawHeader(g, px, py, mx, my);
        
        drawTabs(g, px, py);

        int contentY = py + HEADER_H + TAB_H;
        int contentH = PANEL_H - HEADER_H - TAB_H - FOOTER_H;

        chatInput.visible = (currentTab == Tab.CHAT);

        switch (currentTab) {
            case FREQUENCY -> drawFrequencyTab(g, px, contentY, contentH, mx, my);
            case CHANNELS  -> drawChannelsTab(g, px, py, contentY, contentH);
            case CHAT      -> drawChatTab(g, px, contentY, contentH);
        }

        if (currentTab == Tab.CHAT) {
            int fy = py + PANEL_H - FOOTER_H;
            g.fill(px + 2, fy, px + PANEL_W - 2, py + PANEL_H - 2, C_DEEP);
            g.fill(px + 5, fy + 4, px + PANEL_W - 5, fy + 5, C_BORDER);
        }

        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g, int px, int py) {
        g.fill(px + 3, py + 3, px + PANEL_W + 3, py + PANEL_H + 3, 0x55000000);
        g.fill(px, py, px + PANEL_W, py + PANEL_H, C_BG);
        g.fill(px, py, px + PANEL_W, py + 1, C_BORDER_GLOW);
        g.fill(px, py + PANEL_H - 1, px + PANEL_W, py + PANEL_H, C_BORDER_GLOW);
        g.fill(px, py, px + 1, py + PANEL_H, C_BORDER_GLOW);
        g.fill(px + PANEL_W - 1, py, px + PANEL_W, py + PANEL_H, C_BORDER_GLOW);
        
        g.fill(px + 1, py + 1, px + PANEL_W - 1, py + 2, C_BORDER);
        g.fill(px + 1, py + PANEL_H - 2, px + PANEL_W - 1, py + PANEL_H - 1, C_BORDER);
        g.fill(px + 1, py + 1, px + 2, py + PANEL_H - 1, C_BORDER);
        g.fill(px + PANEL_W - 2, py + 1, px + PANEL_W - 1, py + PANEL_H - 1, C_BORDER);
    }

    private void drawHeader(GuiGraphics g, int px, int py, int mx, int my) {
        long now = System.currentTimeMillis();

        int hx = px + 2, hy = py + 2, hw = PANEL_W - 4, hh = HEADER_H - 2;
        g.fill(hx, hy, hx + hw, hy + hh, C_PANEL);

        float scanPct = (now % 1800) / 1800f;
        int scanY = hy + (int)(scanPct * hh);
        g.fill(hx, scanY, hx + hw, scanY + 1, 0x1A44FF44);

        g.drawString(font, "§2▐ §aWalkie-Talkie", hx + 6, hy + 4, C_TEXT, false);

        boolean blink = (now / 600) % 2 == 0;
        g.drawString(font, (blink ? "§a⬤ " : "§2⬤ ") + "ON", hx + 10, hy + 16, 0xFFFFFFFF, false);

        String freqStr = freqInput.isEmpty() ? "- - - -" : freqInput + " MHz";
        g.drawCenteredString(font, "§a" + freqStr, px + PANEL_W / 2, hy + 4, 0xFFEEFFEE);

        LocalTime lt = LocalTime.now();
        String clock = String.format("%02d:%02d", lt.getHour(), lt.getMinute());
        g.drawString(font, clock, px + PANEL_W - 40, hy + 16, C_TEXT_DIM, false);

        if (now < txFlashEnd) {
            boolean txBlink = (now / 150) % 2 == 0;
            g.drawString(font, Component.literal(txBlink ? "§c" : "§4").append("TX"), px + PANEL_W - 45, hy + 4, 0xFFFFFFFF, false);
        }
    }

    private void drawTabs(GuiGraphics g, int px, int py) {
        int ty = py + HEADER_H;
        int tw = PANEL_W / 3;
        String[] keys = { "FREQ", "CHANNELS", "CHAT" };

        for (int i = 0; i < 3; i++) {
            Tab t = Tab.values()[i];
            int tx = px + 2 + i * (tw - 1);
            boolean isTabActive = currentTab == t;

            g.fill(tx, ty, tx + tw - 2, ty + TAB_H, isTabActive ? C_TAB_ACTIVE : C_TAB_BG);
            if (isTabActive) g.fill(tx, ty, tx + tw - 2, ty + 1, C_ACCENT);
            
            g.drawCenteredString(font, (isTabActive ? "§a" : "§8") + keys[i], tx + tw / 2 - 1, ty + 6, isTabActive ? 0xFFBBFFBB : 0xFF3A5A3A);
        }
    }

    private void drawFrequencyTab(GuiGraphics g, int px, int contentY, int contentH, int mx, int my) {
        long now = System.currentTimeMillis();
        int cx = px + PANEL_W / 2;
        int midY = contentY + contentH / 2;

        int lx = cx - 80, lw = 160, lh = 58;
        int ly = midY - lh / 2 - 10;

        g.fill(lx + 2, ly + 2, lx + lw + 2, ly + lh + 2, 0x44000000);
        g.fill(lx, ly, lx + lw, ly + lh, C_DEEP);
        g.fill(lx, ly, lx + lw, ly + 1, C_BORDER);
        g.fill(lx, ly + lh - 1, lx + lw, ly + lh, C_BORDER);
        g.fill(lx, ly, lx + 1, ly + lh, C_BORDER);
        g.fill(lx + lw - 1, ly, lx + lw, ly + lh, C_BORDER);
        g.fill(lx + 1, ly + 1, lx + lw - 1, ly + 2, 0x22AAFFAA);

        int scanY = ly + 2 + (int)(((now % 1200) / 1200.0) * (lh - 4));
        g.fill(lx + 2, scanY, lx + lw - 2, scanY + 1, 0x1544FF44);

        g.drawCenteredString(font, "§8Input Frequency", cx, ly - 13, C_TEXT_DIM);

        String d1 = freqInput.length() > 0 ? String.valueOf(freqInput.charAt(0)) : "_";
        String d2 = freqInput.length() > 1 ? String.valueOf(freqInput.charAt(1)) : "_";
        String d3 = freqInput.length() > 2 ? String.valueOf(freqInput.charAt(2)) : "_";

        g.drawCenteredString(font, "§a§l" + d1, cx - 16, ly + 20, 0xFFDDFFDD);
        g.drawCenteredString(font, "§a§l" + d2, cx, ly + 20, 0xFFDDFFDD);
        g.drawCenteredString(font, "§a§l" + d3, cx + 16, ly + 20, 0xFFDDFFDD);

        boolean blink = (now / 500) % 2 == 0;
        if (blink && freqInput.length() < 3) {
            int cursorX = cx - 16 + freqInput.length() * 16;
            g.drawCenteredString(font, "§2|", cursorX, ly + 10, 0xFFDDFFDD);
        }

        g.drawCenteredString(font, "§2MHz", cx, ly + 34, 0xFF4A7A4A);

        int btnW = 80;
        int btnH = 18;
        int btnX = cx - btnW / 2;
        int btnY = ly + lh + 10;

        boolean isHovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
        boolean isEnabled = !freqInput.isEmpty();

        int btnBg = isEnabled ? (isHovered ? 0xFF18301C : C_TAB_ACTIVE) : C_DEEP;
        int btnBorder = isEnabled ? (isHovered ? C_ACCENT : C_BORDER) : 0xFF152517;
        int btnTextColor = isEnabled ? (isHovered ? 0xFFFFFFFF : 0xFFBBFFBB) : 0xFF304530;

        g.fill(btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1, btnBg);
        g.fill(btnX, btnY, btnX + btnW, btnY + 1, btnBorder);
        g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, btnBorder);
        g.fill(btnX, btnY, btnX + 1, btnY + btnH, btnBorder);
        g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, btnBorder);

        g.drawCenteredString(font, (isEnabled ? (isHovered ? "§a§l" : "§a") : "§8") + "CONNECT", cx, btnY + 5, btnTextColor);
    }

    private final java.util.List<ChannelRow> channelRows = new java.util.ArrayList<>();
    private record ChannelRow(int y, String frequency) {}

    private void drawChannelsTab(GuiGraphics g, int px, int py, int contentY, int contentH) {
        int cx = px + PANEL_W / 2;
        List<PacketSyncChannels.ChannelInfo> channels = ChannelCache.get();
        channelRows.clear();

        g.drawCenteredString(font, "Active Channels", cx, contentY + 6, C_TEXT_DIM);
        g.drawCenteredString(font, "Click to Join", cx, contentY + 16, 0xFF3A5A3A);

        if (channels.isEmpty()) {
            g.drawCenteredString(font, "No channels found", cx, contentY + contentH / 2, C_TEXT_DIM);
            return;
        }

        int rowY = contentY + 28;
        int listX = px + 10;
        int rowW = PANEL_W - 20;
        for (int i = 0; i < Math.min(10, channels.size()); i++) {
            PacketSyncChannels.ChannelInfo info = channels.get(i);
            boolean isCurrent = info.frequency().equals(freqInput);
            channelRows.add(new ChannelRow(rowY, info.frequency()));

            if (isCurrent) {
                g.fill(listX, rowY - 1, listX + rowW, rowY + 10, 0x2244FF44);
            }

            String prefix = isCurrent ? "§a▶ " : "§7  ";
            g.drawString(font, Component.literal(prefix + "CH " + info.frequency()), listX + 2, rowY, 0xFFFFFFFF, false);
            g.drawString(font, "Users: " + info.playerCount(), listX + 115, rowY, 0xFFFFFFFF, false);

            rowY += 13;
        }
    }

    private int lastMessageCount = 0;

    private void drawChatTab(GuiGraphics g, int px, int contentY, int contentH) {
        if (freqInput.isEmpty()) {
            g.drawCenteredString(font, "Join a channel first", px + PANEL_W / 2, contentY + contentH / 2, C_TEXT_DIM);
            return;
        }

        List<ChannelMessageCache.ChatEntry> channelMsgs = ChannelMessageCache.getRecent(freqInput, 50);

        if (channelMsgs.isEmpty()) {
            g.drawCenteredString(font, "No messages yet", px + PANEL_W / 2, contentY + contentH / 2, 0xFF223322);
            return;
        }

        if (cachedLines.isEmpty() || !cachedFreq.equals(freqInput) || channelMsgs.size() != lastMessageCount) {
            List<ChannelMessageCache.ChatEntry> merged = new ArrayList<>(channelMsgs);
            merged.sort(java.util.Comparator.comparingLong(ChannelMessageCache.ChatEntry::timestampMs));
            if (merged.size() > 50) merged = merged.subList(merged.size() - 50, merged.size());
            
            updateMessageCache(merged);
            cachedFreq = freqInput;
            lastMessageCount = channelMsgs.size();
        }

        int visibleLines = (contentH - 10) / 10;
        scrollOffset = Mth.clamp(scrollOffset, 0, Math.max(0, cachedLines.size() - visibleLines));

        int renderY = contentY + contentH - 12;
        for (int i = cachedLines.size() - 1 - scrollOffset; i >= 0 && renderY > contentY + 5; i--) {
            FormattedCharSequence line = cachedLines.get(i);
            boolean isMine = cachedIsMine.get(i);
            int lx = isMine ? (px + PANEL_W - font.width(line) - 15) : (px + 15);
            
            if (isMine) g.fill(lx - 2, renderY - 1, lx + font.width(line) + 2, renderY + 9, 0x3344FF44);
            
            g.drawString(font, line, lx, renderY, isMine ? 0xFFAAFFBB : 0xFFBBCCAA, false);
            renderY -= 10;
        }
    }

    private void updateMessageCache(List<ChannelMessageCache.ChatEntry> messages) {
        cachedLines.clear();
        cachedIsMine.clear();
        int maxW = PANEL_W - 40;
        for (ChannelMessageCache.ChatEntry entry : messages) {
            String prefix = entry.senderName().equals(myName) ? "" : entry.senderName() + ": ";
            List<FormattedCharSequence> split = font.split(Component.literal(prefix + entry.message()), maxW);
            for (FormattedCharSequence s : split) {
                cachedLines.add(s);
                cachedIsMine.add(entry.senderName().equals(myName));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int px = panelX(), py = panelY();
        int ty = py + HEADER_H, tw = PANEL_W / 3;

        if (my >= ty && my <= ty + TAB_H) {
            for (int i = 0; i < 3; i++) {
                if (mx >= px + 2 + i * (tw - 1) && mx <= px + 2 + (i + 1) * (tw - 1)) {
                    currentTab = Tab.values()[i];
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0f);
                    if (currentTab == Tab.CHAT) chatInput.setFocused(true);
                    return true;
                }
            }
        }

        if (currentTab == Tab.FREQUENCY && btn == 0) {
            int cx = px + PANEL_W / 2;
            int midY = py + HEADER_H + TAB_H + (PANEL_H - HEADER_H - TAB_H - FOOTER_H) / 2;
            int lh = 58;
            int ly = midY - lh / 2 - 10;
            int btnW = 80;
            int btnH = 18;
            int btnX = cx - btnW / 2;
            int btnY = ly + lh + 10;

            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                if (!freqInput.isEmpty()) {
                    Platform.getHelper().sendToServer(new PacketSetFrequency(freqInput, hand));
                    currentTab = Tab.CHAT;
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0f);
                }
                return true;
            }
        }

        if (currentTab == Tab.CHANNELS && btn == 0) {
            for (ChannelRow row : channelRows) {
                if (my >= row.y() - 1 && my <= row.y() + 11) {
                    freqInput = row.frequency();
                    Platform.getHelper().sendToServer(new PacketSetFrequency(freqInput, hand));
                    currentTab = Tab.CHAT;
                    chatInput.setFocused(true);
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0f);
                    return true;
                }
            }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (currentTab == Tab.FREQUENCY) {
            if ((key >= 48 && key <= 57) || (key >= 320 && key <= 329)) {
                if (freqInput.length() < 3) {
                    int digit = (key >= 320) ? key - 320 : key - 48;
                    if (digit == 0 && freqInput.isEmpty()) {
                        return true;
                    }
                    freqInput += digit;
                }
                return true;
            }
            if (key == 259 && !freqInput.isEmpty()) { freqInput = freqInput.substring(0, freqInput.length() - 1); return true; }
            if (key == 257 || key == 335) {
                if (!freqInput.isEmpty()) {
                    Platform.getHelper().sendToServer(new PacketSetFrequency(freqInput, hand));
                    currentTab = Tab.CHAT;
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0f);
                }
                return true;
            }
        } else if (currentTab == Tab.CHAT) {
            if (key == 257 || key == 335) {
                String msg = chatInput.getValue().trim();
                if (!msg.isEmpty() && minecraft != null && minecraft.player != null) {
                    minecraft.player.connection.sendChat(msg);
                    chatInput.setValue("");
                    txFlashEnd = System.currentTimeMillis() + 800;
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_SEND_MSG.get(), 0.6f);
                }
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    private void playSfx(net.minecraft.sounds.SoundEvent se, float vol) {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(se, vol));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
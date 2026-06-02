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

    private enum Tab { FREQUENCY, CHANNELS, CHAT, INFO }
    private Tab currentTab = Tab.FREQUENCY;
    private final InteractionHand hand;
    private String freqInput = "";
    private EditBox chatInput;
    private int scrollOffset = 0;
    private long openTime;
    private String myName = "";
    private boolean active = false;
    private boolean isDraggingScrollbar = false;
    private int channelsScrollOffset = 0;
    private boolean isDraggingChannelsScrollbar = false;

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
        this.scrollOffset = 0;
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
        chatInput = new EditBox(font, px + 10, py + PANEL_H - FOOTER_H + 9, PANEL_W - 20, 14, Component.literal(""));
        chatInput.setMaxLength(100);
        chatInput.setBordered(false);
        chatInput.setTextColor(0xFFAAFFBB);
        chatInput.setResponder(s -> {});
        addRenderableWidget(chatInput);
        
        chatInput.visible = (currentTab == Tab.CHAT && active);
        setInitialFocus(chatInput);
    }

    @Override
    public void tick() {
        super.tick();
        if (chatInput != null) {
            chatInput.tick();
        }
    }

    private int panelX() { return this.width  / 2 - PANEL_W / 2; }
    private int panelY() { return this.height / 2 - PANEL_H / 2; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        int px = panelX(), py = panelY();

        drawPanel(g, px, py);
        drawHeader(g, px, py, mx, my);
        
        drawTabs(g, px, py, mx, my);

        int contentY = py + HEADER_H + TAB_H;
        int contentH = PANEL_H - HEADER_H - TAB_H - FOOTER_H;

        chatInput.visible = (currentTab == Tab.CHAT && active);

        switch (currentTab) {
            case FREQUENCY -> drawFrequencyTab(g, px, contentY, contentH, mx, my);
            case CHANNELS  -> drawChannelsTab(g, px, py, contentY, contentH, mx, my);
            case CHAT      -> drawChatTab(g, px, contentY, contentH, mx, my);
            case INFO      -> drawInfoTab(g, px, contentY, contentH, mx, my);
        }

        if (currentTab == Tab.CHAT && active) {
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
    }

    private void drawTabs(GuiGraphics g, int px, int py, int mx, int my) {
        int ty = py + HEADER_H;
        int tw = PANEL_W / 2;

        String[] keys = !active ? new String[]{ "FREQ", "CHANNELS" } : new String[]{ "CHAT", "INFO" };
        Tab[] tabs = !active ? new Tab[]{ Tab.FREQUENCY, Tab.CHANNELS } : new Tab[]{ Tab.CHAT, Tab.INFO };
        
        for (int i = 0; i < 2; i++) {
            Tab t = tabs[i];
            int tx = px + 2 + i * (tw - 2);
            boolean isTabActive = currentTab == t;
            boolean isHovered = mx >= tx && mx <= tx + tw - 2 && my >= ty && my <= ty + TAB_H;
            
            int bgColor = isTabActive ? C_TAB_ACTIVE : (isHovered ? 0xFF0D1810 : C_TAB_BG);
            int textColor = isTabActive ? 0xFFBBFFBB : (isHovered ? 0xFF619665 : 0xFF3A5A3A);
            
            g.fill(tx, ty, tx + tw - 2, ty + TAB_H, bgColor);
            if (isHovered && !isTabActive) {
                g.fill(tx, ty, tx + tw - 2, ty + 1, C_BORDER_GLOW);
            }
            if (isTabActive) {
                g.fill(tx, ty, tx + tw - 2, ty + 1, C_ACCENT);
            }
            
            g.drawCenteredString(font, (isTabActive ? "§a" : (isHovered ? "§2" : "§8")) + keys[i], tx + (tw - 2) / 2, ty + 6, textColor);
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

        boolean blink = (now / 500) % 2 == 0;
        int inputCx = cx - 12;

        g.fill(inputCx - 20, ly + 28, inputCx + 20, ly + 29, 0xFF1E3D20);
        g.drawString(font, "§2MHz", inputCx + 24, ly + 18, 0xFF4A7A4A, false);

        if (freqInput.isEmpty()) {
            if (blink) {
                g.drawCenteredString(font, "§2|", inputCx, ly + 18, 0xFFDDFFDD);
            }
        } else {
            int w = font.width("§a§l" + freqInput);
            g.drawCenteredString(font, "§a§l" + freqInput, inputCx, ly + 18, 0xFFDDFFDD);
            if (blink && freqInput.length() < 3) {
                g.drawString(font, "§2|", inputCx + w / 2 + 1, ly + 18, 0xFFDDFFDD, false);
            }
        }

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

    private void drawChannelsTab(GuiGraphics g, int px, int py, int contentY, int contentH, int mx, int my) {
        int cx = px + PANEL_W / 2;
        List<PacketSyncChannels.ChannelInfo> channels = ChannelCache.get();

        g.drawCenteredString(font, "Active Channels", cx, contentY + 6, C_TEXT_DIM);
        g.drawCenteredString(font, "Click to Join", cx, contentY + 16, 0xFF3A5A3A);

        if (channels.isEmpty()) {
            g.drawCenteredString(font, "No channels found", cx, contentY + contentH / 2, C_TEXT_DIM);
            return;
        }

        int rowW = 180;
        int rowH = 16;
        int rx = cx - rowW / 2;
        int rowY = contentY + 28;

        int maxVisible = 4;
        int maxScroll = Math.max(0, channels.size() - maxVisible);
        channelsScrollOffset = Mth.clamp(channelsScrollOffset, 0, maxScroll);

        for (int i = 0; i < Math.min(maxVisible, channels.size() - channelsScrollOffset); i++) {
            PacketSyncChannels.ChannelInfo info = channels.get(channelsScrollOffset + i);
            int ry = rowY + i * 20;

            boolean isCurrent = info.frequency().equals(freqInput);
            boolean isHovered = mx >= rx && mx <= rx + rowW && my >= ry && my <= ry + rowH;

            int bg = isCurrent ? 0xFF132418 : (isHovered ? 0xFF101C14 : C_DEEP);
            int border = isCurrent ? C_ACCENT : (isHovered ? C_BORDER_GLOW : C_BORDER);

            g.fill(rx + 1, ry + 1, rx + rowW - 1, ry + rowH - 1, bg);
            g.fill(rx, ry, rx + rowW, ry + 1, border);
            g.fill(rx, ry + rowH - 1, rx + rowW, ry + rowH, border);
            g.fill(rx, ry, rx + 1, ry + rowH, border);
            g.fill(rx + rowW - 1, ry, rx + rowW, ry + rowH, border);

            String prefix = isCurrent ? "§a▶ " : "  ";
            g.drawString(font, prefix + "§a" + info.frequency() + " MHz", rx + 6, ry + 4, 0xFFFFFFFF, false);

            String userText = info.playerCount() + (info.playerCount() == 1 ? " User" : " Users");
            String activeDot = "§a⬤ ";
            int textW = font.width(activeDot + userText);
            g.drawString(font, activeDot + "§2" + userText, rx + rowW - textW - 6, ry + 4, 0xFFFFFFFF, false);
        }

        if (maxScroll > 0) {
            int sbX = px + PANEL_W - 8;
            int sbY = rowY;
            int sbH = maxVisible * 20 - 4;
            g.fill(sbX, sbY, sbX + 4, sbY + sbH, 0x44000000);
            int thumbH = Math.max(10, (int) (((float) maxVisible / channels.size()) * sbH));
            int thumbY = sbY + (int) (((float) channelsScrollOffset / maxScroll) * (sbH - thumbH));
            boolean isHovered = mx >= sbX && mx <= sbX + 4 && my >= thumbY && my <= thumbY + thumbH;
            int thumbColor = isHovered || isDraggingChannelsScrollbar ? 0xFF388540 : C_BORDER_GLOW;
            g.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, thumbColor);
        }
    }

    private void drawInfoTab(GuiGraphics g, int px, int contentY, int contentH, int mx, int my) {
        int cx = px + PANEL_W / 2;
        
        g.drawCenteredString(font, "§a§lCHANNEL: " + freqInput + " MHz", cx, contentY + 10, 0xFFDDFFDD);
        
        int boxW = 160;
        int boxH = 50;
        int boxX = cx - boxW / 2;
        int boxY = contentY + 24;
        
        g.fill(boxX + 1, boxY + 1, boxX + boxW - 1, boxY + boxH - 1, C_DEEP);
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, C_BORDER);
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, C_BORDER);
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, C_BORDER);
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, C_BORDER);
        
        g.drawString(font, "§2Active Users:", boxX + 6, boxY + 6, 0xFF4A7A4A, false);
        
        List<String> players = new ArrayList<>();
        for (PacketSyncChannels.ChannelInfo ch : ChannelCache.get()) {
            if (ch.frequency().equals(freqInput)) {
                players.addAll(ch.players());
                break;
            }
        }
        
        if (players.isEmpty()) {
            players.add(myName);
        }
        
        int pyOffset = boxY + 18;
        for (int i = 0; i < Math.min(3, players.size()); i++) {
            g.drawString(font, "§2⬤ §f" + players.get(i), boxX + 12, pyOffset, 0xFFFFFFFF, false);
            pyOffset += 10;
        }
        
        int btnW = 80;
        int btnH = 18;
        int btnX = cx - btnW / 2;
        int btnY = contentY + 84;
        
        boolean isHovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
        int btnBg = isHovered ? 0xFF5A1818 : 0xFF3D1313;
        int btnBorder = isHovered ? 0xFFE04444 : 0xFF882222;
        int btnTextColor = isHovered ? 0xFFFFFFFF : 0xFFCC8888;
        
        g.fill(btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1, btnBg);
        g.fill(btnX, btnY, btnX + btnW, btnY + 1, btnBorder);
        g.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, btnBorder);
        g.fill(btnX, btnY, btnX + 1, btnY + btnH, btnBorder);
        g.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, btnBorder);
        
        g.drawCenteredString(font, (isHovered ? "§c§l" : "§c") + "LEAVE", cx, btnY + 5, btnTextColor);
    }

    private int lastMessageCount = 0;

    private void drawChatTab(GuiGraphics g, int px, int contentY, int contentH, int mx, int my) {
        if (!active) {
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
            int lx = px + 15;
            
            g.drawString(font, line, lx, renderY, isMine ? 0xFFAAFFBB : 0xFFBBCCAA, false);
            renderY -= 10;
        }

        int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
        if (maxScroll > 0) {
            int sbX = px + PANEL_W - 8;
            int sbY = contentY + 4;
            int sbH = contentH - 8;
            g.fill(sbX, sbY, sbX + 4, sbY + sbH, 0x44000000);
            int thumbH = Math.max(10, (int) (((float) visibleLines / cachedLines.size()) * sbH));
            int thumbY = sbY + (int) (((float) (maxScroll - scrollOffset) / maxScroll) * (sbH - thumbH));
            boolean isHovered = mx >= sbX && mx <= sbX + 4 && my >= thumbY && my <= thumbY + thumbH;
            int thumbColor = isHovered || isDraggingScrollbar ? 0xFF388540 : C_BORDER_GLOW;
            g.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, thumbColor);
        }
    }

    private void updateMessageCache(List<ChannelMessageCache.ChatEntry> messages) {
        cachedLines.clear();
        cachedIsMine.clear();
        int maxW = PANEL_W - 30;
        for (ChannelMessageCache.ChatEntry entry : messages) {
            String prefix = entry.senderName() + ": ";
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
        if (currentTab == Tab.CHAT && btn == 0) {
            int contentY = py + HEADER_H + TAB_H;
            int contentH = PANEL_H - HEADER_H - TAB_H - FOOTER_H;
            int visibleLines = (contentH - 10) / 10;
            int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
            if (maxScroll > 0) {
                int sbX = px + PANEL_W - 8;
                int sbY = contentY + 4;
                int sbH = contentH - 8;
                int thumbH = Math.max(10, (int) (((float) visibleLines / cachedLines.size()) * sbH));
                int thumbY = sbY + (int) (((float) (maxScroll - scrollOffset) / maxScroll) * (sbH - thumbH));
                if (mx >= sbX && mx <= sbX + 4 && my >= sbY && my <= sbY + sbH) {
                    isDraggingScrollbar = true;
                    if (my < thumbY || my > thumbY + thumbH) {
                        float pct = (float)(my - sbY - thumbH / 2) / (sbH - thumbH);
                        pct = Mth.clamp(pct, 0f, 1f);
                        scrollOffset = maxScroll - (int)(pct * maxScroll);
                    }
                    return true;
                }
            }
        }
        int ty = py + HEADER_H;
        int tw = PANEL_W / 2;

        if (my >= ty && my <= ty + TAB_H) {
            if (!active) {
                for (int i = 0; i < 2; i++) {
                    if (mx >= px + 2 + i * (tw - 1) && mx <= px + 2 + (i + 1) * (tw - 1)) {
                        currentTab = i == 0 ? Tab.FREQUENCY : Tab.CHANNELS;
                        playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0f);
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < 2; i++) {
                    if (mx >= px + 2 + i * (tw - 1) && mx <= px + 2 + (i + 1) * (tw - 1)) {
                        currentTab = i == 0 ? Tab.CHAT : Tab.INFO;
                        playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0f);
                        if (currentTab == Tab.CHAT) chatInput.setFocused(true);
                        return true;
                    }
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
                    active = true;
                    playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0f);
                }
                return true;
            }
        }

        if (currentTab == Tab.INFO && btn == 0) {
            int cx = px + PANEL_W / 2;
            int btnW = 80;
            int btnH = 18;
            int btnX = cx - btnW / 2;
            int btnY = py + HEADER_H + TAB_H + 84;

            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                freqInput = "";
                Platform.getHelper().sendToServer(new PacketSetFrequency("", hand));
                currentTab = Tab.FREQUENCY;
                active = false;
                playSfx(com.Theus452.walkietalkie.sound.ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0f);
                return true;
            }
        }

        if (currentTab == Tab.CHANNELS && btn == 0) {
            List<PacketSyncChannels.ChannelInfo> channels = ChannelCache.get();
            int maxVisible = 4;
            int maxScroll = Math.max(0, channels.size() - maxVisible);
            int cx = px + PANEL_W / 2;
            int rowW = 180;
            int rowH = 16;
            int rx = cx - rowW / 2;
            int rowY = py + HEADER_H + TAB_H + 28;

            if (maxScroll > 0) {
                int sbX = px + PANEL_W - 8;
                int sbY = rowY;
                int sbH = maxVisible * 20 - 4;
                int thumbH = Math.max(10, (int) (((float) maxVisible / channels.size()) * sbH));
                int thumbY = sbY + (int) (((float) channelsScrollOffset / maxScroll) * (sbH - thumbH));
                if (mx >= sbX && mx <= sbX + 4 && my >= sbY && my <= sbY + sbH) {
                    isDraggingChannelsScrollbar = true;
                    if (my < thumbY || my > thumbY + thumbH) {
                        float pct = (float)(my - sbY - thumbH / 2) / (sbH - thumbH);
                        pct = Mth.clamp(pct, 0f, 1f);
                        channelsScrollOffset = (int)(pct * maxScroll);
                    }
                    return true;
                }
            }

            for (int i = 0; i < Math.min(maxVisible, channels.size() - channelsScrollOffset); i++) {
                int ry = rowY + i * 20;
                if (mx >= rx && mx <= rx + rowW && my >= ry && my <= ry + rowH) {
                    PacketSyncChannels.ChannelInfo info = channels.get(channelsScrollOffset + i);
                    freqInput = info.frequency();
                    Platform.getHelper().sendToServer(new PacketSetFrequency(freqInput, hand));
                    currentTab = Tab.CHAT;
                    active = true;
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
                    active = true;
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
                }
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dragX, double dragY) {
        if (isDraggingScrollbar && currentTab == Tab.CHAT) {
            int py = panelY();
            int contentY = py + HEADER_H + TAB_H;
            int contentH = PANEL_H - HEADER_H - TAB_H - FOOTER_H;
            int visibleLines = (contentH - 10) / 10;
            int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
            if (maxScroll > 0) {
                int sbY = contentY + 4;
                int sbH = contentH - 8;
                int thumbH = Math.max(10, (int) (((float) visibleLines / cachedLines.size()) * sbH));
                float pct = (float)(my - sbY - thumbH / 2) / (sbH - thumbH);
                pct = Mth.clamp(pct, 0f, 1f);
                scrollOffset = maxScroll - (int)(pct * maxScroll);
                return true;
            }
        } else if (isDraggingChannelsScrollbar && currentTab == Tab.CHANNELS) {
            List<PacketSyncChannels.ChannelInfo> channels = ChannelCache.get();
            int maxVisible = 4;
            int maxScroll = Math.max(0, channels.size() - maxVisible);
            if (maxScroll > 0) {
                int py = panelY();
                int rowY = py + HEADER_H + TAB_H + 28;
                int sbY = rowY;
                int sbH = maxVisible * 20 - 4;
                int thumbH = Math.max(10, (int) (((float) maxVisible / channels.size()) * sbH));
                float pct = (float)(my - sbY - thumbH / 2) / (sbH - thumbH);
                pct = Mth.clamp(pct, 0f, 1f);
                channelsScrollOffset = (int)(pct * maxScroll);
                return true;
            }
        }
        return super.mouseDragged(mx, my, btn, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn == 0) {
            if (isDraggingScrollbar) {
                isDraggingScrollbar = false;
                return true;
            }
            if (isDraggingChannelsScrollbar) {
                isDraggingChannelsScrollbar = false;
                return true;
            }
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (currentTab == Tab.CHAT) {
            int visibleLines = ((PANEL_H - HEADER_H - TAB_H - FOOTER_H) - 10) / 10;
            scrollOffset = Mth.clamp(scrollOffset + (int) Math.signum(amount), 0, Math.max(0, cachedLines.size() - visibleLines));
            return true;
        } else if (currentTab == Tab.CHANNELS) {
            List<PacketSyncChannels.ChannelInfo> channels = ChannelCache.get();
            int maxScroll = Math.max(0, channels.size() - 4);
            channelsScrollOffset = Mth.clamp(channelsScrollOffset - (int) Math.signum(amount), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private void playSfx(net.minecraft.sounds.SoundEvent se, float vol) {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(se, vol));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
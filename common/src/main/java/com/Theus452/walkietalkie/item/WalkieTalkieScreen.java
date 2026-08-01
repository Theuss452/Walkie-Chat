package com.Theus452.walkietalkie.item;

import com.Theus452.walkietalkie.client.ChannelActionCache;
import com.Theus452.walkietalkie.client.ChannelCache;
import com.Theus452.walkietalkie.client.ChannelMessageCache;
import com.Theus452.walkietalkie.networking.packet.ChannelActionType;
import com.Theus452.walkietalkie.networking.packet.PacketCreateChannel;
import com.Theus452.walkietalkie.networking.packet.PacketJoinChannel;
import com.Theus452.walkietalkie.networking.packet.PacketRequestChannels;
import com.Theus452.walkietalkie.networking.packet.PacketSetFrequency;
import com.Theus452.walkietalkie.networking.packet.PacketSyncChannels;
import com.Theus452.walkietalkie.networking.packet.PacketKickPlayer;
import com.Theus452.walkietalkie.platform.Platform;
import com.Theus452.walkietalkie.sound.ModSounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WalkieTalkieScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 200;
    private static final int HEADER_H = 30;
    private static final int TAB_H = 20;
    private static final int STATUS_H = 16;
    private static final int CHAT_FOOTER_H = 26;
    private static final int CHANNEL_ROWS = 4;
    private static final int C_BG = 0xFF080E09;
    private static final int C_PANEL = 0xFF0D1510;
    private static final int C_DEEP = 0xFF050A06;
    private static final int C_BORDER = 0xFF1E3D20;
    private static final int C_BORDER_GLOW = 0xFF2A5A2D;
    private static final int C_ACCENT = 0xFF1E9940;
    private static final int C_TAB_ACTIVE = 0xFF132418;
    private static final int C_TEXT = 0xFF54fc54;
    private static final int C_LABEL = 0xFF78A97C;
    private static final int C_TEXT_DIM = 0xFF456B48;
    private static final int C_HINT = 0xFF284A2B;
    private static final int C_ERROR = 0xFFE07070;
    private static final int C_SUCCESS = 0xFF7BE68A;
    private static final int C_LOCK = 0xFFE04444;

    private enum Tab {
        CHANNELS,
        CREATE,
        CHAT,
        INFO
    }

    private static String savedQuickFrequency = "";
    private static String savedJoinPassword = "";
    private static String savedCreateFrequency = "";
    private static String savedCreateName = "";
    private static String savedCreatePassword = "";
    private static Tab savedTab = Tab.CREATE;

    private boolean showJoinPassword = false;
    private boolean showCreatePassword = false;

    private final InteractionHand hand;
    private final List<FormattedCharSequence> cachedLines = new ArrayList<>();
    private final List<Boolean> cachedIsMine = new ArrayList<>();
    private Tab currentTab = Tab.CREATE;
    private EditBox quickFrequencyInput;
    private EditBox joinPasswordInput;
    private EditBox channelFrequencyInput;
    private EditBox channelNameInput;
    private EditBox channelPasswordInput;
    private EditBox chatInput;
    private String currentFrequency = "";
    private String myName = "";
    private String cachedFrequency = "";
    private String statusKey = "";
    private long statusExpiresAt;
    private boolean statusSuccess;
    private boolean active;
    private boolean privateChannel;
    private boolean draggingChannelScrollbar;
    private boolean draggingChatScrollbar;
    private long pendingRequestId;
    private long pendingExpiresAt;
    private ChannelActionType pendingAction;
    private int channelScrollOffset;
    private int chatScrollOffset;
    private int lastMessageCount;
    private PacketSyncChannels.ChannelInfo selectedChannel;
    private boolean showConfirmPopup = false;
    private Runnable onConfirmAction = null;
    private Component confirmPopupTitle = Component.empty();
    private Component confirmPopupDesc = Component.empty();

    public WalkieTalkieScreen(InteractionHand hand) {
        super(Component.translatable("gui.walkietalkie.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        String previousQuickFrequency = quickFrequencyInput != null ? quickFrequencyInput.getValue()
                : savedQuickFrequency;
        String previousJoinPassword = joinPasswordInput != null ? joinPasswordInput.getValue() : savedJoinPassword;
        String previousFrequency = channelFrequencyInput != null ? channelFrequencyInput.getValue()
                : savedCreateFrequency;
        String previousName = channelNameInput != null ? channelNameInput.getValue() : savedCreateName;
        String previousPassword = channelPasswordInput != null ? channelPasswordInput.getValue() : savedCreatePassword;
        String previousChat = valueOf(chatInput);
        int px = panelX();
        int py = panelY();
        int contentY = contentY();

        if (minecraft != null && minecraft.player != null) {
            myName = minecraft.player.getName().getString();
            ItemStack stack = minecraft.player.getItemInHand(hand);
            if (stack.getItem() instanceof WalkieTalkieItem) {
                currentFrequency = WalkieTalkieItem.getFrequency(stack);
                active = !currentFrequency.isEmpty();
                if (active) {
                    currentTab = Tab.CHAT;
                } else {
                    currentTab = savedTab;
                }
            }
        }

        quickFrequencyInput = createEditBox(quickInputRect(px, contentY), 3, previousQuickFrequency, "Freq.");
        quickFrequencyInput.setFilter(this::isNumericInput);
        joinPasswordInput = createPasswordBox(joinPasswordRect(px, contentY), previousJoinPassword,
                () -> showJoinPassword);
        channelFrequencyInput = createEditBox(createFrequencyRect(px, contentY), 3, previousFrequency, "Freq.");
        channelFrequencyInput.setFilter(this::isNumericInput);
        channelNameInput = createEditBox(createNameRect(px, contentY), 16, previousName,
                "gui.walkietalkie.channel_name_hint");
        channelPasswordInput = createPasswordBox(createPasswordRect(px, contentY), previousPassword,
                () -> showCreatePassword);
        chatInput = createEditBox(chatInputRect(px, py), 256, previousChat, "gui.walkietalkie.chat_hint");

        addRenderableWidget(quickFrequencyInput);
        addRenderableWidget(joinPasswordInput);
        addRenderableWidget(channelFrequencyInput);
        addRenderableWidget(channelNameInput);
        addRenderableWidget(channelPasswordInput);
        addRenderableWidget(chatInput);
        updateWidgetVisibility();
        ChannelActionCache.clear();
        Platform.getHelper().sendToServer(new PacketRequestChannels());
        focusInput(active
                ? chatInput
                : currentTab == Tab.CREATE
                        ? channelFrequencyInput
                        : selectedChannel != null && selectedChannel.passwordProtected()
                                ? joinPasswordInput
                                : quickFrequencyInput);
    }

    @Override
    public void tick() {
        super.tick();
        quickFrequencyInput.tick();
        joinPasswordInput.tick();
        channelFrequencyInput.tick();
        channelNameInput.tick();
        channelPasswordInput.tick();
        chatInput.tick();
        syncHeldFrequency();
        ChannelActionCache.Result result = ChannelActionCache.consume();
        if (result != null
                && result.requestId() == pendingRequestId
                && result.actionType() == pendingAction) {
            handleActionResult(result);
        }
        long now = System.currentTimeMillis();
        if (!statusKey.isEmpty() && now > statusExpiresAt) {
            statusKey = "";
        }
        if (pendingAction != null && now > pendingExpiresAt) {
            pendingAction = null;
            pendingRequestId = 0L;
            showStatus("gui.walkietalkie.error.request_timeout", false);
        }
        updateWidgetVisibility();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int px = panelX();
        int py = panelY();
        int contentY = contentY();
        drawPanel(graphics, px, py);
        drawHeader(graphics, px, py);
        drawTabs(graphics, px, py, mouseX, mouseY);
        updateWidgetVisibility();
        switch (currentTab) {
            case CHANNELS -> drawChannelsTab(graphics, px, contentY, mouseX, mouseY);
            case CREATE -> drawCreateTab(graphics, px, contentY, mouseX, mouseY);
            case CHAT -> drawChatTab(graphics, px, contentY, mouseX, mouseY);
            case INFO -> drawInfoTab(graphics, px, contentY, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        drawStatusBar(graphics, px, py);
        if (showConfirmPopup) {
            drawConfirmPopup(graphics, px, py, mouseX, mouseY);
        }
    }

    private void drawPanel(GuiGraphics graphics, int px, int py) {
        graphics.fill(px + 3, py + 3, px + PANEL_W + 3, py + PANEL_H + 3, 0x55000000);
        graphics.fill(px, py, px + PANEL_W, py + PANEL_H, C_BG);
        drawBorder(graphics, new Rect(px, py, PANEL_W, PANEL_H), C_BORDER_GLOW);
        drawBorder(graphics, new Rect(px + 1, py + 1, PANEL_W - 2, PANEL_H - 2), C_BORDER);
    }

    private void drawHeader(GuiGraphics graphics, int px, int py) {
        int hx = px + 2;
        int hy = py + 2;
        graphics.fill(hx, hy, px + PANEL_W - 2, py + HEADER_H, C_PANEL);
        int scanY = hy + (int) (((System.currentTimeMillis() % 1_800L) / 1_800.0D) * (HEADER_H - 2));
        graphics.fill(hx, scanY, px + PANEL_W - 2, scanY + 1, 0x1A44FF44);
        graphics.drawString(font, "Walkie-Chat", hx + 7, hy + 4, C_TEXT, false);
        graphics.drawString(
                font,
                Component.translatable(active ? "gui.walkietalkie.status.connected" : "gui.walkietalkie.status.ready"),
                hx + 7,
                hy + 16,
                active ? C_SUCCESS : C_TEXT_DIM,
                false);
        graphics.drawCenteredString(
                font,
                currentFrequency.isEmpty() ? "--- MHz" : currentFrequency + " MHz",
                px + PANEL_W / 2,
                hy + 6,
                C_TEXT);
        LocalTime time = LocalTime.now();
        graphics.drawString(
                font,
                String.format("%02d:%02d", time.getHour(), time.getMinute()),
                px + PANEL_W - 39,
                hy + 6,
                C_TEXT_DIM,
                false);
        int signalX = px + PANEL_W - 39 + (font.width("00:00") / 2) - 8;
        int signalY = hy + 18;
        graphics.fill(signalX, signalY + 6, signalX + 3, signalY + 8, C_SUCCESS);
        graphics.fill(signalX + 4, signalY + 4, signalX + 7, signalY + 8, C_SUCCESS);
        graphics.fill(signalX + 8, signalY + 2, signalX + 11, signalY + 8, C_SUCCESS);
        graphics.fill(signalX + 12, signalY, signalX + 15, signalY + 8, C_SUCCESS);
    }

    private void drawTabs(GuiGraphics graphics, int px, int py, int mouseX, int mouseY) {
        Tab[] tabs = active ? new Tab[] { Tab.CHAT, Tab.INFO, Tab.CHANNELS } : new Tab[] { Tab.CREATE, Tab.CHANNELS };
        String[] keys = active
                ? new String[] { "gui.walkietalkie.tab.chat", "gui.walkietalkie.tab.info",
                        "gui.walkietalkie.tab.channels" }
                : new String[] { "gui.walkietalkie.tab.create", "gui.walkietalkie.tab.channels" };
        int tabY = py + HEADER_H;
        int tabWidth = (PANEL_W - 4) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            Rect rect = new Rect(px + 2 + i * tabWidth, tabY, tabWidth, TAB_H);
            boolean selected = currentTab == tabs[i];
            boolean hovered = rect.contains(mouseX, mouseY);
            graphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), selected ? C_TAB_ACTIVE : C_BG);
            graphics.fill(rect.x(), rect.y(), rect.right(), rect.y() + 1,
                    selected ? C_ACCENT : hovered ? C_BORDER_GLOW : C_BORDER);
            graphics.drawCenteredString(
                    font,
                    Component.translatable(keys[i]),
                    rect.x() + rect.width() / 2,
                    rect.y() + 6,
                    selected ? C_TEXT : hovered ? C_LABEL : C_TEXT_DIM);
        }
    }

    private void drawChannelsTab(GuiGraphics graphics, int px, int contentY, int mouseX, int mouseY) {
        List<PacketSyncChannels.ChannelInfo> channels = sortedChannels();
        rebindSelectedChannel(channels);
        Rect list = channelListRect(px, contentY);
        graphics.drawString(font, Component.translatable("gui.walkietalkie.available_channels"), list.x(), contentY + 3,
                C_LABEL, false);
        int maxScroll = Math.max(0, channels.size() - CHANNEL_ROWS);
        channelScrollOffset = Mth.clamp(channelScrollOffset, 0, maxScroll);
        if (channels.isEmpty()) {
            graphics.fill(list.x(), list.y(), list.right(), list.bottom(), C_DEEP);
            drawBorder(graphics, list, C_BORDER);
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.walkietalkie.no_channels"),
                    list.x() + list.width() / 2,
                    list.y() + 33,
                    C_TEXT_DIM);
        } else {
            for (int row = 0; row < Math.min(CHANNEL_ROWS, channels.size() - channelScrollOffset); row++) {
                PacketSyncChannels.ChannelInfo info = channels.get(channelScrollOffset + row);
                Rect rowRect = channelRowRect(list, row);
                boolean selected = selectedChannel != null && selectedChannel.frequency().equals(info.frequency());
                boolean hovered = rowRect.contains(mouseX, mouseY);
                graphics.fill(
                        rowRect.x() + 1,
                        rowRect.y() + 1,
                        rowRect.right() - 1,
                        rowRect.bottom() - 1,
                        selected ? 0xFF17331D : hovered ? 0xFF101C14 : C_DEEP);
                drawBorder(graphics, rowRect, selected ? C_ACCENT : hovered ? C_BORDER_GLOW : C_BORDER);
                int lockX = rowRect.right() - 12;
                int frequencyWidth = font.width(info.frequency());
                int frequencyX = lockX - frequencyWidth - 5;
                int maxNameWidth = frequencyX - rowRect.x() - 13;
                String name = ellipsize(info.name(), maxNameWidth);
                graphics.drawString(font, name, rowRect.x() + 6, rowRect.y() + 5, selected ? C_TEXT : C_LABEL, false);
                graphics.drawString(
                        font,
                        info.frequency(),
                        frequencyX,
                        rowRect.y() + 5,
                        C_TEXT,
                        false);
                if (info.passwordProtected()) {
                    drawLock(graphics, lockX, rowRect.y() + 5);
                } else {
                    drawPublicIcon(graphics, lockX, rowRect.y() + 5);
                }
            }
        }
        drawChannelScrollbar(graphics, px, list, channels.size(), mouseX, mouseY);
        drawSelectedChannel(graphics, px, contentY, mouseX, mouseY);
        Rect quickInput = quickInputRect(px, contentY);
        graphics.drawString(font, Component.translatable("gui.walkietalkie.quick_frequency"), quickInput.x(),
                quickInput.y() - 12, C_LABEL, false);
        drawInputFrame(graphics, quickFrequencyInput);
        Rect quickButton = quickButtonRect(px, contentY);
        boolean enabled = pendingAction == null && !quickFrequencyInput.getValue().isBlank();
        drawButton(
                graphics,
                quickButton,
                Component.translatable(pendingAction == ChannelActionType.JOIN ? "gui.walkietalkie.waiting"
                        : "gui.walkietalkie.connect"),
                enabled,
                mouseX,
                mouseY,
                false);
    }

    private void drawSelectedChannel(GuiGraphics graphics, int px, int contentY, int mouseX, int mouseY) {
        Rect detail = channelDetailRect(px, contentY);
        graphics.fill(detail.x(), detail.y(), detail.right(), detail.bottom(), C_DEEP);
        drawBorder(graphics, detail, C_BORDER);
        if (selectedChannel == null) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.walkietalkie.select_channel"),
                    detail.x() + detail.width() / 2,
                    detail.y() + 41,
                    C_TEXT_DIM);
            return;
        }
        graphics.drawCenteredString(
                font,
                font.plainSubstrByWidth(selectedChannel.name(), detail.width() - 12),
                detail.x() + detail.width() / 2,
                detail.y() + 5,
                C_TEXT);
        graphics.drawCenteredString(
                font,
                selectedChannel.frequency() + " MHz",
                detail.x() + detail.width() / 2,
                detail.y() + 17,
                C_TEXT_DIM);
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        selectedChannel.passwordProtected()
                                ? "gui.walkietalkie.private_channel"
                                : "gui.walkietalkie.public_channel"),
                detail.x() + detail.width() / 2,
                detail.y() + 29,
                selectedChannel.passwordProtected() ? C_LOCK : C_SUCCESS);
        Component members = selectedChannel.playerCount() < 0
                ? Component.translatable("gui.walkietalkie.members_hidden")
                : Component.translatable(
                        selectedChannel.playerCount() == 1
                                ? "gui.walkietalkie.member_count.one"
                                : "gui.walkietalkie.member_count.many",
                        selectedChannel.playerCount());
        graphics.drawCenteredString(font, members, detail.x() + detail.width() / 2, detail.y() + 41, C_TEXT_DIM);
        if (selectedChannel.passwordProtected()) {
            Rect password = joinPasswordRect(px, contentY);
            graphics.drawString(font, Component.translatable("gui.walkietalkie.password"), password.x(),
                    password.y() - 12, C_LABEL, false);
            drawInputFrame(graphics, joinPasswordInput);
            Rect eye = joinPasswordVisibilityRect(px, contentY);
            boolean hovered = eye.contains(mouseX, mouseY);
            drawPasswordVisibilityButton(graphics, eye, showJoinPassword, hovered);
        }
        Rect joinButton = joinButtonRect(px, contentY, selectedChannel.passwordProtected());
        boolean enabled = pendingAction == null
                && (!selectedChannel.passwordProtected() || !joinPasswordInput.getValue().isBlank());
        drawButton(
                graphics,
                joinButton,
                Component.translatable(
                        pendingAction == ChannelActionType.JOIN ? "gui.walkietalkie.waiting" : "gui.walkietalkie.join"),
                enabled,
                mouseX,
                mouseY,
                false);
    }

    private void drawCreateTab(GuiGraphics graphics, int px, int contentY, int mouseX, int mouseY) {
        Rect frequency = createFrequencyRect(px, contentY);
        Rect name = createNameRect(px, contentY);
        Rect visibility = visibilityCardRect(px, contentY);
        graphics.drawString(font, Component.translatable("gui.walkietalkie.frequency"), frequency.x(),
                frequency.y() - 12, C_LABEL, false);
        drawInputFrame(graphics, channelFrequencyInput);
        graphics.drawString(font, "MHz", frequency.right() + 6, frequency.y() + 3, C_TEXT_DIM, false);
        graphics.drawString(font, Component.translatable("gui.walkietalkie.channel_name"), name.x(), name.y() - 12,
                C_LABEL, false);
        drawInputFrame(graphics, channelNameInput);
        graphics.fill(visibility.x(), visibility.y(), visibility.right(), visibility.bottom(), C_DEEP);
        drawBorder(graphics, visibility, C_BORDER);
        graphics.drawCenteredString(
                font,
                Component.translatable("gui.walkietalkie.visibility"),
                visibility.x() + visibility.width() / 2,
                visibility.y() + 2,
                C_LABEL);
        Rect visibilityButton = visibilityButtonRect(px, contentY);
        drawButton(
                graphics,
                visibilityButton,
                Component.translatable(
                        privateChannel ? "gui.walkietalkie.private_channel" : "gui.walkietalkie.public_channel"),
                pendingAction == null,
                mouseX,
                mouseY,
                false);
        if (privateChannel) {
            Rect password = createPasswordRect(px, contentY);
            graphics.drawString(font, Component.translatable("gui.walkietalkie.password"), password.x(),
                    password.y() - 12, C_LABEL, false);
            drawInputFrame(graphics, channelPasswordInput);
            Rect eye = createPasswordVisibilityRect(px, contentY);
            boolean hovered = eye.contains(mouseX, mouseY);
            drawPasswordVisibilityButton(graphics, eye, showCreatePassword, hovered);
        } else {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.walkietalkie.public_hint"),
                    visibility.x() + visibility.width() / 2,
                    visibility.y() + 59,
                    C_TEXT_DIM);
        }
        Rect createButton = createButtonRect(px, contentY);
        boolean enabled = pendingAction == null
                && !channelFrequencyInput.getValue().isBlank()
                && (!privateChannel || channelPasswordInput.getValue().length() >= 4);
        drawButton(
                graphics,
                createButton,
                Component.translatable(pendingAction == ChannelActionType.CREATE ? "gui.walkietalkie.waiting"
                        : "gui.walkietalkie.create_channel"),
                enabled,
                mouseX,
                mouseY,
                false);
        graphics.drawCenteredString(
                font,
                Component.translatable("gui.walkietalkie.create_hint"),
                px + PANEL_W / 2,
                contentY + 123,
                C_HINT);
    }

    private void drawInfoTab(GuiGraphics graphics, int px, int contentY, int mouseX, int mouseY) {
        PacketSyncChannels.ChannelInfo info = findChannel(currentFrequency);
        String name = info == null ? currentFrequency + " MHz" : info.name();

        String labelText = "Name: " + name;
        graphics.drawCenteredString(font, labelText, px + PANEL_W / 2, contentY + 7, C_TEXT);
        graphics.drawCenteredString(font, "CH: " + currentFrequency, px + PANEL_W / 2, contentY + 19, C_TEXT_DIM);

        Rect membersBox = infoMembersRect(px, contentY);
        graphics.fill(membersBox.x(), membersBox.y(), membersBox.right(), membersBox.bottom(), C_DEEP);
        drawBorder(graphics, membersBox, C_BORDER);

        List<String> players = info == null ? List.of(myName) : info.players();
        if (players.isEmpty()) {
            players = List.of(myName);
        }

        Component membersHeader = Component.translatable("gui.walkietalkie.active_members", players.size());
        graphics.drawString(font, membersHeader, membersBox.x() + 7, membersBox.y() + 6, C_LABEL, false);

        boolean isOwner = info != null && myName.equals(info.ownerName());

        for (int i = 0; i < Math.min(4, players.size()); i++) {
            String memberName = players.get(i);
            boolean isMemberOwner = info != null && memberName.equals(info.ownerName());
            Component memberText = isMemberOwner
                    ? Component.literal(memberName)
                            .append(Component.literal(" [Owner]").withStyle(net.minecraft.ChatFormatting.GOLD))
                    : Component.literal(memberName);

            Rect rowRect = new Rect(membersBox.x() + 2, membersBox.y() + 16 + i * 10, membersBox.width() - 4, 10);
            boolean rowHovered = !showConfirmPopup && rowRect.contains(mouseX, mouseY);
            if (rowHovered) {
                graphics.fill(rowRect.x(), rowRect.y(), rowRect.right(), rowRect.bottom(), 0x2254fc54);
            }

            graphics.drawString(
                    font,
                    font.plainSubstrByWidth(memberText.getString(), membersBox.width() - 55),
                    membersBox.x() + 11,
                    membersBox.y() + 17 + i * 10,
                    C_TEXT,
                    false);

            if (isOwner && !memberName.equals(myName)) {
                Rect xRect = new Rect(rowRect.right() - 12, rowRect.y() + 1, 10, 8);
                boolean xHovered = !showConfirmPopup && xRect.contains(mouseX, mouseY);
                graphics.drawString(font, "X", xRect.x(), xRect.y(), xHovered ? 0xFFFFAAAA : C_LOCK, false);
            }
        }
        Rect leave = leaveButtonRect(px, contentY);
        drawButton(
                graphics,
                leave,
                Component.translatable(pendingAction == ChannelActionType.LEAVE ? "gui.walkietalkie.waiting"
                        : "gui.walkietalkie.leave"),
                pendingAction == null,
                mouseX,
                mouseY,
                true);
    }

    private void drawChatTab(GuiGraphics graphics, int px, int contentY, int mouseX, int mouseY) {
        Rect area = chatAreaRect(px, contentY);
        if (!active) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.walkietalkie.join_first"),
                    px + PANEL_W / 2,
                    area.y() + area.height() / 2,
                    C_TEXT_DIM);
            return;
        }
        List<ChannelMessageCache.ChatEntry> messages = ChannelMessageCache.getRecent(currentFrequency, 50);
        if (messages.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("gui.walkietalkie.no_messages"),
                    px + PANEL_W / 2,
                    area.y() + area.height() / 2,
                    C_TEXT_DIM);
        } else {
            if (cachedLines.isEmpty()
                    || !cachedFrequency.equals(currentFrequency)
                    || messages.size() != lastMessageCount) {
                List<ChannelMessageCache.ChatEntry> sortedMessages = new ArrayList<>(messages);
                sortedMessages.sort(Comparator.comparingLong(ChannelMessageCache.ChatEntry::timestampMs));
                updateMessageCache(sortedMessages);
                cachedFrequency = currentFrequency;
                lastMessageCount = messages.size();
            }
            int visibleLines = Math.max(1, (area.height() - 8) / 10);
            int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
            chatScrollOffset = Mth.clamp(chatScrollOffset, 0, maxScroll);
            int renderY = area.bottom() - 12;
            for (int i = cachedLines.size() - 1 - chatScrollOffset; i >= 0 && renderY >= area.y() + 4; i--) {
                graphics.drawString(
                        font,
                        cachedLines.get(i),
                        area.x() + 5,
                        renderY,
                        0xFFFFFFFF,
                        false);
                renderY -= 10;
            }
            drawChatScrollbar(graphics, area, visibleLines, maxScroll, mouseX, mouseY);
        }
        Rect footer = chatFooterRect(px);
        graphics.fill(footer.x(), footer.y(), footer.right(), footer.bottom(), C_DEEP);
        graphics.fill(footer.x() + 5, footer.y() + 3, footer.right() - 5, footer.y() + 4, C_BORDER);
        drawInputFrame(graphics, chatInput);
    }

    private void drawChannelScrollbar(
            GuiGraphics graphics,
            int px,
            Rect list,
            int channelCount,
            int mouseX,
            int mouseY) {
        int maxScroll = Math.max(0, channelCount - CHANNEL_ROWS);
        if (maxScroll == 0) {
            return;
        }
        Rect track = new Rect(px + 190, list.y(), 3, list.height());
        int thumbHeight = Math.max(10, CHANNEL_ROWS * track.height() / channelCount);
        int thumbY = track.y() + channelScrollOffset * (track.height() - thumbHeight) / maxScroll;
        graphics.fill(track.x(), track.y(), track.right(), track.bottom(), 0x44000000);
        int color = draggingChannelScrollbar
                || new Rect(track.x(), thumbY, track.width(), thumbHeight).contains(mouseX, mouseY)
                        ? 0xFF388540
                        : C_BORDER_GLOW;
        graphics.fill(track.x(), thumbY, track.right(), thumbY + thumbHeight, color);
    }

    private void drawChatScrollbar(
            GuiGraphics graphics,
            Rect area,
            int visibleLines,
            int maxScroll,
            int mouseX,
            int mouseY) {
        if (maxScroll == 0) {
            return;
        }
        Rect track = new Rect(area.right() - 4, area.y() + 3, 3, area.height() - 6);
        int thumbHeight = Math.max(9, visibleLines * track.height() / cachedLines.size());
        int thumbY = track.y() + (maxScroll - chatScrollOffset) * (track.height() - thumbHeight) / maxScroll;
        graphics.fill(track.x(), track.y(), track.right(), track.bottom(), 0x44000000);
        int color = draggingChatScrollbar
                || new Rect(track.x(), thumbY, track.width(), thumbHeight).contains(mouseX, mouseY)
                        ? 0xFF388540
                        : C_BORDER_GLOW;
        graphics.fill(track.x(), thumbY, track.right(), thumbY + thumbHeight, color);
    }

    private void drawLock(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 2, y, x + 6, y + 1, C_LOCK);
        graphics.fill(x + 1, y + 1, x + 2, y + 4, C_LOCK);
        graphics.fill(x + 6, y + 1, x + 7, y + 4, C_LOCK);
        graphics.fill(x, y + 3, x + 8, y + 8, C_LOCK);
        graphics.fill(x + 3, y + 5, x + 5, y + 7, C_DEEP);
    }

    private void drawPublicIcon(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 2, y, x + 6, y + 1, C_SUCCESS);
        graphics.fill(x + 1, y + 1, x + 2, y + 7, C_SUCCESS);
        graphics.fill(x + 6, y + 1, x + 7, y + 7, C_SUCCESS);
        graphics.fill(x + 2, y + 7, x + 6, y + 8, C_SUCCESS);
        graphics.fill(x + 3, y + 3, x + 5, y + 5, C_SUCCESS);
    }

    private int getPopupHeight(List<FormattedCharSequence> descLines) {
        return Math.max(76, 52 + descLines.size() * 10);
    }

    private void drawConfirmPopup(GuiGraphics graphics, int px, int py, int mouseX, int mouseY) {
        renderConfirmPopup(graphics, mouseX, mouseY, px, py);
    }

    private void renderConfirmPopup(GuiGraphics graphics, int mouseX, int mouseY, int px, int py) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        int pw = 210;
        List<FormattedCharSequence> descLines = font.split(confirmPopupDesc, pw - 20);
        int ph = getPopupHeight(descLines);
        int x = px + (PANEL_W - pw) / 2;
        int y = py + (PANEL_H - ph) / 2;
        graphics.fill(px, py, px + PANEL_W, py + PANEL_H, 0xAA000000);
        graphics.fill(x, y, x + pw, y + ph, C_BG);
        drawBorder(graphics, new Rect(x, y, pw, ph), C_BORDER);
        graphics.drawCenteredString(font, confirmPopupTitle, x + pw / 2, y + 10, C_TEXT);
        int lineY = y + 22;
        for (FormattedCharSequence line : descLines) {
            int lineW = font.width(line);
            graphics.drawString(font, line, x + (pw - lineW) / 2, lineY, C_TEXT_DIM, false);
            lineY += 10;
        }
        Rect confirmBtn = confirmButtonRect(x, y, pw, ph);
        Rect cancelBtn = cancelButtonRect(x, y, pw, ph);
        boolean confirmHovered = confirmBtn.contains(mouseX, mouseY);
        boolean cancelHovered = cancelBtn.contains(mouseX, mouseY);
        graphics.fill(confirmBtn.x(), confirmBtn.y(), confirmBtn.right(), confirmBtn.bottom(),
                confirmHovered ? 0xFF18301C : C_PANEL);
        drawBorder(graphics, confirmBtn, confirmHovered ? C_ACCENT : C_BORDER);
        graphics.drawCenteredString(font, Component.translatable("gui.walkietalkie.popup.confirm"),
                confirmBtn.x() + confirmBtn.width() / 2, confirmBtn.y() + 4, confirmHovered ? C_TEXT : C_TEXT_DIM);
        graphics.fill(cancelBtn.x(), cancelBtn.y(), cancelBtn.right(), cancelBtn.bottom(),
                cancelHovered ? 0xFF3D1313 : C_PANEL);
        drawBorder(graphics, cancelBtn, cancelHovered ? 0xFFE04444 : C_BORDER);
        graphics.drawCenteredString(font, Component.translatable("gui.walkietalkie.popup.cancel"),
                cancelBtn.x() + cancelBtn.width() / 2, cancelBtn.y() + 4, cancelHovered ? C_TEXT : C_TEXT_DIM);
        graphics.pose().popPose();
    }

    private Rect confirmButtonRect(int popupX, int popupY, int pw, int ph) {
        return new Rect(popupX + 20, popupY + ph - 22, 75, 16);
    }

    private Rect cancelButtonRect(int popupX, int popupY, int pw, int ph) {
        return new Rect(popupX + pw - 95, popupY + ph - 22, 75, 16);
    }

    private void drawInputFrame(GuiGraphics graphics, EditBox input) {
        if (!input.visible) {
            return;
        }
        Rect rect = new Rect(input.getX() - 2, input.getY() - 2, input.getWidth() + 4, input.getHeight() + 4);
        graphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), C_DEEP);
        drawBorder(graphics, rect, input.isFocused() ? C_ACCENT : C_BORDER);
    }

    private void drawButton(
            GuiGraphics graphics,
            Rect rect,
            Component label,
            boolean enabled,
            int mouseX,
            int mouseY,
            boolean destructive) {
        boolean hovered = enabled && rect.contains(mouseX, mouseY);
        int background;
        int border;
        int text;
        if (!enabled) {
            background = C_DEEP;
            border = 0xFF152517;
            text = 0xFF304530;
        } else if (destructive) {
            background = hovered ? 0xFF5A1818 : 0xFF3D1313;
            border = hovered ? 0xFFE04444 : 0xFF882222;
            text = hovered ? 0xFFFFFFFF : 0xFFCC8888;
        } else {
            background = hovered ? 0xFF18301C : C_TAB_ACTIVE;
            border = hovered ? C_ACCENT : C_BORDER_GLOW;
            text = hovered ? 0xFFFFFFFF : C_TEXT;
        }
        graphics.fill(rect.x() + 1, rect.y() + 1, rect.right() - 1, rect.bottom() - 1, background);
        drawBorder(graphics, rect, border);
        graphics.drawCenteredString(font, label, rect.x() + rect.width() / 2, rect.y() + (rect.height() - 8) / 2, text);
    }

    private void drawStatusBar(GuiGraphics graphics, int px, int py) {
        Rect status = statusRect(px, py);
        graphics.fill(status.x(), status.y(), status.right(), status.bottom(), 0xF0080E09);
        graphics.fill(status.x() + 5, status.y(), status.right() - 5, status.y() + 1, C_BORDER);
        if (!statusKey.isEmpty()) {
            String statusText = ellipsize(Component.translatable(statusKey).getString(), status.width() - 12);
            graphics.drawCenteredString(
                    font,
                    statusText,
                    status.x() + status.width() / 2,
                    status.y() + 4,
                    statusSuccess ? C_SUCCESS : C_ERROR);
        }
    }

    private void drawBorder(GuiGraphics graphics, Rect rect, int color) {
        graphics.fill(rect.x(), rect.y(), rect.right(), rect.y() + 1, color);
        graphics.fill(rect.x(), rect.bottom() - 1, rect.right(), rect.bottom(), color);
        graphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.bottom(), color);
        graphics.fill(rect.right() - 1, rect.y(), rect.right(), rect.bottom(), color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int px = panelX();
        int py = panelY();
        int contentY = contentY();
        if (showConfirmPopup && button == 0) {
            int pw = 210;
            List<FormattedCharSequence> descLines = font.split(confirmPopupDesc, pw - 20);
            int ph = getPopupHeight(descLines);
            int x = px + (PANEL_W - pw) / 2;
            int y = py + (PANEL_H - ph) / 2;
            Rect confirmBtn = confirmButtonRect(x, y, pw, ph);
            Rect cancelBtn = cancelButtonRect(x, y, pw, ph);
            if (confirmBtn.contains(mouseX, mouseY)) {
                showConfirmPopup = false;
                playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                if (onConfirmAction != null) {
                    onConfirmAction.run();
                    onConfirmAction = null;
                }
                updateWidgetVisibility();
                return true;
            }
            if (cancelBtn.contains(mouseX, mouseY)) {
                showConfirmPopup = false;
                onConfirmAction = null;
                playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                updateWidgetVisibility();
                return true;
            }
            return true;
        }
        if (button == 0 && handleTabClick(mouseX, mouseY, px, py)) {
            return true;
        }
        if (button == 0 && currentTab == Tab.CHANNELS) {
            if (selectedChannel != null && selectedChannel.passwordProtected()) {
                Rect eye = joinPasswordVisibilityRect(px, contentY);
                if (eye.contains(mouseX, mouseY)) {
                    showJoinPassword = !showJoinPassword;
                    playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                    return true;
                }
            }
            List<PacketSyncChannels.ChannelInfo> channels = sortedChannels();
            Rect list = channelListRect(px, contentY);
            int maxScroll = Math.max(0, channels.size() - CHANNEL_ROWS);
            Rect scrollbar = new Rect(px + 190, list.y(), 3, list.height());
            if (maxScroll > 0 && scrollbar.contains(mouseX, mouseY)) {
                draggingChannelScrollbar = true;
                updateChannelScrollFromMouse(mouseY, scrollbar, channels.size());
                return true;
            }
            for (int row = 0; row < Math.min(CHANNEL_ROWS, channels.size() - channelScrollOffset); row++) {
                if (channelRowRect(list, row).contains(mouseX, mouseY)) {
                    selectedChannel = channels.get(channelScrollOffset + row);
                    joinPasswordInput.setValue("");
                    updateWidgetVisibility();
                    focusInput(selectedChannel.passwordProtected() ? joinPasswordInput : null);
                    playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                    return true;
                }
            }
            if (selectedChannel != null) {
                Rect join = joinButtonRect(px, contentY, selectedChannel.passwordProtected());
                boolean enabled = pendingAction == null
                        && (!selectedChannel.passwordProtected() || !joinPasswordInput.getValue().isBlank());
                if (enabled && join.contains(mouseX, mouseY)) {
                    submitSelectedChannel();
                    return true;
                }
            }
            boolean quickEnabled = pendingAction == null && !quickFrequencyInput.getValue().isBlank();
            if (quickEnabled && quickButtonRect(px, contentY).contains(mouseX, mouseY)) {
                submitQuickFrequency();
                return true;
            }
        }
        if (button == 0 && currentTab == Tab.CREATE) {
            if (privateChannel) {
                Rect eye = createPasswordVisibilityRect(px, contentY);
                if (eye.contains(mouseX, mouseY)) {
                    showCreatePassword = !showCreatePassword;
                    playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                    return true;
                }
            }
            if (pendingAction == null && visibilityButtonRect(px, contentY).contains(mouseX, mouseY)) {
                privateChannel = !privateChannel;
                channelPasswordInput.setValue("");
                updateWidgetVisibility();
                focusInput(privateChannel ? channelPasswordInput : channelNameInput);
                playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                return true;
            }
            boolean enabled = pendingAction == null
                    && !channelFrequencyInput.getValue().isBlank()
                    && (!privateChannel || channelPasswordInput.getValue().length() >= 4);
            if (enabled && createButtonRect(px, contentY).contains(mouseX, mouseY)) {
                submitCreateChannel();
                return true;
            }
        }
        if (button == 0 && currentTab == Tab.INFO) {
            PacketSyncChannels.ChannelInfo info = findChannel(currentFrequency);
            boolean isOwner = info != null && myName.equals(info.ownerName());

            Rect membersBox = infoMembersRect(px, contentY);
            List<String> players = info == null ? List.of(myName) : info.players();
            if (players.isEmpty()) {
                players = List.of(myName);
            }
            for (int i = 0; i < Math.min(4, players.size()); i++) {
                String memberName = players.get(i);
                Rect rowRect = new Rect(membersBox.x() + 2, membersBox.y() + 16 + i * 10, membersBox.width() - 4, 10);
                if (!memberName.equals(myName) && rowRect.contains(mouseX, mouseY)) {
                    if (isOwner) {
                        Rect xRect = new Rect(rowRect.right() - 12, rowRect.y() + 1, 10, 8);
                        if (xRect.contains(mouseX, mouseY)) {
                            showConfirmPopup = true;
                            confirmPopupTitle = Component.translatable("gui.walkietalkie.popup.kick_title");
                            confirmPopupDesc = Component.translatable("gui.walkietalkie.popup.kick_desc", memberName);
                            onConfirmAction = () -> {
                                Platform.getHelper().sendToServer(new PacketKickPlayer(currentFrequency, memberName));
                            };
                            playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
                            updateWidgetVisibility();
                            return true;
                        }
                    }
                }
            }

            if (pendingAction == null && leaveButtonRect(px, contentY).contains(mouseX, mouseY)) {
                submitLeave();
                return true;
            }
        }
        if (button == 0 && currentTab == Tab.CHAT && beginChatScrollbarDrag(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleTabClick(double mouseX, double mouseY, int px, int py) {
        Rect tabsRect = new Rect(px + 2, py + HEADER_H, PANEL_W - 4, TAB_H);
        if (!tabsRect.contains(mouseX, mouseY)) {
            return false;
        }
        Tab[] tabs = active ? new Tab[] { Tab.CHAT, Tab.INFO, Tab.CHANNELS } : new Tab[] { Tab.CREATE, Tab.CHANNELS };
        int tabWidth = (PANEL_W - 4) / tabs.length;
        int index = (int) ((mouseX - (px + 2)) / tabWidth);
        if (index >= 0 && index < tabs.length) {
            currentTab = tabs[index];
            playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
            updateWidgetVisibility();
            focusInput(switch (currentTab) {
                case CREATE -> channelFrequencyInput;
                case CHANNELS -> selectedChannel != null && selectedChannel.passwordProtected()
                        ? joinPasswordInput
                        : quickFrequencyInput;
                case CHAT -> chatInput;
                case INFO -> null;
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showConfirmPopup) {
            if (keyCode == 256) {
                showConfirmPopup = false;
                onConfirmAction = null;
                updateWidgetVisibility();
                return true;
            }
        }
        if (keyCode == 257 || keyCode == 335) {
            if (currentTab == Tab.CHANNELS) {
                if (joinPasswordInput.isFocused() && selectedChannel != null) {
                    submitSelectedChannel();
                } else {
                    submitQuickFrequency();
                }
                return true;
            }
            if (currentTab == Tab.CREATE) {
                submitCreateChannel();
                return true;
            }
            if (currentTab == Tab.CHAT) {
                sendChatMessage();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (currentTab == Tab.CHANNELS) {
            int maxScroll = Math.max(0, ChannelCache.get().size() - CHANNEL_ROWS);
            channelScrollOffset = Mth.clamp(channelScrollOffset - (int) Math.signum(amount), 0, maxScroll);
            return true;
        }
        if (currentTab == Tab.CHAT) {
            Rect area = chatAreaRect(panelX(), contentY());
            int visibleLines = Math.max(1, (area.height() - 8) / 10);
            int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
            chatScrollOffset = Mth.clamp(chatScrollOffset + (int) Math.signum(amount), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingChannelScrollbar && currentTab == Tab.CHANNELS) {
            Rect list = channelListRect(panelX(), contentY());
            updateChannelScrollFromMouse(mouseY, new Rect(panelX() + 190, list.y(), 3, list.height()),
                    ChannelCache.get().size());
            return true;
        }
        if (draggingChatScrollbar && currentTab == Tab.CHAT) {
            Rect area = chatAreaRect(panelX(), contentY());
            int visibleLines = Math.max(1, (area.height() - 8) / 10);
            int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
            if (maxScroll > 0) {
                Rect track = new Rect(area.right() - 4, area.y() + 3, 3, area.height() - 6);
                int thumbHeight = Math.max(9, visibleLines * track.height() / cachedLines.size());
                float percentage = (float) (mouseY - track.y() - thumbHeight / 2.0D) / (track.height() - thumbHeight);
                chatScrollOffset = maxScroll - Math.round(Mth.clamp(percentage, 0.0F, 1.0F) * maxScroll);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingChannelScrollbar || draggingChatScrollbar)) {
            draggingChannelScrollbar = false;
            draggingChatScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean beginChatScrollbarDrag(double mouseX, double mouseY) {
        Rect area = chatAreaRect(panelX(), contentY());
        int visibleLines = Math.max(1, (area.height() - 8) / 10);
        int maxScroll = Math.max(0, cachedLines.size() - visibleLines);
        Rect track = new Rect(area.right() - 4, area.y() + 3, 3, area.height() - 6);
        if (maxScroll == 0 || !track.contains(mouseX, mouseY)) {
            return false;
        }
        draggingChatScrollbar = true;
        return true;
    }

    private void submitQuickFrequency() {
        if (pendingAction != null) {
            return;
        }
        String frequency = normalizeFrequency(quickFrequencyInput.getValue());
        PacketSyncChannels.ChannelInfo info = findChannel(frequency);
        if (info == null) {
            showStatus("gui.walkietalkie.error.channel_not_found", false);
            return;
        }
        selectedChannel = info;
        if (info.passwordProtected()) {
            joinPasswordInput.setValue("");
            updateWidgetVisibility();
            focusInput(joinPasswordInput);
            showStatus("gui.walkietalkie.error.password_required", false);
            return;
        }
        if (active && !frequency.equals(currentFrequency)) {
            showConfirmPopup = true;
            confirmPopupTitle = Component.translatable("gui.walkietalkie.popup.confirm_title");
            confirmPopupDesc = Component.translatable("gui.walkietalkie.popup.confirm_desc");
            onConfirmAction = () -> performJoinQuickFrequency(frequency);
            updateWidgetVisibility();
            return;
        }
        performJoinQuickFrequency(frequency);
    }

    private void performJoinQuickFrequency(String frequency) {
        long requestId = beginAction(ChannelActionType.JOIN);
        Platform.getHelper().sendToServer(new PacketJoinChannel(frequency, "", hand, requestId));
        playSfx(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0F);
    }

    private void submitSelectedChannel() {
        if (pendingAction != null || selectedChannel == null) {
            return;
        }
        String password = joinPasswordInput.getValue();
        if (selectedChannel.passwordProtected() && password.isBlank()) {
            showStatus("gui.walkietalkie.error.password_required", false);
            focusInput(joinPasswordInput);
            return;
        }
        if (active && !selectedChannel.frequency().equals(currentFrequency)) {
            showConfirmPopup = true;
            confirmPopupTitle = Component.translatable("gui.walkietalkie.popup.confirm_title");
            confirmPopupDesc = Component.translatable("gui.walkietalkie.popup.confirm_desc");
            onConfirmAction = () -> performJoinSelectedChannel(password);
            updateWidgetVisibility();
            return;
        }
        performJoinSelectedChannel(password);
    }

    private void performJoinSelectedChannel(String password) {
        long requestId = beginAction(ChannelActionType.JOIN);
        Platform.getHelper().sendToServer(
                new PacketJoinChannel(selectedChannel.frequency(), password, hand, requestId));
        playSfx(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0F);
    }

    private void submitCreateChannel() {
        if (pendingAction != null) {
            return;
        }
        String frequency = normalizeFrequency(channelFrequencyInput.getValue());
        String name = channelNameInput.getValue().trim();
        if (name.length() < 1) {
            showStatus("gui.walkietalkie.error.name", false);
            return;
        }
        String password = channelPasswordInput.getValue();
        if (frequency.isEmpty() || privateChannel && password.length() < 4) {
            showStatus("gui.walkietalkie.error.form", false);
            return;
        }
        long requestId = beginAction(ChannelActionType.CREATE);
        Platform.getHelper().sendToServer(
                new PacketCreateChannel(frequency, name, privateChannel, password, hand, requestId));
        playSfx(ModSounds.WALKIE_TALKIE_BUTTON_CLICK.get(), 1.0F);
    }

    private void submitLeave() {
        if (pendingAction != null) {
            return;
        }
        long requestId = beginAction(ChannelActionType.LEAVE);
        Platform.getHelper().sendToServer(new PacketSetFrequency("", hand, requestId));
        playSfx(ModSounds.WALKIE_TALKIE_CHANGE_CHANNEL.get(), 1.0F);
    }

    private void sendChatMessage() {
        String message = chatInput.getValue().trim();
        if (!message.isEmpty() && minecraft != null && minecraft.player != null) {
            minecraft.player.connection.sendChat(message);
            chatInput.setValue("");
        }
    }

    private long beginAction(ChannelActionType actionType) {
        pendingAction = actionType;
        pendingRequestId = ChannelActionCache.nextRequestId();
        pendingExpiresAt = System.currentTimeMillis() + 10_000L;
        statusKey = "";
        return pendingRequestId;
    }

    private void handleActionResult(ChannelActionCache.Result result) {
        pendingAction = null;
        pendingRequestId = 0L;
        pendingExpiresAt = 0L;
        showStatus(result.messageKey(), result.success());
        if (!result.success()) {
            if (result.actionType() == ChannelActionType.JOIN
                    && selectedChannel != null
                    && selectedChannel.passwordProtected()) {
                updateWidgetVisibility();
                focusInput(joinPasswordInput);
            }
            return;
        }
        if (result.actionType() == ChannelActionType.CREATE || result.actionType() == ChannelActionType.JOIN) {
            currentFrequency = result.frequency();
            active = true;
            currentTab = Tab.CHAT;
            joinPasswordInput.setValue("");
            resetCreateTabFields();
            updateWidgetVisibility();
            focusInput(chatInput);
        } else {
            currentFrequency = "";
            active = false;
            currentTab = Tab.CHANNELS;
            selectedChannel = null;
            updateWidgetVisibility();
            focusInput(quickFrequencyInput);
        }
        Platform.getHelper().sendToServer(new PacketRequestChannels());
    }

    private void resetCreateTabFields() {
        if (channelFrequencyInput != null) channelFrequencyInput.setValue("");
        if (channelNameInput != null) channelNameInput.setValue("");
        if (channelPasswordInput != null) channelPasswordInput.setValue("");
        privateChannel = false;
    }

    private void syncHeldFrequency() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        String heldFrequency = stack.getItem() instanceof WalkieTalkieItem
                ? WalkieTalkieItem.getFrequency(stack)
                : "";
        if (active && heldFrequency.isEmpty() && pendingAction == null) {
            currentFrequency = "";
            active = false;
            currentTab = Tab.CHANNELS;
            selectedChannel = null;
            showStatus("gui.walkietalkie.error.kicked_by_owner", false);
            updateWidgetVisibility();
            focusInput(quickFrequencyInput);
            Platform.getHelper().sendToServer(new PacketRequestChannels());
        }
    }

    private void showStatus(String messageKey, boolean success) {
        statusKey = messageKey;
        statusSuccess = success;
        statusExpiresAt = System.currentTimeMillis() + 3_500L;
    }

    private void updateMessageCache(List<ChannelMessageCache.ChatEntry> messages) {
        cachedLines.clear();
        cachedIsMine.clear();
        int maxWidth = PANEL_W - 28;
        int first = Math.max(0, messages.size() - 50);
        for (int i = first; i < messages.size(); i++) {
            ChannelMessageCache.ChatEntry entry = messages.get(i);
            boolean isMine = entry.senderName().equals(myName);

            net.minecraft.network.chat.MutableComponent formatted = Component.literal("> " + entry.senderName() + ": ")
                    .withStyle(style -> style.withColor(isMine ? 0xFF53FA53 : 0xFF82F282))
                    .append(Component.literal(entry.message())
                            .withStyle(style -> style.withColor(isMine ? 0xFFAAFFBB : 0xFFABC5A4)));

            for (FormattedCharSequence line : font.split(formatted, maxWidth)) {
                cachedLines.add(line);
                cachedIsMine.add(isMine);
            }
        }
    }

    private void updateChannelScrollFromMouse(double mouseY, Rect track, int channelCount) {
        int maxScroll = Math.max(0, channelCount - CHANNEL_ROWS);
        if (maxScroll == 0) {
            channelScrollOffset = 0;
            return;
        }
        int thumbHeight = Math.max(10, CHANNEL_ROWS * track.height() / channelCount);
        float percentage = (float) (mouseY - track.y() - thumbHeight / 2.0D) / (track.height() - thumbHeight);
        channelScrollOffset = Math.round(Mth.clamp(percentage, 0.0F, 1.0F) * maxScroll);
    }

    private void updateWidgetVisibility() {
        if (quickFrequencyInput == null) {
            return;
        }
        boolean showWidgets = !showConfirmPopup;
        boolean channels = showWidgets && currentTab == Tab.CHANNELS;
        boolean create = showWidgets && !active && currentTab == Tab.CREATE;
        quickFrequencyInput.visible = channels;
        joinPasswordInput.visible = channels && selectedChannel != null && selectedChannel.passwordProtected();
        channelFrequencyInput.visible = create;
        channelNameInput.visible = create;
        channelPasswordInput.visible = create && privateChannel;
        chatInput.visible = showWidgets && active && currentTab == Tab.CHAT;
    }

    private void rebindSelectedChannel(List<PacketSyncChannels.ChannelInfo> channels) {
        if (selectedChannel == null) {
            return;
        }
        String frequency = selectedChannel.frequency();
        selectedChannel = null;
        for (PacketSyncChannels.ChannelInfo channel : channels) {
            if (channel.frequency().equals(frequency)) {
                selectedChannel = channel;
                return;
            }
        }
        if (currentTab == Tab.CHANNELS) {
            focusInput(quickFrequencyInput);
        }
    }

    private List<PacketSyncChannels.ChannelInfo> sortedChannels() {
        List<PacketSyncChannels.ChannelInfo> channels = new ArrayList<>(ChannelCache.get());
        channels.sort(Comparator.comparingInt(info -> Integer.parseInt(info.frequency())));
        return channels;
    }

    private PacketSyncChannels.ChannelInfo findChannel(String frequency) {
        for (PacketSyncChannels.ChannelInfo info : ChannelCache.get()) {
            if (info.frequency().equals(frequency)) {
                return info;
            }
        }
        return null;
    }

    private EditBox createEditBox(Rect rect, int maxLength, String value, String hintKey) {
        EditBox input = new CenteredEditBox(font, rect.x(), rect.y(), rect.width(), rect.height(), Component.empty());
        input.setMaxLength(maxLength);
        input.setBordered(false);
        input.setTextColor(0xFFC4F2C7);
        input.setTextColorUneditable(C_TEXT_DIM);
        input.setValue(value);
        input.setHint(Component.translatable(hintKey).withStyle(style -> style.withColor(C_HINT & 0xFFFFFF)));
        return input;
    }

    private void focusInput(EditBox input) {
        quickFrequencyInput.setFocused(false);
        joinPasswordInput.setFocused(false);
        channelFrequencyInput.setFocused(false);
        channelNameInput.setFocused(false);
        channelPasswordInput.setFocused(false);
        chatInput.setFocused(false);
        setFocused(input);
        if (input != null) {
            input.setFocused(true);
        }
    }

    private EditBox createPasswordBox(Rect rect, String value, java.util.function.Supplier<Boolean> showPassword) {
        EditBox input = createEditBox(rect, 15, value, "gui.walkietalkie.password_hint");
        input.setFormatter((text, cursor) -> showPassword.get()
                ? FormattedCharSequence.forward(text, Style.EMPTY)
                : FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY));
        return input;
    }

    private boolean isNumericInput(String value) {
        if (value.isEmpty()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String normalizeFrequency(String value) {
        String normalized = value == null ? "" : value.trim();
        int index = 0;
        while (index < normalized.length() - 1 && normalized.charAt(index) == '0') {
            index++;
        }
        return normalized.substring(index);
    }

    private String ellipsize(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        String suffix = "...";
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width(suffix))) + suffix;
    }

    private String valueOf(EditBox input) {
        return input == null ? "" : input.getValue();
    }

    private int panelX() {
        return width / 2 - PANEL_W / 2;
    }

    private int panelY() {
        return height / 2 - PANEL_H / 2;
    }

    private int contentY() {
        return panelY() + HEADER_H + TAB_H;
    }

    private Rect channelListRect(int px, int contentY) {
        return new Rect(px + 8, contentY + 15, 180, 78);
    }

    private Rect channelRowRect(Rect list, int row) {
        return new Rect(list.x(), list.y() + row * 20, list.width(), 18);
    }

    private Rect channelDetailRect(int px, int contentY) {
        return new Rect(px + 198, contentY + 15, 114, 98);
    }

    private Rect joinPasswordRect(int px, int contentY) {
        return new Rect(px + 206, contentY + 78, 86, 14);
    }

    private Rect joinPasswordVisibilityRect(int px, int contentY) {
        return new Rect(px + 296, contentY + 78, 12, 14);
    }

    private Rect joinButtonRect(int px, int contentY, boolean passwordProtected) {
        return new Rect(px + 218, contentY + (passwordProtected ? 96 : 78), 74, 14);
    }

    private Rect quickInputRect(int px, int contentY) {
        return new Rect(px + 10, contentY + 111, 52, 14);
    }

    private Rect quickButtonRect(int px, int contentY) {
        return new Rect(px + 68, contentY + 109, 78, 18);
    }

    private Rect createFrequencyRect(int px, int contentY) {
        return new Rect(px + 18, contentY + 22, 70, 14);
    }

    private Rect createNameRect(int px, int contentY) {
        return new Rect(px + 18, contentY + 62, 176, 14);
    }

    private Rect visibilityCardRect(int px, int contentY) {
        return new Rect(px + 204, contentY + 8, 106, 82);
    }

    private Rect visibilityButtonRect(int px, int contentY) {
        return new Rect(px + 212, contentY + 20, 90, 18);
    }

    private Rect createPasswordRect(int px, int contentY) {
        return new Rect(px + 212, contentY + 62, 72, 14);
    }

    private Rect createPasswordVisibilityRect(int px, int contentY) {
        return new Rect(px + 290, contentY + 62, 12, 14);
    }

    private Rect createButtonRect(int px, int contentY) {
        return new Rect(px + 104, contentY + 99, 112, 18);
    }

    private Rect infoMembersRect(int px, int contentY) {
        return new Rect(px + 34, contentY + 34, 252, 58);
    }

    private Rect leaveButtonRect(int px, int contentY) {
        return new Rect(px + 119, contentY + 101, 82, 18);
    }

    private Rect chatAreaRect(int px, int contentY) {
        int footerY = panelY() + PANEL_H - STATUS_H - CHAT_FOOTER_H;
        return new Rect(px + 6, contentY + 2, PANEL_W - 12, footerY - contentY - 4);
    }

    private Rect chatFooterRect(int px) {
        int y = panelY() + PANEL_H - STATUS_H - CHAT_FOOTER_H;
        return new Rect(px + 2, y, PANEL_W - 4, CHAT_FOOTER_H);
    }

    private Rect chatInputRect(int px, int py) {
        return new Rect(px + 10, py + PANEL_H - STATUS_H - CHAT_FOOTER_H + 8, PANEL_W - 20, 14);
    }

    private Rect statusRect(int px, int py) {
        return new Rect(px + 2, py + PANEL_H - STATUS_H, PANEL_W - 4, STATUS_H - 2);
    }

    private void playSfx(net.minecraft.sounds.SoundEvent sound, float volume) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, volume));
        }
    }

    private void drawPasswordVisibilityButton(GuiGraphics graphics, Rect rect, boolean showPassword, boolean hovered) {
        int c = hovered ? C_TEXT : C_LABEL;
        graphics.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), C_DEEP);
        drawBorder(graphics, rect, hovered ? C_ACCENT : C_BORDER);
        int cx = rect.x() + 2;
        int cy = rect.y() + 4;
        graphics.fill(cx + 2, cy, cx + 6, cy + 1, c);
        graphics.fill(cx + 1, cy + 1, cx + 2, cy + 2, c);
        graphics.fill(cx + 6, cy + 1, cx + 7, cy + 2, c);
        graphics.fill(cx, cy + 2, cx + 1, cy + 4, c);
        graphics.fill(cx + 7, cy + 2, cx + 8, cy + 4, c);
        graphics.fill(cx + 1, cy + 4, cx + 2, cy + 5, c);
        graphics.fill(cx + 6, cy + 4, cx + 7, cy + 5, c);
        graphics.fill(cx + 2, cy + 5, cx + 6, cy + 6, c);
        if (showPassword) {
            graphics.fill(cx + 3, cy + 2, cx + 5, cy + 4, c);
        } else {
            graphics.fill(cx + 1, cy + 1, cx + 2, cy + 2, C_LOCK);
            graphics.fill(cx + 2, cy + 2, cx + 3, cy + 3, C_LOCK);
            graphics.fill(cx + 3, cy + 3, cx + 4, cy + 4, C_LOCK);
            graphics.fill(cx + 4, cy + 4, cx + 5, cy + 5, C_LOCK);
            graphics.fill(cx + 5, cy + 5, cx + 6, cy + 6, C_LOCK);
            graphics.fill(cx + 6, cy + 6, cx + 7, cy + 7, C_LOCK);
        }
    }

    @Override
    public void removed() {
        if (quickFrequencyInput != null) {
            savedQuickFrequency = quickFrequencyInput.getValue();
        }
        if (joinPasswordInput != null) {
            savedJoinPassword = joinPasswordInput.getValue();
        }
        if (channelFrequencyInput != null) {
            savedCreateFrequency = channelFrequencyInput.getValue();
        }
        if (channelNameInput != null) {
            savedCreateName = channelNameInput.getValue();
        }
        if (channelPasswordInput != null) {
            savedCreatePassword = channelPasswordInput.getValue();
        }
        if (currentTab != Tab.CHAT) {
            savedTab = currentTab;
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class CenteredEditBox extends EditBox {
        private CenteredEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int originalY = getY();
            try {
                setY(originalY + Math.max(0, (getHeight() - 8) / 2));
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
            } finally {
                setY(originalY);
            }
        }
    }

    private record Rect(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }
    }
}

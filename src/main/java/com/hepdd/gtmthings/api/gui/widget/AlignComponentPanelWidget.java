package com.hepdd.gtmthings.api.gui.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.hepdd.gtmthings.utils.FormatUtil;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class AlignComponentPanelWidget extends Widget {

    protected int maxWidthLimit;
    protected @Nullable Consumer<List<Component>> textSupplier;
    protected BiConsumer<String, ClickData> clickHandler;
    protected List<Component> lastText = new ArrayList<>();
    protected List<FormattedCharSequence> cacheLines = Collections.emptyList();
    protected boolean isCenter = false;
    protected int space = 2;
    protected String splitChar;

    public AlignComponentPanelWidget(int x, int y, @Nonnull Consumer<List<Component>> textSupplier) {
        super(x, y, 0, 0);
        this.textSupplier = textSupplier;
        this.textSupplier.accept(this.lastText);
    }

    public AlignComponentPanelWidget(int x, int y, List<Component> text) {
        super(x, y, 0, 0);
        this.lastText.addAll(text);
    }

    public static Component withButton(Component textComponent, String componentData) {
        Style style = textComponent.getStyle();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
        style = style.withColor(ChatFormatting.YELLOW);
        return textComponent.copy().withStyle(style);
    }

    public static Component withButton(Component textComponent, String componentData, int color) {
        Style style = textComponent.getStyle();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
        style = style.withColor(color);
        return textComponent.copy().withStyle(style);
    }

    public static Component withHoverTextTranslate(Component textComponent, Component hover) {
        Style style = textComponent.getStyle();
        style = style.withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, hover));
        return textComponent.copy().withStyle(style);
    }

    public AlignComponentPanelWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }

        return this;
    }

    public AlignComponentPanelWidget setCenter(boolean center) {
        this.isCenter = center;
        if (this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }

        return this;
    }

    public AlignComponentPanelWidget setSpace(int space) {
        this.space = space;
        if (this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }

        return this;
    }

    public AlignComponentPanelWidget setSplitChar(String splitChar) {
        this.splitChar = splitChar;
        return this;
    }

    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeVarInt(this.lastText.size());

        for (Component textComponent : this.lastText) {
            buffer.writeComponent(textComponent);
        }
    }

    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        this.readUpdateInfo(1, buffer);
    }

    public void initWidget() {
        super.initWidget();
        if (this.textSupplier != null) {
            this.lastText.clear();
            this.textSupplier.accept(this.lastText);
        }

        if (this.isClientSideWidget && this.isRemote()) {
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
    }

    public void updateScreen() {
        super.updateScreen();
        if (this.isClientSideWidget && this.textSupplier != null) {
            List<Component> textBuffer = new ArrayList();
            this.textSupplier.accept(textBuffer);
            if (!this.lastText.equals(textBuffer)) {
                this.lastText = textBuffer;
                this.formatDisplayText();
                this.updateComponentTextSize();
            }
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.textSupplier != null) {
            List<Component> textBuffer = new ArrayList();
            this.textSupplier.accept(textBuffer);
            if (!this.lastText.equals(textBuffer)) {
                this.lastText = textBuffer;
                this.writeUpdateInfo(1, (buffer) -> {
                    buffer.writeVarInt(this.lastText.size());

                    for (Component textComponent : this.lastText) {
                        buffer.writeComponent(textComponent);
                    }

                });
            }
        }
    }

    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.lastText.clear();
            int count = buffer.readVarInt();

            for (int i = 0; i < count; ++i) {
                this.lastText.add(buffer.readComponent());
            }

            this.formatDisplayText();
            this.updateComponentTextSize();
        }
    }

    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readUtf();
            if (this.clickHandler != null) {
                this.clickHandler.accept(componentData, clickData);
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updateComponentTextSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        int var10000 = this.cacheLines.size();
        Objects.requireNonNull(fontRenderer);
        int totalHeight = var10000 * (9 + this.space);
        if (totalHeight > 0) {
            totalHeight -= this.space;
        }

        if (this.isCenter) {
            this.setSize(new Size(this.maxWidthLimit, totalHeight));
        } else {
            int maxStringWidth = 0;

            for (FormattedCharSequence line : this.cacheLines) {
                maxStringWidth = Math.max(fontRenderer.width(line), maxStringWidth);
            }

            this.setSize(new Size(this.maxWidthLimit == 0 ? maxStringWidth : Math.min(this.maxWidthLimit, maxStringWidth), totalHeight));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void formatDisplayText() {
        Font fontRenderer = Minecraft.getInstance().font;
        int maxTextWidthResult = this.maxWidthLimit == 0 ? Integer.MAX_VALUE : this.maxWidthLimit;
        this.cacheLines = this.lastText.stream().flatMap((component) -> FormatUtil.formatJustifyComponent(component, maxTextWidthResult, fontRenderer, this.splitChar).stream()).toList();
    }

    @OnlyIn(Dist.CLIENT)
    protected @Nullable Style getStyleUnderMouse(double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = this.getPosition();
        Size size = this.getSize();
        double var10000 = mouseY - (double) position.y;
        Objects.requireNonNull(fontRenderer);
        double selectedLine = var10000 / (double) (9 + this.space);
        if (this.isCenter) {
            if (selectedLine >= (double) 0.0F && selectedLine < (double) this.cacheLines.size()) {
                FormattedCharSequence cacheLine = this.cacheLines.get((int) selectedLine);
                int lineWidth = fontRenderer.width(cacheLine);
                float offsetX = (float) position.x + (float) (size.width - lineWidth) / 2.0F;
                if (mouseX >= (double) offsetX) {
                    int mouseOffset = (int) (mouseX - (double) position.x);
                    return fontRenderer.getSplitter().componentStyleAtWidth(cacheLine, mouseOffset);
                }
            }
        } else if (mouseX >= (double) position.x && selectedLine >= (double) 0.0F && selectedLine < (double) this.cacheLines.size()) {
            FormattedCharSequence cacheLine = this.cacheLines.get((int) selectedLine);
            int mouseOffset = (int) (mouseX - (double) position.x);
            return fontRenderer.getSplitter().componentStyleAtWidth(cacheLine, mouseOffset);
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Style style = this.getStyleUnderMouse(mouseX, mouseY);
        if (style != null && style.getClickEvent() != null) {
            ClickEvent clickEvent = style.getClickEvent();
            String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                if (componentText.startsWith("@!")) {
                    String rawText = componentText.substring(2);
                    ClickData clickData = new ClickData();
                    if (this.clickHandler != null) {
                        this.clickHandler.accept(rawText, clickData);
                    }

                    this.writeClientAction(1, (buf) -> {
                        clickData.writeToBuf(buf);
                        buf.writeUtf(rawText);
                    });
                } else if (componentText.startsWith("@#")) {
                    String rawText = componentText.substring(2);
                    Util.getPlatform().openUri(rawText);
                }

                playButtonClickSound();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull @NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Style style = this.getStyleUnderMouse(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            HoverEvent hoverEvent = style.getHoverEvent();
            Component hoverTips = hoverEvent.getValue(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT);
            if (hoverTips != null) {
                this.gui.getModularUIGui().setHoverTooltip(List.of(hoverTips), ItemStack.EMPTY, null, null);
                return;
            }
        }

        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = this.getPosition();
        Size size = this.getSize();

        for (int i = 0; i < this.cacheLines.size(); ++i) {
            FormattedCharSequence cacheLine = this.cacheLines.get(i);
            if (this.isCenter) {
                int lineWidth = fontRenderer.width(cacheLine);
                int var10003 = position.x + (size.width - lineWidth) / 2;
                int var10004 = position.y;
                Objects.requireNonNull(fontRenderer);
                graphics.drawString(fontRenderer, cacheLine, var10003, var10004 + i * (9 + this.space), -1);
            } else {
                FormattedCharSequence var10002 = this.cacheLines.get(i);

                int var11 = position.x;
                int var12 = position.y;
                Objects.requireNonNull(fontRenderer);
                graphics.drawString(fontRenderer, var10002, var11, var12 + i * (9 + 2), -1);
            }
        }
    }

    public AlignComponentPanelWidget textSupplier(@Nullable Consumer<List<Component>> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public AlignComponentPanelWidget clickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public List<FormattedCharSequence> cacheLines() {
        return this.cacheLines;
    }
}

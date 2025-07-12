package com.hepdd.gtmthings.api.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberColor;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AlignLabelWidget extends Widget implements IConfigurableWidget {

    @Nonnull
    protected Supplier<String> textSupplier;
    @Nullable
    protected Component component;
    @Configurable(
                  name = "ldlib.gui.editor.name.text")
    private String lastTextValue;
    @Configurable(
                  name = "ldlib.gui.editor.name.color")
    @NumberColor
    private int color;
    @Configurable(
                  name = "ldlib.gui.editor.name.isShadow")
    private boolean dropShadow;

    public static String ALIGN_LEFT = "left";
    public static String ALIGN_CENTER = "center";
    public static String ALIGN_RIGHT = "right";

    private String textAlign;

    public AlignLabelWidget() {
        this(0, 0, "label");
    }

    public AlignLabelWidget(int xPosition, int yPosition, String text) {
        this(xPosition, yPosition, (Supplier) (() -> text));
    }

    public AlignLabelWidget(int xPosition, int yPosition, Component component) {
        super(new Position(xPosition, yPosition), new Size(10, 10));
        this.lastTextValue = "";
        this.setDropShadow(true);
        this.setTextColor(-1);
        this.setComponent(component);
        this.textAlign = ALIGN_LEFT;
    }

    public AlignLabelWidget(int xPosition, int yPosition, Supplier<String> text) {
        super(new Position(xPosition, yPosition), new Size(10, 10));
        this.lastTextValue = "";
        this.setDropShadow(true);
        this.setTextColor(-1);
        this.setTextProvider(text);
        this.textAlign = ALIGN_LEFT;
    }

    @ConfigSetter(
                  field = "lastTextValue")
    public void setText(String text) {
        this.textSupplier = () -> text;
        if (this.isRemote()) {
            this.lastTextValue = this.textSupplier.get();
            this.updateSize();
        }
    }

    public void setTextProvider(Supplier<String> textProvider) {
        this.textSupplier = textProvider;
        if (this.isRemote()) {
            this.lastTextValue = this.textSupplier.get();
            this.updateSize();
        }
    }

    public void setComponent(Component component) {
        this.component = component;
        if (this.isRemote()) {
            this.lastTextValue = component.getString();
            this.updateSize();
        }
    }

    /** @deprecated */
    @Deprecated
    public AlignLabelWidget setTextColor(int color) {
        this.color = color;
        if (this.component != null) {
            this.component = this.component.copy().withStyle(this.component.getStyle().withColor(color));
        }

        return this;
    }

    /** @deprecated */
    @Deprecated
    public AlignLabelWidget setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public AlignLabelWidget setTextAlign(String textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    public void setColor(int color) {
        this.setTextColor(color);
    }

    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        if (!this.isClientSideWidget) {
            if (this.component != null) {
                buffer.writeBoolean(true);
                buffer.writeComponent(this.component);
            } else {
                buffer.writeBoolean(false);
                this.lastTextValue = this.textSupplier.get();
                buffer.writeUtf(this.lastTextValue);
            }
        } else {
            buffer.writeBoolean(false);
            buffer.writeUtf(this.lastTextValue);
        }
    }

    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (buffer.readBoolean()) {
            this.component = buffer.readComponent();
            this.lastTextValue = this.component.getString();
        } else {
            this.lastTextValue = buffer.readUtf();
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!this.isClientSideWidget) {
            if (this.component != null) {
                String latest = this.component.getString();
                if (!latest.equals(this.lastTextValue)) {
                    this.lastTextValue = latest;
                    this.writeUpdateInfo(-2, (buffer) -> buffer.writeComponent(this.component));
                }
            }

            String latest = this.textSupplier.get();
            if (!latest.equals(this.lastTextValue)) {
                this.lastTextValue = latest;
                this.writeUpdateInfo(-1, (buffer) -> buffer.writeUtf(this.lastTextValue));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == -1) {
            this.lastTextValue = buffer.readUtf();
            this.updateSize();
        } else if (id == -2) {
            this.component = buffer.readComponent();
            this.lastTextValue = this.component.getString();
            this.updateSize();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (this.isClientSideWidget) {
            String latest = this.component == null ? this.textSupplier.get() : this.component.getString();
            if (!latest.equals(this.lastTextValue)) {
                this.lastTextValue = latest;
                this.updateSize();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updateSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        int var10003 = this.component == null ? fontRenderer.width(LocalizationUtils.format(this.lastTextValue)) : fontRenderer.width(this.component);
        Objects.requireNonNull(fontRenderer);
        this.setSize(new Size(var10003, 9));
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = this.getPosition();
        if (this.component == null) {
            String suppliedText = LocalizationUtils.format(this.lastTextValue);
            String[] split = suppliedText.split("\n");

            for (int i = 0; i < split.length; ++i) {
                int var10000 = position.y;
                Objects.requireNonNull(fontRenderer);
                int y = var10000 + i * (9 + 2);
                int x = position.x;
                switch (textAlign) {
                    case "left":
                        break;
                    case "center":
                        x = x - fontRenderer.width(split[i]) / 2;
                        break;
                    case "right":
                        x = x - fontRenderer.width(split[i]);
                        break;
                }
                graphics.drawString(fontRenderer, split[i], x, y, this.color, this.dropShadow);
            }
        } else {
            int x = position.x;
            switch (textAlign) {
                case "left":
                    break;
                case "center":
                    x = x - fontRenderer.width(this.component) / 2;
                    break;
                case "right":
                    x = x - fontRenderer.width(this.component);
                    break;
            }
            graphics.drawString(fontRenderer, this.component, x, position.y, this.color, this.dropShadow);
        }
    }

    public boolean handleDragging(Object dragging) {
        if (dragging instanceof String string) {
            this.setText(string);
            return true;
        } else {
            return IConfigurableWidget.super.handleDragging(dragging);
        }
    }

    public void setTextSupplier(@Nonnull Supplier<String> textSupplier) {
        if (textSupplier == null) {
            throw new NullPointerException("textSupplier is marked non-null but is null");
        } else {
            this.textSupplier = textSupplier;
        }
    }
}

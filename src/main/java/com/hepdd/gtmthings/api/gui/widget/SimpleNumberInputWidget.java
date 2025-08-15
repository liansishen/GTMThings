package com.hepdd.gtmthings.api.gui.widget;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class SimpleNumberInputWidget extends WidgetGroup {

    @Getter
    private final IntSupplier valueSupplier;
    @Getter
    private int min = defaultMin();
    @Getter
    private int max = defaultMax();

    private final IntConsumer onChanged;

    private TextFieldWidget textField;

    protected String toText(int value) {
        return String.valueOf(value);
    }

    protected int fromText(String value) {
        return Integer.parseInt(value);
    }

    protected int clamp(int value, int min, int max) {
        return Mth.clamp(value, min, max);
    }

    protected int defaultMin() {
        return 0;
    }

    protected int defaultMax() {
        return Integer.MAX_VALUE;
    }

    protected void setTextFieldRange(TextFieldWidget textField, int min, int max) {
        textField.setNumbersOnly(min, max);
    }

    public SimpleNumberInputWidget(int x, int y, int width, int height, IntSupplier valueSupplier,
                                   IntConsumer onChanged) {
        super(x, y, width, height);
        this.valueSupplier = valueSupplier;
        this.onChanged = onChanged;
        buildUI();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.textField.setCurrentString(toText(valueSupplier.getAsInt()));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeUtf(toText(valueSupplier.getAsInt()));
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        this.textField.setCurrentString(buffer.readUtf());
    }

    private void buildUI() {
        this.textField = new TextFieldWidget(0, 0, getSizeWidth(), 12,
                () -> toText(valueSupplier.getAsInt()),
                stringValue -> this.setValue(clamp(fromText(stringValue), min, max)));

        this.addWidget(this.textField);
    }

    public SimpleNumberInputWidget setValue(int value) {
        onChanged.accept(value);
        return this;
    }

    public SimpleNumberInputWidget setMin(int min) {
        this.min = min;
        updateTextFieldRange();

        return this;
    }

    public SimpleNumberInputWidget setMax(int max) {
        this.max = max;
        updateTextFieldRange();

        return this;
    }

    protected void updateTextFieldRange() {
        setTextFieldRange(textField, min, max);

        this.setValue(clamp(valueSupplier.getAsInt(), min, max));
    }
}

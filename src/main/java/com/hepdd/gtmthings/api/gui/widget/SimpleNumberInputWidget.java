package com.hepdd.gtmthings.api.gui.widget;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleNumberInputWidget extends WidgetGroup {

    @Getter
    private Supplier<Integer> valueSupplier;
    @Getter
    private Integer min = defaultMin();
    @Getter
    private Integer max = defaultMax();

    private final Consumer<Integer> onChanged;

    private TextFieldWidget textField;

    protected String toText(Integer value) {
        return String.valueOf(value);
    }

    protected Integer fromText(String value) {
        return Integer.parseInt(value);
    }

    protected Integer clamp(Integer value, Integer min, Integer max) {
        return Mth.clamp(value, min, max);
    }

    protected Integer defaultMin() {
        return 0;
    }

    protected Integer defaultMax() {
        return Integer.MAX_VALUE;
    }

    protected void setTextFieldRange(TextFieldWidget textField, Integer min, Integer max) {
        textField.setNumbersOnly(min, max);
    }

    public SimpleNumberInputWidget(int x, int y, int width, int height, Supplier<Integer> valueSupplier,
                                   Consumer<Integer> onChanged) {
        super(x, y, width, height);
        this.valueSupplier = valueSupplier;
        this.onChanged = onChanged;
        buildUI();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.textField.setCurrentString(toText(valueSupplier.get()));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeUtf(toText(valueSupplier.get()));
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        this.textField.setCurrentString(buffer.readUtf());
    }

    private void buildUI() {
        this.textField = new TextFieldWidget(0, 0, getSizeWidth(), 12,
                () -> toText(valueSupplier.get()),
                stringValue -> this.setValue(clamp(fromText(stringValue), min, max)));

        this.addWidget(this.textField);
    }

    public SimpleNumberInputWidget setValue(Integer value) {
        onChanged.accept(value);
        return this;
    }

    public SimpleNumberInputWidget setMin(Integer min) {
        this.min = min;
        updateTextFieldRange();

        return this;
    }

    public SimpleNumberInputWidget setMax(Integer max) {
        this.max = max;
        updateTextFieldRange();

        return this;
    }

    protected void updateTextFieldRange() {
        setTextFieldRange(textField, min, max);

        this.setValue(clamp(valueSupplier.get(), min, max));
    }
}

package com.hepdd.gtmthings.api.gui.widget

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.util.Mth

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup

import java.util.function.Consumer
import java.util.function.Supplier

open class SimpleNumberInputWidget(x: Int, y: Int, width: Int, height: Int, valueSupplier: Supplier<Int?>, onChanged: Consumer<Int?>) : WidgetGroup(x, y, width, height) {
    private var valueSupplier: Supplier<Int?>? = null

    private var min: Int = defaultMin()

    private var max: Int = defaultMax()

    private var onChanged: Consumer<Int?>? = null

    private var textField: TextFieldWidget? = null

    init {
        this.valueSupplier = valueSupplier
        this.onChanged = onChanged
        buildUI()
    }

    private fun toText(value: Int?): String? = value.toString()

    private fun fromText(value: String): Int = value.toInt()

    private fun clamp(value: Int, min: Int, max: Int): Int = Mth.clamp(value, min, max)

    private fun defaultMin(): Int = 0

    private fun defaultMax(): Int = Int.Companion.MAX_VALUE

    private fun setTextFieldRange(textField: TextFieldWidget, min: Int, max: Int) {
        textField.setNumbersOnly(min, max)
    }

    override fun initWidget() {
        super.initWidget()
        this.textField!!.setCurrentString(toText(valueSupplier!!.get()))
    }

    override fun writeInitialData(buffer: FriendlyByteBuf) {
        super.writeInitialData(buffer)
        buffer.writeUtf(toText(valueSupplier!!.get())!!)
    }

    override fun readInitialData(buffer: FriendlyByteBuf) {
        super.readInitialData(buffer)
        this.textField!!.setCurrentString(buffer.readUtf())
    }

    private fun buildUI() {
        this.textField = TextFieldWidget(
            0,
            0,
            sizeWidth,
            12,
            { toText(valueSupplier!!.get()) },
            { stringValue: String? -> this.setValue(clamp(fromText(stringValue!!), min, max)) },
        )

        this.addWidget(this.textField)
    }

    fun setValue(value: Int?): SimpleNumberInputWidget {
        onChanged!!.accept(value)
        return this
    }

    fun setMin(min: Int): SimpleNumberInputWidget {
        this.min = min
        updateTextFieldRange()

        return this
    }

    fun setMax(max: Int): SimpleNumberInputWidget {
        this.max = max
        updateTextFieldRange()

        return this
    }

    protected fun updateTextFieldRange() {
        setTextFieldRange(textField!!, min, max)

        this.setValue(clamp(valueSupplier!!.get()!!, min, max))
    }
}

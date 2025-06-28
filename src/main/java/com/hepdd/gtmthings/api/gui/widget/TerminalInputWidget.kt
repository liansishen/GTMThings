package com.hepdd.gtmthings.api.gui.widget

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.util.Mth

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup

import java.util.function.Consumer
import java.util.function.Supplier

open class TerminalInputWidget(x: Int, y: Int, width: Int, height: Int, valueSupplier: Supplier<Int?>, onChanged: Consumer<Int?>) : WidgetGroup(x, y, width, height) {
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
        this.textField = object : TextFieldWidget(
            0,
            0,
            sizeWidth,
            12,
            Supplier { toText(valueSupplier!!.get()) },
            Consumer { stringValue: String? -> this.setValue(clamp(fromText(stringValue!!), min, max)) },
        ) {
            override fun mouseWheelMove(mouseX: Double, mouseY: Double, wheelDelta: Double): Boolean {
                if (wheelDur > 0 && numberInstance != null && isMouseOverElement(mouseX, mouseY) && isFocus) {
                    try {
                        onTextChanged((getCurrentString().toInt() + ((if (wheelDelta > 0) 1 else -1) * wheelDur).toInt()).toString())
                    } catch (_: Exception) {
                    }
                    isFocus = true
                    return true
                }
                return false
            }
        }

        this.addWidget(this.textField)
    }

    fun setValue(value: Int?): TerminalInputWidget {
        onChanged!!.accept(value)

        return this
    }

    fun setMin(min: Int): TerminalInputWidget {
        this.min = min
        updateTextFieldRange()

        return this
    }

    fun setMax(max: Int): TerminalInputWidget {
        this.max = max
        updateTextFieldRange()

        return this
    }

    protected fun updateTextFieldRange() {
        setTextFieldRange(textField!!, min, max)

        this.setValue(clamp(valueSupplier!!.get()!!, min, max))
    }
}

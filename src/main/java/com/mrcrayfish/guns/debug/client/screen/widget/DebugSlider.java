package com.mrcrayfish.guns.debug.client.screen.widget;

import com.mrcrayfish.guns.debug.IDebugWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Fabric port: vanilla AbstractSliderButton replaces ForgeSlider. Rendering and the
 * Consumer<Double> callback follow the baseline ForgeSlider behavior.
 *
 * Author: MrCrayfish
 */
public class DebugSlider extends AbstractSliderButton implements IDebugWidget
{
    private final Consumer<Double> callback;
    private final double minValue;
    private final double maxValue;
    private final double stepSize;
    private final int precision;

    public DebugSlider(double minValue, double maxValue, double currentValue, double stepSize, int precision, Consumer<Double> callback)
    {
        super(0, 0, 0, 14, Component.empty(), Mth.clamp((currentValue - minValue) / (maxValue - minValue), 0.0, 1.0));
        this.callback = callback;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
        this.precision = precision;
        this.updateMessage();
    }

    public double getValue()
    {
        double raw = this.minValue + (this.maxValue - this.minValue) * this.value;
        return this.stepSize > 0 ? Math.round(raw / this.stepSize) * this.stepSize : raw;
    }

    private double getDisplayValue()
    {
        return Math.round(this.getValue() * Math.pow(10, this.precision)) / Math.pow(10, this.precision);
    }

    @Override
    protected void updateMessage()
    {
        this.setMessage(Component.literal(String.valueOf(this.getDisplayValue())));
    }

    @Override
    protected void applyValue()
    {
        this.callback.accept(this.getValue());
    }

    // Vanilla AbstractSliderButton renders the slider sprite and the message set by
    // updateMessage; the baseline ForgeSlider texture look differs cosmetically only.
}

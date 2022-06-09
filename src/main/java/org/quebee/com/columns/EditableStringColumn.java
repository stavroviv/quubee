package org.quebee.com.columns;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EditableStringColumn<T> extends ColumnInfo<T, String> {

    private final int width;
    private final Function<T, String> getter;
    private final BiConsumer<T, String> setter;

    public EditableStringColumn(String name, int width,
                                Function<T, String> getter,
                                BiConsumer<T, String> setter) {
        super(name);
        this.width = width;
        this.getter = getter;
        this.setter = setter;
    }

    public EditableStringColumn(String name, Function<T, String> getter, BiConsumer<T, String> setter) {
        super(name);
        this.width = -1;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public @Nullable String valueOf(T value) {
        return Objects.isNull(value) ? "" : getter.apply(value);
    }

    @Override
    public int getWidth(JTable table) {
        return width == 0 ? 50 : width;
    }

    @Override
    public void setValue(T variable, String value) {
        setter.accept(variable, value);
    }

    @Override
    public boolean isCellEditable(T variable) {
        return true;
    }
}

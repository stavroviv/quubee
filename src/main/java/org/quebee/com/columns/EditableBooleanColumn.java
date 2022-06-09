package org.quebee.com.columns;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EditableBooleanColumn<T> extends ColumnInfo<T, Boolean> {

    private final int width;
    private final Function<T, Boolean> getter;
    private final BiConsumer<T, Boolean> setter;

    public EditableBooleanColumn(String name, int width,
                                 Function<T, Boolean> getter,
                                 BiConsumer<T, Boolean> setter) {
        super(name);
        this.width = width;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public @Nullable Boolean valueOf(T tableElement) {
       return getter.apply(tableElement);
    }

    @Override
    public int getWidth(JTable table) {
        return width == 0 ? -1 : width;
    }

    @Override
    public void setValue(T variable, Boolean value) {
        setter.accept(variable, value);
    }

    @Override
    public Class<Boolean> getColumnClass() {
        return Boolean.class;
    }

    @Override
    public boolean isCellEditable(T variable) {
        return true;
    }
}

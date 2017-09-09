package br.ufpr.dinf.gres.opla.view.model;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.Arrays;
import java.util.List;

/**
 * @param <T>
 * @author Fernando
 */
@SuppressWarnings("rawtypes")
public class ComboModelBase<T> implements ComboBoxModel {

    private List<T> list;

    private T selected;

    public ComboModelBase(T[] type) {
        if (type != null) {
            this.list = Arrays.asList(type);
        }
    }

    @Override
    public T getSelectedItem() {
        return selected;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        selected = (T) anItem;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T getElementAt(int index) {
        return list.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

}

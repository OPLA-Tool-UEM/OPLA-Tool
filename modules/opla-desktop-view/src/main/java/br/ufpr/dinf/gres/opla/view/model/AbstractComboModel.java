package br.ufpr.dinf.gres.opla.view.model;

import java.util.Arrays;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Fernando
 * 
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class AbstractComboModel<T> implements ComboBoxModel {

    private List<T> list;

    private T selected;

    public AbstractComboModel(T[] type) {
        if (type != null) {
            this.list = Arrays.asList(type);
        }
    }

	@Override
	@SuppressWarnings("unchecked")
    public void setSelectedItem(Object anItem) {
        selected = (T) anItem;
    }

    @Override
    public T getSelectedItem() {
        return selected;
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

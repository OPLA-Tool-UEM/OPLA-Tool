package br.ufpr.dinf.gres.opla.view.model;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Fernando
 */
public abstract class TableModelBase<T> extends AbstractTableModel {

    protected List<T> lista;

    public void setLista(List<T> lista) {
        this.lista = lista;
    }

    @Override
    public int getRowCount() {
        if (lista == null) {
            return 0;
        }
        return lista.size();
    }

    @Override
    public abstract int getColumnCount();

    @Override
    public abstract String getColumnName(int column);

    @Override
    public abstract Object getValueAt(int rowIndex, int columnIndex);

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

}

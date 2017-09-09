package br.ufpr.dinf.gres.opla.view.model;

import br.ufpr.dinf.gres.opla.entity.Execution;

/**
 * @author Fernando
 */
public class TableModelExecution extends TableModelBase<Execution> {

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        String[] colunas = {"Run", "Time (min:seg)", "Genr. Solutions", "Non Dominated Solutions"};
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Execution obj = lista.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return obj.getId();
            case 1:
                return obj.getTime();
            case 2:
                return "";
            case 3:
                return "";

        }
        return obj;
    }

}

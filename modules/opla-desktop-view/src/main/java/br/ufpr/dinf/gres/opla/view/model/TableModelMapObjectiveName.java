package br.ufpr.dinf.gres.opla.view.model;

import br.ufpr.dinf.gres.opla.entity.MapObjectiveName;

/**
 * @author Fernando
 */
public class TableModelMapObjectiveName extends TableModelBase<MapObjectiveName> {

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        String[] colunas = new String[]{"Objective Function", "Value"};
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MapObjectiveName obj = lista.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return obj.getNames();
            case 1:
                return "";
        }
        return obj;
    }

}

package br.ufpr.dinf.gres.opla.view.model;

import br.ufpr.dinf.gres.opla.entity.Experiment;


/**
 * @author Fernando
 */
public class TableModelExperiment extends TableModelBase<Experiment> {

    @Override
    public int getColumnCount() {
        return 4;
    }
    
    @Override
    public String getColumnName(int column) {
        String[] colunas = {"ID", "PLA", "Algorithm", "Create At"};
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Experiment obj = lista.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return obj.getId();
            case 1:
                return obj.getName();
            case 2:
                return obj.getAlgorithm();
            case 3:
                return obj.getCreatedAt();

        }
        return obj;
    }

}

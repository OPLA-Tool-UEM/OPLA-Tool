package br.ufpr.dinf.gres.opla.view.model.tablemodel;

import br.ufpr.dinf.gres.opla.view.model.TableModelBase;
import metrics.PLAExtensibility;

/**
 * 
 * @author Fernando
 *
 */
public class PlaExtensibleMetricTableModel extends TableModelBase<PLAExtensibility> {

	private static final long serialVersionUID = 1L;

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int column) {
		String[] colunas = { "PLAExtensibility" };
		return colunas[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		PLAExtensibility obj = lista.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return obj.getPlaExtensibility();
		}
		return obj;
	}

}

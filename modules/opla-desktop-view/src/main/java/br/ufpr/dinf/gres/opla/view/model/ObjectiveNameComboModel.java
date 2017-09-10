package br.ufpr.dinf.gres.opla.view.model;

import java.util.List;

public class ObjectiveNameComboModel extends ComboModelBase<String> {

	public ObjectiveNameComboModel(List<String> values) {
		super(values);
	}

	public void setList(List<String> names) {
		this.list = names;
	}

}

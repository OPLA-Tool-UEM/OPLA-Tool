package br.ufpr.dinf.gres.opla.view.model;

import java.util.List;

/**
 *
 * @author Fernando
 */
public class SolutionNameComboModel extends ComboModelBase<String> {

	public SolutionNameComboModel(List<String> values) {
		super(values);
	}

	public void setList(List<String> list) {
		this.list = list;
	}

}

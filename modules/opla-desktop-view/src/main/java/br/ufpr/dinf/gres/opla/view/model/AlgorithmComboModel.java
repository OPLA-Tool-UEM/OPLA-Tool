package br.ufpr.dinf.gres.opla.view.model;

import br.ufpr.dinf.gres.opla.view.enumerators.AlgorithmType;

/**
 * @author Fernando
 */
public class AlgorithmComboModel extends ComboModelBase<AlgorithmType> {

    public AlgorithmComboModel() {
        super(AlgorithmType.values());
    }
}

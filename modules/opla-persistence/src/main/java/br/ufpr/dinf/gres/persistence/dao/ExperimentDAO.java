package br.ufpr.dinf.gres.persistence.dao;

import br.ufpr.dinf.gres.opla.entity.Experiment;
import br.ufpr.dinf.gres.persistence.util.GenericDAO;

/**
 * 
 * @author Fernando
 *
 */
public class ExperimentDAO extends GenericDAO<Experiment> {

	public ExperimentDAO() {
		super(Experiment.class);
	}

}

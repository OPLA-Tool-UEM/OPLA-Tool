package br.ufpr.dinf.gres.persistence.dao;

import java.util.List;

import javax.persistence.TypedQuery;

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
	
	public List<Experiment> findAllOrdened(){
		TypedQuery<Experiment> query = getEntityManager().createQuery("SELECT o FROM Experiment o ORDER BY o.id asc", Experiment.class);
		return query.getResultList();
	}

}

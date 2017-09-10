package br.ufpr.dinf.gres.persistence.dao;

import br.ufpr.dinf.gres.opla.entity.Execution;
import br.ufpr.dinf.gres.opla.entity.Experiment;
import br.ufpr.dinf.gres.persistence.util.GenericDAO;

import javax.persistence.TypedQuery;
import java.util.List;

public class ExecutionDAO extends GenericDAO<Execution> {

	public ExecutionDAO() {
		super(Execution.class);
	}

	public List<Execution> findByExperiment(Experiment experiment) {

		TypedQuery<Execution> query = getEntityManager()
				.createQuery("SELECT o FROM Execution o where o.experiment = :experiment", Execution.class);
		query.setParameter("experiment", experiment);

		return query.getResultList();
	}

}

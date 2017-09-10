package br.ufpr.dinf.gres.persistence.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import br.ufpr.dinf.gres.opla.entity.Execution;
import br.ufpr.dinf.gres.opla.entity.Experiment;
import br.ufpr.dinf.gres.opla.entity.Objective;
import br.ufpr.dinf.gres.persistence.util.GenericDAO;

/**
 * 
 * @author Fernando
 *
 */
public class ObjectiveDAO extends GenericDAO<Objective> {

	private static final Logger LOGGER = Logger.getLogger(ObjectiveDAO.class);

	public ObjectiveDAO() {
		super(Objective.class);
	}

	public Long countNonDominatedSolutionByExperiment(Experiment experiment) {
		LOGGER.debug("Counting Non Dominated Solution for experiment:  " + experiment.getId());

		TypedQuery<Long> query = getEntityManager().createQuery(
				"SELECT count(o.id) FROM Objective o WHERE o.experiment = :experiment AND o.execution is empty",
				Long.class);
		query.setParameter("experiment", experiment);

		Long result = Long.valueOf(0);

		try {
			LOGGER.debug("Number of Solution " + result);
			result = query.getSingleResult();
		} catch (NoResultException e) {
			LOGGER.warn(e);
		}

		LOGGER.debug("Number of Solution " + result);
		return result;

	}

	public Long countAllSoluctionsByExperimentAndExecution(Experiment experiment, Execution execution) {
		LOGGER.debug("Counting All Solution for experiment:  " + experiment.getId());

		TypedQuery<Long> query = getEntityManager().createQuery(
				"SELECT count(o.id) FROM Objective o WHERE o.experiment = :experiment AND (o.execution = :execution OR o.execution is empty)",
				Long.class);
		query.setParameter("experiment", experiment);
		query.setParameter("execution", execution);

		Long result = Long.valueOf(0);

		try {
			LOGGER.debug("Number of Solution " + result);
			result = query.getSingleResult();
		} catch (NoResultException e) {
			LOGGER.warn(e);
		}

		LOGGER.debug("Number of Solution " + result);
		return result;
	}

	public List<String> findNameSolutionByExperimentAndExecution(Experiment experiment, Execution execution) {
		LOGGER.debug("Listing name of soluctions by experiment = " + experiment.getId() + " and execution = "
				+ execution.getId());

		TypedQuery<String> query = getEntityManager().createQuery(
				"SELECT o.solutionName FROM Objective o WHERE o.experiment = :experiment AND (o.execution = :execution OR o.execution is empty)",
				String.class);
		query.setParameter("experiment", experiment);
		query.setParameter("execution", execution);

		List<String> resultList = query.getResultList();

		LOGGER.debug("Number of Soluction encoutered" + resultList.size());
		return resultList;
	}

}

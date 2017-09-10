package br.ufpr.dinf.gres.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

/**
 * Generic implementation for DAO Pattern
 *
 * @param <T>
 * @author Fernando
 */
public abstract class GenericDAO<T extends Serializable> {

	private static final Logger LOGGER = Logger.getLogger(GenericDAO.class);

	private final EntityManager em = PersistenceManager.getEntityManager();

	private final Class<T> clazz;

	public GenericDAO(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T findById(Integer id) {
		LOGGER.debug("Finding by id: " + id);
		return em.find(clazz, id);
	}

	public List<T> findAll() {
		LOGGER.debug("List all" + clazz.getSimpleName());
		TypedQuery<T> query = em.createQuery(" FROM " + clazz.getSimpleName(), clazz);
		List<T> resultList = query.getResultList();
		LOGGER.debug("Listing " + resultList.size() + " results");
		return resultList;
	}

	public void save(T clazz) {
		LOGGER.debug("Saving: " + clazz.getClass().getSimpleName());
		em.getTransaction().begin();
		em.persist(clazz);
		em.getTransaction().commit();
		LOGGER.debug("Saved Success");
	}

	public void udpate(T clazz) {
		LOGGER.debug("Updating: " + clazz.getClass().getSimpleName());
		em.getTransaction().begin();
		em.merge(clazz);
		em.getTransaction().commit();
		LOGGER.debug("Updated Success");
	}

	public void excluir(T clazz) {
		LOGGER.debug("Deleting: " + clazz.getClass().getSimpleName());
		em.getTransaction().begin();
		em.remove(clazz);
		em.getTransaction().commit();
		LOGGER.debug("Deleted Success");
	}

	public void excluir(Integer id) {
		excluir(findById(id));
	}

	public EntityManager getEntityManager() {
		return em;
	}
}

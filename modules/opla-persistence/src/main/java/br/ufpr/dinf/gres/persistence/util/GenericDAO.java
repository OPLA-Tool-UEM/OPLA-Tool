package br.ufpr.dinf.gres.persistence.util;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Generic implementation for DAO Pattern
 * 
 * @author Fernando
 *
 * @param <T>
 */
public abstract class GenericDAO<T extends Serializable> {

	private final EntityManager emf = PersistenceManager.getEntityManager();

	private final Class<T> clazz;

	public GenericDAO(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T getById(Integer id) {
		return emf.find(clazz, id);
	}

	public List<T> getAll() {
		TypedQuery<T> query = emf.createQuery(" FROM " + clazz.getSimpleName(), clazz);
		return query.getResultList();
	}

	public void save(T clazz) {
		emf.getTransaction().begin();
		emf.persist(clazz);
		emf.getTransaction().commit();
	}

	public void udpate(T clazz) {
		emf.getTransaction().begin();
		emf.merge(clazz);
		emf.getTransaction().commit();
	}

	public void excluir(T clazz) {
		emf.getTransaction().begin();
		emf.remove(clazz);
		emf.getTransaction().commit();
	}

	public void excluir(Integer id) {
		excluir(getById(id));
	}
	
	public EntityManager getEntityManager() {
		return emf;
	}
}

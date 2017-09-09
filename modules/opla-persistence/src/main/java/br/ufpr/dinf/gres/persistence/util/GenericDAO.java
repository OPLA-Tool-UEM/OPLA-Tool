package br.ufpr.dinf.gres.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * Generic implementation for DAO Pattern
 *
 * @param <T>
 * @author Fernando
 */
public abstract class GenericDAO<T extends Serializable> {

	private final EntityManager emf = PersistenceManager.getEntityManager();

	private final Class<T> clazz;

	public GenericDAO(Class<T> clazz) {
		this.clazz = clazz;
	}

	public T findById(Integer id) {
		return emf.find(clazz, id);
	}

	public List<T> findAll() {
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
		excluir(findById(id));
	}
	
	
    public List<T> getAll() {
        TypedQuery<T> query = emf.createQuery(" FROM " + clazz.getSimpleName(), clazz);
        return query.getResultList();
    }

    public EntityManager getEntityManager() {
        return emf;
    }
}

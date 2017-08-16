package br.ufpr.dinf.gres.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * 
 * @author Fernando
 *
 */
public class PersistenceManager {

	private static EntityManagerFactory entityManager;

	static {
		entityManager = Persistence.createEntityManagerFactory("oplaPU");
	}

	public static EntityManager getEntityManager() {
		return entityManager.createEntityManager();
	}

}

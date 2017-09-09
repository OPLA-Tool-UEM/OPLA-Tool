package br.ufpr.dinf.gres.persistence.dao;

import br.ufpr.dinf.gres.opla.entity.Execution;
import br.ufpr.dinf.gres.persistence.util.GenericDAO;

import javax.persistence.TypedQuery;
import java.util.List;

public class ExecutionDAO extends GenericDAO<Execution> {

    public ExecutionDAO() {
        super(Execution.class);
    }

    public List<Execution> findExecuttions() {
        TypedQuery<Execution> query = getEntityManager().createQuery("SELECT o FROM Execution o order by o.experiment desc", Execution.class);
        return query.getResultList();
    }

}

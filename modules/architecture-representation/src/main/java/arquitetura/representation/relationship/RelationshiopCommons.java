package arquitetura.representation.relationship;

import arquitetura.representation.Element;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RelationshiopCommons {

    /**
     * Retornar todos os relacionamentos para o elemento passado
     *
     * @param relationships - Lista com relacionamentos
     * @param element       - Elemento que se deseja recuperar relacionamentos
     * @return - set imutavel com os relacionamentos
     */
    public static Set<Relationship> getRelationships(Set<Relationship> relationships, Element element) {
        Set<Relationship> relations = new HashSet<>();
        for (Relationship r : relationships) {
            if (r.hasRelationshipWithElement(element))
                relations.add(r);
        }
        return Collections.unmodifiableSet(relations);
    }

}

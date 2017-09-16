package arquitetura.representation;

import arquitetura.helpers.UtilResources;
import arquitetura.representation.relationship.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationshipsHolder implements Serializable {

    private Set<Relationship> relationships = new HashSet<Relationship>();


    public void clearLists() {
        relationships.clear();
    }

    public Set<Relationship> getRelationships() {
        return Collections.unmodifiableSet(relationships);
    }

    public void setRelationships(Set<Relationship> rs) {
        relationships = rs;
    }

    /**
     * Dado um {@link Element} remove todos relacionamentos em que o elemento esteja envolvido
     *
     * @param element
     */
    public void removeRelatedRelationships(Element element) {
        relationships.removeIf(r -> r.hasRelationshipWithElement(element));
    }

    public Set<Relationship> getAllRelationships() {
        return Collections.unmodifiableSet(getRelationships());
    }

    public List<GeneralizationRelationship> getAllGeneralizations() {
        return UtilResources.getFilteredList(getRelationships(), GeneralizationRelationship.class);
    }

    public Stream<AssociationRelationship> getAllAssociationsRelationshipsStream() {
        return UtilResources.filterInstances(getRelationships(), AssociationRelationship.class);
    }

    public List<AssociationRelationship> getAllAssociationsRelationshipsNotCompOrAgreg() {
        return getAllAssociationsRelationshipsStream().filter(r -> !r.isAggregation() && !r.isComposition()).collect(Collectors.toList());
    }

    public List<AssociationRelationship> getAllAggregations() {
        return getAllAssociationsRelationshipsStream().filter(AssociationRelationship::isAggregation).collect(Collectors.toList());
    }


    public List<AssociationRelationship> getAllCompositions() {
        return getAllAssociationsRelationshipsStream().filter(AssociationRelationship::isComposition).collect(Collectors.toList());
    }

    public List<UsageRelationship> getAllUsage() {
        return UtilResources.getFilteredList(getRelationships(), UsageRelationship.class);
    }

    public List<DependencyRelationship> getAllDependencies() {
        return UtilResources.getFilteredList(getRelationships(), DependencyRelationship.class);
    }

    public List<RealizationRelationship> getAllRealizations() {
        return UtilResources.getFilteredList(getRelationships(), RealizationRelationship.class);
    }

    public List<AbstractionRelationship> getAllAbstractions() {
        return UtilResources.getFilteredList(getRelationships(), AbstractionRelationship.class);
    }

    public List<AssociationClassRelationship> getAllAssociationsClass() {
        return UtilResources.getFilteredList(getRelationships(), AssociationClassRelationship.class);
    }

    public <R extends Relationship> List<R> getAllRelationshipsOfType(java.lang.Class<R> type) {
        return UtilResources.getFilteredList(getRelationships(), type);
    }

    public boolean haveRelationship(Relationship relationship) {
        if(relationship instanceof AssociationRelationship) {
            AssociationRelationship associationRelationship = (AssociationRelationship) relationship;
            if (getAllAssociationsRelationshipsStream().map(AssociationRelationship::getParticipants)
                    .anyMatch(associationRelationship.getParticipants()::equals))
                return true;
        }

        return getAllRelationships().contains(relationship);

        //Association
        /*for (Relationship r : getAllRelationships()) {
            if ((r instanceof AssociationRelationship) && (relationship instanceof AssociationRelationship)) {
                final List<AssociationEnd> participantsNew = ((AssociationRelationship) relationship).getParticipants();
                final List<AssociationEnd> participantsExists = ((AssociationRelationship) r).getParticipants();

                if (participantsNew.equals(participantsExists))
                    return true;
            }
        }

        if (relationship instanceof GeneralizationRelationship)
            if (getAllGeneralizations().contains(relationship)) return true;
        if (relationship instanceof DependencyRelationship)
            if (getAllDependencies().contains(relationship)) return true;
        if (relationship instanceof UsageRelationship)
            if (getAllUsage().contains(relationship)) return true;
        if (relationship instanceof RealizationRelationship)
            if (getAllRealizations().contains(relationship)) return true;
        if (relationship instanceof AbstractionRelationship)
            if (getAllAbstractions().contains(relationship)) return true;
        if (relationship instanceof AssociationClassRelationship)
            if (getAllAssociationsClass().contains(relationship)) return true;

        return false;*/

    }

    public boolean removeRelationship(Relationship relation) {
        return this.relationships.remove(relation);
    }

    public boolean addRelationship(Relationship relationship) {
        return !haveRelationship(relationship) && this.relationships.add(relationship);
    }

}

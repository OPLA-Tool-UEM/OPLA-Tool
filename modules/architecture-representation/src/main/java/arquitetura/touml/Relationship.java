package arquitetura.touml;

import arquitetura.exceptions.NotSuppportedOperation;

/**
 * @author edipofederle<edipofederle@gmail.com>
 */
public interface Relationship {

    Relationship createRelation();

    Relationship between(String idElement) throws NotSuppportedOperation;

    Relationship and(String idElement) throws NotSuppportedOperation;

    String build();

    Relationship withMultiplicy(String string) throws NotSuppportedOperation;

}
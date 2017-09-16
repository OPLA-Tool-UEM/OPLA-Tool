package br.ufpr.inf.opla.patterns.operator.impl.jmetal5;

import arquitetura.helpers.UtilResources;
import arquitetura.representation.*;
import arquitetura.representation.Class;
import arquitetura.representation.Package;
import arquitetura.representation.relationship.GeneralizationRelationship;
import br.ufpr.inf.opla.patterns.solution.ArchitectureSolution;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;
import java.util.stream.Collectors;

public class PLACrossover implements CrossoverOperator<ArchitectureSolution> {

    private double probability;

    public PLACrossover(double probability) {
        this.probability = probability;
    }

    private <T> T randomObjectFromCollection(Collection<T> collection) {
        //avoid creating a list if unecessary
        if (collection.isEmpty()) return null;

        if (collection.size() == 1) {
            return collection.iterator().next();
        }

        List<T> tmpList;
        //avoid creating a NEW list if unecessary
        if (collection instanceof List) {
            tmpList = (List<T>) collection;
        } else {
            tmpList = new ArrayList<>(collection);
        }

        return tmpList.get(JMetalRandom.getInstance().getRandomGenerator().nextInt(0, tmpList.size() - 1));
    }

    public boolean isChild(Class cls) {
        if (cls == null) return false;
        GeneralizationRelationship gr = cls.getGeneralizationRelationship();
        return gr != null && gr.getChild().equals(cls);
    }

    public Class getGeneralizationParent(Class cls) {
        if (cls == null) return null;
        GeneralizationRelationship gr = cls.getGeneralizationRelationship();
        return gr != null && gr.getChild().equals(cls) ? (Class) gr.getParent() : null;
    }

    public Set<Element> getChildren(Element cls) {
        GeneralizationRelationship gr = cls.getGeneralizationRelationship();
        if (gr == null || !gr.getParent().equals(cls)) {
            return Collections.emptySet();
        }
        return gr.getAllChildrenForGeneralClass();
    }

    public List<ArchitectureSolution> FeatureCrossover(List<ArchitectureSolution> parents) {
        if (parents.size() != getNumberOfParents()) {
            throw new JMetalException("Invalid number of parents");
        }

        if (JMetalRandom.getInstance().nextDouble() > probability) {
            return parents.stream().map(ArchitectureSolution::copy).collect(Collectors.toList());
        }

        ArchitectureSolution offspring1 = parents.get(0).copy();
        ArchitectureSolution offspring2 = parents.get(1).copy();

        Architecture arch1 = offspring1.getArchitecture();
        Architecture arch2 = offspring2.getArchitecture();

        List<Concern> concernsArch1 = new ArrayList<>(arch1.getAllConcerns());
        Concern feature = randomObjectFromCollection(concernsArch1);

        obtainChild(feature, arch2, arch1);

        if (!isValidSolution(offspring1)) {
            offspring1 = parents.get(0).copy();
        }

        obtainChild(feature, arch1, arch2);
        if (!isValidSolution(offspring2)) {
            offspring2 = parents.get(1).copy();
        }

        return Arrays.asList(offspring1, offspring2);
    }

    private boolean isValidSolution(ArchitectureSolution mutatedSolution) {
        Architecture arch = mutatedSolution.getArchitecture();
        return arch.getAllInterfaces().stream().noneMatch(itf ->
                (itf.getImplementors().isEmpty())
                        && (itf.getDependents().isEmpty())
                        && (!itf.getOperations().isEmpty()));
    }

    private void obtainChild(Concern feature, Architecture parent, Architecture offspring) {
        //crossoverutils.removeArchitecturalElementsRealizingFeature
        removeArchitecturalElementsRealizingFeature(feature, offspring);

        addElementsToOffspring(feature, offspring, parent);
        updateVariabilitiesOffspring(offspring);
    }

    private void updateVariabilitiesOffspring(Architecture offspring) {
        for (Variability variability : offspring.getAllVariabilities()) {
            VariationPoint variationPoint = variability.getVariationPoint();
            if (variationPoint != null) {
                Element elementVP = variationPoint.getVariationPointElement();
                Element VP = offspring.findElementByName(elementVP.getName());
                if (VP != null && !VP.equals(elementVP)) {
                    variationPoint.replaceVariationPointElement(offspring.findElementByName(elementVP.getName(), "class"));
                }
            }
        }
    }

    private void addElementsToOffspring(Concern feature, Architecture offspring, Architecture parent) {
        List<Package> packages = new ArrayList<>(parent.getAllPackages());
        for (Package pkg : packages) {
            addOrCreatePackageIntoOffspring(feature, offspring, parent, pkg);
        }
    }

    private void addOrCreatePackageIntoOffspring(Concern feature, Architecture offspring, Architecture parent, Package parentPkg) {
        if (parentPkg.hasOnlyOneConcern(feature)) {
            Package pkgInOffspring = offspring.findPackageByName(parentPkg.getName());
            if (pkgInOffspring == null) {
                pkgInOffspring = offspring.createPackage(parentPkg.getName());
            }

            addInterfacesImplementedByPackageInOffspring(parentPkg, offspring);
            addInterfacesRequiredByPackageInOffspring(parentPkg, offspring);
            addInterfacesToPackageInOffspring(parentPkg, pkgInOffspring, offspring, parent);
            addClassesToOffspring(feature, parentPkg, pkgInOffspring, offspring, parent);
        } else {
            addClassesRealizingFeatureToOffspring(feature, parentPkg, offspring, parent);
            addInterfacesRealizingFeatureToOffspring(feature, parentPkg, offspring, parent);
        }
        saveAllRelationshipsForElement(parentPkg, offspring);
    }

    private void addInterfacesRealizingFeatureToOffspring(Concern feature, Package parentPkg, Architecture offspring, Architecture parent) {
        Package newPkg = offspring.findPackageByName(parentPkg.getName());
        if (newPkg == null) {
            newPkg = offspring.createPackage(parentPkg.getName());
            saveAllRelationshipsForElement(newPkg, offspring);
        }

        List<Interface> interfacesRealizingFeatureImplByPackage = parentPkg
                .getOnlyInterfacesImplementedByPackage()
                .stream().filter(feature::isOnlyConcernOfElement)
                .collect(Collectors.toList());

        addInterfacesInOffspring(offspring, interfacesRealizingFeatureImplByPackage);
    }

    private void addClassesRealizingFeatureToOffspring(Concern feature, Package parentPkg, Architecture offspring, Architecture parent) {
        List<Class> allClasses = new ArrayList<>(parentPkg.getAllClasses());
        Package newPkg = offspring.findPackageByName(parentPkg.getName());

        for (Class classComp : allClasses) {
            if (classComp.hasOnlyOneConcern(feature)) {
                if (newPkg == null) {
                    newPkg = offspring.createPackage(parentPkg.getName());
                }

                GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
                if (gr == null) {
                    newPkg.addExternalClass(classComp);
                    saveAllRelationshipsForElement(classComp, offspring);
                } else {
                    if (isHierarchyInASameComponent(classComp, parent)) {
                        moveHierarchyToSameComponent(classComp, newPkg, parentPkg, offspring, parent, feature);
                        saveAllRelationshipsForElement(classComp, offspring);
                    } else {
                        newPkg.addExternalClass(classComp);
                        moveHierarchyToSameComponent(classComp, newPkg, parentPkg, offspring, parent, feature);
                        saveAllRelationshipsForElement(classComp, offspring);
                    }
                }
            } else {
                GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
                //allLevels
                if (gr == null) {
                    addAttributesRealizingFeatureToOffspring(feature, classComp, parentPkg, offspring, parent);
                    addMethodsRealizingFeatureToOffspring(feature, classComp, parentPkg, offspring, parent);
                }
            }
            addInterfacesImplementedByClass(classComp, offspring);
            addInterfacesRequiredByClass(classComp, offspring);
        }
    }

    private void addMethodsRealizingFeatureToOffspring(Concern feature, Class classComp, Package parentPkg, Architecture offspring, Architecture parent) {
        List<Class> offspringClasses = offspring.findClassByName(classComp.getName());
        Class targetClass = null;
        if (offspringClasses != null) {
            targetClass = offspringClasses.get(0);
        }

        List<Method> classMethods = new ArrayList<>(classComp.getAllMethods());
        for (Method method : classMethods) {
            if (method.hasOnlyOneConcern(feature)) {
                if (targetClass == null) {
                    Package newPkg = offspring.findPackageByName(parentPkg.getName());
                    if (newPkg == null) {
                        newPkg = offspring.createPackage(parentPkg.getName());
                    }

                    targetClass = newPkg.createClass(classComp.getName(), false);
                    targetClass.addConcern(feature);
                }
                saveAllRelationshipsForElement(classComp, offspring);
                targetClass.addExternalMethod(method);
            }
        }

    }

    private void addAttributesRealizingFeatureToOffspring(Concern feature, Class classComp, Package parentPkg, Architecture offspring, Architecture parent) {
        List<Class> offspringClasses = offspring.findClassByName(classComp.getName());
        Class targetClass = null;
        if (offspringClasses != null) {
            targetClass = offspringClasses.get(0);
        }

        List<Attribute> classAttrs = new ArrayList<>(classComp.getAllAttributes());
        for (Attribute attr : classAttrs) {
            if (attr.hasOnlyOneConcern(feature)) {
                if (targetClass == null) {
                    Package newPkg = offspring.findPackageByName(parentPkg.getName());
                    if (newPkg == null) {
                        newPkg = offspring.createPackage(parentPkg.getName());
                    }

                    targetClass = newPkg.createClass(classComp.getName(), false);
                    targetClass.addConcern(feature);
                }
                saveAllRelationshipsForElement(classComp, offspring);
                targetClass.addExternalAttribute(attr);
            }
        }
    }

    private void addClassesToOffspring(Concern feature, Package parentPkg, Package pkgInOffspring, Architecture offspring, Architecture parent) {
        List<Class> allClasses = new ArrayList<>(parentPkg.getAllClasses());
        for (Class classComp : allClasses) {
            GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
            if (gr == null) {
                addClassToOffspring(classComp, pkgInOffspring, offspring);
            } else {
                if (isHierarchyInASameComponent(classComp, parent)) {
                    moveHierarchyToSameComponent(classComp, pkgInOffspring, parentPkg, offspring, parent, feature);
                } else {
                    pkgInOffspring.addExternalClass(classComp);
                    moveHierarchyToDifferentPackage(classComp, offspring, parent);
                }
                saveAllRelationshipsForElement(classComp, offspring);
            }
            addInterfacesImplementedByClass(classComp, offspring);
            addInterfacesRequiredByClass(classComp, offspring);
        }
    }

    private void addInterfacesRequiredByClass(Class classComp, Architecture offspring) {
        List<Interface> classImplIntf = new ArrayList<>(classComp.getRequiredInterfaces());
        addInterfacesInOffspring(offspring, classImplIntf);
    }

    private void addInterfacesImplementedByClass(Class classComp, Architecture offspring) {
        List<Interface> classImplIntf = new ArrayList<>(classComp.getImplementedInterfaces());
        addInterfacesInOffspring(offspring, classImplIntf);
    }

    private void moveHierarchyToDifferentPackage(Class cls, Architecture offspring, Architecture architecture) {
        Class root = cls;
        while (isChild(root)) {
            root = getGeneralizationParent(root);
        }
        moveChildrenToDifferentComponent(root, offspring, architecture);
    }

    private void moveChildrenToDifferentComponent(Class root, Architecture offspring, Architecture architecture) {
        String rootPkgName = UtilResources.extractPackageName(root.getNamespace());
        Package rootPkg = offspring.findPackageByName(rootPkgName);
        if (rootPkg == null) {
            rootPkg = offspring.createPackage(rootPkgName);
        }

        addClassToOffspring(root, rootPkg, offspring);
        saveAllRelationshipsForElement(architecture.findPackageByName(rootPkgName), offspring);
        for (Element child : getChildren(root)) {
            String pkgName = UtilResources.extractPackageName(child.getNamespace());
            Package targetPkg = architecture.findPackageByName(pkgName);
            if (targetPkg != null) {
                moveChildrenToDifferentComponent((Class) child, offspring, architecture);
            }
        }
    }

    private void moveHierarchyToSameComponent(Class cls, Package targetPkg, Package sourcePkg, Architecture offspring, Architecture parent, Concern feature) {
        Class root = cls;
        while (isChild(root)) {
            root = getGeneralizationParent(root);
        }

        if (sourcePkg.getAllClasses().contains(root)) {
            moveChildrenToSameComponent(root, sourcePkg, targetPkg, offspring, parent);
        }
    }

    private void moveChildrenToSameComponent(Class parent, Package sourcePkg, Package targetPkg, Architecture offspring, Architecture architecture) {
        Collection<Element> children = getChildren(parent);
        for (Element child : children) {
            moveChildrenToSameComponent((Class) child, sourcePkg, targetPkg, offspring, architecture);
        }

        if (sourcePkg.getAllClasses().contains(parent)) {
            addClassToOffspring(parent, targetPkg, offspring);
        } else {
            Package realTarget = targetPkg;
            //WARN um pouco diferente da versao original
            // a versao original parece mal-implementada
            for (Package parentArchPkg : architecture.getAllPackages()) {
                if (parentArchPkg.getAllClasses().contains(parent)) {
                    if (!parentArchPkg.nameEquals(targetPkg.getName())) {
                        Package newTarget = offspring.findPackageByName(parentArchPkg.getName());
                        if (newTarget == null) {
                            newTarget = offspring.createPackage(parentArchPkg.getName());
                            newTarget.addConcerns(parentArchPkg.getOwnConcerns());
                        }

                        realTarget = newTarget;
                        break;
                    }
                }
            }

            addClassToOffspring(parent, realTarget, offspring);
        }
    }

    private boolean isHierarchyInASameComponent(Class classComp, Architecture architecture) {
        Package componentOfClass = architecture.findPackageOfClass(classComp);

        Class parent = classComp;
        Package componentOfParent;
        while (isChild(parent)) {
            parent = getGeneralizationParent(parent);
            componentOfParent = architecture.findPackageOfClass(parent);
            if (!componentOfClass.equals(componentOfParent))
                return false;
        }
        return true;
    }

    private void addClassToOffspring(Class classComp, Package pkgInOffspring, Architecture offspring) {
        pkgInOffspring.addExternalClass(classComp);
        saveAllRelationshipsForElement(classComp, offspring);
    }

    private void addInterfacesToPackageInOffspring(Package parentPkg, Package pkgInOffspring, Architecture offspring, Architecture parent) {
        List<Interface> allInterfaces = new ArrayList<>(parentPkg.getAllInterfaces());
        for (Interface intf : allInterfaces) {
            pkgInOffspring.addExternalInterface(intf);
            saveAllRelationshipsForElement(intf, offspring);
        }
    }

    private void addInterfacesRequiredByPackageInOffspring(Package parentPkg, Architecture offspring) {
        List<Interface> interfacesRequiredByPackage = new ArrayList<>(parentPkg.getOnlyInterfacesRequiredByPackage());
        addInterfacesInOffspring(offspring, interfacesRequiredByPackage);

    }

    private void addInterfacesImplementedByPackageInOffspring(Package parentPkg, Architecture offspring) {
        List<Interface> interfacesImplByPkg = new ArrayList<>(parentPkg.getOnlyInterfacesImplementedByPackage());
        addInterfacesInOffspring(offspring, interfacesImplByPkg);
    }

    private void addInterfacesInOffspring(Architecture offspring, List<Interface> interfacesRequiredByPackage) {
        for (Interface interfacePkg : interfacesRequiredByPackage) {
            if (interfacePkg.getNamespace().equalsIgnoreCase("model")) {
                offspring.addExternalInterface(interfacePkg);
            } else {
                String packageName = UtilResources.extractPackageName(interfacePkg.getNamespace());

                Package pkg = offspring.findPackageByName(packageName);
                if (pkg == null) {
                    pkg = offspring.createPackage(packageName);
                }

                pkg.addExternalInterface(interfacePkg);
            }
            saveAllRelationshipsForElement(interfacePkg, offspring);
        }
    }

    private void saveAllRelationshipsForElement(Element element, Architecture offspring) {
        element.getRelationships().forEach(offspring.getRelationshipHolder()::addRelationship);
    }

    private void removeArchitecturalElementsRealizingFeature(Concern feature, Architecture offspring) {
        List<Package> allPackages = new ArrayList<>(offspring.getAllPackages());
        for (Package comp : allPackages) {
            if (comp.hasOnlyOneConcern(feature)) {
                List<Interface> allInterfacesComp = new ArrayList<>(comp.getImplementedInterfaces());
                if (!allInterfacesComp.isEmpty()) {
                    for (Interface interfaceComp : allInterfacesComp) {
                        offspring.removeInterface(interfaceComp);
                    }
                }
                removeClassesFromComponent(comp, offspring);
                offspring.removePackage(comp);
            } else {
                removeInterfacesRealizingFeatureFromComponent(comp, feature, offspring);
                removeClassesRealizingFeatureFromComponent(comp, feature, offspring);
            }
        }
    }

    private void removeClassesRealizingFeatureFromComponent(Package comp, Concern feature, Architecture offspring) {
        List<Class> allClasses = new ArrayList<>(comp.getAllClasses());
        for (Class classComp : allClasses) {
            if (classComp.hasOnlyOneConcern(feature)) {
                GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
                if (gr == null) {
                    comp.removeClass(classComp);
                } else {
                    removeHierarchyOfComponent(classComp, comp, offspring);
                }
            } else {
                //scope == "allLevels"?
                removeAttributesRealizingFeatureOfClass(classComp, feature);
                removeMethodsRealizingFeatureOfClass(classComp, feature);
            }
        }
    }

    private void removeAttributesRealizingFeatureOfClass(Class cls, Concern feature) {
        List<Attribute> attrsCls = new ArrayList<>(cls.getAllAttributes());
        for (Attribute attribute : attrsCls) {
            if (attribute.hasOnlyOneConcern(feature)) {
                if (cls.getGeneralizationRelationship() == null) {
                    cls.removeAttribute(attribute);
                }
            }
        }
    }

    private void removeMethodsRealizingFeatureOfClass(Class cls, Concern feature) {
        List<Method> clsMethods = new ArrayList<>(cls.getAllMethods());
        for (Method method : clsMethods) {
            if (method.hasOnlyOneConcern(feature)) {
                if (cls.getGeneralizationRelationship() == null) {
                    cls.removeMethod(method);
                }
            }
        }

    }

    private void removeInterfacesRealizingFeatureFromComponent(Package comp, Concern feature, Architecture offspring) {
        List<Interface> allInterface = new ArrayList<>(comp.getImplementedInterfaces());
        for (Interface interfaceComp : allInterface) {
            if (interfaceComp.hasOnlyOneConcern(feature)) {
                offspring.removeInterface(interfaceComp);
            } else {
                removeOperationsRealizingFeatureOfInterface(interfaceComp, feature);
            }
        }
    }

    private void removeOperationsRealizingFeatureOfInterface(Interface interfaceComp, Concern feature) {
        List<Method> methodsIntfComp = new ArrayList<>(interfaceComp.getOperations());
        for (Method method : methodsIntfComp) {
            if (method.hasOnlyOneConcern(feature)) {
                interfaceComp.removeOperation(method);
            }
        }
    }

    private void removeClassesFromComponent(Package comp, Architecture offspring) {
        List<Class> allClasses = new ArrayList<>(comp.getAllClasses());
        for (Class classComp : allClasses) {
            if (comp.getAllClasses().contains(classComp)) {
                GeneralizationRelationship gr = classComp.getGeneralizationRelationship();
                if (gr == null) {
                    comp.removeClass(classComp);
                } else {
                    removeHierarchyOfComponent(classComp, comp, offspring);
                }
            }
        }

    }

    private void removeHierarchyOfComponent(Class cls, Package comp, Architecture architecture) {
        Class parent = cls;
        while (isChild(cls)) {
            parent = getGeneralizationParent(cls);
        }

        removeChildrenOfComponent(parent, comp, architecture);
    }

    private void removeChildrenOfComponent(Element cls, Package comp, Architecture architecture) {
        Set<Element> children = getChildren(cls);
        for (Element child : children) {
            removeChildrenOfComponent(child, comp, architecture);
        }

        if (cls instanceof Class) {
            if (comp.getAllClasses().contains(cls)) {
                comp.removeClass(cls);
            } else {
                for (Package archPkg : architecture.getAllPackages()) {
                    if (archPkg.getAllClasses().contains(cls)) {
                        archPkg.removeClass(cls);
                        break;
                    }
                }
            }
        }
    }


    @Override
    public int getNumberOfParents() {
        return 2;
    }

    @Override
    public List<ArchitectureSolution> execute(List<ArchitectureSolution> architectureSolutions) {
        return FeatureCrossover(architectureSolutions);
    }
}

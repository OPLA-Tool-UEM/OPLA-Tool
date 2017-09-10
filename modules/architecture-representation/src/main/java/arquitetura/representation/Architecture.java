package arquitetura.representation;

import arquitetura.exceptions.ClassNotFound;
import arquitetura.flyweights.VariabilityFlyweight;
import arquitetura.flyweights.VariantFlyweight;
import arquitetura.flyweights.VariationPointFlyweight;
import arquitetura.helpers.UtilResources;
import arquitetura.main.GenerateArchitecture;
import arquitetura.representation.relationship.DependencyRelationship;
import arquitetura.representation.relationship.RealizationRelationship;
import arquitetura.representation.relationship.Relationship;
import com.rits.cloning.Cloner;
import jmetal4.core.Variable;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author edipofederle<edipofederle@gmail.com>
 */
public class Architecture extends Variable {
    private static final long serialVersionUID = -7764906574709840088L;
    public static String ARCHITECTURE_TYPE = "arquitetura.representation.Architecture";
    private static Logger LOGGER = LogManager.getLogger(Architecture.class.getName());
    //    private Cloner cloner;
    private Set<Package> packages = new HashSet<Package>();
    private Set<Class> classes = new HashSet<Class>();
    private Set<Interface> interfaces = new HashSet<Interface>();
    private String name;
    private boolean appliedPatterns;

    private RelationshipsHolder relationshipHolder = new RelationshipsHolder();


    public Architecture(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public List<Element> getElements() {
        return Stream.concat(getAllPackages().stream().map(Package::getElements).flatMap(Set::stream),
                Stream.concat(this.classes.stream(), this.interfaces.stream()))
                .collect(Collectors.toList());
    }


    public Collection<Concern> getAllConcerns() {
        return ConcernHolder.INSTANCE.getConcerns().values();
    }

    /**
     * Retorna um Map imutável. É feito isso para garantir que nenhum modificação seja
     * feita diretamente na lista
     * <p>
     * Set<Package>
     *
     * @return Set<Package>
     */
    public Set<Package> getAllPackages() {
        return Collections.unmodifiableSet(this.packages);
    }

    /**
     * Retorna interfaces que não tem nenhum pacote.
     * <p>
     * Retorna um Set imutável. É feito isso para garantir que nenhum modificação seja
     * feita diretamente na lista.
     *
     * @return Set<Class>
     */
    public Set<Interface> getInterfaces() {
        return Collections.unmodifiableSet(this.interfaces);
    }

    /**
     * Retorna todas as interfaces que existem na arquiteutra.
     * Este método faz um merge de todas as interfaces de todos os pacotes + as interfaces que não tem pacote
     *
     * @return
     */
    public Set<Interface> getAllInterfaces() {
        return Stream.concat(this.packages.stream().map(Package::getAllInterfaces).flatMap(Set::stream),
                             this.interfaces.stream())
                .collect(Collectors.toSet());
    }

    /**
     * Retorna classes que não tem nenhum pacote.
     * <p>
     * Retorna um Set imutável. É feito isso para garantir que nenhum modificação seja
     * feita diretamente na lista
     *
     * @return Set<Class>
     */
    public Set<Class> getClasses() {
        return Collections.unmodifiableSet(this.classes);
    }

    /**
     * Retorna todas as classes que existem na arquiteutra.
     * Este método faz um merge de todas as classes de todos os pacotes + as classes que não tem pacote
     *
     * @return
     */
    public Set<Class> getAllClasses() {
        return Stream.concat(this.packages.stream().map(Package::getAllClasses).flatMap(Set::stream),
                             this.classes.stream())
                .collect(Collectors.toSet());
    }

    /**
     * Busca elemento por nome.<br/>
     * <p>
     * No momento busca por class, interface ou package <br/>
     * <p>
     * <p>
     * TODO refatorar para buscar todo tipo de elemento
     *
     * @param name - Nome do elemento
     * @return
     * @parm type - tipo do elemento (class, interface ou package)
     */
    public Element findElementByName(String name, String type) {
        return findElement(name, type);
    }

    private Element findElement(String name, String type) {
        Optional<Element> maybeElement = Optional.empty();

        if (type.equalsIgnoreCase("class")) {

            maybeElement = getClasses().stream().map(Element.class::cast)
                    .filter(element -> element.nameEquals(name)).findAny();

        } else if (type.equalsIgnoreCase("interface")) {

            maybeElement = getInterfaces().stream().map(Element.class::cast)
                    .filter(element -> element.nameEquals(name)).findAny();

        }

        if (!maybeElement.isPresent()) {
            maybeElement = getAllPackages().stream().map(Package::getElements).flatMap(Set::stream)
                    .filter(element -> element.nameEquals(name)).findAny();
        }

        return maybeElement.orElse(null);
    }


    /**
     * Recupera uma classe por nome.
     *
     * @param className
     * @return {@link Class}
     */
    public List<Class> findClassByName(String className) {
        List<Class> classesFound =
                Stream.concat(getClasses().stream(), this.packages.stream().flatMap(p -> p.getAllClasses().stream()))
                        .filter(klass -> className.trim().equalsIgnoreCase(klass.getName().trim()))
                        .collect(Collectors.toList());

        if (classesFound.isEmpty())
            return null;
        return classesFound;
    }

    /**
     * Busca elemento por nome.
     *
     * @param elementName
     * @return - null se nao encontrar
     */
    public Element findElementByName(String elementName) {
        Element element = deepPackageSearch(this.packages, elementName);
        if (element == null) {
            Optional<Element> maybeElement = Stream.concat(this.classes.stream(), this.interfaces.stream())
                    .filter(elm -> elm.getName().equals(elementName)).findFirst();
            if(maybeElement.isPresent()) return maybeElement.get();

            LOGGER.info("No element called: " + elementName + " found");
        }
        return element;
    }

    private Element deepPackageSearch(Set<Package> packages, String elementName) {
        return packages.stream().map(p -> {
            if(p.nameEquals(elementName))
                return p;

            for(Element e : p.getElements()) {
                if(e.nameEquals(elementName))
                    return e;
            }

            Element recursiveSearch = deepPackageSearch(p.getNestedPackages(), elementName);
            if(recursiveSearch != null)
                return recursiveSearch;

            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Interface findInterfaceByName(String interfaceName) {
        return Stream.concat(getInterfaces().stream(), packages.stream().flatMap(p->p.getAllInterfaces().stream()))
                .filter(i->i.nameEquals(interfaceName)).findFirst().orElse(null);
    }

    /**
     * Busca um pacote por nome.
     *
     * @param packageName
     * @return Package
     */
    public Package findPackageByName(String packageName) {
        return getAllPackages().stream().filter(pkg -> packageName.equalsIgnoreCase(pkg.getName())).findFirst().orElse(null);

    }


    public Package createPackage(String packageName) {
        Package pkg = new Package(getRelationshipHolder(), packageName);
        this.packages.add(pkg);
        return pkg;
    }

    public Package createPackage(String packageName, String id) {
        Package pkg = new Package(getRelationshipHolder(), packageName, id);
        this.packages.add(pkg);
        return pkg;
    }

    /**
     * Remove qualquer relacionamento que os elementos do pacote
     * que esta sendo deletado possa ter.
     */
    public void removePackage(Package p) {
        p.getElements().forEach(element -> relationshipHolder.removeRelatedRelationships(element));
        //Remove os relacionamentos que o pacote possa pertencer
        relationshipHolder.removeRelatedRelationships(p);

        this.packages.remove(p);
        LOGGER.info("Pacote:" + p.getName() + "removido");
    }

    public Interface createInterface(String interfaceName) {
        Interface interfacee = new Interface(getRelationshipHolder(), interfaceName);
        this.addExternalInterface(interfacee);
        return interfacee;
    }

    public Interface createInterface(String interfaceName, String id) {
        Interface interfacee = new Interface(getRelationshipHolder(), interfaceName, id);
        this.addExternalInterface(interfacee);
        return interfacee;
    }

    public Class createClass(String klassName, boolean isAbstract) {
        Class klass = new Class(getRelationshipHolder(), klassName, isAbstract);
        this.addExternalClass(klass);
        return klass;
    }

    public void removeInterface(Interface interfacee) {
        interfacee.removeInterfaceFromRequiredOrImplemented();
        relationshipHolder.removeRelatedRelationships(interfacee);
        if (removeInterfaceFromArch(interfacee)) {
            LOGGER.info("Interface:" + interfacee.getName() + " removida da arquitetura");
        }
    }


    private boolean removeInterfaceFromArch(Interface interfacee) {
        return this.interfaces.remove(interfacee) || this.packages.stream().anyMatch(p -> p.removeInterface(interfacee));
    }

    public void removeClass(Element klass) {
        if(!(klass instanceof Class)) return;

        relationshipHolder.removeRelatedRelationships(klass);
        if (this.classes.remove(klass))
            LOGGER.info("Classe " + klass.getName() + "(" + klass.getId() + ") removida da arquitetura");

        this.getAllPackages().stream().filter(pkg -> pkg.getAllClasses().contains(klass))
                .filter(pkg -> pkg.removeClass(klass))
                .forEach(pkg -> LOGGER.info("Classe " + klass.getName() + "(" + klass.getId() + ") removida da arquitetura. Pacote(" + pkg.getName() + ")"));
    }

    public List<VariationPoint> getAllVariationPoints() {
        return VariationPointFlyweight.getInstance().getVariationPoints();
    }

    public List<Variant> getAllVariants() {
        return VariantFlyweight.getInstance().getVariants();
    }

    public List<Variability> getAllVariabilities() {
        return VariabilityFlyweight.getInstance().getVariabilities();
    }

    public Class findClassById(String idClass) throws ClassNotFound {
        return Stream.concat(getClasses().stream(), getAllPackages().stream().map(Package::getAllClasses).flatMap(Set::stream))
                .filter(c -> idClass.equalsIgnoreCase(c.getId())).findAny()
                .orElseThrow(() -> new ClassNotFound("Class " + idClass + " can not found.\n"));
    }

    public Interface findInterfaceById(String idClass) throws ClassNotFound {
        return getInterfaces().stream().filter(i -> idClass.equalsIgnoreCase(i.getId()))
                .findAny().orElseThrow(() -> new ClassNotFound("Class " + idClass + " can not found.\n"));
    }

    public void addExternalInterface(Interface interface_) {
        if (interfaces.add(interface_))
            LOGGER.info("Interface: " + interface_.getName() + " adicionada na arquiteutra");
        else
            LOGGER.info("TENTOU adicionar a interface : " + interface_.getName() + " na arquiteutra, porém não conseguiu");
    }

    /**
     * Retorna classe contendo método para manipular relacionamentos
     *
     * @return OperationsOverRelationships
     */
    public OperationsOverRelationships operationsOverRelationship() {
        return new OperationsOverRelationships(this);
    }

    public OperationsOverAssociation forAssociation() {
        return new OperationsOverAssociation(relationshipHolder);
    }

    public OperationsOverDependency forDependency() {
        return new OperationsOverDependency(relationshipHolder);
    }

    public void moveElementToPackage(Element klass, Package pkg) {
        if (pkg.getElements().contains(klass)) {
            return;
        }
        String oldPackageName = UtilResources.extractPackageName(klass.getNamespace());
        if (this.packages.contains(pkg)) {
            if (oldPackageName.equals("model")) {
                addClassOrInterface(klass, pkg);
                this.removeOnlyElement(klass);
            } else {
                Package oldPackage = this.findPackageByName(oldPackageName);
                if (oldPackage != null) {
                    addClassOrInterface(klass, pkg);
                    oldPackage.removeOnlyElement(klass);
                }
            }
        }
        klass.setNamespace(ArchitectureHolder.getName() + "::" + pkg.getName());
    }

    private void addClassOrInterface(Element klass, Package pkg) {
        if (klass instanceof Class) {
            pkg.addExternalClass((Class) klass);
        } else if (klass instanceof Interface) {
            pkg.addExternalInterface((Interface) klass);
        }
    }


    public OperationsOverGeneralization forGeneralization() {
        return new OperationsOverGeneralization(this);
    }


    public OperationsOverAbstraction forAbstraction() {
        return new OperationsOverAbstraction(this);
    }

    public boolean removeRelationship(Relationship as) {
        if (as == null) return false;
        if (relationshipHolder.removeRelationship(as)) {
            LOGGER.info("Relacionamento : " + as.getType() + " removido da arquitetura");
            return true;
        } else {
            LOGGER.info("TENTOU remover Relacionamento : " + as.getType() + " da arquitetura porém não consegiu");
            return false;
        }
    }

    public OperationsOverUsage forUsage() {
        return new OperationsOverUsage(this);
    }

    /**
     * Create an exact copy of the <code>Architecture</code> object.
     *
     * @return An exact copy of the object.
     */
    public Variable deepCopy() {
        return this.deepClone();
    }

    public Architecture deepClone() {
        return SerializationUtils.clone(this);
    }

    public boolean addImplementedInterface(Interface supplier, Element genericElement) {
        if (genericElement instanceof Class) {
            return addImplementedInterface(supplier, (Class) genericElement);
        } else if (genericElement instanceof Package) {
            return addImplementedInterface(supplier, (Package) genericElement);
        }

        return true;
    }

    public boolean addImplementedInterface(Interface supplier, Class client) {
        if (!haveRelationship(supplier, client)) {
            if (addRelationship(new RealizationRelationship(client, supplier, "", UtilResources.getRandonUUID()))) {
                LOGGER.info("ImplementedInterface: " + supplier.getName() + " adicionada na classe: " + client.getName());
                return true;
            } else {
                LOGGER.info("Tentou adicionar a interface " + supplier.getName() + " como interface implementada pela classe: " + client.getName());
                return false;
            }
        }
        return false;
    }

    private boolean haveRelationship(Interface supplier, Element client) {
        return relationshipHolder.getAllRelationships().stream().anyMatch(r -> {
            if (r instanceof RealizationRelationship) {
                RealizationRelationship rR = (RealizationRelationship) r;
                if (rR.getClient().equals(client) && rR.getSupplier().equals(supplier))
                    return true;
            } else if (r instanceof DependencyRelationship) {
                DependencyRelationship dR = (DependencyRelationship) r;
                if (dR.getClient().equals(client) && dR.getSupplier().equals(supplier))
                    return true;
            }
            return false;
        });

    }

    public boolean addImplementedInterface(Interface supplier, Package client) {
        if (!haveRelationship(supplier, client)) {
            if (addRelationship(new RealizationRelationship(client, supplier, "", UtilResources.getRandonUUID()))) {
                LOGGER.info("ImplementedInterface: " + supplier.getName() + " adicionada ao pacote: " + client.getName());
                return true;
            } else {
                LOGGER.info("Tentou adicionar a interface " + supplier.getName() + " como interface implementada no pacote: " + client.getName());
                return false;
            }
        }
        return false;
    }

    public void removeImplementedInterface(Interface inter, Package pacote) {
        pacote.removeImplementedInterface(inter);
        relationshipHolder.removeRelatedRelationships(inter);
    }

    public void addRequiredInterface(Interface supplier, Class client) {
        if (!haveRelationship(supplier, client)) {
            if (addRelationship(new DependencyRelationship(supplier, client, "", UtilResources.getRandonUUID())))
                LOGGER.info("RequiredInterface: " + supplier.getName() + " adicionada a: " + client.getName());
            else
                LOGGER.info("TENTOU adicionar RequiredInterface: " + supplier.getName() + " a : " + client.getName() + " porém não consegiu");
        }
    }

    public void addRequiredInterface(Interface supplier, Package client) {
        if (!haveRelationship(supplier, client)) {
            if (addRelationship(new DependencyRelationship(supplier, client, "", UtilResources.getRandonUUID())))
                LOGGER.info("RequiredInterface: " + supplier.getName() + " adicionada a: " + client.getName());
            else
                LOGGER.info("TENTOU adicionar RequiredInterface: " + supplier.getName() + " a : " + client.getName() + " porém não consegiu");
        }
    }

    public boolean addRelationship(Relationship relationship) {
        if (!relationshipHolder.haveRelationship(relationship)) {
            if (relationshipHolder.addRelationship(relationship)) {
                LOGGER.info("Relacionamento: " + relationship.getType() + " adicionado na arquitetura.(" + UtilResources.detailLogRelationship(relationship) + ")");
                return true;
            } else {
                LOGGER.info("TENTOU adicionar Relacionamento: " + relationship.getType() + " na arquitetura porém não consegiu");
                return false;
            }
        }
        return false;
    }

    public Package findPackageOfClass(Class targetClass) {
        String packageName = UtilResources.extractPackageName(targetClass.getNamespace());
        return findPackageByName(packageName);
    }

    public void save(Architecture architecture, String pathToSave, String i) {
        GenerateArchitecture generate = new GenerateArchitecture();
        generate.generate(architecture, pathToSave + architecture.getName() + i);
    }

    /**
     * Procura um elemento por ID.<br>
     * Este método busca por elementos diretamente no primeiro nível da arquitetura (Ex: classes que não possuem pacotes)
     * , e também em pacotes.<br/><br/>
     *
     * @param xmiId
     * @return
     */
    public Element findElementById(String xmiId) {
        for (Class element : this.classes) {
            if (element.getId().equals(xmiId))
                return element;
        }
        for (Interface element : this.interfaces) {
            if (element.getId().equals(xmiId))
                return element;
        }
        for (Package p : getAllPackages()) {
            for (Element element : p.getElements()) {
                if (element.getId().equalsIgnoreCase(xmiId))
                    return element;
            }
        }

        for (Package p : getAllPackages()) {
            if (p.getId().equalsIgnoreCase(xmiId))
                return p;
        }

        return null;
    }

    /**
     * Adiciona um pacote na lista de pacotes
     *
     * @param {@link Package}
     */
    public void addPackage(arquitetura.representation.Package p) {
        if (this.packages.add(p))
            LOGGER.info("Pacote: " + p.getName() + " adicionado na arquitetura");
        else
            LOGGER.info("TENTOU adicionar o Pacote: " + p.getName() + " na arquitetura porém não consegiu");
    }

    /**
     * Adiciona uma classe na lista de classes.
     *
     * @param {@link Class}
     */
    public void addExternalClass(Class klass) {
        if (this.classes.add(klass))
            LOGGER.info("Classe: " + klass.getName() + " adicionado na arquitetura");
        else
            LOGGER.info("TENTOU adicionar a Classe: " + klass.getName() + " na arquitetura porém não consegiu");
    }

    public void removeRequiredInterface(Interface supplier, Package client) {
        if (!client.removeRequiredInterface(supplier)) ;
        relationshipHolder.removeRelatedRelationships(supplier);
    }

    public void removeRequiredInterface(Interface supplier, Class client) {
        if (!client.removeRequiredInterface(supplier)) ;
        relationshipHolder.removeRelatedRelationships(supplier);
    }

    public boolean removeOnlyElement(Element element) {
        if (element instanceof Class) {
            if (this.classes.remove(element)) {
                LOGGER.info("Classe: " + element.getName() + " removida do pacote: " + this.getName());
                return true;
            }
        } else if (element instanceof Interface) {
            if (this.interfaces.remove(element)) {
                LOGGER.info("Interface: " + element.getName() + " removida do pacote: " + this.getName());
                return true;
            }
        }

        return false;
    }

    public RelationshipsHolder getRelationshipHolder() {
        return relationshipHolder;
    }

    public boolean isAppliedPatterns() {
        return appliedPatterns;
    }

    public void setAppliedPatterns(boolean b) {
        // TODO Auto-generated method stub
        this.appliedPatterns = b;
    }

}
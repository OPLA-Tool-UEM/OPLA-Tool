package arquitetura.touml;

import arquitetura.exceptions.*;
import arquitetura.helpers.XmiHelper;
import arquitetura.io.ReaderConfig;
import arquitetura.io.SaveAndMove;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author edipofederle<edipofederle@gmail.com>
 */
public class DocumentManager extends XmiHelper {

    static Logger LOGGER = LogManager.getLogger(DocumentManager.class.getName());
    private final String BASE_DOCUMENT = "simples";
    private org.w3c.dom.Document docUml;
    private org.w3c.dom.Document docNotation;
    private org.w3c.dom.Document docDi;
    private String outputModelName;

    public DocumentManager(String outputModelName) throws ModelNotFoundException, ModelIncompleteException {
        this.outputModelName = outputModelName;
        makeACopy(BASE_DOCUMENT);
        createXMIDocument();

        updateProfilesRefs();
        copyProfilesToDestination();

        this.saveAndCopy(outputModelName);
    }

    private void copyProfilesToDestination() {

        try {
            createResourcesDirectoryIfNotExist();

            if (ReaderConfig.hasSmartyProfile()) {
                Path pathSmarty = Paths.get(ReaderConfig.getPathToProfileSMarty());
                Path destSmarty = Paths.get(ReaderConfig.getDirExportTarget(), "resources", "smarty.profile.uml");

                Files.copy(pathSmarty, destSmarty, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // Caso perfil não esteja setado remove do arquivo de tempalte
                XmiHelper.removeNode(docUml, "profileApplication", "_2RlssY9OEeO5xq3Ur4qgFw"); // id
            }

            if (ReaderConfig.hasConcernsProfile()) {
                Path pathConcern = Paths.get(ReaderConfig.getPathToProfileConcerns());
                Path destConcern = Paths.get(ReaderConfig.getDirExportTarget(), "resources", "concerns.profile.uml");

                Files.copy(pathConcern, destConcern, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // Caso perfil não esteja setado remove do arquivo de tempalte
                XmiHelper.removeNode(docUml, "profileApplication", "_2Q2s4I9OEeO5xq3Ur4qgFw"); // id
            }

            if (ReaderConfig.hasRelationsShipProfile()) {
                Path pathToProfileRelationships = Paths.get(ReaderConfig.getPathToProfileRelationships());
                Path destRelationships = Paths.get(ReaderConfig.getDirExportTarget(), "resources", "relationships.profile.uml");

                Files.copy(pathToProfileRelationships, destRelationships, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // Caso perfil não esteja setado remove do arquivo de tempalte
                XmiHelper.removeNode(docUml, "profileApplication", "_2RXDMI9OEeO5xq3Ur4qgFw");
            }

            if (ReaderConfig.hasPatternsProfile()) {
                Path pathProfilePattern = Paths.get(ReaderConfig.getPathToProfilePatterns());
                Path destProfPattern = Paths.get(ReaderConfig.getDirExportTarget(), "resources", "patterns.profile.uml");

                Files.copy(pathProfilePattern, destProfPattern, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // Caso perfil não esteja setado remove do arquivo de tempalte
                XmiHelper.removeNode(docUml, "profileApplication", "_cyBBIJJmEeOENZsdUoZvrw");
            }

        } catch (IOException e) {
            LOGGER.warn("I cannot copy resources to destination. " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void createResourcesDirectoryIfNotExist() {
        File resourcesDir = new File(ReaderConfig.getDirExportTarget() + "/resources/");
        if (!resourcesDir.exists())
            resourcesDir.mkdir();
    }

    private void createXMIDocument() {
        DocumentBuilderFactory docBuilderFactoryNotation = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilderNotation = null;
        DocumentBuilder docBuilderUml = null;

        try {
            docBuilderNotation = docBuilderFactoryNotation.newDocumentBuilder();
            DocumentBuilderFactory docBuilderFactoryUml = DocumentBuilderFactory.newInstance();
            docBuilderUml = docBuilderFactoryUml.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            this.docNotation = docBuilderNotation.parse(ReaderConfig.getDirTarget() + BASE_DOCUMENT + ".notation");
            this.docUml = docBuilderUml.parse(ReaderConfig.getDirTarget() + BASE_DOCUMENT + ".uml");
            this.docDi = docBuilderUml.parse(ReaderConfig.getDirTarget() + BASE_DOCUMENT + ".di");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Realiza um cópia dos três arquivos para o diretório <b>manipulation</b>.
     * <p>
     * Esse diretório deve ser setado no arquivo de configuração
     * <b>application.yml</b> na propriedade "directoryToSaveModels".
     *
     * @param pathToFiles
     * @param modelName
     * @throws ModelIncompleteException
     * @throws ModelNotFoundException
     * @throws SMartyProfileNotAppliedToModelExcepetion
     * @throws IOException
     */
    private void makeACopy(String modelName) throws ModelNotFoundException, ModelIncompleteException {

        LOGGER.info("makeACopy(String modelName) - Enter");

        // Verifica se o diretorio configurado em directoryToSaveModels existe.
        // caso nao exista, o cria.
        Path targetDir = Paths.get(ReaderConfig.getDirTarget());
        try {
            Files.createDirectories(targetDir);

            Path notationPath = targetDir.resolve(BASE_DOCUMENT + ".notation");
            Path umlPath = targetDir.resolve(BASE_DOCUMENT + ".uml");
            Path diPath = targetDir.resolve(BASE_DOCUMENT + ".di");

            Path templateModelsDir = Paths.get(ReaderConfig.getPathToTemplateModelsDirectory());
            Path n = templateModelsDir.resolve(modelName + ".notation");
            Path u = templateModelsDir.resolve(modelName + ".uml");
            Path d = templateModelsDir.resolve(modelName + ".di");

            Files.copy(n, notationPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(u, umlPath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(d, diPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }


        LOGGER.info("makeACopy(String modelName) - Exit");

    }

    /**
     * @return the docUml
     */
    public org.w3c.dom.Document getDocUml() {
        return docUml;
    }

    /**
     * @return the docNotation
     */
    public org.w3c.dom.Document getDocNotation() {
        return docNotation;
    }

    public void saveAndCopy(String newModelName) {
        this.outputModelName = newModelName;

        try {
            SaveAndMove.saveAndMove(docNotation, docUml, docDi, BASE_DOCUMENT, newModelName);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getModelName() {
        return BASE_DOCUMENT;
    }

    public String getNewModelName() {
        return this.outputModelName;
    }

    /**
     * Esse método é responsável por atualizar as referencias aos profiles
     * (definidos no arquivo application.yml) que são usados no modelo.
     * <p>
     * Basicamente é lido dois valores de cada arquivo de profile e atualizado
     * no arquivo simples.uml do qual é usado como base para escrever o modelo
     * novamente em disco.
     *
     * @throws ModelNotFoundException
     * @throws ModelIncompleteException
     * @throws CustonTypeNotFound
     * @throws NodeNotFound
     * @throws InvalidMultiplictyForAssociationException
     */
    public void updateProfilesRefs() {
        String pathToProfileConcern = ReaderConfig.getPathToProfileConcerns();

        DocumentBuilderFactory factoryConcern = DocumentBuilderFactory.newInstance();
        DocumentBuilder profileConcern = null;

        try {

            if (ReaderConfig.hasConcernsProfile()) {
                profileConcern = factoryConcern.newDocumentBuilder();
                final Document docConcern = profileConcern.parse(pathToProfileConcern);

                updateHrefAtt(getIdOnNode(docConcern, "contents", "xmi:id"), "concerns", "appliedProfile", false);
                updateHrefAtt(getIdOnNode(docConcern, "uml:Profile", "xmi:id"), "concerns", "appliedProfile", true);

                final String nsUriPerfilConcern = getIdOnNode(docConcern, "contents", "nsURI");
                arquitetura.touml.Document.executeTransformation(this, new Transformation() {
                    public void useTransformation() {
                        Node xmlsnsConcern = docUml.getElementsByTagName("xmi:XMI").item(0).getAttributes()
                                .getNamedItem("xmlns:concerns");
                        xmlsnsConcern.setNodeValue(nsUriPerfilConcern);
                        String concernLocaltionSchema = nsUriPerfilConcern + " " + "resources/concerns.profile.uml#"
                                + getIdOnNode(docConcern, "contents", "xmi:id");

                        Node nodeSchemaLocation = docUml.getElementsByTagName("xmi:XMI").item(0).getAttributes()
                                .getNamedItem("xsi:schemaLocation");
                        nodeSchemaLocation
                                .setNodeValue(nodeSchemaLocation.getNodeValue() + " " + concernLocaltionSchema + " ");
                    }
                });

            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    private void updateHrefAtt(final String idApplied, final String profileName, final String tagName,
                               final boolean updateReference) {
        arquitetura.touml.Document.executeTransformation(this, new Transformation() {
            public void useTransformation() {
                Node node = null;
                if (updateReference) {
                    node = getAppliedHrefProfile(profileName, tagName);
                } else {
                    node = getReference(profileName, tagName);
                }
                Node nodeAttr = node.getAttributes().getNamedItem("href");
                String oldValueAttr = nodeAttr.getNodeValue().substring(0, nodeAttr.getNodeValue().indexOf("#"));
                nodeAttr.setNodeValue(oldValueAttr + "#" + idApplied);
            }
        });
    }

    private Node getAppliedHrefProfile(String profileName, String tagName) {
        NodeList elements = docUml.getElementsByTagName("profileApplication");
        for (int i = 0; i < elements.getLength(); i++) {
            NodeList childs = (elements.item(i).getChildNodes());
            for (int j = 0; j < childs.getLength(); j++)
                if (childs.item(j).getNodeName().equals(tagName)
                        && (childs.item(j).getAttributes().getNamedItem("href").getNodeValue().contains(profileName)))
                    return childs.item(j);
        }
        return null;
    }

    private Node getReference(String profileName, String tagName) {
        NodeList elements = this.docUml.getElementsByTagName("profileApplication");
        for (int i = 0; i < elements.getLength(); i++) {
            NodeList childs = (elements.item(i).getChildNodes());
            for (int j = 0; j < childs.getLength(); j++) {
                if (childs.item(j).getNodeName().equalsIgnoreCase("eAnnotations")) {
                    for (int k = 0; k < childs.item(j).getChildNodes().getLength(); k++) {
                        if (childs.item(j).getChildNodes().item(k).getNodeName().equalsIgnoreCase("references")) {
                            NodeList eAnnotationsChilds = childs.item(j).getChildNodes();
                            for (int l = 0; l < eAnnotationsChilds.getLength(); l++)
                                if (isProfileNode(profileName, eAnnotationsChilds, l))
                                    return eAnnotationsChilds.item(l);
                        }
                    }
                }

            }
        }

        return null;
    }

    private boolean isProfileNode(String profileName, NodeList eAnnotationsChilds, int l) {
        return (eAnnotationsChilds.item(l).getNodeName().equalsIgnoreCase("references") && (eAnnotationsChilds.item(l)
                .getAttributes().getNamedItem("href").getNodeValue().contains(profileName)));
    }

    /**
     * @param document - O documento em que se quer pesquisar.
     * @param tagName  - elemento desejado
     * @param attrName - atributo do elemento desejado
     * @return
     */
    private String getIdOnNode(Document document, String tagName, String attrName) {
        return document.getElementsByTagName(tagName).item(0).getAttributes().getNamedItem(attrName).getNodeValue();
    }

}
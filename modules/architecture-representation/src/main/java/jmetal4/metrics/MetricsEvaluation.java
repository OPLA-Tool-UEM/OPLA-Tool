package jmetal4.metrics;


import arquitetura.representation.Architecture;
import jmetal4.metrics.PLAMetrics.extensibility.ExtensPLA;
import jmetal4.metrics.concernDrivenMetrics.concernCohesion.LCC;
import jmetal4.metrics.concernDrivenMetrics.concernCohesion.LCCClass;
import jmetal4.metrics.concernDrivenMetrics.concernCohesion.LCCClassComponentResult;
import jmetal4.metrics.concernDrivenMetrics.concernCohesion.LCCComponentResult;
import jmetal4.metrics.concernDrivenMetrics.concernDiffusion.CDAC;
import jmetal4.metrics.concernDrivenMetrics.concernDiffusion.CDAClass;
import jmetal4.metrics.concernDrivenMetrics.concernDiffusion.CDAI;
import jmetal4.metrics.concernDrivenMetrics.concernDiffusion.CDAO;
import jmetal4.metrics.concernDrivenMetrics.interactionBeteweenConcerns.CIBC;
import jmetal4.metrics.concernDrivenMetrics.interactionBeteweenConcerns.CIBClass;
import jmetal4.metrics.concernDrivenMetrics.interactionBeteweenConcerns.IIBC;
import jmetal4.metrics.concernDrivenMetrics.interactionBeteweenConcerns.OOBC;
import jmetal4.metrics.conventionalMetrics.*;


public class MetricsEvaluation {

    //<editor-fold default-state="folded" desc="ELEG(pla)">

    /**
     * Elegância = NAC + EC + ATMR
     */
    public double evaluateElegance(Architecture architecture) {
        return evaluateATMRElegance(architecture) + evaluateECElegance(architecture) + evaluateNACElegance(architecture);
    }

    /**
     * Elegância da Razão de Atributos
     */
    public double evaluateATMRElegance(Architecture architecture) {
        ATMRElegance ATMR = new ATMRElegance(architecture);
        return ATMR.getResults();
    }

    /**
     * Elegância de Acoplamentos Externos
     */
    public double evaluateECElegance(Architecture architecture) {
        ECElegance EC = new ECElegance(architecture);
        return EC.getResults();
    }

    /**
     * Elegância de Número entre classes
     */
    public double evaluateNACElegance(Architecture architecture) {
        NACElegance NAC = new NACElegance(architecture);
        return NAC.getResults();
    }
    //</editor-fold>

    //<editor-fold default-state="folded" desc="Ext(pla)">

    /**
     * Capacidade de Extensão de cada Pacote = Extens(pla)
     */
    public float evaluatePLAExtensibility(Architecture architecture) {
        float ExtensibilityFitness = 0;
        float Extensibility;
        ExtensPLA PLAExtens = new ExtensPLA(architecture);
        ExtensibilityFitness = PLAExtens.getValue();
        if (ExtensibilityFitness == 0)
            Extensibility = 1000;
        else Extensibility = 1 / ExtensibilityFitness;
        return (Extensibility);
    }
    //</editor-fold>

    //<editor-fold default-state="folded" desc="FM(pla)">
    public double evaluateMSIFitness(Architecture architecture) {
        double sumLCC = evaluateLCC(architecture);

        double sumEC = evaluateEC(architecture);

        double sumDC = evaluateDC(architecture);

        return sumLCC + sumDC + sumEC;
    }

    /**
     * Lack of Concern-based Cohesion
     */
    public double evaluateLCC(Architecture architecture) {
        LCC result = new LCC(architecture);
        return result.getResults().stream().mapToDouble(LCCComponentResult::numberOfConcerns).sum();
    }

    /**
     * Concern Diffusion over Architectural Components
     */
    public double evaluateCDAC(Architecture architecture) {
        CDAC cdac = new CDAC(architecture);
        return cdac.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();
    }

    /**
     * Concern Diffusion over Architectural Interfaces
     */
    public double evaluateCDAI(Architecture architecture) {
        CDAI cdai = new CDAI(architecture);
        return cdai.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();
    }

    /**
     * Concern Diffusion over Architectural Operations
     */
    public double evaluateCDAO(Architecture architecture) {
        CDAO cdao = new CDAO(architecture);
        return cdao.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();
    }

    /**
     * Component-level Interlacing Between Concerns
     */
    public double evaluateCIBC(Architecture architecture) {
        CIBC cibc = new CIBC(architecture);
        return cibc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();

    }

    /**
     * Interface-level Interlacing Between Concerns
     */
    public double evaluateIIBC(Architecture architecture) {
        IIBC iibc = new IIBC(architecture);
        return iibc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();
    }


    /**
     * Operation-level Overlapping Between Concerns
     */
    public double evaluateOOBC(Architecture architecture) {
        OOBC oobc = new OOBC(architecture);
        return oobc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();
    }
    //</editor-fold>

    //<editor-fold default-state="folded" desc="FM+DesignOutset(pla)">
    public double evaluateMSIFitnessDesignOutset(Architecture architecture) {
        double sumLCCClass = evaluateLCCClass(architecture);

        double sumCDAClass = evaluateCDAClass(architecture);

        double sumCIBClass = evaluateCIBClass(architecture);

        return evaluateMSIFitness(architecture) + sumLCCClass + sumCDAClass + sumCIBClass;
    }


    public double evaluateCDAClass(Architecture architecture) {
        CDAClass cdaclass = new CDAClass(architecture);
        return cdaclass.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();
    }


    public double evaluateCIBClass(Architecture architecture) {
        CIBClass cibclass = new CIBClass(architecture);
        return cibclass.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();
    }

    public double evaluateLCCClass(Architecture architecture) {
        LCCClass result = new LCCClass(architecture);
        return result.getResults().stream().mapToDouble(LCCClassComponentResult::numberOfConcerns).sum();
    }
    //</editor-fold>

    //<editor-fold default-state="folded" desc="CM(pla)">
    public double evaluateMACFitness(Architecture architecture) {
        double meanNumOps = evaluateMeanNumOps(architecture);

        double meanDepComps = evaluateMeanDepComps(architecture);

        double sumClassesDepOut = evaluateSumClassesDepOut(architecture);

        double sumClassesDepIn = evaluateSumClassesDepIn(architecture);

        double sumDepOut = evaluateSumDepOut(architecture);

        double sumDepIn = evaluateSumDepIn(architecture);

        double iCohesion = evaluateCohesion(architecture);

        return meanNumOps + meanDepComps + sumClassesDepIn + sumClassesDepOut + sumDepIn + sumDepOut + iCohesion;
    }


    /**
     * Dependências de Entrada
     */
    public double evaluateSumDepIn(Architecture architecture) {
        DependencyIn DepIn = new DependencyIn(architecture);
        return DepIn.getResults();
    }

    /**
     * Dependências de Saída
     */
    public double evaluateSumDepOut(Architecture architecture) {
        DependencyOut DepOut = new DependencyOut(architecture);
        return DepOut.getResults();
    }

    /**
     * Dependências de Entrada de uma Classe
     */
    public double evaluateSumClassesDepIn(Architecture architecture) {
        ClassDependencyIn classesDepIn = new ClassDependencyIn(architecture);
        return classesDepIn.getResults();
    }

    /**
     * Dependências de Saída de uma Classe
     */
    public double evaluateSumClassesDepOut(Architecture architecture) {
        ClassDependencyOut classesDepOut = new ClassDependencyOut(architecture);
        return classesDepOut.getResults();
    }

    /**
     * Número de Operações por Interface
     */
    public double evaluateMeanNumOps(Architecture architecture) {
        MeanNumOpsByInterface numOps = new MeanNumOpsByInterface(architecture);
        return numOps.getResults();
    }

    /**
     * Dependência de Pacotes
     */
    public double evaluateMeanDepComps(Architecture architecture) {
        MeanDepComponents depComps = new MeanDepComponents(architecture);
        return depComps.getResults();
    }

    /**
     * Coesão relacional
     */
    public double evaluateCohesion(Architecture architecture) {

        RelationalCohesion cohesion = new RelationalCohesion(architecture);
        double sumCohesion = cohesion.getResults();
        if (sumCohesion == 0) {
            return 1.0;
        }
        return 1 / sumCohesion;
    }

    public double evaluateICohesion(double sumCohesion) {
        return sumCohesion == 0 ? 1.0 : 1 / sumCohesion;
    }
    //</editor-fold>

    //<editor-fold default-state="folded" desc="implementado por marcelo">

    /**
     * Acoplamento de Componentes
     */
    public double evaluateACOMP(Architecture architecture) {
        return evaluateSumDepIn(architecture) + evaluateSumDepOut(architecture);
    }

    /**
     * Acoplamento de Classes
     */
    public double evaluateACLASS(Architecture architecture) {
        return evaluateSumClassesDepIn(architecture) + evaluateSumClassesDepOut(architecture);
    }

    /**
     * Tamanho = Número de Operações / Número de Interfaces
     */
    public double evaluateTAM(Architecture architecture) {
        return evaluateMeanNumOps(architecture);
    }

    /**
     * Coesão
     */
    public double evaluateCOE(Architecture architecture) {
        RelationalCohesion cohesion = new RelationalCohesion(architecture);
        double iC = 1.0;
        if (cohesion.getResults() != 0) {
            iC = 1 / cohesion.getResults();
        }
        return iC + evaluateLCC(architecture);
    }

    /**
     * Difusão de Características
     */
    public double evaluateDC(Architecture architecture) {
        CDAI cdai = new CDAI(architecture);
        double sumCDAI = cdai.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();

        CDAO cdao = new CDAO(architecture);
        double sumCDAO = cdao.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();

        CDAC cdac = new CDAC(architecture);
        double sumCDAC = cdac.getResults().stream().mapToDouble(c -> c.getElements().size()).sum();

        return sumCDAI + sumCDAO + sumCDAC;
    }

    /**
     * Entrelaçamento de Características
     */
    public double evaluateEC(Architecture architecture) {

        CIBC cibc = new CIBC(architecture);
        double sumCIBC = cibc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();

        IIBC iibc = new IIBC(architecture);
        double sumIIBC = iibc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();

        OOBC oobc = new OOBC(architecture);
        double sumOOBC = oobc.getResults().values().stream().mapToDouble(c -> c.getInterlacedConcerns().size()).sum();

        return sumCIBC + sumIIBC + sumOOBC;
    }
    //</editor-fold>

}

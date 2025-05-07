package org.analysis.exp3;

import org.configurations.Configuration;
import org.experiments.exp3.experiment.AxiomIterator;
import org.experiments.exp3.experiment.NotSoRandomAxiomIterator;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.utility.YAMLConfigLoader;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class ResultCheck extends PartialResultBase {

    public static void main(String[] args) {
        Configuration config = new YAMLConfigLoader().getConfig(args[0], Configuration.class);
        new ResultCheck(config, Integer.parseInt(args[1]), Integer.parseInt(args[2])).run();
    }

    private OWLOntology learnedOntology;

    private OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();


    public ResultCheck(Configuration config, int limit, int base) {
        super(config, limit, base);
    }

    @Override
    protected void compareOntologies(String model) throws InterruptedException {
        // These are not confusion matrices. DO NOT BE CONFUSED
        // I just dont care
        int[][] confusionMatrix = new int[2][2];

        String shortName = Path.of(ontology).getFileName().toString();
        String sep = FileSystems.getDefault().getSeparator();
        String ontologyResultPath = "results" + sep + "ontologies";
        String learnedOntologyPath = ontologyResultPath + sep + shortName + "_" + model + "_" + setting + ".owl";
        this.learnedOntology = loadOntology(learnedOntologyPath);

        OWLReasoner predictedReasoner = new ElkReasonerFactory().createReasoner(learnedOntology);

        NotSoRandomAxiomIterator iterator = new NotSoRandomAxiomIterator(expectedOntology, 150, 10);
        for (OWLSubClassOfAxiom axiom : iterator) {
            entail(predictedReasoner, axiom, confusionMatrix);
        }

        otherWayAround(new AxiomIterator(learnedOntology), confusionMatrix);

        results("%s %s %s".formatted(shortName, model, setting), confusionMatrix);
    }

    private void entail(OWLReasoner target, OWLSubClassOfAxiom axiom, int[][] confusionMatrix) {
        for (OWLSubClassOfAxiom a : splitAxioms(axiom)) {
            if (target.isEntailed(a)) {
                confusionMatrix[0][0]++;
            } else {
                confusionMatrix[0][1]++;
            }
        }
    }

    private void otherWayAround(AxiomIterator axioms, int[][] confusionMatrix) {
        Set<OWLSubClassOfAxiom> axiomSet = new HashSet<>();
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        for (OWLSubClassOfAxiom axiom : axioms) {
            axiomSet.addAll(splitAxioms(axiom));
        }
        for (OWLSubClassOfAxiom axiom : axiomSet) {
            if (expectedReasoner.isEntailed(axiom)) {
                confusionMatrix[1][0]++;
            } else {
                confusionMatrix[1][1]++;
            }
        }
    }

    private List<OWLSubClassOfAxiom> splitAxioms(OWLSubClassOfAxiom axiom) {
        //return List.of(axiom);

        if (axiom.getSuperClass() instanceof OWLObjectIntersectionOf intersection) {
            OWLClassExpression sub = axiom.getSubClass();
            List<OWLSubClassOfAxiom> l = new ArrayList<>();
            for (OWLClassExpression ex : intersection.getOperands()) {
                l.add(factory.getOWLSubClassOfAxiom(sub, ex));
            }
            return l;
        } else {
            return List.of(axiom);
        }
    }

    private void results(String name, int[][] res) {
        System.out.println("----------------\n" + name);
        System.out.println("Selected axioms from original in learned - \t" + res[0][0] + " true \t" + res[0][1] + " false");
        System.out.println("Leaned axioms in target ontology - \t" + res[1][0] + " true \t" + res[1][1] + " false");
    }

}

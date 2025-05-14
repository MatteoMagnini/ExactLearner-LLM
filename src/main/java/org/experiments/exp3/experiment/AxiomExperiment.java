package org.experiments.exp3.experiment;

import org.exactlearner.engine.ELEngine;
import org.experiments.exp3.DBModelQuery;
import org.experiments.exp3.corrupter.ConceptSwap;
import org.experiments.exp3.render.axiom.AxiomRenderer;
import org.experiments.exp3.result.QueryResult;
import org.experiments.exp3.result.ResultManagerDB;
import org.experiments.exp3.result.SettingResult;
import org.exactlearner.learner.ConceptRelation;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class AxiomExperiment extends ExperimentBase {
    private final AxiomRenderer ident;
    private final String ontologyName;
    private final int testId;

    public AxiomExperiment(List<DBModelQuery> models, ResultManagerDB resultManager, AxiomRenderer ident, String ontologyName, int testId) {
        super(models, getOntology(ontologyName), resultManager);
        this.ident = ident;
        this.ontologyName = ontologyName;
        this.testId = testId;
    }

    public void runExperiment() {
        SettingResult answer = resultManager.getSetting();
        ConceptSwap conceptSwap = new ConceptSwap(ontology, getConceptRelation(ontology), new ELEngine(ontology));
        for (OWLSubClassOfAxiom axiom : new AxiomIterator(ontology, 1000)) {

                QueryResult query = resultManager.getQuery(ident.render(axiom), ontologyName, testId);
                resultManager.saveResult(query, answer, null, "true");
                runForAxiom(axiom, query);

                Optional<OWLSubClassOfAxiom> corrupt = conceptSwap.corrupt(axiom);
                if (corrupt.isEmpty()) {
                    System.out.println("Could not corrupt");
                    continue;
                }
                axiom = corrupt.get();
                query = resultManager.getQuery(ident.render(axiom), ontologyName, testId);
                resultManager.saveResult(query, answer, null, "false");
                runForAxiom(axiom, query);

        }
        System.out.println("Axioms created");
        runAll();
        System.out.println("Corrupter did invent a false axiom " + conceptSwap.getNotFirst() + " out of " + conceptSwap.getTotal() + " axioms. It failed " + conceptSwap.getFails() + " number of times.");
    }

    private ConceptRelation<OWLClass> getConceptRelation(OWLOntology ontology) {
        ConceptRelation<OWLClass> conceptRelation = new ConceptRelation<>();
        Set<OWLAxiom> axioms = ontology.getAxioms();
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLSubClassOfAxiom subClassOfAxiom) {
                if (subClassOfAxiom.getSubClass() instanceof OWLClass sub && subClassOfAxiom.getSuperClass() instanceof OWLClass sup) {
                    conceptRelation.addEdge(sub, sup);
                }
            }
        }
        return conceptRelation;
    }
}

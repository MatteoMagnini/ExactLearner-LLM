package org.experiments.exp3.experiment;

import org.exactlearner.engine.ELEngine;
import org.experiments.exp3.DBModelQuery;
import org.experiments.exp3.render.axiom.AxiomRenderer;
import org.experiments.exp3.result.QueryResult;
import org.experiments.exp3.result.ResultManagerDB;
import org.experiments.exp3.result.SettingResult;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassExperiment extends ExperimentBase {
    private final AxiomRenderer ident;
    private final String ontologyName;
    private final int testId;

    public ClassExperiment(List<DBModelQuery> models, ResultManagerDB resultManager, AxiomRenderer ident, String ontologyName, int testId) {
        super(models, getOntology(ontologyName), resultManager);
        this.ident = ident;
        this.ontologyName = ontologyName;
        this.testId = testId;
    }

    public void runExperiment() {
        SettingResult answer = resultManager.getSetting();
        ELEngine engine = new ELEngine(ontology);
        Set<OWLClass> classes = new HashSet<>(ontology.getClassesInSignature());
        classes.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
        for (OWLClass a : classes) {
            for (OWLClass b : classes) {
                try {
                    OWLSubClassOfAxiom axiom = engine.getSubClassAxiom(a, b);
                    QueryResult query = resultManager.getQuery(ident.render(axiom), ontologyName, testId);
                    resultManager.saveResult(query, answer, null, engine.entailed(axiom).toString());
                    runForAxiom(axiom, query);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        runAll();
    }
}

package org.experiments.exp3;

import org.experiments.exp3.experiment.Experiment;
import org.experiments.exp3.experiment.OntologyStructure;
import org.experiments.exp3.render.axiom.ManchesterRender;
import org.experiments.exp3.render.concept.ANameRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.List;

public class StructureLaunch {
    public static void main(String[] args) {
        List<String> l = List.of(
                "src/main/resources/ontologies/large/go.owl",
                "src/main/resources/ontologies/large/galen_fixed.owl"
        );
        for (String o : l) {
            File fileName = new File(o);
            OWLOntology ontology = null;
            try {
                ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(fileName);
            } catch (Exception e) {
                System.out.println("Could not load ontology: " + o + "\n" + e.getMessage());
                continue;
            }
            Experiment exp = new OntologyStructure(ontology, new ManchesterRender(new ANameRenderer()));
            exp.runExperiment();
        }
    }
}

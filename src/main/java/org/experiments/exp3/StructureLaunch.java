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
			"results/ontologies/target_AboveElbowJacketCast.owl",
"results/ontologies/target_BNFSection13_11.owl",
"results/ontologies/target_Chlorhexidine.owl",
"results/ontologies/target_ConeOfTissue.owl",
"results/ontologies/target_GO_0044204.owl",
"results/ontologies/target_GO_0044220.owl",
"results/ontologies/target_GO_0044223.owl",
"results/ontologies/target_GO_0044225.owl",
"results/ontologies/target_GO_0044231.owl",
"results/ontologies/target_GO_0044232.owl",
"results/ontologies/target_GO_0044233.owl",
"results/ontologies/target_GO_0044280.owl",
"results/ontologies/target_GO_0044284.owl",
"results/ontologies/target_GO_0044295.owl",
"results/ontologies/target_Kallikrein.owl",
"results/ontologies/target_Neon.owl",
"results/ontologies/target_Pin.owl",
"results/ontologies/target_ProstaglandinDrug.owl",
"results/ontologies/target_Zopiclone.owl",
"results/ontologies/target_Zuccini.owl"
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

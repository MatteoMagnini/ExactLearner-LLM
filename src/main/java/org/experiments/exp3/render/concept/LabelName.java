package org.experiments.exp3.render.concept;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.Map;

public class LabelName implements ConceptNameRenderer {

    private final OWLOntology ontology;
    private final Map<IRI, String> labelMap = new HashMap<>();

    public LabelName(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public String render(OWLObjectProperty p) {
        return render(p.getIRI());
    }

    @Override
    public String render(OWLClass c) {
        return render(c.getIRI());
    }

    public String render(IRI iri) {
        if (labelMap.containsKey(iri)) {
            return labelMap.get(iri);
        }
        for (OWLAnnotationAssertionAxiom a : ontology.getAnnotationAssertionAxioms(iri)) {
            if (a != null && a.getProperty().isLabel() && a.getValue() instanceof OWLLiteral val) {
                labelMap.put(iri, val.getLiteral());
                return val.getLiteral();
            }
        }
        return iri.getFragment();
    }
}

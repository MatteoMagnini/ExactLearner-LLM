package org.experiments.exp3.render.concept;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class ANameRenderer implements ConceptNameRenderer {
    @Override
    public String render(OWLClass c) {
        return "A";
    }

    @Override
    public String render(OWLObjectProperty p) {
        return "r";
    }
}

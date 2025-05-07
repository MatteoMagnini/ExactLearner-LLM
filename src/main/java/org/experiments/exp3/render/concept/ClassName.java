package org.experiments.exp3.render.concept;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class ClassName implements ConceptNameRenderer {

    @Override
    public String render(OWLClass c) {
        return c.getIRI().getFragment();
    }

    @Override
    public String render(OWLObjectProperty p) {
        return p.getIRI().getFragment();
    }
}

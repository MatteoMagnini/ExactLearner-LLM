package org.experiments.exp3.render.concept;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface ConceptNameRenderer {
    String render(OWLClass c);
    String render(OWLObjectProperty p);
}

package org.experiments.exp3.render.axiom;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface AxiomRenderer {
    String render(OWLSubClassOfAxiom axiom);
}

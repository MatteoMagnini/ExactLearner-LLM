package org.experiments.exp3.result;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface ResultSaver {
    void add(OWLSubClassOfAxiom axiom, String query, String result);
}

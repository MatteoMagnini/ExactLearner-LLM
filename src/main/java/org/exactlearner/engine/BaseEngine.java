package org.exactlearner.engine;

import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.Set;

public interface BaseEngine {

    OWLSubClassOfAxiom getSubClassAxiom(OWLClassExpression classA, OWLClassExpression classB);

    OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(OWLClassExpression concept1, OWLClassExpression concept2);

    OWLClassExpression getOWLObjectIntersectionOf(Set<OWLClassExpression> mySet);

    List<OWLClass> getClassesInSignature();

    Boolean entailed(OWLAxiom a);

    Boolean entailed(Set<OWLAxiom> s);

    OWLOntology getOntology();

    // Set<OWLClass> getSuperClasses(OWLClassExpression superclass, boolean direct);

    void disposeOfReasoner();

   void applyChange(OWLOntologyChange change);
}

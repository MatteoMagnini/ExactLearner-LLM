package org.exactlearner.parser;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Optional;
import java.util.Set;

public class OWLParserCustomSignature extends OWLParserBase {
    private final Set<OWLClass> classes;
    private final Set<OWLObjectProperty> properties;
    private final Set<OWLAxiom> axioms;

    public OWLParserCustomSignature(Set<OWLClass> classes, Set<OWLObjectProperty> properties, Set<OWLAxiom> axioms) {
        this.classes = classes;
        this.properties = properties;
        this.axioms = axioms;
    }

    @Override
    public Optional<Set<OWLClass>> getClasses() {
        return Optional.of(classes);
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return axioms;
    }

    @Override
    public Set<OWLObjectProperty> getObjectProperties() {
        return properties;
    }
}

package org.exactlearner.parser;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OWLParser {
    List<OWLClass> getOrderedClasses();

    Optional<Set<OWLClass>> getClasses();

    Set<String> getClassesNamesAsString();

    Set<OWLAxiom> getAxioms();

    Set<OWLObjectProperty> getObjectProperties();

    Set<String> getObjectPropertiesAsString();
}

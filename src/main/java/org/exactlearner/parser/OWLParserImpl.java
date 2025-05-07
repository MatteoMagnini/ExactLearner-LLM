package org.exactlearner.parser;

import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class OWLParserImpl extends OWLParserBase {
    private OWLOntology owl;

    public OWLParserImpl(String pathOfFile, OWLOntologyManager manager) {
        System.out.println("Parsing file: " + pathOfFile);
        try {
            loadFile(pathOfFile, manager);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public OWLParserImpl(OWLOntology owl) {
        this.owl = owl;
    }

    private void loadFile(String pathOfFile, OWLOntologyManager manager) throws OWLOntologyCreationException {
        owl = manager.loadOntologyFromOntologyDocument(new File(pathOfFile));
    }

    @Override
    public Optional<Set<OWLClass>> getClasses() {
        return Optional.ofNullable(owl.getClassesInSignature());
    }

    public OWLOntology getOwl() {
        return owl;
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return owl.getAxioms();
    }

    @Override
    public Set<OWLObjectProperty> getObjectProperties() {
        return owl.getObjectPropertiesInSignature();
    }

}

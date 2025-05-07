package org.experiments.exp3.experiment;

import org.exactlearner.tree.ELEdge;
import org.exactlearner.tree.ELNode;
import org.exactlearner.tree.ELTree;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;
import java.util.stream.Collectors;

public class AxiomHandler {
    private Set<OWLClass> classes;
    private Set<OWLObjectProperty> properties;
    private final Set<OWLSubClassOfAxiom> axioms;
    private Iterator<OWLSubClassOfAxiom> iterator;

    public AxiomHandler(Iterator<OWLSubClassOfAxiom> iterator) {
        this.iterator = iterator;
        axioms = new HashSet<>();
    }

    public Set<OWLSubClassOfAxiom> getAxioms() {
        if (iterator == null) {
            return axioms;
        }
        while (iterator.hasNext()) {
            axioms.add(iterator.next());
        }
        iterator = null;
        return axioms;
    }

    public Set<OWLClass> getClasses() {
        if (classes == null) {
            getAllowedValues();
        }
        return classes;
    }

    public Set<String> getClassesStrings() {
        return getClasses().stream().map(c -> c.getIRI().getFragment()).collect(Collectors.toSet());
    }

    public Set<String> getPropertiesStrings() {
        return getProperties().stream().map(p -> p.getIRI().getFragment()).collect(Collectors.toSet());
    }

    public Set<OWLObjectProperty> getProperties() {
        if (properties == null) {
            getAllowedValues();
        }
        return properties;
    }

    private void getAllowedValues() {
        Set<ELTree> trees = new HashSet<>();
        for (OWLSubClassOfAxiom axiom : getAxioms()) {
            try {
                trees.add(new ELTree(axiom.getSubClass()));
                trees.add(new ELTree(axiom.getSuperClass()));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        classes = trees.stream().flatMap(t -> getConcepts(t).stream()).collect(Collectors.toSet());
        properties = trees.stream().flatMap(t -> getRoles(t).stream()).collect(Collectors.toSet());
    }

    private Set<OWLClass> getConcepts(ELTree tree) {
        return getConcepts(tree.getRootNode());
    }

    private Set<OWLClass> getConcepts(ELNode node) {
        Set<OWLClass> concepts = new HashSet<>(node.getLabel());
        for (ELEdge edge : node.getEdges()) {
            concepts.addAll(getConcepts(edge.getNode()));
        }
        return concepts;
    }

    private Set<OWLObjectProperty> getRoles(ELTree tree) {
        return getRoles(tree.getRootNode());
    }

    private Set<OWLObjectProperty> getRoles(ELNode node) {
        Set<OWLObjectProperty> roles = new HashSet<>();
        for (ELEdge edge : node.getEdges()) {
            if (edge.isObjectProperty()) {
                roles.add((OWLObjectProperty) edge.getLabel());
            }
            roles.addAll(getRoles(edge.getNode()));
        }
        return roles;
    }

}

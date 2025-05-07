package org.experiments.exp3.experiment;

import org.exactlearner.tree.ELEdge;
import org.exactlearner.tree.ELNode;
import org.exactlearner.tree.ELTree;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.*;

public class NotSoRandomAxiomIterator implements Iterable<OWLSubClassOfAxiom> {
    private final OWLOntology ontology;
    private final int limit;
    private final int base;


    public NotSoRandomAxiomIterator(OWLOntology ontology, int limit, int base) {
        this.ontology = ontology;
        this.limit = limit;
        this.base = base;
    }

    @Override
    public @NotNull Iterator<OWLSubClassOfAxiom> iterator() {
        return new Iterator<>() {
            private final Map<OWLClass, Set<OWLSubClassOfAxiom>> map = new HashMap<>();
            private final Set<OWLSubClassOfAxiom> potentialAxioms = new HashSet<>();
            private final List<OWLSubClassOfAxiom> current = getIterator();
            private int conceptNameCount = 0;

            private List<OWLSubClassOfAxiom> getIterator() {
                ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

                List<OWLSubClassOfAxiom> axioms = new ArrayList<>();
                for (OWLAxiom axiom : ontology.getAxioms()) {
                    if (axiom instanceof OWLSubClassOfAxiom sub) {
                        axioms.add(sub);
                    } else if (axiom instanceof OWLEquivalentClassesAxiom eq) {
                        axioms.addAll(eq.asOWLSubClassOfAxioms());
                    }
                }

                List<Map.Entry<OWLSubClassOfAxiom, String>> renderedList = new ArrayList<>();
                for (OWLSubClassOfAxiom axiom : axioms) {
                    try {
                        Set<OWLClass> classes = getConcepts(new ELTree(axiom.getSuperClass()));
                        classes.addAll(getConcepts(new ELTree(axiom.getSubClass())));
                        for (OWLClass c : classes) {
                            if (!map.containsKey(c)) {
                                map.put(c, new HashSet<>());
                            }
                            map.get(c).add(axiom);
                        }
                        renderedList.add(new AbstractMap.SimpleEntry<>(axiom, renderer.render(axiom)));
                    } catch (Exception e) {
                        System.out.println("Could not use axiom: " + renderer.render(axiom));
                    }
                }

                renderedList.sort(Map.Entry.comparingByValue());

                List<OWLSubClassOfAxiom> sortedList = new ArrayList<>();
                for (Map.Entry<OWLSubClassOfAxiom, String> entry : renderedList) {
                    sortedList.add(entry.getKey());
                }

                Collections.shuffle(sortedList, new Random(42));

                return new ArrayList<>(sortedList.stream().limit(base).toList());
            }

            @Override
            public boolean hasNext() {
                if (limit <= conceptNameCount) return false;
                if (!current.isEmpty()) {
                    return true;
                }
                if (potentialAxioms.isEmpty()) {
                    System.out.print("no more related axioms");
                    return false;
                }

                OWLSubClassOfAxiom axiom = selectOne(potentialAxioms);
                current.add(axiom);
                potentialAxioms.remove(axiom);
                return true;
            }

            @Override
            public OWLSubClassOfAxiom next() {
                if (current.isEmpty()) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                OWLSubClassOfAxiom axiom = current.remove(0);
                try {
                    Set<OWLClass> classes = getConcepts(new ELTree(axiom.getSuperClass()));
                    classes.addAll(getConcepts(new ELTree(axiom.getSubClass())));

                    for (OWLClass c : classes) {
                        if (map.containsKey(c)) {
                            potentialAxioms.addAll(map.remove(c));
                            conceptNameCount++;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return axiom;
            }
        };
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

    private OWLSubClassOfAxiom selectOne(Collection<OWLSubClassOfAxiom> axioms) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

        List<Map.Entry<OWLSubClassOfAxiom, String>> renderedList = new ArrayList<>();
        for (OWLSubClassOfAxiom axiom : axioms) {
            renderedList.add(new AbstractMap.SimpleEntry<>(axiom, renderer.render(axiom)));
        }

        renderedList.sort(Map.Entry.comparingByValue());

        List<OWLSubClassOfAxiom> sortedList = new ArrayList<>();
        for (Map.Entry<OWLSubClassOfAxiom, String> entry : renderedList) {
            sortedList.add(entry.getKey());
        }

        Collections.shuffle(sortedList, new Random(42));

        return sortedList.remove(0);
    }
}
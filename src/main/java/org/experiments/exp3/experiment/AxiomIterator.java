package org.experiments.exp3.experiment;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.*;

public class AxiomIterator implements Iterable<OWLSubClassOfAxiom> {
    private final OWLOntology ontology;
    private final int limit;

    public AxiomIterator(OWLOntology ontology) {
        this.ontology = ontology;
        this.limit = (100+ontology.getAxiomCount()) * 2; // sure. that makes sense.
    }

    public AxiomIterator(OWLOntology ontology, int limit) {
        this.ontology = ontology;
        this.limit = limit;
    }

    @Override
    public @NotNull Iterator<OWLSubClassOfAxiom> iterator() {
        return new Iterator<>() {
            private final Iterator<OWLAxiom> it = getIterator();
            private final List<OWLSubClassOfAxiom> current = new ArrayList<>();
            private int currentIndex = 0;

            private Iterator<OWLAxiom> getIterator() {
                ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

                List<Map.Entry<OWLAxiom, String>> renderedList = new ArrayList<>();
                for (OWLAxiom axiom : ontology.getAxioms()) {
                    renderedList.add(new AbstractMap.SimpleEntry<>(axiom, renderer.render(axiom)));
                }

                renderedList.sort(Map.Entry.comparingByValue());

                List<OWLAxiom> sortedList = new ArrayList<>();
                for (Map.Entry<OWLAxiom, String> entry : renderedList) {
                    sortedList.add(entry.getKey());
                }

                Collections.shuffle(sortedList, new Random(42));

                return sortedList.iterator();
            }

            @Override
            public boolean hasNext() {
                if (limit <= currentIndex) return false;
                if (!current.isEmpty()) {
                    return true;
                }
                while (it.hasNext()) {
                    OWLAxiom next = it.next();
                    if (next instanceof OWLSubClassOfAxiom s) {

                        current.add(s);
                        return true;
                    } else if (next instanceof OWLEquivalentClassesAxiom e) {
                        current.addAll(e.asOWLSubClassOfAxioms());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public OWLSubClassOfAxiom next() {
                if (current.isEmpty()) {
                    if (!it.hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                currentIndex++;
                return current.remove(0);
            }
        };
    }
}

package org.experiments.exp3.experiment;

import org.exactlearner.engine.ELEngine;
import org.experiments.exp3.render.axiom.AxiomRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class OntologyStructure implements Experiment {
    private final OWLOntology ontology;
    private final AxiomRenderer render;

    public OntologyStructure(OWLOntology ontology, AxiomRenderer render) {
        this.ontology = ontology;
        this.render = render;
    }

    public void runExperiment() {
        ELEngine eng = new ELEngine(ontology);
        Map<String, Integer> map = new HashMap<>();
        List<OWLSubClassOfAxiom> axioms = getAxioms();
        for (OWLSubClassOfAxiom axiom : axioms) {
            try {
            String r = render.render(axiom);
            if (map.containsKey(r)) {
                map.put(r, map.get(r) + 1);
            } else {
                map.put(r, 1);
            }
            } catch (Exception e) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(axiom));
            }
        }
        map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
            System.out.println("" + entry.getValue() + "\t-  " + entry.getKey());
        });
    }

    protected List<OWLSubClassOfAxiom> getAxioms() {
        List<OWLSubClassOfAxiom> axioms = new ArrayList<>();
        for (OWLAxiom axiom : ontology.getAxioms()) {
            if (axiom instanceof OWLAnnotationAssertionAxiom || axiom instanceof OWLDeclarationAxiom) {
                continue;
            }

            if (axiom instanceof OWLEquivalentClassesAxiom eq) {
                axioms.addAll(eq.asOWLSubClassOfAxioms());
            } else if (axiom instanceof OWLSubClassOfAxiom sub) {
                axioms.add(sub);
            } else if (axiom instanceof OWLSubPropertyAxiom sp) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(sp));
            } else if (axiom instanceof OWLSubPropertyChainOfAxiom ax) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(ax));
            } else if (axiom instanceof OWLFunctionalObjectPropertyAxiom func) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(func));
            } else if (axiom instanceof OWLTransitiveObjectPropertyAxiom prop) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(prop));
            } else if (axiom instanceof OWLSubAnnotationPropertyOfAxiom prop) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(prop));
            } else if (axiom instanceof OWLDisjointClassesAxiom prop) {
                System.out.println(new ManchesterOWLSyntaxOWLObjectRendererImpl().render(prop));
            } else {
                System.out.println(axiom.getAxiomType().getName());
                //throw new RuntimeException("Axiom type not supported " + axiom);
            }
        }
        // Map<String, Long> axiomsStrings = axioms.stream().map(this::axiomType).collect(Collectors.groupingBy(a -> a, Collectors.counting()));
        return axioms;// axioms.stream().filter(this::notSimple).limit(1000).toList();
    }
}

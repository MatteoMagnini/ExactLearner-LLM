package org.experiments.exp3.corrupter;

import org.exactlearner.engine.BaseEngine;
import org.exactlearner.engine.ELEngine;
import org.exactlearner.tree.ELEdge;
import org.exactlearner.tree.ELNode;
import org.exactlearner.tree.ELTree;
import org.exactlearner.learner.ConceptRelation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;

public class ConceptSwap {
    private Random random;
    private ConceptRelation<OWLClass> conceptRelation;
    private final BaseEngine engine;
    private int total = 0;
    private int notFirst = 0;
    private int failed = 0;

    public ConceptSwap(OWLOntology ontology, ConceptRelation<OWLClass> conceptRelation, ELEngine engine) {
        this.random = new Random(42);
        this.conceptRelation = conceptRelation;
        this.engine = engine;
    }

    public OWLSubClassOfAxiom simpleCorrupt(OWLSubClassOfAxiom axiom) throws Exception {
        ELTree sub = new ELTree(axiom.getSubClass());
        ELTree sup = new ELTree(axiom.getSuperClass());
        Set<OWLClass> concepts = getConcepts(sub);
        concepts.addAll(getConcepts(sup));
        List<OWLClass> c = new ArrayList<>(concepts);
        c.sort(Comparator.comparing(o -> o.getIRI().getFragment()));
        Collections.shuffle(c, random);
        OWLClass concept = c.get(0);

        List<OWLClass> swappable = getUnrelatedConcepts(concept);

        OWLClass s = swappable.get(0);
        ELTree nSub = swapConcepts(new ELTree(sub), concept, s);
        ELTree nSup = swapConcepts(new ELTree(sup), concept, s);
        return this.engine.getSubClassAxiom(nSub.transformToClassExpression(), nSup.transformToClassExpression());
    }

    public OWLSubClassOfAxiom corrupt(OWLSubClassOfAxiom axiom) throws Exception {
        total++;
        boolean first = true;
        ELTree sub = new ELTree(axiom.getSubClass());
        ELTree sup = new ELTree(axiom.getSuperClass());
        Set<OWLClass> concepts = getConcepts(sub);
        concepts.addAll(getConcepts(sup));
        List<OWLClass> c = new ArrayList<>(concepts);
        c.sort(Comparator.comparing(o -> o.getIRI().getFragment()));
        Collections.shuffle(c, random);

        for (OWLClass concept : c) {
            List<OWLClass> swappable = getUnrelatedConcepts(concept);
            for (OWLClass s : swappable) {
                ELTree nSub = swapConcepts(new ELTree(sub), concept, s);
                ELTree nSup = swapConcepts(new ELTree(sup), concept, s);
                OWLSubClassOfAxiom a = this.engine.getSubClassAxiom(nSub.transformToClassExpression(), nSup.transformToClassExpression());
                if (!engine.entailed(a)) {
                    return a;
                }
                if (first) {
                    first = false;
                    notFirst++;
                }
            }
        }
        failed++;
        throw new Exception();
    }

    private List<OWLClass> getUnrelatedConcepts(OWLClass concept) {
        Set<OWLClass> related = conceptRelation.getAllRelated(concept);
        List<OWLClass> all = new ArrayList<>(engine.getClassesInSignature());
        all.removeAll(related);

        List<OWLClass> swappable = new ArrayList<>(all);
        swappable.sort(Comparator.comparing(o -> o.getIRI().getFragment()));
        Collections.shuffle(swappable, random);
        return swappable;
    }

    public int getTotal() {
        return total;
    }

    public int getNotFirst() {
        return notFirst;
    }

    private ELTree swapConcepts(ELTree tree, OWLClass o, OWLClass n) {
        swapConcepts(tree.getRootNode(), o, n);
        return tree;
    }

    private ELNode swapConcepts(ELNode node, OWLClass o, OWLClass n) {
        if (node.getLabel().remove(o)) {
            node.getLabel().add(n);
        }
        for (ELEdge edge : node.getEdges()) {
            swapConcepts(edge.getNode(), o, n);
        }
        return node;
    }

    private Set<OWLClass> getConcepts(ELTree tree) throws Exception {
        return getConcepts(tree.getRootNode());
    }

    private Set<OWLClass> getConcepts(ELNode node) throws Exception {
        Set<OWLClass> concepts = new HashSet<>(node.getLabel());
        for (ELEdge edge : node.getEdges()) {
            concepts.addAll(getConcepts(edge.getNode()));
        }
        return concepts;
    }

    public int getFails() {
        return failed;
    }
}

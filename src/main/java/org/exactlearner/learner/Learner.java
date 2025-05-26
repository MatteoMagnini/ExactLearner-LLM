package org.exactlearner.learner;

import org.exactlearner.engine.BaseEngine;
import org.exactlearner.tree.ELEdge;
import org.exactlearner.tree.ELNode;
import org.exactlearner.tree.ELTree;
import org.exactlearner.utils.Metrics;
import org.semanticweb.elk.util.collections.Pair;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class Learner implements BaseLearner {

    private int unsaturationCounter = 0;
    private int saturationCounter = 0;
    private int leftDecompositionCounter = 0;
    private int rightDecompositionCounter = 0;
    private int mergeCounter = 0;
    private int branchCounter = 0;
    private final BaseEngine myEngineForT;
    private final BaseEngine myEngineForH;
    private final Metrics myMetrics;
    private OWLClassExpression myExpression;
    private OWLClass myClass;
    private ELTree leftTree;
    private ELTree rightTree;
    private final ConceptRelation<OWLClass> relation;

    public Learner(BaseEngine elEngineForT, BaseEngine elEngineForH, Metrics metrics) {
        this(elEngineForT, elEngineForH, metrics, new ConceptRelation<>());
    }

    public Learner(BaseEngine elEngineForT, BaseEngine elEngineForH, Metrics metrics, ConceptRelation<OWLClass> relation) {
        myEngineForH = elEngineForH;
        myEngineForT = elEngineForT;
        myMetrics = metrics;
        this.relation = relation;
    }

    /**
     * @param left  class expression on the left of an inclusion
     * @param right class expression on the right of an inclusion
     * @author anaozaki Naive algorithm to return a counterexample where one of the
     * sides is a concept name
     */
    @Override
    public OWLSubClassOfAxiom decompose(OWLClassExpression left, OWLClassExpression right) throws Exception {

        ELTree treeR = new ELTree(right);
        ELTree treeL = new ELTree(left);

        for (int i = 0; i < treeL.getMaxLevel(); i++) {

            for (ELNode nod : treeL.getNodesOnLevel(i + 1)) {

                for (OWLClass cl : myEngineForT.getClassesInSignature()) {
                    myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                    if (isCounterExample(nod.transformToDescription(), cl)) {
                        //leftDecompositionCounter++;
                        return myEngineForT.getSubClassAxiom(nod.transformToDescription(), cl);
                    }
                }
            }
        }

        for (int i = 0; i < treeR.getMaxLevel(); i++) {

            for (ELNode nod : treeR.getNodesOnLevel(i + 1)) {

                for (OWLClass cl : myEngineForT.getClassesInSignature()) {
                    myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                    if (isCounterExample(cl, nod.transformToDescription())) {
                        //rightDecompositionCounter++;
                        return myEngineForT.getSubClassAxiom(cl, nod.transformToDescription());
                    }
                }
            }
        }
        System.out.println(
                "Error decomposing. Not an EL Terminology: " + left.toString() + "subclass of" + right.toString());
        return myEngineForT.getSubClassAxiom(left, right);

    }

    private void saturateHypothesisLeft(OWLClassExpression expression, OWLClass cl) throws Exception {
        ELTree oldTree = new ELTree(expression);
        this.leftTree = new ELTree(expression);
        this.rightTree = new ELTree(cl);
        //if (leftTree.getMaxLevel() > 1) {

            for (int i = 0; i < leftTree.getMaxLevel(); i++) {
                for (ELNode nod : leftTree.getNodesOnLevel(i + 1)) {
                    for (OWLClass cl1 : myEngineForH.getClassesInSignature()) {
                        if (!nod.getLabel().contains(cl1)) {
                            nod.extendLabel(cl1);
                            if (myEngineForH.entailed(myEngineForH.getSubClassAxiom(
                                    oldTree.transformToClassExpression(), leftTree.transformToClassExpression()))) {
                                oldTree = new ELTree(leftTree.transformToClassExpression());
                            } else {
                                nod.remove(cl1);
                            }
                        }
                    }
                }
            }
        //}
        myExpression = leftTree.transformToClassExpression();
        myClass = (OWLClass) rightTree.transformToClassExpression();
        myEngineForT.getSubClassAxiom(myExpression, myClass);
    }

    @Override
    public OWLSubClassOfAxiom decomposeLeft(OWLClassExpression expression, OWLClass cl) throws Exception {
        myClass = cl;
        myExpression = expression;

        saturateHypothesisLeft(myExpression, myClass);

        while (decomposingLeft(myExpression)) {
        }
        return myEngineForT.getSubClassAxiom(myExpression, myClass);
    }

    private Boolean decomposingLeft(OWLClassExpression expression) throws Exception {
        ELTree tree = new ELTree(expression);
        for (int i = 0; i < tree.getMaxLevel(); i++) {
            for (ELNode nod : tree.getNodesOnLevel(i + 1)) {
                if (!nod.isRoot()) {
                    for (OWLClass cls : myEngineForT.getClassesInSignature()) {
                        myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                        if (isCounterExample(nod.transformToDescription(), cls)) {
                            myExpression = nod.transformToDescription();
                            myClass = cls;
                            leftDecompositionCounter++;
                            return true;
                        }
                    }
                }
                for (int j = 0; j < nod.getEdges().size(); j++) {
                    ELTree oldTree = new ELTree(tree.transformToClassExpression());

                    nod.getEdges().remove(j);
                    if (!myEngineForT.entailed(myEngineForT.getSubClassAxiom(tree.transformToClassExpression(),
                            oldTree.transformToClassExpression()))) {// we are removing things with top, this check is
                        // to avoid loop
                        for (OWLClass cls : myEngineForT.getClassesInSignature()) {
                            myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                            if (isCounterExample(tree.transformToClassExpression(), cls)) {
                                myExpression = tree.transformToClassExpression();
                                myClass = cls;
                                leftDecompositionCounter++;
                                return true;
                            }
                        }
                    }
                    tree = oldTree;
                }

            }
        }
        return false;
    }

    private void saturateHypothesisRight(OWLClass cl, OWLClassExpression expression) throws Exception {
        ELTree oldTree = new ELTree(expression);
        this.leftTree = new ELTree(cl);
        this.rightTree = new ELTree(expression);
        if (rightTree.getMaxLevel() > 1) {
            for (int i = 0; i < rightTree.getMaxLevel(); i++) {
                for (ELNode nod : rightTree.getNodesOnLevel(i + 1)) {
                    if (nod.isRoot()) {
                        for (OWLClass cl1 : myEngineForT.getClassesInSignature()) {
                            if (!nod.getLabel().contains(cl1) && !cl1.equals(cl)) {
                                if (myEngineForH.entailed(myEngineForH.getSubClassAxiom(cl, cl1))) {
                                    nod.extendLabel(cl1);
                                }
                            }
                        }
                    }
                    oldTree = new ELTree(rightTree.transformToClassExpression());
                    for (OWLClass cl1 : myEngineForH.getClassesInSignature()) {
                        if (!nod.getLabel().contains(cl1)) {
                            nod.extendLabel(cl1);
                            if (myEngineForH.entailed(myEngineForH.getSubClassAxiom(
                                    oldTree.transformToClassExpression(), rightTree.transformToClassExpression()))) {
                                oldTree = new ELTree(rightTree.transformToClassExpression());
                            } else {
                                nod.remove(cl1);
                            }
                        }
                    }
                }
            }
        }
        myClass = (OWLClass) leftTree.transformToClassExpression();
        myExpression = rightTree.transformToClassExpression();
        myEngineForT.getSubClassAxiom(myClass, myExpression);
    }

    @Override
    public OWLSubClassOfAxiom decomposeRight(OWLClass cl, OWLClassExpression expression) throws Exception {
        myClass = cl;
        myExpression = expression;
        saturateHypothesisRight(myClass, myExpression);
        // Check if a pair of myClass and myExpression has been previously found
        // In this case there is a loop and we should stop
        List<Pair<OWLClass, OWLClassExpression>> visited = new ArrayList<>();
        visited.add(new Pair<>(myClass, myExpression));
        while (decomposingRight(myClass, myExpression)) {
            if (visited.contains(new Pair<>(myClass, myExpression))) {
                break;
            }
            visited.add(new Pair<>(myClass, myExpression));
            saturateHypothesisRight(myClass, myExpression);
        }
        return myEngineForT.getSubClassAxiom(myClass, myExpression);
    }


    private boolean decomposingRight(OWLClass cl, OWLClassExpression expression) throws Exception {
        int startCount = rightDecompositionCounter;
        ELTree tree = new ELTree(expression);
        for (int i = 0; i < tree.getMaxLevel(); i++) {
            for (ELNode nod : tree.getNodesOnLevel(i + 1)) {
                List<ELEdge> edges = new ArrayList<>(nod.getEdges());
                for (ELEdge edge : edges) { // Iterates through all the edges of the current node
                    for (OWLClass c : nod.getLabel()) { // Iterates through all the labels of the current node
                        if (nod.isRoot()) {
                            // If it is the root node, the selected label should not be the same as the left concept of the axiom
                            myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                            if (myEngineForT.entailed(myEngineForT.getOWLEquivalentClassesAxiom(cl, c))) {
                                continue;
                            }
                        }
                        OWLSubClassOfAxiom axiom = myEngineForT.getSubClassAxiom(c, edge.transformToDescription());
                        myMetrics.setMembCount(myMetrics.getMembCount() + 1);

                        // If the new axiom is not entailed from the target ontology, we don't do anything with it.
                        if (!myEngineForT.entailed(axiom)) {
                            continue;
                        }

                        if (myEngineForH.entailed(axiom)) {
                            // If the axiom is entailed from the hypothesis ontology, it is removed from the node
                            nod.remove(edge);
                            rightDecompositionCounter++;
                            myExpression = tree.transformToClassExpression();
                            break;
                        } else {
                            // If the axiom is not entailed from the hypothesis, it becomes the new counter example
                            myExpression = axiom.getSuperClass();
                            myClass = c;
                            rightDecompositionCounter++;
                            return true;
                        }
                    }
                }
            }
        }
        return rightDecompositionCounter > startCount;
    }

    /**
     * @param expression class expression on the left of an inclusion
     * @param cl         class name on the right of an inclusion
     * @author anaozaki Concept Unsaturation on the left side of the inclusion
     */
    @Override
    public OWLSubClassOfAxiom unsaturateLeft(OWLClassExpression expression, OWLClass cl) throws Exception {
        this.leftTree = new ELTree(expression);
        this.rightTree = new ELTree(cl);
        myExpression = leftTree.transformToClassExpression();
        myClass = (OWLClass) rightTree.transformToClassExpression();
        for (int i = 0; i < leftTree.getMaxLevel(); i++) {
            List<ELNode> nodesList = leftTree.getNodesOnLevel(i + 1);
            for (ELNode nod : nodesList) {
                List<OWLClass> classesList = relation.topologicalOrder(nod.getLabel());
                for (OWLClass cl1 : classesList) {
                    if (nod.getLabel().contains(cl1) && !cl1.toString().contains("Thing")) {
                        nod.remove(cl1);
                        myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                        if (myEngineForT.entailed(myEngineForT.getSubClassAxiom(leftTree.transformToClassExpression(),
                                rightTree.transformToClassExpression()))) {
                            myExpression = leftTree.transformToClassExpression();
                            myClass = (OWLClass) rightTree.transformToClassExpression();

                            unsaturationCounter++;
                        } else {
                            nod.extendLabel(cl1);
                        }
                    }
                }
            }
        }
        return myEngineForT.getSubClassAxiom(myExpression, myClass);
    }

    /**
     * @param cl         class name on the left of an inclusion
     * @param expression class expression on the right of an inclusion
     * @author anaozaki Concept Saturation on the right side of the inclusion
     */
    @Override
    public OWLSubClassOfAxiom saturateRight(OWLClass cl, OWLClassExpression expression) throws Exception {
        this.leftTree = new ELTree(cl);
        this.rightTree = new ELTree(expression);
        for (int i = 0; i < rightTree.getMaxLevel(); i++) {
            for (ELNode nod : rightTree.getNodesOnLevel(i + 1)) {
                for (OWLClass cl1 : myEngineForT.getClassesInSignature()) {
                    if (!nod.getLabel().contains(cl1)) {
                        if (nod.isRoot()) {
                            if (cl1.equals(cl)) {
                                continue;
                            }
                            OWLSubClassOfAxiom axiom = myEngineForH.getSubClassAxiom(cl, cl1);
                            if (myEngineForH.entailed(axiom)) {
                                nod.extendLabel(cl1);
                                saturationCounter++;
                            } // No need to check other cases, as it should be in the hypothesis because of precomputation
                        } else {
                            nod.extendLabel(cl1);
                            myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                            if (myEngineForT.entailed(myEngineForT.getSubClassAxiom(leftTree.transformToClassExpression(),
                                    rightTree.transformToClassExpression()))) {
                                saturationCounter++;
                            } else {
                                nod.remove(cl1);
                            }
                        }
                    }
                }
            }
        }
        myClass = (OWLClass) leftTree.transformToClassExpression();
        myExpression = rightTree.transformToClassExpression();
        return myEngineForT.getSubClassAxiom(myClass, myExpression);
    }

    @Override
    public OWLSubClassOfAxiom mergeRight(OWLClass cl, OWLClassExpression expression) throws Exception {
        myClass = cl;
        myExpression = expression;
        while (merging(myClass, myExpression)) {
        }
        return myEngineForT.getSubClassAxiom(myClass, myExpression);
    }

    private Boolean merging(OWLClass cl, OWLClassExpression expression) throws Exception {

        ELTree tree = new ELTree(expression);


        for (int i = 0; i < tree.getMaxLevel(); i++) {
            int l1 = 0;
            for (ELNode nod : tree.getNodesOnLevel(i + 1)) {

                if (!nod.getEdges().isEmpty() && nod.getEdges().size() > 1) {

                    for (int j = 0; j < nod.getEdges().size(); j++) {

                        for (int k = 0; k < nod.getEdges().size(); k++) {

                            if (j != k && nod.getEdges().get(j).getStrLabel()
                                    .equals(nod.getEdges().get(k).getStrLabel())) {
                                ELTree tmp = new ELTree(tree.transformToClassExpression());
                                List<ELNode> set = tmp.getNodesOnLevel(i + 1);
                                ELNode n = set.iterator().next();
                                for (int i1 = 0; i1 < l1; i1++) {
                                    n = set.iterator().next();
                                }
                                n.getEdges().get(j).getNode().getLabel()
                                        .addAll(n.getEdges().get(k).getNode().getLabel());

                                if (!n.getEdges().get(k).getNode().getEdges().isEmpty())
                                    n.getEdges().get(j).getNode().getEdges()
                                            .addAll(n.getEdges().get(k).getNode().getEdges());

                                n.getEdges().remove(n.getEdges().get(k));

                                myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                                if (!myEngineForT.entailed(myEngineForT.getSubClassAxiom(
                                        tree.transformToClassExpression(), tmp.transformToClassExpression()))
                                        // if the merged tree is in fact a stronger expression
                                        && myEngineForT.entailed(
                                        myEngineForT.getSubClassAxiom(cl, tmp.transformToClassExpression()))) {
                                    myExpression = tmp.transformToClassExpression();
                                    myClass = cl;
                                    mergeCounter++;

                                    return true;
                                }

                            }
                        }
                    }
                }
                l1++;
            }
        }
        return false;
    }

    @Override
    public OWLSubClassOfAxiom branchLeft(OWLClassExpression expression, OWLClass cl) throws Exception {
        myClass = cl;
        myExpression = expression;
        ELTree tree = new ELTree(expression);
        for (int i = 0; i < tree.getMaxLevel(); i++) {
            for (ELNode nod : tree.getNodesOnLevel(i + 1)) {
                if (!nod.getEdges().isEmpty()) {

                    for (int j = 0; j < nod.getEdges().size(); j++) {
                        if (nod.getEdges().get(j).getNode().getLabel().size() > 1) {
                            TreeSet<OWLClass> s = new TreeSet<>(nod.getEdges().get(j).getNode().getLabel());
                            for (OWLClass lab : s) {
                                ELTree oldTree = new ELTree(tree.transformToClassExpression());
                                ELTree newSubtree = new ELTree(
                                        nod.getEdges().get(j).getNode().transformToDescription());
                                TreeSet<OWLClass> ts = new TreeSet<>(newSubtree.getRootNode().getLabel());
                                for (OWLClass l : ts) {
                                    newSubtree.getRootNode().getLabel().remove(l);
                                }
                                newSubtree.getRootNode().extendLabel(lab);
                                ELEdge newEdge = new ELEdge(nod.getEdges().get(j).getLabel(), newSubtree.getRootNode());
                                nod.getEdges().add(newEdge);
                                nod.getEdges().get(j).getNode().remove(lab);
                                myMetrics.setMembCount(myMetrics.getMembCount() + 1);
                                if (!myEngineForT.entailed(myEngineForT.getSubClassAxiom(
                                        tree.transformToClassExpression(), oldTree.transformToClassExpression()))
                                        && myEngineForT.entailed(
                                        myEngineForT.getSubClassAxiom(tree.transformToClassExpression(), cl))) {
                                    myExpression = tree.transformToClassExpression();
                                    myClass = cl;

                                    branchCounter++;
                                } else {
                                    tree = oldTree;
                                }
                            }
                        }
                    }

                }
            }
        }
        return myEngineForT.getSubClassAxiom(myExpression, myClass);
    }

    // at the moment duplicated
    // @Todo: @Riccardo please check if this is correct.
    private Boolean isCounterExample(OWLClassExpression left, OWLClassExpression right) {
        return !myEngineForH.entailed(myEngineForH.getSubClassAxiom(left, right)) &&
                myEngineForT.entailed(myEngineForT.getSubClassAxiom(left, right));
    }

    @Override
    public int getNumberUnsaturations() {
        return unsaturationCounter;
    }

    @Override
    public int getNumberSaturations() {
        return saturationCounter;
    }

    @Override
    public int getNumberMerging() {
        return mergeCounter;
    }

    @Override
    public int getNumberBranching() {
        return branchCounter;
    }

    @Override
    public int getNumberLeftDecomposition() {
        return leftDecompositionCounter;
    }

    @Override
    public int getNumberRightDecomposition() {
        return rightDecompositionCounter;
    }

    @Override
    public void minimiseHypothesis(BaseEngine elQueryEngineForH, OWLOntology hypothesisOntology) {
        Set<OWLAxiom> tmpaxiomsH = elQueryEngineForH.getOntology().getAxioms();
        Iterator<OWLAxiom> ineratorMinH = tmpaxiomsH.iterator();

        if (tmpaxiomsH.size() > 1) {
            while (ineratorMinH.hasNext()) {
                OWLAxiom checkedAxiom = ineratorMinH.next();

                if (checkedAxiom.isOfType(AxiomType.SUBCLASS_OF)) {
                    OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) checkedAxiom;
                    OWLClassExpression left = axiom.getSubClass();
                    OWLClassExpression right = axiom.getSuperClass();

                    if (elQueryEngineForH
                            .entailed(elQueryEngineForH.getSubClassAxiom(right, left))) {
                        RemoveAxiom removedAxiom = new RemoveAxiom(elQueryEngineForH.getOntology(),
                                checkedAxiom);
                        elQueryEngineForH.applyChange(removedAxiom);
                        checkedAxiom = elQueryEngineForH.getOWLEquivalentClassesAxiom(left, right);

                        AddAxiom addAxiomtoH = new AddAxiom(hypothesisOntology, checkedAxiom);

                        elQueryEngineForH.applyChange(addAxiomtoH);
                    }
                }
                RemoveAxiom removedAxiom = new RemoveAxiom(elQueryEngineForH.getOntology(),
                        checkedAxiom);
                elQueryEngineForH.applyChange(removedAxiom);

                if (!elQueryEngineForH.entailed(checkedAxiom)) {
                    // minimize and put it back
                    checkedAxiom = minimizeAxiom(checkedAxiom);

                    AddAxiom addAxiomtoH = new AddAxiom(hypothesisOntology, checkedAxiom);
                    elQueryEngineForH.applyChange(addAxiomtoH);
                }

            }
        }

    }

    @Override
    public void precomputation() {
        int i = myEngineForT.getClassesInSignature().size();
        myMetrics.setMembCount(myMetrics.getMembCount() + i * (i - 1));
        for (OWLClass cl1 : myEngineForT.getClassesInSignature()) {
            for (OWLClass cl2 : myEngineForT.getClassesInSignature()) {
                if (cl1.equals(cl2)) {
                    continue;
                }
                OWLSubClassOfAxiom addedAxiom = myEngineForT.getSubClassAxiom(cl1, cl2);
                if (myEngineForH.entailed(addedAxiom)) {
                    addHypothesis(addedAxiom);
                }
                if (myEngineForT.entailed(addedAxiom)) {
                    relation.addEdge(cl1, cl2);
                    addHypothesis(addedAxiom);
                }
            }
        }
    }

    private OWLAxiom minimizeAxiom(OWLAxiom checkedAxiom) {

        if (checkedAxiom.isOfType(AxiomType.SUBCLASS_OF)) {

            checkedAxiom = minimizeRightConcept(((OWLSubClassOfAxiom) checkedAxiom).getSubClass(),
                    ((OWLSubClassOfAxiom) checkedAxiom).getSuperClass());

        }
        return checkedAxiom;
    }

    private OWLSubClassOfAxiom minimizeRightConcept(OWLClassExpression leftExpr, OWLClassExpression rightExpr) {

        ELTree tree;
        try {
            tree = new ELTree(rightExpr);

            for (int i = 0; i < tree.getMaxLevel(); i++) {
                for (ELNode nod : tree.getNodesOnLevel(i + 1)) {
                    OWLClassExpression cls = nod.transformToDescription();
                    for (OWLClass cl1 : cls.getClassesInSignature()) {
                        if ((nod.getLabel().contains(cl1) && !cl1.toString().contains("Thing"))) {
                            nod.remove(cl1);
                            if (myEngineForH.entailed(
                                    myEngineForH.getSubClassAxiom(tree.transformToClassExpression(), rightExpr))) {
                            } else {
                                nod.extendLabel(cl1);
                            }
                        }
                    }
                }
            }
            myExpression = tree.transformToClassExpression();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myEngineForH.getSubClassAxiom(leftExpr, myExpression);
    }

    private void addHypothesis(OWLSubClassOfAxiom axiom) {
        OWLAxiomChange add = new AddAxiom(myEngineForH.getOntology(), axiom);
        myEngineForH.applyChange(add);
    }
}
package org.exactlearner.learner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConceptRelationNode<T> {
    private final List<T> nodeLabels;
    private final List<ConceptRelationNode<T>> children;
    private final List<ConceptRelationNode<T>> parents;

    public ConceptRelationNode(T saturationClass) {
        this.nodeLabels = new ArrayList<>();
        this.nodeLabels.add(saturationClass);
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
    }

    public ConceptRelationNode(List<T> nodeLabels, List<ConceptRelationNode<T>> children, List<ConceptRelationNode<T>> parents) {
        this.nodeLabels = new ArrayList<>(nodeLabels);
        this.children = new ArrayList<>(children);
        this.parents = new ArrayList<>(parents);
    }

    public List<ConceptRelationNode<T>> getPossiblePath(ConceptRelationNode<T> ancestor) {
        if (ancestor == this) {
            List<ConceptRelationNode<T>> s = new ArrayList<>();
            s.add(this);
            return s;
        }
        List<ConceptRelationNode<T>> possiblePath = new ArrayList<>();
        for (ConceptRelationNode<T> parent : parents) {
            possiblePath.addAll(parent.getPossiblePath(ancestor));
        }
        if (!possiblePath.isEmpty()) {
            possiblePath.add(this);
        }
        return possiblePath;
    }

    public List<T> getAllAncestors() {
        Set<T> ancestors = new HashSet<>();
        for (ConceptRelationNode<T> parent : parents) {
            ancestors.addAll(parent.getAllAncestors());
            ancestors.addAll(parent.getNodeLabels());
        }
        return new ArrayList<>(ancestors);
    }

    public List<T> getAllDescendants() {
        Set<T> descendants = new HashSet<>();
        for (ConceptRelationNode<T> children : children) {
            descendants.addAll(children.getAllDescendants());
            descendants.addAll(children.getNodeLabels());
        }
        return new ArrayList<>(descendants);
    }

    public List<T> getNodeLabels() {
        return nodeLabels;
    }

    public void addChild(ConceptRelationNode<T> child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public List<ConceptRelationNode<T>> getChildren() {
        return children;
    }

    public void addParent(ConceptRelationNode<T> parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public List<ConceptRelationNode<T>> getParents() {
        return parents;
    }
}

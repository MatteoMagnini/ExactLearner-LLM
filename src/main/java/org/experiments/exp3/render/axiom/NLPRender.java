package org.experiments.exp3.render.axiom;

import org.experiments.exp3.render.concept.ConceptNameRenderer;
import org.semanticweb.owlapi.io.OWLRenderer;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NLPRender implements AxiomRenderer {
    private final ConceptNameRenderer conceptNameRenderer;

    public NLPRender(ConceptNameRenderer conceptNameRenderer) {
        this.conceptNameRenderer = conceptNameRenderer;
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        return "Can " + render(axiom.getSubClass()) +  " be considered a subcategory of '" + render(axiom.getSuperClass()) + "'?";
    }

    public String render(OWLObjectSomeValuesFrom axiom) {
        return "something that " + render(axiom.getProperty()) + " some " + render(axiom.getFiller()) ;
    }

    public String render(OWLObjectProperty property) {
        return conceptNameRenderer.render(property);
    }

    public String render(OWLObjectIntersectionOf intersection) {
        return intersection.getOperands().stream().map(this::render).collect(Collectors.joining(" that is also "));
    }

    public String render(OWLObjectPropertyExpression property) {
        if (property instanceof OWLObjectProperty p) {
            return render(p);
        }

        throw new RuntimeException("Unexpected property type: " + property.getClass());
    }

    protected String render(OWLClassExpression expression) {
        if (expression instanceof OWLClass c) {
            return conceptNameRenderer.render(c);
        }
        if (expression instanceof OWLObjectSomeValuesFrom c) {
            return render(c);
        }
        if (expression instanceof OWLObjectIntersectionOf c) {
            return render(c);
        }

        throw new RuntimeException("Unexpected class: " + expression);
    }
}

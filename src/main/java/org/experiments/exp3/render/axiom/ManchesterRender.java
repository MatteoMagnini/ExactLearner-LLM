package org.experiments.exp3.render.axiom;

import org.experiments.exp3.render.concept.ConceptNameRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManchesterRender implements AxiomRenderer {
    private final ConceptNameRenderer conceptNameRenderer;

    public ManchesterRender(ConceptNameRenderer conceptNameRenderer) {
        this.conceptNameRenderer = conceptNameRenderer;
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        return render(axiom.getSubClass()) +  " SubClassOf " + render(axiom.getSuperClass());
    }

    public String render(OWLObjectSomeValuesFrom axiom) {
        String s;
        if (axiom.getFiller() instanceof OWLObjectIntersectionOf o) {
            s = "(" + render(o) + ")";
        } else {
            s = render(axiom.getFiller());
        }
        return render(axiom.getProperty()) + " some " + s ;
    }

    public String render(OWLObjectProperty property) {
        return conceptNameRenderer.render(property);
    }

    public String render(OWLObjectIntersectionOf intersection) {
        List<String> s = new ArrayList<>();
        for (OWLClassExpression e : intersection.getOperands()) {
            if (e instanceof OWLObjectSomeValuesFrom o) {
                s.add("( " + render(o) + " )");
            } else {
                s.add(render(e));
            }
        }
        return String.join(" and ", s);
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

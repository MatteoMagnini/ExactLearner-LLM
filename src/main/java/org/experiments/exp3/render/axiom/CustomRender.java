package org.experiments.exp3.render.axiom;

import org.experiments.exp3.render.concept.ConceptNameRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class CustomRender implements AxiomRenderer {
    private final ConceptNameRenderer conceptNameRenderer;
    private final String format;

    public CustomRender(ConceptNameRenderer conceptNameRenderer, String customString) {
        this.conceptNameRenderer = conceptNameRenderer;
        this.format = customString.substring(7);
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        String s = String.format(format, render(axiom.getSubClass()), render(axiom.getSuperClass()));
        return s.replaceAll("\r", " ").replaceAll("\n", " ");
    }

    public String render(OWLObjectSomeValuesFrom axiom) {
        return "";
    }

    protected String render(OWLClassExpression expression) {
        if (expression instanceof OWLClass c) {
            return conceptNameRenderer.render(c);
        }
        if (expression instanceof OWLObjectSomeValuesFrom c) {
            render(c);
        }

        throw new RuntimeException("Unexpected class: " + expression);
    }
}

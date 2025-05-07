package org.experiments.exp3.render.axiom;

import org.experiments.exp3.render.concept.ConceptNameRenderer;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class NewNLPRender extends NLPRender {
    public NewNLPRender(ConceptNameRenderer conceptNameRenderer) {
        super(conceptNameRenderer);
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        String s = "Can " + render(axiom.getSubClass()) +  " be considered a subcategory of " + render(axiom.getSuperClass()) + "?";
        return s.replaceAll("\r", " ").replaceAll("\n", " ");
    }
}

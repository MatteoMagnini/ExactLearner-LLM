package org.experiments.exp3.render.axiom;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class NLPRender extends ManchesterRender {
    public NLPRender(ShortFormProvider provider) {
        super(provider);
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        return addExtraSemantic(super.render(axiom));
    }

    private String addExtraSemantic(String message) {
        if (message.contains(" SubClassOf ")) {
            var parts = message.split(" SubClassOf ", 2);
            String pt1 = addExtraSemantic(parts[0]);
            String pt2 = addExtraSemantic(parts[1]);
            return "Can " + pt1 + " be considered a subcategory of '" + pt2 + "'?";
        } else if (message.contains(" and ")) {
            var parts = message.split(" and ", 2);
            return addExtraSemantic(parts[0]) + " that is also " + addExtraSemantic(parts[1]);
        } else if (message.contains(" some ")) {
            var parts = message.split(" some ", 2);
            return "something that " + addExtraSemantic(parts[0]) + " some " + addExtraSemantic(parts[1]);
        } else if (message.startsWith("(") && message.endsWith(")")) {
            return addExtraSemantic(message.substring(1, message.length() - 1));
        } else {
            return message;
        }
    }
}

package org.experiments.exp3.render.concept;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class ANameRenderer implements ShortFormProvider {
    @Override
    public String getShortForm(OWLEntity owlEntity) {
        if (owlEntity instanceof OWLClass) {
            return "A";
        }
        if (owlEntity instanceof OWLObjectProperty) {
            return "r";
        }
        return "x";
    }

    @Override
    public void dispose() {
        ShortFormProvider.super.dispose();
    }
}

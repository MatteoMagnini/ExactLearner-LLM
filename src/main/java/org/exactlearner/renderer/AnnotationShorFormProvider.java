package org.exactlearner.renderer;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.HashMap;
import java.util.Map;

public class AnnotationShorFormProvider implements ShortFormProvider {

    private final OWLOntology ontology;
    private final Map<IRI, String> labelMap = new HashMap<>();

    public AnnotationShorFormProvider(OWLOntology ontology) {
        this.ontology = ontology;
    }

    @Override
    public String getShortForm(OWLEntity owlEntity) {
        return render(owlEntity.getIRI());
    }

    public String render(IRI iri) {
        if (labelMap.containsKey(iri)) {
            return labelMap.get(iri);
        }
        for (OWLAnnotationAssertionAxiom a : ontology.getAnnotationAssertionAxioms(iri)) {
            if (a != null && a.getProperty().isLabel() && a.getValue() instanceof OWLLiteral val) {
                labelMap.put(iri, val.getLiteral());
                return val.getLiteral();
            }
        }
        return iri.getFragment();
    }

    @Override
    public void dispose() {
    }
}

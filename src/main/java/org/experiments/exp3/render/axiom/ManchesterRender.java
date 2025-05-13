package org.experiments.exp3.render.axiom;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class ManchesterRender implements AxiomRenderer {
    private final ManchesterOWLSyntaxOWLObjectRendererImpl renderer;

    public ManchesterRender() {
        renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
    }

    public ManchesterRender(ShortFormProvider provider) {
        renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        if (provider != null) {
            renderer.setShortFormProvider(provider);
        }
    }

    @Override
    public String render(OWLSubClassOfAxiom axiom) {
        return renderer.render(axiom);
    }
}

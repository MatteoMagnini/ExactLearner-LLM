package org.experiments.exp3;

import org.configurations.Configuration;
import org.exactlearner.renderer.AnnotationShorFormProvider;
import org.experiments.exp3.render.axiom.*;
import org.experiments.exp3.result.SettingResult;
import org.experiments.logger.Cache;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.HashMap;
import java.util.Map;

public class DBModelQuery {
    private final SettingResult setting;
    private final ModelQuery modelQuery;
    private final Map<OWLOntology, AxiomRenderer> axiomRenderer;
    private final Configuration.QueryFormat queryFormat;

    public DBModelQuery(Cache cache, String model, String system, Configuration.QueryFormat queryFormat, int maxTokens, SettingResult settingResult) {
        this.modelQuery = new ModelQuery(cache, model, system, queryFormat.getName(), maxTokens);
        this.setting = settingResult;
        this.queryFormat = queryFormat;
        this.axiomRenderer = new HashMap<>();
    }

    public SettingResult getSetting() {
        return setting;
    }

    public String getText(OWLSubClassOfAxiom axiom, OWLOntology ontology) {
        return getAxiomRender(ontology).render(axiom);
    }

    public String getResult(String text) {
        return modelQuery.result(text);
    }

    private AxiomRenderer getAxiomRender(OWLOntology ontology) {
        if (axiomRenderer.containsKey(ontology)) {
            return axiomRenderer.get(ontology);
        }
        ShortFormProvider cr = switch (queryFormat.getConceptName()) {
            case "class" -> null;
            default -> new AnnotationShorFormProvider(ontology);
        };

        AxiomRenderer renderer = switch (queryFormat.getAxiom().toLowerCase()) {
            case "manchester" -> new ManchesterRender(cr);
            case "nlp" -> new NLPRender(cr);
            default -> throw new IllegalStateException("Unexpected value: " + queryFormat.getAxiom());
        };
        axiomRenderer.put(ontology, renderer);
        return renderer;
    }
}

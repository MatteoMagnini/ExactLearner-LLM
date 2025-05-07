package org.experiments.exp3;

import org.configurations.Configuration;
import org.experiments.exp3.render.axiom.*;
import org.experiments.exp3.render.concept.ClassName;
import org.experiments.exp3.render.concept.ConceptNameRenderer;
import org.experiments.exp3.render.concept.LabelName;
import org.experiments.exp3.result.SettingResult;
import org.experiments.logger.Cache;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

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
        ConceptNameRenderer cr = switch (queryFormat .getConceptName()) {
            case "class" -> new ClassName();
            default -> new LabelName(ontology);
        };
        if (queryFormat.getAxiom().startsWith("Custom;")) {
            return new CustomRender(cr, queryFormat.getAxiom());
        }

        AxiomRenderer renderer = switch (queryFormat.getAxiom()) {
            case "manchester" -> new ManchesterRender(cr);
            case "originalNLP" -> new NLPRender(cr);
            case "NLP" -> new NewNLPRender(cr);
            default -> throw new IllegalStateException("Unexpected value: " + queryFormat.getAxiom());
        };
        axiomRenderer.put(ontology, renderer);
        return renderer;
    }
}

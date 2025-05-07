package org.exactlearner.engine;

import org.exactlearner.parser.OWLParser;
import org.experiments.logger.CacheManager;
import org.experiments.workload.WorkloadManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class NLPLLMEngine extends LLMEngine {
    public NLPLLMEngine(OWLOntology ontology, String ontologyName, String model, String system, Integer maxTokens, OWLOntologyManager manager) {
        super(ontology, ontologyName, model, system, maxTokens, manager);
    }

    public NLPLLMEngine(OWLOntology ontology, OWLOntologyManager manager, WorkloadManager workloadManager) {
        super(ontology, manager, workloadManager);
    }

    public NLPLLMEngine(OWLOntology ontology, OWLOntologyManager manager, WorkloadManager workloadManager, OWLParser parser, AxiomSimplifier simplifier) {
        super(ontology, manager, workloadManager, parser, simplifier);
    }

    @Override
    protected Boolean runTaskAndGetResult(String message) {
        message = message.replace("  ", " ");
        message = addExtraSemantic(message);
        return super.runTaskAndGetResult(message);
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

package org.exactlearner.engine;

import org.exactlearner.parser.OWLParser;
import org.exactlearner.parser.OWLParserImpl;
import org.exactlearner.renderer.AnnotationShorFormProvider;
import org.experiments.workload.WorkloadManager;
import org.experiments.workload.WorkloadManagerImpl;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LLMEngine implements BaseEngine {

    private final OWLOntology ontology;
    private final OWLOntologyManager manager;
    private final OWLParser parser;
    private final WorkloadManager workloadManager;
    private final OWLObjectRenderer renderer;
    private final AxiomSimplifier simplifier;

    public LLMEngine(OWLOntology ontology, String ontologyName, String model, String system, Integer maxTokens, OWLOntologyManager manager) {
        this.ontology = ontology;
        this.manager = manager;
        this.parser = new OWLParserImpl(ontology);
        String queryFormat = "";
        this.workloadManager = new WorkloadManagerImpl(model, system, maxTokens, queryFormat, ontologyName, null);
        this.renderer = createRenderer(ontology);
        simplifier = null;
    }

    public LLMEngine(OWLOntology ontology, OWLOntologyManager manager, WorkloadManager workloadManager) {
        this(ontology, manager, workloadManager, new OWLParserImpl(ontology), null);
    }

    public LLMEngine(OWLOntology ontology, OWLOntologyManager manager, WorkloadManager workloadManager, OWLParser parser, AxiomSimplifier simplifier) {
        this.ontology = ontology;
        this.manager = manager;
        this.parser = parser;
        this.workloadManager = workloadManager;
        this.renderer = createRenderer(ontology);
        this.simplifier = simplifier;
    }

    private OWLObjectRenderer createRenderer(OWLOntology ontology) {
        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        renderer.setShortFormProvider(new AnnotationShorFormProvider(ontology));
        return renderer;
    }

    @Override
    public OWLSubClassOfAxiom getSubClassAxiom(OWLClassExpression classA, OWLClassExpression classB) {
        return manager.getOWLDataFactory().getOWLSubClassOfAxiom(classA, classB);
    }


    protected Boolean runTaskAndGetResult(String message) {
        message = message.replace("  ", " ");
        return workloadManager.runWorkload(message);
    }

    @Override
    public List<OWLClass> getClassesInSignature() {
        return parser.getOrderedClasses();
    }

    @Override
    public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(OWLClassExpression concept1, OWLClassExpression concept2) {
        return manager.getOWLDataFactory().getOWLEquivalentClassesAxiom(concept1, concept2);
    }

    @Override
    public OWLClassExpression getOWLObjectIntersectionOf(Set<OWLClassExpression> mySet) {
        return manager.getOWLDataFactory().getOWLObjectIntersectionOf(mySet);
    }

    @Override
    public Boolean entailed(OWLAxiom ax) {
        if (ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
            OWLEquivalentClassesAxiom eax = (OWLEquivalentClassesAxiom) ax;
            for (OWLSubClassOfAxiom sax : eax.asOWLSubClassOfAxioms()) {
                if (!entailed(sax)) {
                    return false;
                }
            }
            return true;
        }

        if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
            return entailed((OWLSubClassOfAxiom) ax);
        }

        throw new RuntimeException("Axiom type not supported " + ax);

    }

    @Override
    public Boolean entailed(Set<OWLAxiom> axioms) {
        for (OWLAxiom ax : axioms) {
            if (!entailed(ax)) {
                return false;
            }
        }
        return true;
    }

    private Boolean entailed(OWLSubClassOfAxiom axiom) {
        if (simplifier != null) {
            Optional<OWLSubClassOfAxiom> opt = simplifier.shorten(axiom);
            if (opt.isEmpty()) {
                return true;
            }
            axiom = opt.get();
        }
        if (System.getenv("EXACTLEARNER_SPLIT") == null || System.getenv("EXACTLEARNER_SPLIT").equals("true")) {
            if (axiom.getSuperClass() instanceof OWLObjectIntersectionOf intersection) {
                OWLClassExpression expression = axiom.getSubClass();
                for (OWLClassExpression sup : intersection.getOperands()) {
                    OWLSubClassOfAxiom ax = getSubClassAxiom(expression, sup);
                    String query = renderer.render(ax).replaceAll("\r", " ").replaceAll("\n", " ");
                    if (!runTaskAndGetResult(query)) {
                        return false;
                    }
                }
                return true;
            }
        }
        var query = renderer.render(axiom).replaceAll("\r", " ").replaceAll("\n", " ");
        return runTaskAndGetResult(query);
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public void disposeOfReasoner() {
        System.out.flush();
    }

    @Override
    public void applyChange(OWLOntologyChange change) {
        manager.applyChange(change);
    }
}

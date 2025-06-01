package org.experiments.exp3.experiment;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.configurations.Configuration;
import org.exactlearner.engine.AxiomSimplifier;
import org.exactlearner.engine.LLMEngine;
import org.exactlearner.engine.NLPLLMEngine;
import org.exactlearner.parser.OWLParser;
import org.exactlearner.parser.OWLParserCustomSignature;
import org.experiments.LaunchLLMLearner;
import org.experiments.workload.WorkLoadCounter;
import org.experiments.workload.WorkloadManager;
import org.experiments.workload.WorkloadManagerImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.utility.YAMLConfigLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

public class PACLaunch extends LaunchLLMLearner {
    private int limit = 150;
    private int base = 10;

    public static void main(String[] args) {
        LogManager.getRootLogger().atLevel(Level.OFF);
        new PACLaunch().run(args);
    }

    @Override
    protected void loadTargetOntology(String ontology) throws OWLOntologyCreationException, IOException {
        super.loadTargetOntology(ontology);
        parser = getParser();
    }

    @Override
    protected void computeConceptAndRoleNumbers() throws IOException {
        this.conceptNumber = parser.getClassesNamesAsString().size();
        this.roleNumber = parser.getObjectPropertiesAsString().size();
    }

    @Override
    protected void loadConfiguration(String fileName) {
        Configuration config = new YAMLConfigLoader().getConfig(fileName, Configuration.class);
        //choose configuration from file here:
        models = config.getModels();
        system = config.getSystem();
        queryFormat = config.getQueryFormat();
        ontologies = config.getOntologies();
        maxTokens = config.getMaxTokens();
        hypothesisSizes = ontologies.stream().map(a -> limit*3).toList();
    }

    @Override
    protected void setLLMEngine(String model, String ontologyShortName) {
        WorkloadManager workloadManager = new WorkloadManagerImpl(model, system, maxTokens, queryFormat, ontologyShortName, cacheManager, counter);
        switch (queryFormat) {
            case "manchester" ->
                    llmQueryEngineForT = new LLMEngine(groundTruthOntology, myManager, workloadManager, parser, new AxiomSimplifier(elQueryEngineForH)); //new AxiomSimplifier(elQueryEngineForH));
            case "nlp" ->
                    llmQueryEngineForT = new NLPLLMEngine(groundTruthOntology, myManager, workloadManager, parser, new AxiomSimplifier(elQueryEngineForH)); // new AxiomSimplifier(elQueryEngineForH));
            default -> throw new IllegalStateException("Unexpected value: " + queryFormat);
        }
    }

    private OWLParser getParser() {
        AxiomHandler axiomHandler = new AxiomHandler(new NotSoRandomAxiomIterator(groundTruthOntology, limit, base).iterator());

        return new OWLParserCustomSignature(axiomHandler.getClasses(), axiomHandler.getProperties(), new HashSet<>(axiomHandler.getAxioms()));
    }

    @Override
    public void run(String[] args) {
        if (args.length > 3) {
            limit = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            base = Integer.parseInt(args[4]);
        }
        super.run(args);
    }

    @Override
    protected void createWorkCounter(String ontologyShortName, String model) {
        counter = new WorkLoadCounter(infoString(ontologyShortName, model, queryFormat, system), limit, base);
    }

    @Override
    protected void setUpOntologyFolders(String format, String system, String model, String ontology) {
        String name = Path.of(ontology).getFileName().toString().replace(".owl", "");

        ontologyFolder = "results" + fileSeparator + "ontologies" + fileSeparator + "target_" + name + ".owl";
        ontologyFolderH = "results" + fileSeparator + "ontologies" + fileSeparator + infoString(name, model, format, system) + "_" + limit + "_" + base + ".owl";
    }
}

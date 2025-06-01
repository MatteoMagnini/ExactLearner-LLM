package org.analysis.specialized;

import org.configurations.Configuration;
import org.experiments.exp3.experiment.AxiomHandler;
import org.experiments.exp3.experiment.AxiomIterator;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ResultBase {

    private final Collection<ModelSetting> models;
    private final Collection<String> ontologies;

    protected String setting;

    protected String ontology;
    protected OWLOntology expectedOntology;
    protected OWLReasoner expectedReasoner;

    protected int limit;
    protected int base;

    protected Set<OWLClass> classes;
    protected Set<OWLObjectProperty> properties;

    public ResultBase(Configuration config, int limit, int base) {
        String queryFormat = config.getQueryFormat().toLowerCase();
        String system = config.getSystem().strip().equals("Answer with only True or False.") ? "base" : "advanced";
        setting = queryFormat + "_" + system;

        models = config.getModels().stream()
                .map(s -> s.replace(":", "-"))
                .map(s -> new ModelSetting(setting, s))
                .collect(Collectors.toList());
        ontologies = config.getOntologies().stream().map(o -> o.substring(0, o.length() - 4)).collect(Collectors.toList());

        this.limit = limit;
        this.base = base;
    }

    protected ResultBase(Collection<ModelSetting> models, Collection<String> ontologies, int limit, int base) {
        this.models = models;
        this.ontologies = ontologies;
        this.limit = limit;
        this.base = base;
    }

    public void run() {
        try {
            for (String ontology : ontologies) {
                loadExpected(ontology);
                for (ModelSetting model : models) {
                    setting = model.getSetting();
                    compareOntologies(model.getModel());
                }
                close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void loadExpected(String ontology) {
        this.ontology = ontology;
        this.expectedOntology = loadOntology();
        //expectedReasoner = new ELEngine(expectedOntology);
        //expectedReasoner = new Reasoner(expectedOntology);
        expectedReasoner = new ElkReasonerFactory().createReasoner(expectedOntology);
        getAllowedValues();
    }

    protected abstract void compareOntologies(String model) throws Exception;


    protected String getOntologyStringName() {
        return Path.of(ontology).getFileName().toString().replaceAll("\\(.*\\)", "");
    }

    protected Path getOntologyPath(String type, String enginePrefix, String model) {
        return Path.of(String.format("%1$shome%1$smartint%1$sonto%1$sontologies%1$s%2$s%1$s%3$slearned_%4$s_%5$s",
                FileSystems.getDefault().getSeparator(), type, enginePrefix, model.replace(":", "-"), getOntologyStringName()));
    }

    protected String getOntologyPathString(String type, String enginePrefix, String model) {
        return getOntologyPath(type, enginePrefix, model).toString();
    }

    protected OWLOntology loadOntology(String type, String enginePrefix, String model) {
        var path = getOntologyPath(type, enginePrefix, model);
        if (!path.toFile().exists()) {
            System.err.println("Ontology file not found: " + path);
            return null;
        }
        return loadOntology(getOntologyPath(type, enginePrefix, model).toString());
    }

    protected OWLOntology loadOntology() {
        String ontologyName = Path.of(ontology).getFileName().toString().replaceAll("\\(.*\\)", "");
        String path = String.format("results%1$sontologies%1$s%2$s_%3$s",
                FileSystems.getDefault().getSeparator(), "target", ontologyName);
        return loadOntology(path + ".owl");
    }

    protected OWLOntology loadOntology(String path) {
        if (!new File(path).exists()) {
            System.out.println("Ontology file not found: " + path + "\nSkipping...");
            return null;
        }
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path));
            if (classes != null && properties != null) {
                AxiomHandler handler = new AxiomHandler(new AxiomIterator(ontology).iterator());
                assert classes.containsAll(handler.getClasses());
                assert properties.containsAll(handler.getProperties());
            }
            return ontology;
        } catch (OWLOntologyCreationException e) {
            System.err.println("Error loading ontology: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    protected void getAllowedValues() {
        AxiomHandler handler = new AxiomHandler(new AxiomIterator(expectedOntology).iterator());
        classes = handler.getClasses();
        properties = handler.getProperties();
    }

    private void close() {
        ontology = null;
        expectedReasoner.dispose();
        expectedReasoner = null;
        expectedOntology = null;

        classes = null;
        properties = null;
    }

    protected static class ModelSetting {
        private String setting;
        private String model;

        public ModelSetting(String setting, String model) {
            this.setting = setting;
            this.model = model;
        }

        public String getSetting() {
            return setting;
        }

        public void setSetting(String setting) {
            this.setting = setting;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}

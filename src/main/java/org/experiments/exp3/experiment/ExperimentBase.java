package org.experiments.exp3.experiment;

import org.experiments.exp3.DBModelQuery;
import org.experiments.exp3.result.QueryResult;
import org.experiments.exp3.result.ResultManagerDB;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class ExperimentBase implements Experiment {
    private final List<DBModelQuery> models;
    protected final OWLOntology ontology;
    private ThreadPoolExecutor executor;
    protected final ResultManagerDB resultManager;
    private final List<QueryStuff> stuff = new ArrayList<>();

    protected ExperimentBase(List<DBModelQuery> models, OWLOntology ontology, ResultManagerDB resultManager) {
        this.models = models;
        this.ontology = ontology;
        this.resultManager = resultManager;
        createExecutor();
    }

    protected void runForAxiom(OWLSubClassOfAxiom axiom, QueryResult query) {
        DBModelQuery model = models.get(0);
        addQuery(axiom, query, model);
        stuff.add(new QueryStuff(axiom, query));
    }

    protected void addQuery(OWLSubClassOfAxiom axiom, QueryResult query, DBModelQuery model) {
        String text = model.getText(axiom, ontology);
        executor.execute(() -> {
            String result = model.getResult(text);
            resultManager.saveResult(query, model.getSetting(), text, result);
        });
    }

    protected static class QueryStuff {
        OWLSubClassOfAxiom axiom;
        QueryResult query;

        public QueryStuff(OWLSubClassOfAxiom axiom, QueryResult query) {
            this.axiom = axiom;
            this.query = query;
        }

        public OWLSubClassOfAxiom getAxiom() {
            return axiom;
        }

        public void setAxiom(OWLSubClassOfAxiom axiom) {
            this.axiom = axiom;
        }

        public QueryResult getQuery() {
            return query;
        }

        public void setQuery(QueryResult query) {
            this.query = query;
        }
    }

    protected void runAll() {
        for (DBModelQuery modelQuery : models.subList(1, models.size())) {
            createExecutor();
            for (QueryStuff stuff : stuff) {
                addQuery(stuff.getAxiom(), stuff.getQuery(), modelQuery);
            }
        }
        awaitExecutor();
    }

    private void createExecutor() {
        if (executor != null) {
            awaitExecutor();
        }
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2000);
        this.executor = new ThreadPoolExecutor(
                4, 4, 0L, TimeUnit.MILLISECONDS, queue,
                (runnable, threadPoolExecutor) -> {
                    // System.out.println("task pool full");
                    runnable.run();
                }
        );;
    }

    private void awaitExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(12, TimeUnit.HOURS)) {
                System.out.println("Timed out waiting for threads to finish");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for threads to finish");
            executor.shutdownNow();
            throw new RuntimeException(e);
        }
    }

    protected static OWLOntology getOntology(String ontologyName) {
        try {
            File fileName = new File(ontologyName);
            OWLOntology ont = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(fileName);
            System.out.printf("Ontology '%s' loaded%n", ontologyName);
            return ont;
        } catch (Exception e) {
            System.out.println("Could not load ontology: " + ontologyName + "\n" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

package org.analysis.specialized;

import org.analysis.common.Metrics;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.configurations.Configuration;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.utility.OntologyManipulator;
import org.utility.YAMLConfigLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ResultAnalyzer extends ResultBase {

    public static void main(String[] args) {
        LogManager.getRootLogger().atLevel(Level.OFF);
        Configuration config = new YAMLConfigLoader().getConfig(args[0], Configuration.class);
        new ResultAnalyzer(config).run();
    }

    private List<Iterable<OWLSubClassOfAxiom>> allPossibleAxioms;
    private List<List<Boolean>> inferredAxiomsByExpectedOntology = new ArrayList<>();
    private OWLOntology learnedOntology;

    public ResultAnalyzer(Configuration configuration) {
        super(configuration, 0,0);
    }

    @Override
    protected void compareOntologies(String model) throws Exception {
        long startingTime = System.currentTimeMillis();

        String shortName = Path.of(ontology).getFileName().toString();
        String sep = FileSystems.getDefault().getSeparator();
        String ontologyResultPath = "results" + sep + "ontologies";
        String nameThing = "%s_%s_%s".formatted(shortName, model, setting);
        String learnedOntologyPath = ontologyResultPath + sep + nameThing + ".owl";
        this.learnedOntology = loadOntology(learnedOntologyPath);

        // Inizializzazione delle matrici di confusione
        List<List<Integer>> confusionMatrix = new ArrayList<>();

        OWLReasoner predictedReasoner = initializeReasonerOrFillMatrix(learnedOntology);
        updateConfusionMatrix(predictedReasoner, confusionMatrix);

        printResults(confusionMatrix,nameThing);
        generateSummaryFilesForLatexTable(confusionMatrix, nameThing);
        System.out.println("Evaluation completed in " + (System.currentTimeMillis() - startingTime) / 1000 + " seconds.");
    }

    private OWLReasoner initializeReasonerOrFillMatrix(OWLOntology ontology) {
        //return new Reasoner(ontology);
        return new ElkReasonerFactory().createReasoner(ontology);
    }


    private void updateConfusionMatrix(OWLReasoner predictedReasoner, List<List<Integer>> confusionMatrix) {
        for (int i1 = 0; i1 < allPossibleAxioms.size(); i1++) {
            int[] confusion = new int[4];
            int i = 0;
            for (OWLSubClassOfAxiom axiom : allPossibleAxioms.get(i1)) {
                int col = inferredAxiomsByExpectedOntology.get(i1).get(i++) ? 0 : 1;
                int row = predictedReasoner.isEntailed(axiom) ? 0 : 1;
                confusion[col + row * 2]++;
            }
            confusionMatrix.add(Arrays.stream(confusion).boxed().toList());
        }
    }

    private void printResults(List<List<Integer>> confusionMatrix, String info) {
        int[][] matrix = new int[3][3];
        for (List<Integer> stuff : confusionMatrix) {
            matrix[0][0] += stuff.get(0);
            matrix[0][1] += stuff.get(1);
            matrix[1][0] += stuff.get(2);
            matrix[1][1] += stuff.get(3);
        }
        System.out.printf("Test %s", info);
        printMetrics("M.Syntax", matrix);
        System.out.println("##############################################################");
    }

    private void generateSummaryFilesForLatexTable(List<List<Integer>> confusionMatrix, String info) {
        FileWriter fw;
        var s = FileSystems.getDefault().getSeparator();
        try {
            File f = new File("analysis" + s + "exp3" + s + info + ".txt");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            f.createNewFile();
            fw = new FileWriter(f.getPath());
            String result = confusionMatrix.stream()
                    .flatMap(Collection::stream)
                    .map(Number::toString)
                    .collect(Collectors.joining(" "));
            fw.write(result);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String calculateMetrics(int[][] confusionMatrix) {
        return Metrics.calculateAccuracy(confusionMatrix) + " " + Metrics.calculateRecall(confusionMatrix) + " " + Metrics.calculatePrecision(confusionMatrix) + " " + Metrics.calculateF1Score(confusionMatrix);
    }

    private void printMetrics(String label, int[][] confusionMatrix) {
        System.out.printf("%s RECALL: %.2f%n", label, Metrics.calculateRecall(confusionMatrix));
        System.out.printf("%s PRECISION: %.2f%n", label, Metrics.calculatePrecision(confusionMatrix));
        System.out.printf("%s F1-Score: %.2f%n", label, Metrics.calculateF1Score(confusionMatrix));
        System.out.printf("%s Accuracy: %.2f%n", label, Metrics.calculateAccuracy(confusionMatrix));
    }

    @Override
    protected void getAllowedValues() {
        super.getAllowedValues();
        inferredAxiomsByExpectedOntology = new ArrayList<>();
        allPossibleAxioms = OntologyManipulator.getAllPossibleAxiomsCombinationsOWL(classes, properties).stream().toList();
        if (expectedReasoner != null) {
            for (Iterable<OWLSubClassOfAxiom> axioms : allPossibleAxioms) {
                AtomicInteger acc = new AtomicInteger();
                List<Boolean> inferred = new ArrayList<>();
                axioms.forEach(ax -> {
                    inferred.add(expectedReasoner.isEntailed(ax));
                });
                inferredAxiomsByExpectedOntology.add(inferred);
                System.out.println(acc.get());

            }
        }
    }
}

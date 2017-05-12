package levelGenerators.jaspGeneticLevelGenerator;

import java.util.ArrayList;
import java.util.Collections;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Population {

    private ArrayList<Individual> population;

    private double[] probabilityArray;

    public Population(int size) {
        population = new ArrayList<>(size);
        probabilityArray = null;
        initializePopulation(size);
    }

    private void initializePopulation(int populationSize) {
        while (population.size() < populationSize) {
            Individual individual = new Individual();
            individual.initializeRandom();
            population.add(individual);
        }
    }

    public Individual getBestSolution() {
        Collections.sort(population);
        System.out.println("last individual: " + population.get(population.size() - 1).fitness());
        System.out.println("first individual: " + population.get(0).fitness());

        return population.get(population.size() - 1);
    }

    public void nextGeneration() {
        ArrayList<Individual> nextGeneration = new ArrayList<>(population.size());

        //individuals are sorted by fitness in ascending order
        for (Individual i : population) {
            i.fitness();
        }
        System.out.println("BEFORE SORT");
        for (Individual i : population) {
            System.out.println(i.fitness());
        }
        Collections.sort(population);
        System.out.println("AFTER SORT");
        for (Individual i : population) {
            System.out.println(i.fitness());
        }

        addEliteToNextGeneration(nextGeneration);

        while (nextGeneration.size() < population.size()) {
            Individual parent1 = selectIndividual();
            Individual parent2 = selectIndividual();

            ArrayList<Individual> children = doPermutations(parent1, parent2);
            for (Individual child : children) {
                if (nextGeneration.size() < population.size()) {
                    nextGeneration.add(child);
                }
            }
        }

        probabilityArray = null;
        population = nextGeneration;
    }

    private void addEliteToNextGeneration(ArrayList<Individual> nextGeneration) {
        for (int i = population.size() - ELITE_SIZE; i < population.size(); i++) {
            nextGeneration.add(population.get(i));
        }
    }

    private ArrayList<Individual> doPermutations(Individual parent1, Individual parent2) {

        Individual child1 = parent1.clone();
        Individual child2 = parent2.clone();

        if (random.nextDouble() < CROSSOVER_PROB) {
            ArrayList<Individual> children = parent1.crossover(parent2);
            child1 = children.get(0);
            child2 = children.get(1);
        }

        if (random.nextDouble() < MUTATION_PROB) {
            child1.mutate();
        }

        if (random.nextDouble() < MUTATION_PROB) {
            child2.mutate();
        }

        ArrayList<Individual> childrenToReturn = new ArrayList<>(2);
        childrenToReturn.add(child1);
        childrenToReturn.add(child2);

        return childrenToReturn;
    }

    //select an Individual using fitness proportionate selection (roulette selection)
    private Individual selectIndividual() {
        Individual individualToReturn = null;

        double[] probabilities = getProbabilities();
        double rouletteNumber = random.nextDouble();
        System.out.println("Roulette: " + rouletteNumber);

        for (int i = 0; i < population.size(); i++) {
            if (rouletteNumber < probabilities[i]) {
                individualToReturn = population.get(i);
                break;
            }
        }

        System.out.println("size: " + population.size());
        System.out.println("last: " + probabilities[population.size() - 1]);

        return individualToReturn;
    }

    private double[] getProbabilities() {
        if (probabilityArray != null) {
            return probabilityArray;
        }

        double totalFitness = 0.0;
        for (Individual individual : population) {
            totalFitness += individual.fitness();
        }

        double[] probabilities = new double[population.size()];
        double totalProbability = 0.0;
        for (int i = 0; i < population.size(); i++) {
            double probability = population.get(i).fitness() / totalFitness;
            probabilities[i] = probability + totalProbability + EPSILON;
            totalProbability += probability;
        }

        probabilityArray = probabilities;
        return probabilityArray;
    }
}

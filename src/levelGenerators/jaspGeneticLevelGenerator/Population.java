package levelGenerators.jaspGeneticLevelGenerator;

import java.util.ArrayList;
import java.util.Collections;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Population {

    private int populationSize;

    private ArrayList<Individual> population;
    private double[] probabilityArray;

    public Population(int size) {
        populationSize = size;
        population = new ArrayList<>(populationSize);
        probabilityArray = null;
        initializePopulation();
    }

    private void initializePopulation() {
        while (population.size() <= populationSize) {
            Individual individual = new Individual();
            individual.initializeRandom();
            population.add(individual);
        }
    }

    public Individual getBestSolution() {
        Collections.sort(population);
        return population.get(populationSize - 1);
    }

    public void nextGeneration() {
        ArrayList<Individual> nextGeneration = new ArrayList<>(populationSize);

        //individuals are sorted by fitness in ascending order
        Collections.sort(population);

        //add the elite to the next generation
        for (int i = populationSize - ELITE_SIZE; i < POPULATION_SIZE; i++) {
            nextGeneration.add(population.get(i));
        }

        while (nextGeneration.size() <= populationSize) {
            Individual parent1 = selectIndividual();
            Individual parent2 = selectIndividual();

            ArrayList<Individual> children = doPermutations(parent1, parent2);
            nextGeneration.addAll(children);
        }

        probabilityArray = null;
        population = nextGeneration;
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
        //System.out.println("roulette: " + rouletteNumber);

        for (int i = 0; i < populationSize; i++) {
            if (rouletteNumber < probabilities[i]) {
                individualToReturn = population.get(i);
                break;
            }
        }

        return individualToReturn;
    }

    private double[] getProbabilities() {
        if (probabilityArray != null) {
            return probabilityArray;
        }

        double totalFitness = 0.0;
        for (Individual i : population) {
            totalFitness += i.fitness();
        }

        double[] probabilities = new double[populationSize];
        double totalProbability = 0.0;
        for (int i = 0; i < populationSize; i++) {
            double probability = population.get(i).fitness() / totalFitness;
            probabilities[i] = probability + totalProbability + EPSILON;
            totalProbability += probability;
        }

        probabilityArray = probabilities;
        return probabilityArray;
    }
}

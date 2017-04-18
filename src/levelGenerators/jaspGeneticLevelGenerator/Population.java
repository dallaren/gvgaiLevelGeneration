package levelGenerators.jaspGeneticLevelGenerator;

import java.util.ArrayList;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Population {

    private ArrayList<Individual> population;
    private double[] probabilityArray;

    public Population() {
        population = new ArrayList<>(POPULATION_SIZE);
        probabilityArray = null;
    }

    private void nextGeneration() {


        probabilityArray = null;
    }

    //pick an Individual using fitness proportionate selection (roulette selection)
    private Individual rouletteSelection() {

        Individual individualToReturn = null;
        double[] probabilities = getProbabilities();
        double rouletteNumber = random.nextDouble();

        for (int j = 0; j < POPULATION_SIZE; j++) {
            if (probabilities[j] < rouletteNumber) {
                individualToReturn = population.get(j);
                break;
            }
        }

        return individualToReturn;
    }

    private double[] getProbabilities() {
        if (probabilityArray != null) {
            return probabilityArray;
        }

        double totalFitness = 0;
        for (Individual i : population) {
            totalFitness += i.fitness();
        }

        double[] probabilities = new double[POPULATION_SIZE];
        double totalProbability = 0;
        for (int i = 0; i < POPULATION_SIZE; i++) {
            double probability = population.get(i).fitness() / totalFitness;
            probabilities[i] = probability + totalProbability + EPSILON;
            totalProbability += probability;
        }

        probabilityArray = probabilities;
        return probabilityArray;
    }

}

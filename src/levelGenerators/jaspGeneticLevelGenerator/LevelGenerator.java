package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;

import java.util.ArrayList;
import java.util.Random;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class LevelGenerator extends AbstractLevelGenerator{

    Individual test;

    public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        Shared.game = game;
        Shared.gameAnalyzer = new GameAnalyzer(game);
        Shared.random = new Random();

        Shared.width = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
        Shared.height = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
        System.out.println("W: " + width + " ; H: " + height);
        //test();
    }

    private void test() {
        test = new Individual();
        Individual test2 = new Individual();
        test2.initializeRandom();
        test.initializeRandom();
        test.mutate();
        ArrayList<Individual> children = test.crossover(test2);
        test.print();
        test2.print();
        children.get(0).print();
        children.get(1).print();
        //test.fitness();
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {

        Population population = new Population(POPULATION_SIZE);

        for (int generation = 1; generation < GENERATION_AMOUNT; generation++) {
            System.out.println("Generation #" + generation);
            population.nextGeneration();
            System.out.println("Best solution fitness: " + population.getBestSolution().fitness());
            System.out.println("elapsed minutes: " + elapsedTimer.elapsedMinutes());
        }

        return population.getBestSolution().getLevelString();
    }
}

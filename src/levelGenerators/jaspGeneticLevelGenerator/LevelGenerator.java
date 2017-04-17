package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;

import java.util.Random;

public class LevelGenerator extends AbstractLevelGenerator{

    Individual test;

    public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        Shared.game = game;
        Shared.gameAnalyzer = new GameAnalyzer(game);
        Shared.random = new Random();
        Shared.width = 10;
        Shared.height = 10;
        test();
    }

    private void test() {
        test = new Individual();
        test.initializeRandom();
        test.print();
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {

        return test.toString();
    }
}

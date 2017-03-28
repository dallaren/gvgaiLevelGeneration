package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;

import java.util.Random;

public class LevelGenerator extends AbstractLevelGenerator{

    public LevelGenerator(GameDescription game, ElapsedCpuTimer elpasedTimer) {
        Shared.game = game;
        Shared.random = new Random();
        Shared.levelWidth = 10;
        Shared.levelHeight = 10;
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        return null;
    }
}

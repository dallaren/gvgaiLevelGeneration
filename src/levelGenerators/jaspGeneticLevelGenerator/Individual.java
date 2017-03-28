package levelGenerators.jaspGeneticLevelGenerator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Individual implements Comparator<Individual> {
    private char[][] level;

    public Individual() {
        level = new char[levelHeight][levelWidth];
    }

    public void initializeRandom() {
        throw new NotImplementedException();
    }

    //randomly mutate a single tile in the level
    public void mutate() {
        int row = random.nextInt(levelHeight);
        int col = random.nextInt(levelWidth);
        char c = getRandomCharFromLevelMapping();

        level[col][row] = c;
    }

    private char getRandomCharFromLevelMapping() {
        Character[] mappingChars = (Character[]) game.getLevelMapping().keySet().toArray();
        int c = random.nextInt(mappingChars.length);
        return mappingChars[c];
    }

    public Iterable<Individual> crossover(Individual partner) {
        throw new NotImplementedException();
    }

    public int fitness() {
        throw new NotImplementedException();
    }

    public char[][] getLevel() { return level; }

    @Override
    public int compare(Individual o1, Individual o2) {
        throw new NotImplementedException();
    }
}

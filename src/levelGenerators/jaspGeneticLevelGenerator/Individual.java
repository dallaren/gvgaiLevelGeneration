package levelGenerators.jaspGeneticLevelGenerator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Comparator;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Individual implements Comparator<Individual> {
    private char[][] level;
    private int size;

    public Individual(int size) {
        this.size = size;
        level = new char[size][size];
    }

    public void initializeRandom() {
        throw new NotImplementedException();
    }

    //randomly mutate a single tile in the level
    public void mutate() {
        int row = random.nextInt(size);
        int col = random.nextInt(size);
        char c = getRandomCharFromLevelMapping();

        level[row][col] = c;
    }

    private char getRandomCharFromLevelMapping() {
        Character[] mappingChars = (Character[]) game.getLevelMapping().keySet().toArray();
        int c = random.nextInt(mappingChars.length);
        return mappingChars[c];
    }

    //do a crossover around a random row or column
    public Iterable<Individual> crossOver(Individual partner) {
        ArrayList<Individual> offspring = new ArrayList<>(2);
        Individual child1 = new Individual(size);
        Individual child2 = new Individual(size);

        int crossOverPoint = random.nextInt(size);
        boolean splitX = random.nextBoolean();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (splitX) {
                    if (row > crossOverPoint) {
                        child1.getLevel()[row][col] = this.level[row][col];
                        child2.getLevel()[row][col] = partner.getLevel()[row][col];
                    } else {
                        child2.getLevel()[row][col] = this.level[row][col];
                        child1.getLevel()[row][col] = partner.getLevel()[row][col];
                    }
                } else {
                    if (col > crossOverPoint) {
                        child1.getLevel()[row][col] = this.level[row][col];
                        child2.getLevel()[row][col] = partner.getLevel()[row][col];
                    } else {
                        child2.getLevel()[row][col] = this.level[row][col];
                        child1.getLevel()[row][col] = partner.getLevel()[row][col];
                    }
                }
            }
        }

        child1.constrainLevel();
        child2.constrainLevel();
        offspring.add(child1);
        offspring.add(child2);
        return offspring;
    }

    //make sure the level well-formed
    private void constrainLevel() {
        constrainAvatar();
    }

    //make sure the level has at most 1 avatar
    private void constrainAvatar() {
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

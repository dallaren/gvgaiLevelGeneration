package levelGenerators.jaspGeneticLevelGenerator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Individual {
    private char[][] level;

    public Individual() {

    }

    //randomly mutate a single tile in the level
    public void mutate() {

    }

    public Iterable<Individual> crossover(Individual partner) {
        throw new NotImplementedException();
    }

    public int fitness() {
        throw new NotImplementedException();
    }

}

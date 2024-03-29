package com.felonardo.main;

import com.felonardo.crossover.Crossover2;
import com.felonardo.crossover.CrossoverOperator;
import com.felonardo.crossover.CrossoverResult;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MagicSquareFinder {
    
    public static final int LOG_EVENT = 0;
    public static final int MAGIC_SQUARE_FOUND_EVENT = 1;
    public static final int SEARCH_ENDED_EVENT = 2;
    
    private final int size;
    private final int arraySize;
    private final int populationSize;
    private final int eliteSize;
    private final int eliteDeathPeriod;
    private final int minimumCrossoverPoint;
    private final int maximumCrossoverPoint;
    private final double mutationProbability;
    private final boolean allowDuplicates;
    private final boolean showGenerationDetails;
    
    private final MagicSquareFitnessCalculator fitnessCalculator;
    private final RandomMagicSquareGenerator randomGenerator;
    private final IndividualComparator comparator;
    private final CrossoverOperator crossoverOperator;
    private final ActionListener listener;
    private final Random random = new Random();
    private final Set<Individual> magicSquaresFound;
    private final List<Individual> population;
    private final StringBuilder log;

    private Thread thread;
    private int generationCount;
    private int amountOfGenerationsSinceLastNewMagicSquare;
    
    public MagicSquareFinder(int size, int populationSize, int eliteSize,
             int eliteDeathPeriod, double mutationProbability,
             boolean allowDuplicates, int minimumCrossoverPoint,
             int maximumCrossoverPoint, boolean showGenerationDetails,
             ActionListener listener) {
        this.size = size;
        this.arraySize = (int)Math.pow(size, 2);
        this.populationSize = populationSize;
        this.eliteSize = eliteSize;
        this.eliteDeathPeriod = eliteDeathPeriod;
        this.mutationProbability = mutationProbability;
        this.allowDuplicates = allowDuplicates;
        this.minimumCrossoverPoint = minimumCrossoverPoint;
        this.maximumCrossoverPoint = maximumCrossoverPoint;
        this.showGenerationDetails = showGenerationDetails;
        
        this.fitnessCalculator = new MagicSquareFitnessCalculator(size);
        this.randomGenerator = new RandomMagicSquareGenerator(size);
        this.crossoverOperator = new Crossover2();
        this.comparator = new IndividualComparator();
        this.magicSquaresFound = new HashSet<>();
        this.population = new ArrayList<>();
        this.log = new StringBuilder();
        this.listener = listener;
    }
    
    public void start() {
        stop();
        
        thread = new Thread() {
            @Override
            public void run() {
                startGeneticAlgorithm();
            }
        };
        
        thread.start();
    }
    
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public int getGenerationCount() {
        return generationCount;
    }

    private void startGeneticAlgorithm() {
        generationCount = amountOfGenerationsSinceLastNewMagicSquare = 0;
        log.setLength(0);

        generateInitialPopulation();
        boolean published;
        
        while (!thread.isInterrupted()) {
            sortPopulation();
            addCurrentGenerationToLog();
            
            if (generationCount == 0 || generationCount % 200 == 0) {
                publishAndClearLog();
                published = true;
            } else {
                published = false;
            }
            
            addAndPublishMagicSquares();
            
            if (checkForCompletion(published)) {
                break;
            }
            
            createNewGeneration();
        }
        
        stop();
    }
    
    private void sortPopulation() {
        Collections.sort(population, comparator);
    }
    
    private void generateInitialPopulation() {
        population.clear();

        for (int i = 0; i < populationSize; i++) {
            population.add(new Individual(randomGenerator.generate(),
                null, null, null, "", fitnessCalculator));
        }
    }

    /**
     * Checks if there are new magic squares on the current generation.
     * If there are new ones, add them to the magic square list and publishes
     * them to the action listener.
     */
    private void addAndPublishMagicSquares() {
        Individual[] magicSquares = population.stream()
            .filter(i -> i.getFitness() == 0)
            .toArray(Individual[]::new);
        
        for (Individual magicSquare : magicSquares) {
            boolean added = magicSquaresFound.add(magicSquare);

            if (added) {
                amountOfGenerationsSinceLastNewMagicSquare = 0;
                publishMagicSquare(magicSquare);
            }
        }
    }
    
    private boolean checkForCompletion(boolean published) {
        int amountFound = magicSquaresFound.size();
        
        if (!thread.isInterrupted() && (
            (size == 1 && amountFound == 1) ||
            (size == 3 && amountFound == 8) ||
            amountFound >= 10)
        ) {
            listener.actionPerformed(new ActionEvent(this, SEARCH_ENDED_EVENT,
                null));
           
            if (!published) {
                publishAndClearLog();
            }
            
            return true;
        }
        
        return false;
    }
    
    private void addCurrentGenerationToLog() {
        log.append("======================\nGeneration ").append(generationCount)
            .append("\n======================");

        int i = 0;
        
        for (Individual individual : population) {
            if (showGenerationDetails) {
                log.append("\nIndividual #").append(++i);
            }
            
            log.append("\n").append(individual.toString(true));
            
            if (showGenerationDetails) {
                log.append("\n").append(individual.getGenerationDetails(true))
                    .append("\n---");
            }
        }
        
        log.append("\n");
    }
    
    /**
     * Outputs the log
     */
    private void publishAndClearLog() {
        listener.actionPerformed(new ActionEvent(this, LOG_EVENT, log.toString()));
        log.setLength(0);
    }
    
    private void publishMagicSquare(Individual magicSquare) {
        StringBuilder sb = new StringBuilder();
        sb.append(SquareFormatter.format(magicSquare.getSquare()));
        sb.append("\n");
        sb.append("\nGeneration number: ").append(generationCount);
        sb.append("\n").append(magicSquare.getGenerationDetails(false));

        listener.actionPerformed(new ActionEvent(this, MAGIC_SQUARE_FOUND_EVENT,
            sb.toString()));
    }
    
    private List<Individual> createMatingPool() {
        List<Individual> matingPool = new ArrayList<>();
        
        int poolSize = populationSize / 2;
        while (matingPool.size() < poolSize) {
            Individual i1 = Utils.getRandom(population);
            Individual i2 = Utils.getRandom(population);
            
            if (i1 == i2) {
                continue;
            }
            
            matingPool.add(i1.getFitness() > i2.getFitness() ? i1 : i2);
        }
        
        return matingPool;
    }
    
    private void createNewGeneration() {
        generationCount++;

        // Applies the elite death period
        if (eliteDeathPeriod != 0 && amountOfGenerationsSinceLastNewMagicSquare > eliteDeathPeriod) {
            population.subList(0, eliteSize).clear();
            amountOfGenerationsSinceLastNewMagicSquare = 0;
        } else {
            amountOfGenerationsSinceLastNewMagicSquare++;
        }
        
        List<Individual> matingPool = createMatingPool();
        
        // Elitism. Transfers the N best individuals to the next generation.
        try {
            population.subList(eliteSize, populationSize).clear();
            population.stream().forEach(individual -> {
                individual.setBelongsToElite(true);
            });
        } catch (java.lang.IndexOutOfBoundsException e) { }
        
        while (population.size() < populationSize) {
            Individual i1 = Utils.getRandom(matingPool);
            Individual i2 = Utils.getRandom(matingPool);
            Individual[] children = crossoverAndMutate(i1, i2);
            
            if (allowDuplicates) {
                population.addAll(Arrays.asList(children));
            } else {
                for (Individual child : children) {
                    String representation = child.toString();
                    boolean duplicate = false;

                    for (Individual individual : population) {
                        if (representation.equals(individual.toString())) {
                            duplicate = true;
                            break;
                        }
                    }

                    if (!duplicate) {
                        population.add(child);
                    }
                }   
            }
        }
    }
    
    /**
     * Performs the crossover of two individuals and (possibily) mutation
     * @param parent1 1st parent
     * @param parent2 2nd parent
     * @return children
     */
    private Individual[] crossoverAndMutate(Individual parent1, Individual parent2) {
        CrossoverResult result = crossoverOperator.crossover(parent1.getSquare(),
                parent2.getSquare(), minimumCrossoverPoint, maximumCrossoverPoint);

        int[][] children = result.getChildren();
        int[][] mutationPoints = new int[children.length][];
 
        // Mutation
        for (int i = 0; i < children.length; i++) {
            int[] child = children[i];

            if (Math.random() <= mutationProbability) {
                int index1, index2;
                
                do {
                    index1 = random.nextInt(arraySize);
                    index2 = random.nextInt(arraySize);
                }
                while (index1 == index2);
                
                int aux = child[index1];
                child[index1] = child[index2];
                child[index2] = aux;

                mutationPoints[i] = new int[] { index1, index2 };
            } else {
                mutationPoints[i] = null;
            }
        }
        
        // Transforms the int arrays to Individual objects
        Individual[] individuals = new Individual[children.length];
        
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(children[i], parent1.getSquare(),
                parent2.getSquare(), mutationPoints[i], result.getDetails(),
                fitnessCalculator);
        }
        
        return individuals;
    }

}

class IndividualComparator implements Comparator<Individual> {
    @Override
    public int compare(Individual o1, Individual o2) {
        int f1 = o1.getFitness();
        int f2 = o2.getFitness();

        if (f1 > f2) {
            return 1;
        }

        if (f1 == f2) {
            return 0;
        }

        return -1;
    }
}

import java.util.Arrays;
import java.util.HashMap;

import ga.STM;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class AiController {
	
	final static double CROSSOVER_PROB = 0.8;								
	final static double MUTATION_PROB = 0.02;									
	final static int MAX_CHROMS = 3000;
	final static int MAX_GENERATIONS = 100000;
	final static int STEP = 10;
	final static int COUNT = 10;
	final static int TIER1 = 1;
	final static int TIER2 = 20;
	final static int TIER3 = 400;
	final static int MAX_PROGRAMS = 12;
	final static int INSTRUCTION_SET_SIZE = 7;
	
	private boolean isInitialized;
	private int currentGeneration;
	private int[][] args;
	private double[] expec;
	private double[] avgFit;
	private Chromosome[] minFit;
	private Chromosome[] maxFit;
	private Chromosome[][] generation;
	
	public AiController() {
		isInitialized = false;
	}
	
	/**
	 * Initializes the controller with the given expression. After initializing the network is ready to solve
	 * @param expression
	 */
	public void initializeController(String expression) {
		minFit = new Chromosome[MAX_GENERATIONS];
		avgFit = new double[MAX_GENERATIONS];
		maxFit = new Chromosome[MAX_GENERATIONS];
		generation = new Chromosome[MAX_GENERATIONS][MAX_CHROMS];
		expec = new double[COUNT];
	//Break down the expression and figure out how many variables are in it by searching for letters
		HashMap<String, Integer> letters = new HashMap<String, Integer>();
		for(int i = 0; i < expression.length(); i++) {
			if(Character.isLetter(expression.charAt(i))) letters.put(expression.charAt(i) + "", 1);
		}
		args = new int[COUNT][letters.size()];
		String[] variables = new String[letters.size()];
		int i2 = 0;
		for (String s : letters.keySet()) {
			variables[i2++] = s;
		}
	//Create the expression builder to later evaluate the expression
		Expression e = new ExpressionBuilder(expression).variables(variables).build();
	//Initialize random values to be plugged into the expression
		for(int i = 0; i < COUNT; i++) {
			for(int i1 = 0; i1 < letters.size(); i1++) {
				args[i][i1] = (int)(Math.random() * 31);
			}
		}
	//Evaluate each argument set and store the result in expec
		for(int i = 0; i < COUNT; i++) {
			int i1 = 0;
			for(String l : letters.keySet()) {
				e.setVariable(l + "", args[i][i1]);
				i1++;
			}
			expec[i] = e.evaluate();
		}
	//Initialize the first generation of chromosomes
		int[] temp;
		for(int j = 0; j < generation[0].length; j++) {
			temp = new int[MAX_PROGRAMS];
			for(int i = 0; i < MAX_PROGRAMS; i++) {
				 temp[i] = (int)(Math.random() * INSTRUCTION_SET_SIZE);
			}
			generation[0][j] = new Chromosome(temp);
		}
		isInitialized = true;
	}
	
	/**
	 * Evolves the network and returns a solution chromosome
	 * @return Solution chromosome 
	 */
	public Chromosome solveEquation() {
		Chromosome winner;
		currentGeneration = 0;
		while(currentGeneration < MAX_GENERATIONS && isInitialized){
			if((winner = testGeneration(generation[currentGeneration], args, expec)) != null) {
				calculateGenerationFitness(generation[currentGeneration], currentGeneration);
				return winner;
			}
			breedGeneration(generation[currentGeneration], currentGeneration);
			if(currentGeneration % STEP == 0) {
				System.out.println("Current Generation: " + currentGeneration + "\nMax Fitness: " + maxFit[currentGeneration] + "\nAverage Fitness: " + avgFit[currentGeneration] + "\n");
			}
			currentGeneration++;
		}
		isInitialized = false;
		return null;
	}
	
	public Chromosome[][] getLastGenerationArray() {
		return Arrays.copyOf(generation, currentGeneration + 1);
	}
	
	/**
	 * Calculates fitness of a generation
	 * @param chromosomes Generation array
	 * @param cG Current Generation Number
	 */
	private void calculateGenerationFitness(Chromosome[] chromosomes, int cG) {
		maxFit[cG] = chromosomes[0];
		minFit[cG] = chromosomes[0];
		int totalFitness = 0;
		for(Chromosome g : chromosomes) {
			totalFitness += g.fitness;
			minFit[cG] = minFit[cG].fitness > g.fitness ? g : minFit[cG];
			maxFit[cG] = maxFit[cG].fitness < g.fitness ? g : maxFit[cG];	
		}
		avgFit[cG] = totalFitness / MAX_CHROMS;
	}

	/**
	 * Breeds the current generation and stores it in the next slot in the generation array
	 * @param chromosomes The generation of chromosomes 
	 * @param cG Current Generation number
	 */
	private void breedGeneration(Chromosome[] chromosomes, int cG) {
		maxFit[cG] = chromosomes[0];
		minFit[cG] = chromosomes[0];
		int totalFitness = 0;
		Chromosome[] contenders = new Chromosome[MAX_CHROMS];
		int i = 0;
		for(Chromosome g : chromosomes) {
			totalFitness += g.fitness;
			if(g.fitness != 0) {
				contenders[i++] = g;
			}
			minFit[cG] = minFit[cG].fitness > g.fitness ? g : minFit[cG];
			maxFit[cG] = maxFit[cG].fitness < g.fitness ? g : maxFit[cG];	
		}
		avgFit[cG] = totalFitness / MAX_CHROMS;
		Chromosome parent1 = null;
		Chromosome parent2 = null;
		int child = 0;
		Chromosome[] newGeneration = new Chromosome[chromosomes.length];
		Chromosome[] offspring;
		while(child < MAX_CHROMS){
			parent1 = selectParent(contenders, maxFit[cG].fitness);
			parent2 = selectParent(contenders, maxFit[cG].fitness);
			offspring = produceOffspring(parent1, parent2);
			newGeneration[child] = offspring[0];
			newGeneration[child + 1] = offspring[1];
			parent2 = null;
			parent1 = null;
			child += 2;
		}
		generation[cG + 1] = newGeneration;
	}
	
	/**
	 * Runs through the given contenders and pseudo randomly selects parents
	 */
	private Chromosome selectParent(Chromosome[] contenders, int max) {
		for(int i = (int)(Math.random() * contenders.length); true; i++) {
			if(contenders[i] == null) {
				i = 0;
			}
			if(Math.random() < contenders[i].fitness / max) {
				return contenders[i];
			}
		}
	}

	/**
	 * Tests to see if there is a winning chromosome in the generation
	 * @param generation Current Generation
	 * @param args Arguments
	 * @param expected Expected values
	 * @return Winning value if any
	 */
	private Chromosome testGeneration(Chromosome[] generation, int[][] args, double[] expected) {
		for(int i = 0; i < generation.length; i++) {
			if((generation[i].fitness = multiInterpretSTM(generation[i], args, expected)) == 421) {
				return generation[i];
			}
		}
		return null;
	}
	
	/**
	 * Helper Method for produceOffspring()
	 * @param parent1
	 * @param parent2
	 * @return The generated child
	 */
	private Chromosome generateChild(Chromosome parent1, Chromosome parent2) {
		int[] temp = new int[MAX_PROGRAMS];
		int split =  (int)((Math.random() * MAX_PROGRAMS - 2)) + 2;
		for(int i = 0; i < split; i++) {
			temp[i] = mutateChance(parent1.program[i]);
		}
		for(int i = split; i < parent2.program.length; i++) {
			temp[i] = mutateChance(parent2.program[i]);
		}
		return new Chromosome(temp);
	}
	
	/**
	 * Given a gene check if it mutates
	 * @param i The gene in question
	 * @return The resulting gene
	 */
	private int mutateChance(int i) {
		if(Math.random() < MUTATION_PROB) {
			return (int)(Math.random() * INSTRUCTION_SET_SIZE);
		}
		return i;
	}
	
	/**
	 * Takes in 2 parents and generates a child through genetic splicing
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	private Chromosome[] produceOffspring(Chromosome parent1, Chromosome parent2) {
		Chromosome[] children = new Chromosome[2];
		if(Math.random() < CROSSOVER_PROB) {
			children[0] = generateChild(parent1, parent2);
			children[1] = generateChild(parent1, parent2);
		} else {
			return new Chromosome[] {  new Chromosome(parent1.program), new Chromosome(parent2.program)};
		}
		return children;
	}
	
	/**
	 * Calculates the fitness of a given GA
	 * @param ga GA in question
	 * @param expected Expected result
	 * @param res Actual result
	 * @return Fitness value
	 */
	private int calculateFitness(STM ga, double expected, int res) {
		if(res != 0) {
			return 0;
		}
		int fit = TIER1;
		if(ga.getStackPointer() > 1) {
			return fit;
		} 
		fit += TIER2;
		if(ga.SPEEK() == expected) {
			return fit + TIER3;
		}
		return fit + (int)(TIER3 / (Math.abs(ga.SPEEK() - expected) + 1));//(1 / ( 1 + Math.abs(expected - ga.SPEEK()))) * TIER3;
		
	}
	
	/**
	 * Interprets and tests an array of arguments and expected values for a given Chromosome
	 * @param chromosome chromosome in question
	 * @param args Arguments of the function
	 * @param expected Expected calculated value of the function
	 * @return Fitness of this chromosome
	 */
	private int multiInterpretSTM(Chromosome chromosome, int[][] args, double[] expected) {
		int fit = 0;
		STM ga = null;
		for(int i = 0; i < args.length; i++) {
			ga = new STM();
			int p = ga.interpretSTM(chromosome.program, args[i]);
			fit += calculateFitness(ga, expected[i], p);
		}
		return fit / args.length;
	}
}

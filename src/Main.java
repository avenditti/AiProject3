import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import ga.STM;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Main {
	
	final static double CROSSOVER_PROB = 0.8;								
	final static double MUTATION_PROB = 0.02;									
	final static int MAX_CHROMS = 3000;
	final static int MAX_GENERATIONS = 100000;
	final static int STEP = 10;
	final static int COUNT = 10;
	final static int TIER1 = 1;
	final static int TIER2 = 20;
	final static int TIER3 = 400;
	final static double MAX_FIT  = (double)((TIER3 * COUNT) + (TIER2 * COUNT) + (TIER1 * COUNT));
	final static int MAX_PROGRAMS = 12;
	final static int INSTRUCTION_SET_SIZE = 6;
	static Chromosome[][] generation;
	
	static Chromosome minFit[] = new Chromosome[MAX_GENERATIONS];
	static double avgFit[] = new double[MAX_GENERATIONS];
	static Chromosome maxFit[] = new Chromosome[MAX_GENERATIONS];

	
	public static void main(String[] ar) {
		Scanner scan = new Scanner(System.in);
		String t = scan.nextLine();
		scan.close();
		HashMap<String, Integer> letters = new HashMap<String, Integer>();
		for(int i = 0; i < t.length(); i++) {
			if(Character.isLetter(t.charAt(i))) {
				letters.put(t.charAt(i) + "", 1);
			}
		}
		String[] s1 = new String[letters.size()];
		int i2 = 0;
		for (String s : letters.keySet()) {
			s1[i2] = s;
			i2++;
		}
		Expression e = new ExpressionBuilder(t).variables(s1).build();
		generation = new Chromosome[MAX_GENERATIONS][MAX_CHROMS];
		double[] expec = new double[COUNT];
		int[][] args = new int[COUNT][letters.size()];
		for(int i = 0; i < COUNT; i++) {
			for(int i1 = 0; i < letters.size(); i++) {
				args[i][i1] = i;
			}
		}
		int i1;
		for(int i = 0; i < COUNT; i++) {
			i1 = 0;
			for(String l : letters.keySet()) {
				e.setVariable(l + "", args[i][i1]);
				i1++;
			}
			expec[i] = e.evaluate();
		}
		int[] temp;
		for(int j = 0; j < generation[0].length; j++) {
			temp = new int[MAX_PROGRAMS];
			for(int i = 0; i < MAX_PROGRAMS; i++) {
				 temp[i] = (int)(Math.random() * INSTRUCTION_SET_SIZE);
			}
			generation[0][j] = new Chromosome(temp);
		}
		Chromosome winner;
		int currentGeneration = 0;
		while(currentGeneration < MAX_GENERATIONS){
			if((winner = testGeneration(generation[currentGeneration], args, expec)) != null) {
				winnerDetected(winner, currentGeneration);
				break;
			}
			breedGeneration(generation[currentGeneration], currentGeneration);
			if(currentGeneration % STEP == 0) {
				System.out.println(currentGeneration);
				System.out.println(maxFit[currentGeneration] + " AVG: " + avgFit[currentGeneration]);
				STM s = new STM();
				s.interpretSTM(maxFit[currentGeneration].program, args[3], false);
				multiInterpretSTM(maxFit[currentGeneration], args, expec, true);
			}
			currentGeneration++;
		}
	}
	
	private static void winnerDetected(Chromosome winner, int currentGeneration) {
		System.out.println(currentGeneration + " " + winner.fitness);
		for(int i : winner.program) {
			System.out.print(i + " ");
		}
	}

	private static void breedGeneration(Chromosome[] chromosomes, int cG) {
		Arrays.sort(chromosomes);
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

	private static Chromosome selectParent(Chromosome[] contenders, int max) {
		for(int i = (int)(Math.random() * contenders.length); true; i++) {
			if(contenders[i] == null) {
				i = 0;
			}
			if(Math.random() < contenders[i].fitness / max) {
				return contenders[i];
			}
		}
	}

	public static Chromosome testGeneration(Chromosome[] generation, int[][] args, double[] expected) {
		for(int i = 0; i < generation.length; i++) {
			if((generation[i].fitness = multiInterpretSTM(generation[i], args, expected, false)) == 421) {
				return generation[i];
			}
		}
		return null;
	}
	
	private static Chromosome generateChild(Chromosome parent1, Chromosome parent2) {
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
	
	private static int mutateChance(int i) {
		if(Math.random() < MUTATION_PROB) {
			return (int)(Math.random() * INSTRUCTION_SET_SIZE);
		}
		return i;
	}
	
	private static Chromosome[] produceOffspring(Chromosome parent1, Chromosome parent2) {
		Chromosome[] children = new Chromosome[2];
		if(Math.random() < CROSSOVER_PROB) {
			children[0] = generateChild(parent1, parent2);
			children[1] = generateChild(parent1, parent2);
		} else {
			return new Chromosome[] {  new Chromosome(parent1.program), new Chromosome(parent2.program)};
		}
		return children;
	}
	
	private static int calculateFitness(STM ga, double expected, int res) {
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
	
	private static int multiInterpretSTM(Chromosome gene, int[][] args, double[] expected, boolean debug) {
		int fit = 0;
		STM ga = null;
		for(int i = 0; i < args.length; i++) {
			ga = new STM();
			int p = ga.interpretSTM(gene.program, args[i], false);
			if(debug) {
				System.out.println(p + " " + calculateFitness(ga, expected[i], p));
			}
			fit += calculateFitness(ga, expected[i], p);
		}
		return fit / args.length;
	}
}

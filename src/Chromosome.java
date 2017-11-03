
public class Chromosome implements Comparable<Chromosome>{

	int fitness;
	int[] program;
	
	public Chromosome(int[] i) {
		this.program = i;
	}

	@Override
	public int compareTo(Chromosome o) {
		return this.fitness - o.fitness;
	}
	
	@Override
	public String toString() {
		String temp = "";
		for(int i : program) {
			temp += i + " ";
		}
		return temp + fitness;
	}
}

import java.util.Arrays;

public class Chromosome implements Comparable<Chromosome> {

	double fitness;
	int[] program;

	public Chromosome(int[] i) {
		this.program = Arrays.copyOf(i, i.length);
	}

	@Override
	public int compareTo(Chromosome o) {
		return (int)(this.fitness - o.fitness);
	}

	@Override
	public String toString() {
		String temp = "";
		for (int i : program) {
			temp += i + " ";
		}
		return temp;
	}
}

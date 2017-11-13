import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		AiController ai = new AiController();
		Scanner scan = new Scanner(System.in);
		if (args.length < 1) {
			System.out.println("Please enter an expression");
			ai.initializeController(scan.nextLine());
		} else {
			ai.initializeController(args[0]);
		}
		System.out.println("Solution Chromosome " + ai.solveEquation(true));
		scan.close();
	}
}

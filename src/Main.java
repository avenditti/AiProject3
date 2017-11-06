
public class Main {
	public static void main(String[] args) {
		AiController ai = new AiController();
		ai.initializeController("4x");
		System.out.println(ai.solveEquation());
	}
}

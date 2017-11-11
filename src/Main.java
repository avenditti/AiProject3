import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{
	
	static AiController ai;
	
	public static void main(String[] args) {
		ai = new AiController();
		ai.initializeController("4x");
		ai.solveEquation();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
	}
}

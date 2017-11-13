import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application{
	
	static AiController ai;
	static NanoBot[] bots;
	static int onScreenBots = 100;
	static int stageHeight = 1000;
	static int stageWidth = 1500;
	static boolean running = true;
	
	public static void main(String[] args) {
		Main.launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Pane p = new Pane();
		ai = new AiController();
		bots = new NanoBot[onScreenBots];
		for(int i = 0; i < onScreenBots; i++) {
			NanoBot n = new NanoBot(50, stageHeight/2);
			bots[i] = n;
			p.getChildren().add(n.getBody());
		}
		Scene rS = new Scene(p, stageWidth, stageHeight);
		primaryStage.setScene(rS);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				running = false;
			}
			
		});
		new Thread() {
			@Override
			public void run() {
				for(NanoBot n : bots) {
					
				}
			}
		}.start();
		new Thread() {
			@Override
			public void run() {
				int i = 0;
				while(running) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for(NanoBot n : bots) {
						n.doPhysics();
						if(i % 500 == 0) {
							n.turn(Math.random() * 360);
						}
					}
					i++;
				}
			}
		}.start();
	}
}

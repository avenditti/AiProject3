
import javafx.application.Platform;
import javafx.scene.shape.Polygon;

public class NanoBot {
	
	Polygon body;
	double angle = 0;
	double speed = 0;
	double x = 0;
	double y = 0;
	double xCos = 0;
	double ySin = 0;
	
	public NanoBot(int x, int y) {
		body = new Polygon(0,0,15,6.25,0,12.5);
		this.x = x;
		this.y = y;
		angle = Math.random() * 360;
		body.setRotate(angle);
		speed =2 ;
		xCos = Math.cos(Math.toRadians(angle));
		ySin = Math.sin(Math.toRadians(angle));
	}
	
	public void relocate(double x, double y) {
		body.relocate(x, y);
	}
	
	public Polygon getBody() {
		return body;
	}
	
	public void turn(double angle2) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(angle - angle2 < 0) {
					for (double i = angle; i < angle2; i+=15) {
						body.setRotate(i);
						setAngle(i);
						xCos = Math.cos(Math.toRadians(angle));
						ySin = Math.sin(Math.toRadians(angle));
						System.out.println(angle);
					}
				} else {
					for (double i = angle; i > angle2; i-=15) {
						if(angle < 0) {
							i = 0;
							setAngle(i);
							xCos = Math.cos(Math.toRadians(angle));
							ySin = Math.sin(Math.toRadians(angle));
							body.setRotate(i);
							
							break;
						}
						setAngle(i);
						xCos = Math.cos(Math.toRadians(angle));
						ySin = Math.sin(Math.toRadians(angle));
						body.setRotate(i);
						
					}
				}
			}
		});
	}
	
	private void setAngle(double s) {
		angle = s;
	}
	
	public void doPhysics() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				x = x + speed * xCos;
		        y = y + speed * ySin;
				body.relocate(x, y);
			}
			
		});
	}

	public void setSpeed(double d) {
		this.speed = d;
	}
}

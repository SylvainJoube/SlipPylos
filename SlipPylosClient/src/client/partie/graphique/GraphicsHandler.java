package client.partie.graphique;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;

import commun.partie.nonGraphique.PylosPartie;

/** Classe s'occupant de la fenêtre de l'application
 * D'autres classes, comme GameHandler, dessinent dans et via GraphicsHandler.
 *
 */
public class GraphicsHandler extends Canvas {
	
	/** The strategy that allows us to use accelerate page flipping */
	private BufferStrategy currentBufferStrategy;
	public final int windowWidth = 1400;
	public final int windowHeight = 960;
	
	public AtomicBoolean gameRunning = new AtomicBoolean(true);
	private Graphics2D currentGraphics = null;
	private static GraphicsHandler mainInstance = null; // une seule instance possible
	
	public static RoomType currentRoomType = RoomType.PARTIE;
	
	public GraphicsHandler() throws Exception {
		if (mainInstance != null) {
			System.out.println("GraphicsHandler : constructeur : une instance de GraphicsHandler existe déjà. Elle doit être unique.");
			throw new Exception("GraphicsHandler : constructeur : une instance de GraphicsHandler existe déjà. Elle doit être unique.");
		}
		mainInstance = this;
		initWindow();
	}
	
	public static GraphicsHandler getMainInstance() {
		return mainInstance;
	}
	public static Graphics2D getMainGraphics() {
		return (Graphics2D) mainInstance.currentBufferStrategy.getDrawGraphics();
	}
	
	
	public void initWindow() {
		JFrame container = new JFrame("Jeu de pylos en java");
		JPanel panel = (JPanel) container.getContentPane(); // get hold the content of the frame and set up the resolution of the game
		panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
		panel.setLayout(null);
		setBounds(0, 0, windowWidth, windowHeight); // setup our canvas size and put it into the content of the frame
		panel.add(this);
		setIgnoreRepaint(true); // Tell AWT not to bother repainting our canvas since we're going to do that ourself in accelerated mode
		container.pack(); // finally make the window visible 
		container.setResizable(false);
		container.setVisible(true);
		// add a listener to respond to the user closing the window. If they do we'd like to exit the game
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// create the buffering strategy which will allow AWT to manage our accelerated graphics
		createBufferStrategy(2);
		currentBufferStrategy = getBufferStrategy();
		LogWriter.Log("GameHandler.InitWindow : currentPath = " + System.getProperty("user.dir"));
	}
	
	public void loop() {
		long lastLoopTime = System.currentTimeMillis();
		
		// keep looping round till the game ends
		while (gameRunning.get()) {
			// work out how long its been since the last update, this will be used to calculate how far the entities should move this loop
			long delta = System.currentTimeMillis() - lastLoopTime; // temps écoulé depuis la dernière itération
			lastLoopTime = System.currentTimeMillis();
			// Get hold of a graphics context for the accelerated surface and blank it out
			currentGraphics = getMainGraphics();//(Graphics2D) currentBufferStrategy.getDrawGraphics();
			currentGraphics.setColor(Color.darkGray);
			currentGraphics.fillRect(0, 0, windowWidth, windowHeight);
			
			Point mousePos = MouseInfo.getPointerInfo().getLocation();
			// (débug) dessin de la position de la souris
			Color col = new Color(200, 20, 200);
			currentGraphics.setColor(col);
			currentGraphics.drawString("Mouse pos : " + mousePos.x + ", " + mousePos.y, 10, 100);
			
			// Dessin du bon type d'écran
			if (currentRoomType == RoomType.PARTIE) {
				GameHandler.staticGameLoop();
			}
			
			// finally, we've completed drawing so clear up the graphics
			// and flip the buffer over
			currentGraphics.dispose();
			currentBufferStrategy.show();
			
			// finally pause for a bit. Note: this should run us at about
			// 100 fps but on windows this might vary each loop due to
			// a bad implementation of timer
			try { Thread.sleep(16); } catch (Exception e) {}
			
			
		}
		
		
	}
}

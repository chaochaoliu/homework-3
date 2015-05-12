/*
 * File: Breakout.java
 * -------------------
 * Name:Chao Liu
 * Date: 11/05/2015
 * Contact； chao200240@gmail.com
 * 
 * This file will eventually implement the game of Breakout.
 * You have three turns totally.When the ball fall down the window, 
 * you lost one turn. When the ball hit the paddle, it will bounce back.
 * When the ball hit the bricks, the brick will disappear.
 * When all the bricks disappear, you win the game.
 * 
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1)
			* BRICK_SEP)
			/ NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;


	private int turns = NTURNS;
	private GOval ball;
	private GRect paddle;
	private double vx, vy;
	private RandomGenerator rgen;
	
	private GLabel message;
	AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	
	private GLabel bricksNumber;
	private int bricksLeft = NBRICKS_PER_ROW * NBRICK_ROWS;
	

	/* Method: run() */
	/** Runs the Breakout program. */
	public void run() {
		setup();
		/* If turns is bigger than 0, continue; Or game is over. */
		while (turns > 0) {
			moveBall();
			checkForCollisions();
			checkTurns();
		}

	}
		
	/**
	 * set up the game graphics, including draw the bricks, paddle, ball, start label.
	 */
		private void setup() {
			drawBricks();
			drawPaddle();
			setBricksNumber();
			drawBall();
			setVelocity();
			startGame();
		}
		
		private void setBricksNumber() {
			bricksNumber = new GLabel("BrickLeft: "+ bricksLeft);
			bricksNumber.setLocation(260, 20);
			bricksNumber.setFont(new Font("TimesRoman", Font.PLAIN, 18));
			add(bricksNumber);
		
	}

		/** draw all the bricks.*/
		private void drawBricks() {
			for (int row = 0; row < NBRICKS_PER_ROW; row++) {
				for (int number = 0; number < NBRICKS_PER_ROW; number++) {
					GRect brick = new GRect(number * (BRICK_WIDTH + BRICK_SEP)
							+ BRICK_SEP / 2, BRICK_Y_OFFSET
							+ (BRICK_SEP + BRICK_HEIGHT) * row, BRICK_WIDTH,
							BRICK_HEIGHT);
					addBrickColor(brick, row);
					add(brick);
				}
			}

		}
		/**add color for the bricks.*/
		private void addBrickColor(GRect brick, int row) {
			if (0 <= row && row <= 1) {
				addSpecificColor(brick, Color.red);
			} else if (2 <= row && row <= 3) {
				addSpecificColor(brick, Color.orange);
			} else if (4 <= row && row <= 5) {
				addSpecificColor(brick, Color.yellow);
			} else if (6 <= row && row <= 7) {
				addSpecificColor(brick, Color.green);
			} else if (8 <= row && row <= 9) {
				addSpecificColor(brick, Color.cyan);
			}
		}
		/**set the specific color to the bricks.*/
		private void addSpecificColor(GRect rect, Color color) {
			rect.setColor(color);
			rect.setFilled(true);
			rect.setFillColor(color);

		}

		/* draw the ball.*/
			private void drawBall() {
				ball = new GOval(WIDTH / 2, HEIGHT / 2, 2 * BALL_RADIUS,
						2 * BALL_RADIUS);
				ball.setFilled(true);
				ball.setFillColor(Color.black);
				add(ball);
			}
			/* draw the paddle.*/
			private void drawPaddle() {
				paddle = new GRect(WIDTH / 2, HEIGHT - PADDLE_Y_OFFSET, PADDLE_WIDTH,
						PADDLE_HEIGHT);
				paddle.setFilled(true);
				paddle.setFillColor(Color.black);
				add(paddle);
				addMouseListeners();
			}
		/**
		 * set the velocity of the ball down forward with random direction.
		 */
		private void setVelocity() {
			rgen = RandomGenerator.getInstance();
			vx = rgen.nextDouble(1.0, 3.0);
			if (rgen.nextBoolean(0.5))
				vx = -vx;
			vy = 3.0;
		}
/**
 * add the "Click to Start" label and wait for click.
 * After click remove the label.
 */
	private void startGame() {
	 displayMessage("Click to Start");
		waitForClick();
		remove(message);
	}
	/**
	 * display the specific message.
	 */	
private void displayMessage(String news){
    message = new GLabel(news);
	message.setLocation((WIDTH - message.getWidth()) / 2, HEIGHT / 2);
	message.setFont(new Font("TimesRoman", Font.PLAIN, 18));
	add(message);
}

/**
 * make the ball begin to move. If it encounters the wall, change direction.
 */
private void moveBall() {

	ball.move(vx, vy);

	if (ball.getLocation().getX() > (WIDTH - 2 * BALL_RADIUS)
			|| ball.getLocation().getX() < 0) {
		vx = -vx;
	} else if (ball.getLocation().getY() < 0) {
		vy = -vy;
	}
	pause(10);
}

	
/*
 * check whether collision happens, when it happens, display collision sound.
 * and check if the collision object is paddle, change direction.
 * If collision object is bricks, remove it and the ball change direction until the 
 * all the bricks are removed.
 */
	private void checkForCollisions() {
		GObject collObj = getCollidingObject();
		if (collObj != null) {
			if (collObj == paddle) {
				bounceClip.play();//display the sound when collision.
				vy = -vy;

			} else {
				bounceClip.play();
				remove(collObj);
				collObj = null;
				
				vy = -vy;
				reSetBricksNumber();
				if(bricksLeft == 0){
					remove(ball);
					displayMessage("Congratulations!");
				}
			}
		}
	}
	

	private void reSetBricksNumber() {
		bricksLeft --;
//		brickRemaining = Integer.toString(bricksLeft);
		bricksNumber.setLabel("BrickLeft: " + bricksLeft);	
}


	/**
	 * When the ball fall down the bottom of the window. Check how many turns
	 * left. If it is the last turn, then game is over.
	 */
	private void checkTurns() {
		if (ball.getLocation().getY() > HEIGHT && turns > 1) {
			turns--;
			ball.setLocation(WIDTH / 2, HEIGHT / 2);
			pause(300);
		} else if (ball.getLocation().getY() > HEIGHT && turns == 1) {
			turns--;
			displayMessage("Game Over");
		}

	}

/*
 * get the object that collide with the ball.
 */
	private GObject getCollidingObject() {
		if (getElementAt(ball.getX(), ball.getY()) != null) {
			return getElementAt(ball.getX(), ball.getY());
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY()) != null) {
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY());
		} else if (getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS) != null) {
			return getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2
				* BALL_RADIUS) != null) {
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2
					* BALL_RADIUS);
		} else {
			return null;
		}
	}

	/* set the X coordination of the ball to the X coordination of the mouse.*/
	public void mouseMoved(MouseEvent e) {
		if(e.getX() < WIDTH-paddle.getWidth()){
		paddle.setLocation(e.getX(), HEIGHT - PADDLE_Y_OFFSET);
		}else{
			paddle.setLocation(WIDTH-paddle.getWidth(), HEIGHT - PADDLE_Y_OFFSET);
		}
	}

}

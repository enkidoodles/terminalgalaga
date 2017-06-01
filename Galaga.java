import java.util.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;


class Galaga {

	// main function
	public static void main(String[] args) {

		// initialize all threads
		Board newBoard = new Board();
		Laser newLaser = new Laser(newBoard);
		Ship newShip = new Ship(newBoard, newLaser);
		Swarm newSwarm = new Swarm(newBoard);
		Bomb newBomb = new Bomb(newBoard, newSwarm);
		Game newGame = new Game(newBoard);

		//newLaser.setDaemon(true);

		// get all threads to start();
		newShip.start();
		newSwarm.start();
		newLaser.start();
		newBomb.start();
		newGame.start();
		try {
			newGame.join();
		}
		catch (InterruptedException err) {}

	}

}

class Board {

	public int gameState, shipState, swarmState, shipPOS, swarmPOS;
	public char[] line0 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line1 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line2 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line3 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line4 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line5 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line6 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public char[] line7 = { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
	public List<char[]> boardList = new ArrayList<char []>();

	Board() {

		gameState = 1;
		shipState = 1;
		swarmState = 1;
		boardList.add(line0);
		boardList.add(line1);
		boardList.add(line2);
		boardList.add(line3);
		boardList.add(line4);
		boardList.add(line5);
		boardList.add(line6);
		boardList.add(line7);

	}

	// draw the screen
	public void draw() {

		// another way of clearing the screen kase Runtime.getRuntime().exec("clear"); won't work sa terminal
		final String ANSI_CLS = "\u001b[2J";
		final String ANSI_HOME = "\u001b[H";
		System.out.print(ANSI_CLS + ANSI_HOME);
		System.out.flush();

		// print top banner (replace if you do not like it)
		System.out.println("+=====================+");
		System.out.println("|===== ermergerd =====|");
		System.out.println("|======== it's =======|");
		System.out.println("|== G E R L E G E R ==|");
		System.out.println("+=====================+");

		// print board
		for (char[] element : boardList) {
			printSingleLine(element);
		}

		// print lower banner
		System.out.println("+=====================+");
		System.out.println("| A/D - move ship     |");
		System.out.println("| K   - pew pew pew   |");
		System.out.println("| Q   - bye osm game  |");
		System.out.println("+=====================+");

	}

	// for printing the char[]
	public void printSingleLine(char[] line) {
		System.out.printf("|");
		for (int i = 0; i < 21; i++) {
			System.out.printf(""+line[i]);
		}
		System.out.printf("|\n");
	}

	public synchronized int countSwarm(){
		int count = 0;
		for (char[] element : boardList) {
			for (int i = 0; i < 21; i++){
				if (element[i] == '*')
					count++;
			}
		}
		return count;
	}
}


// THIS IS THE GAME THREAD
class Game extends Thread {

	private Board board;

	// game's constructor receives reference to newShip and newSwarm in Galaga's main() function
	Game(Board board) {
		this.board = board;
	}


	@ Override
	public void run() {

		try {

			// draw the game board every 75 millis until someone wins
			while(true) {
				if (gameOver())
					break;
				board.draw();
				Thread.sleep(76);
			}

			// stty sane turns echo and icanon back on again
			String[] cmd = {"/bin/sh", "-c", "stty sane </dev/tty"};
			Runtime.getRuntime().exec(cmd);

			// draw screen again one more time
			board.draw();

			// append message below screen depending on who the winner is
			if (board.shipState == 0) {
				System.out.println("+== G A M E O V E R ==+");
				System.out.println("+=====================+\n");
			}
			else {
				System.out.println("+==== Y O U W I N ====+");
				System.out.println("+=====================+\n");
			}
			System.exit(1);

		}
		catch (InterruptedException err) {} // for Thread.sleep()
		catch (IOException err) {}	// for Runtime.getRuntime().exec()
	}

	private synchronized boolean gameOver(){
		if (board.swarmState == 0 || board.shipState == 0)
			return true;
		else
			return false;
	}

}


// THIS IS THE SHIP THREAD
class Ship extends Thread {

	private Board board;
	private Laser laser;

	// to initialize values
	Ship(Board board, Laser laser) {
		this.board = board;
		this.laser = laser;
		board.shipPOS = 10;
		board.line7[10] = '^';
	}

	@Override
	public void run() {

		try {

			// disable echo and icanon
		    String[] cmd0 = {"/bin/sh", "-c", "stty -echo </dev/tty"};
		    String[] cmd1 = {"/bin/sh", "-c", "stty -icanon time 0 min 0 </dev/tty"};
		    String[] cmd2 = {"/bin/sh", "-c", "stty sane </dev/tty"};
			Runtime.getRuntime().exec(cmd0);
			Runtime.getRuntime().exec(cmd1);

			char keyPress = ' ';

			// read input from keyboard until user presses q or Q
			while (keyPress != 'q' && keyPress != 'Q') {
				keyPress = (char) System.in.read();
				moveShip(keyPress);
			}

			// if q or Q is pressed quit the game
			Runtime.getRuntime().exec(cmd2); // turns echo and icanon back on
			System.out.println("+=== Y O U Q U I T ===+");
			System.out.println("+=====================+\n");
			System.exit(1);

		}
		catch (IOException err) {}

	}

	// updates the ship's position based on input from keyboard
	public void moveShip(char key) {

		switch(key){
			// move left
			case 'a':
			case 'A':
				if (board.shipPOS > 0){
					board.shipPOS--;
					board.line7[board.shipPOS+1] = ' ';
				}
				break;
			// move right
			case 'd':
			case 'D':
				if (board.shipPOS < 20){
					board.shipPOS++;
					board.line7[board.shipPOS-1] = ' ';
				}
				break;
			case 'k':
			case 'K':
			//case 's':
				laser.blast();
		}

		if (board.shipState != 0) {
			board.line7[board.shipPOS] = '^';
		} else {
			board.line7[board.shipPOS] = ' ';
		}

	}

}

// THIS IS THE SWARM THREAD
class Swarm extends Thread {

	private Board board;
	public int xpos;
	public int ypos;

	Swarm(Board board){

		this.board = board;

		// 2 lines lang yung swarm initially
		for (int i = 5; i < 16; i++){
			board.line0[i] = '*';
		}
		for (int i = 6; i < 15; i++){
			board.line1[i] = '*';
		}

		// tip of swarm is initially located at line1
		board.swarmPOS = 1;

		// define swarm starting position
		this.ypos = 1;
		this.xpos = 5;
	}

	@Override
	public void run() {

		// speed of swarm movement
		int n = 230;

		try {

			Thread.sleep(n);

			// swarm movement is from center -> left -> center -> right -> down
			// swarm repeats movement until bottom of screen is reached (6 times)

			for (int i = 0; i < 6; i++) {

				for (int j = 0; j < 5; j++) {
					moveSwarmLEFT();
					this.xpos--;
					Thread.sleep(n);
				}

				for (int j = 0; j < 10; j++) {
					moveSwarmRIGHT();
					this.xpos++;
					Thread.sleep(n);
				}

				for (int j = 0; j < 5; j++) {
					moveSwarmLEFT();
					this.xpos--;
					Thread.sleep(n);
				}

				moveSwarmDOWN();
				this.ypos++;
				Thread.sleep(n);

			}

		}
		catch (InterruptedException err){}
	}

	public synchronized void moveSwarmLEFT(){

		char[] frontArmy = board.boardList.get(board.swarmPOS);
		char[] backArmy = board.boardList.get(board.swarmPOS-1);

		for (int i = 0; i < 20; i++){
			if (frontArmy[i+1] == '\'') frontArmy[i+1] = ' ';
			if (backArmy[i+1] == '\'') backArmy[i+1] = ' ';
			frontArmy[i] = frontArmy[i+1];
			backArmy[i] = backArmy[i+1];
		}
		frontArmy[20] = ' ';
		backArmy[20] = ' ';
	}

	public synchronized void moveSwarmRIGHT(){

		char[] frontArmy = board.boardList.get(board.swarmPOS);
		char[] backArmy = board.boardList.get(board.swarmPOS-1);

		for (int i = 20; i > 0; i--){
			if (frontArmy[i-1] == '\'') frontArmy[i-1] = ' ';
			if (backArmy[i-1] == '\'') backArmy[i-1] = ' ';
			frontArmy[i] = frontArmy[i-1];
			backArmy[i] = backArmy[i-1];
		}
		frontArmy[0] = ' ';
		backArmy[0] = ' ';
	}

	public synchronized void moveSwarmDOWN(){

		char[] frontArmy = board.boardList.get(board.swarmPOS);
		char[] backArmy = board.boardList.get(board.swarmPOS-1);

		if(board.swarmPOS != 6){

			char[] newFront = board.boardList.get(board.swarmPOS+1);

			for (int i = 0; i < 21; i++){
				newFront[i] = frontArmy[i];
			}
			for (int i = 0; i < 21; i++){
				frontArmy[i] = backArmy[i];
			}
			for (int i = 0; i < 21; i++){
				backArmy[i] = ' ';
			}

			board.swarmPOS++;
		}

		else{

			// swarm has already reached the bottom of the screen therefore ship loses

			for(int i = 0; i < 21; i++){
				board.line7[i] = board.line6[i];
			}
			for (int i = 0; i < 21; i++){
				board.line6[i] = board.line5[i];
				board.line5[i] = ' ';
			}

			board.shipState = 0;

		}

	}

}


class Laser extends Thread {

	private Board board;
	private boolean shoot = false;

	Laser(Board board) {
		this.board = board;
	}

	@Override
	public void run() {

		while(true) {
			while(!willShoot()) {}
			shoot = false;
			pew();
		}

	}

	public synchronized void blast() {
		shoot = true;
	}

	synchronized boolean willShoot(){
		if (board.countSwarm() == 0){
			board.swarmState = 0;
		}
		return shoot;
	}

	private synchronized void pew() {
		try{
			int k = 100;
			char[] laserPOS;
			int shipPOSfin = board.shipPOS;
			for (int i = 7; i >= 0; i--){
				//Thread.sleep(k);
				if (i != 7){
					laserPOS = board.boardList.get(i);
					if (laserPOS[shipPOSfin] == '*'){
						laserPOS[shipPOSfin] = ' ';
						break;
					}
					laserPOS[shipPOSfin] = '\'';
					Thread.sleep(k);
					laserPOS[shipPOSfin] = ' ';
				}

			}
		}
		catch (InterruptedException err) {}
	}

}


class Bomb extends Thread {
	private Board board;
	private Swarm swarm;

	Bomb(Board board, Swarm swarm) {
		this.board = board;
		this.swarm = swarm;
	}

	@Override
	public void run() {

		while(true) {
			try{
				Thread.sleep(200);
				pew();
			}
			catch(InterruptedException err) {}
		}

	}

	private synchronized void pew() {
		try{
			int k = 300;
			char[] ypos;
			int xpos = ThreadLocalRandom.current().nextInt(swarm.xpos, swarm.xpos + 11);;
			for (int i = 1; i < 8; i++){
				if (swarm.ypos+i < 8) {
					ypos = board.boardList.get(swarm.ypos+i);
					if (ypos[xpos] == '^'){
						// play bomb explosion animation
						ypos[xpos] = 'o';
						Thread.sleep(50);
						if (xpos-1 >= 0) ypos[xpos-1] = 'o';
						if (xpos+1 <= 21) ypos[xpos+1] = 'o';
						Thread.sleep(50);
						ypos[xpos] = ' ';
						if (xpos-2 >= 0) ypos[xpos-2] = 'o';
						if (xpos+2 <= 21) ypos[xpos+2] = 'o';
						Thread.sleep(50);
						if (xpos-1 >= 0) ypos[xpos-1] = ' ';
						if (xpos+1 <= 21) ypos[xpos+1] = ' ';
						if (xpos-3 >= 0) ypos[xpos-3] = 'o';
						if (xpos+3 <= 21) ypos[xpos+3] = 'o';
						Thread.sleep(50);
						if (xpos-2 >= 0) ypos[xpos-2] = ' ';
						if (xpos+2 <= 21) ypos[xpos+2] = ' ';
						if (xpos-4 >= 0) ypos[xpos-4] = 'o';
						if (xpos+4 <= 21) ypos[xpos+4] = 'o';
						Thread.sleep(50);
						if (xpos-3 >= 0) ypos[xpos-3] = ' ';
						if (xpos+3 <= 21) ypos[xpos+3] = ' ';
						if (xpos-5 >= 0) ypos[xpos-5] = 'o';
						if (xpos+5 <= 21) ypos[xpos+5] = 'o';
						Thread.sleep(50);
						if (xpos-4 >= 0) ypos[xpos-4] = ' ';
						if (xpos+4 <= 21) ypos[xpos+4] = ' ';
						Thread.sleep(50);
						if (xpos-5 >= 0) ypos[xpos-5] = ' ';
						if (xpos+5 <= 21) ypos[xpos+5] = ' ';
						Thread.sleep(50);
						board.shipState = 0;
						break;
					}
					ypos[xpos] = '\"';
					Thread.sleep(k);
					ypos[xpos] = ' ';
				}
			}
		}
		catch (InterruptedException err) {}
	}
}

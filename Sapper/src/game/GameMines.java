package game;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;

public class GameMines extends JFrame {
	
	final String TITLE_OF_PROGRAMM = "Сапер";
	final String SIGN_OF_FLAG = "f"; //метка/флажок
	final int BLOCK_SIZE = 30;//px
	final int FIELD_SIZE = 12;// размер поля (количество блоков)
	final int FIELD_DX = 2;
	final int FIELD_DY = 20+20;
	final int START_LOCATION = 200; //стартовая позиция окна, координаты верхнего левого угла
	final int MOUSE_BUTTON_LEFT = 1; // константы значение которых возвращается при нажатии на кнопку мыши
	final int MOUSE_BUTTON_RIGHT = 3;
	final int NUMBER_OF_MINE = 15; // количество мин
	final int [] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0};
	
	Cell[] [] field = new Cell[FIELD_SIZE][FIELD_SIZE];
	
	Random random = new Random();
	
	int countOpenedCells; // хранит количество открытых ячеек
	boolean youWon, bangMined; 
	int bangX, bangY;
	
	
	public static void main(String[] args) {
		
		new GameMines();
	}
	
	public GameMines() {
		setTitle(TITLE_OF_PROGRAMM);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE*BLOCK_SIZE+FIELD_DX, 
				  FIELD_SIZE*BLOCK_SIZE+FIELD_DY); //метод который устанавливает стартовую позицию окна
		setResizable(false); //определяет возможность масштабировать окно
		final TimerLabel timeLabel = new TimerLabel();
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final Canvas canvas = new Canvas(); //final для более ранних версий jre
		canvas.setBackground(Color.white);
		canvas.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent e){
				super.mouseReleased(e);
				int x = e.getX()/BLOCK_SIZE; // определяем координаты по какой ячейке кликнули
				int y = e.getY()/BLOCK_SIZE;
				
				if(e.getButton() == MOUSE_BUTTON_LEFT && !bangMined && !youWon){
					if(field[x][y].isNotOpen()){
						openCells(x, y);
						youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE-NUMBER_OF_MINE;//winning
						//check
						if(bangMined){
							bangX = x;
							bangY = y;
						}
						
					}
					
				}
				if(e.getButton() == MOUSE_BUTTON_RIGHT){
					field[x][y].inverseFlag();
				}
				if(bangMined || youWon){
					
					timeLabel.stopTimer();
					
				}
				
				canvas.repaint();
				
			}
			
		});
		add(BorderLayout.CENTER, canvas);
		add(BorderLayout.SOUTH, timeLabel);
		setVisible(true);
		initField();
	}
	
	void openCells(int x, int y){
		if(x < 0 || x > FIELD_SIZE -1 || y < 0 || y > FIELD_SIZE - 1)
			return;
		if(!field[y][x].isNotOpen())
			return;
		field[y][x].open();
		if(field[y][x].getCountBomb() > 0 || bangMined) 
			return;
		for(int dx = -1; dx < 2; dx++)
			for(int dy = -1; dy < 2; dy++)
				openCells(x + dx, y + dy);
	}
	void initField(){ //инициализация игрового поля
		int x, y, countMines =0;
		//создание ячеек для поля
		for(x = 0; x < FIELD_SIZE; x++){
			for(y = 0; y < FIELD_SIZE; y++){
				field[y][x] = new Cell();
			}
		}
		
		//поля для мин
		while(countMines < NUMBER_OF_MINE){
			do{
				x = random.nextInt(FIELD_SIZE);
				y = random.nextInt(FIELD_SIZE);
			}while(field[y][x].isMined());
			field[y][x].mine();
			countMines++;
		}
		//считает бомбы рядом
		for(x = 0; x < FIELD_SIZE; x++)
			for(y = 0; y < FIELD_SIZE; y++)
				if(!field[y][x].isMined()){
					int count = 0;
					for(int dx = -1; dx<2; dx++)
						for(int dy = -1; dy<2; dy++){
							int nX = x + dx;
							int nY = y + dy;
							if(nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1){
								nX = x;
								nY = y;
							}
							count += (field[nY][nX].isMined()) ? 1 : 0;
						}
					field[y][x].setCountBomb(count);
				}
	}
	class Cell{
		
		private boolean isOpen, isMine, isFlag;
		private int countBombNear;
		
		void open(){
			
			isOpen = true;
			bangMined = isMine;
			
			if(!isMine)
				countOpenedCells++;
		}
		
		void mine(){ isMine = true; }
		
		void setCountBomb(int count){ countBombNear = count; }
		
		int getCountBomb(){ return countBombNear; }
		
		boolean isNotOpen(){ return !isOpen; }
		
		boolean isMined(){ return isMine; }
		
		void inverseFlag(){	isFlag = !isFlag; }
	
		void paintBomb(Graphics g, int x, int y, Color color){
			
			g.setColor(color);
			g.fillRect(x*BLOCK_SIZE + 7, y*BLOCK_SIZE + 10, 18, 10);
			g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
			g.fillRect(x*BLOCK_SIZE + 9, y*BLOCK_SIZE + 8, 14, 14);
			g.setColor(Color.white);
			g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE +10, 4, 4);
		}
		
		void paintString(Graphics g, String str, int x, int y, Color color){
			
			g.setColor(color);
			g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
			g.drawString(str, x*BLOCK_SIZE+8, y*BLOCK_SIZE + 26);
		}
		
		void paint(Graphics g, int x, int y){
			g.setColor(Color.lightGray);
			g.drawRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
			if(!isOpen){
				if((bangMined || youWon) && isMine)
					paintBomb(g, x, y, Color.black);
				else{
					g.setColor(Color.lightGray);
					g.fill3DRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
					if(isFlag)
						paintString(g, SIGN_OF_FLAG, x, y, Color.red);
				}
			}else{
				if(isMine)
					paintBomb(g, x, y, bangMined ? Color.red : Color.black);
				else if(countBombNear > 0)
					paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1 ]));
					
				
			}
		}
	}
	
	class TimerLabel extends JLabel{
		
		Timer timer = new Timer();
		
		TimerLabel(){
			timer.scheduleAtFixedRate(timerTask, 0, 1000);
		}
		
		TimerTask timerTask = new TimerTask() {
			
			volatile int time;
			
			Runnable refresher = new Runnable() {
				
				@Override
				public void run() {
					TimerLabel.this.setText(String.format("%02d:%02d", time/60, time % 60));
					
				}
			};
			@Override
			public void run() {
				time++;
				SwingUtilities.invokeLater(refresher);
				
			}
		};
		
		void stopTimer(){
			timer.cancel();
		}
	}
	
	class Canvas extends JPanel{//Прорисовка
		
		@Override
		public void paint(Graphics g){
			super.paint(g);
			
			for(int x = 0; x< FIELD_SIZE; x++)
				for(int y = 0; y< FIELD_SIZE; y++)
				field[y][x].paint(g, x, y);
				
			
		}
	}
}

package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import main.Update;
import res.Player;
import res.NPC;
import res.Sword;
import res.Item;

//import res.NPC AND STUFF

/**
 * Render class for Trash Smash, runs on render thread, contains draw methods
 * @author Brian Chen
 */
public class Render implements Runnable {
	//graphics resources
	private Graphics2D g;
	private Queue<BufferedImage> dblBuffer = new LinkedList<BufferedImage>();
	//vast array of Buffered Images used for graphics
	BufferedImage background;
	BufferedImage talkBubble, dialogueBox;
	BufferedImage shop;
	BufferedImage sword[] = new BufferedImage[7];
	
	//thread resources
	public volatile ReentrantReadWriteLock lck = Main.lck;
	
	/**
	 * Constructs the render object
	 * @param g
	 */
	public Render(Graphics2D g) {
		this.g = g;
	}

	/**
	 * Run method for render thread, triggers the draw list, keeps its own FPS
	 */
	@Override
	public void run() {
		init();
		long lastTime = System.nanoTime();
		double nanoPerUpdate = 1000000000D/50D;
		double delta = 0D;
		
		
			while(Update.running) {
				long now = System.nanoTime();
				delta += (now - lastTime) / nanoPerUpdate;
				lastTime = now;
				
				while(delta >= 1) {
					draw();
					delta--;
				}
			}
		
		if(Update.running = false) {
			return;
		}
	}
	//Loads all image objects into game from Assets folder
	private void init() { 
		try {
			background = ImageIO.read(getClass().getClassLoader().getResource("Backdrops/backgroundTest.png"));
			talkBubble = ImageIO.read(getClass().getClassLoader().getResource("Icons/talkBubble.png"));
			dialogueBox = ImageIO.read(getClass().getClassLoader().getResource("Icons/dialogueBox.png"));
			shop = ImageIO.read(getClass().getClassLoader().getResource("Equip/shop.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main command that calls functions to draw different aspects of the game
	 */
	private void draw() { 
		BufferedImage screen = new BufferedImage(GraphicsMain.WIDTH, GraphicsMain.HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) screen.getGraphics();
		//Things are Drawn to the Screen here.
		drawBackground(g);
		drawNPC(g);
		drawPlayer(g);
		drawBounds(g);
		drawPrompts(g);
		drawDialogue(g);
		if(Main.appState == Main.DEAD_STATE) {
			drawDeadScreen(g);
		}
		dblBuffer.add(screen);
		if(dblBuffer.size() == 2) {
			this.g.drawImage(dblBuffer.poll(), 0, 0, GraphicsMain.WIDTH, GraphicsMain.HEIGHT, null);
		}
	}
	
	//Loops through the dialogue options
	private void drawDialogue(Graphics2D g){
		if(Main.update.commenceDialogue > 0){
			g.drawImage(dialogueBox, 0, GraphicsMain.HEIGHT - 200, 1024, 200, null);
			NPC speak = Main.update.speakingWith;
			g.drawImage(speak.getHead(), 65, GraphicsMain.HEIGHT - 139, 114, 114, null);
			g.drawString(speak.getName(), 135, GraphicsMain.HEIGHT - 158);
			g.setColor(Color.black);
			if(Main.update.commenceDialogue >= 1){
				g.drawString(speak.getPhrase(Main.update.commenceDialogue - 1), 200, GraphicsMain.HEIGHT - 100);
				if(Main.update.nextDialogue){
					Main.update.commenceDialogue--;
					Main.update.nextDialogue = false;
				}
			}
			if(Main.update.commenceDialogue == 1 && (speak.getID() == "shop" || speak.getID() == "blacksmith")){
				drawShop(g);
			}
		}
	}
	
	private void drawShop(Graphics2D g){
		g.drawImage(shop,  0, 28, 1024, 550, null);
		Main.update.shopping = true;
		LinkedList<Sword> swordShop = Main.update.shopSwords;
		LinkedList<Item> itemShop = Main.update.shopItems;
		if(Main.update.speakingWith.getID() == "blacksmith"){
			for(int i = 0; i < swordShop.size(); i++){
				g.drawImage(swordShop.get(i).getImage(), swordShop.get(i).getX(), swordShop.get(i).getY(), swordShop.get(i).getHeight(), swordShop.get(i).getWidth(), null);
				if(Main.update.drawInfo){
					g.setColor(Color.white);
					//Price
					g.drawString(swordShop.get(Main.update.drawInfoIndx).getID() + "     Price: " + swordShop.get(Main.update.drawInfoIndx).getValue(),swordShop.get(Main.update.drawInfoIndx).getX() + 200, swordShop.get(Main.update.drawInfoIndx).getY() + 20);
					//Info
					g.drawString(swordShop.get(Main.update.drawInfoIndx).getInfo(), swordShop.get(Main.update.drawInfoIndx).getX() + 200, swordShop.get(Main.update.drawInfoIndx).getY() + 45);
					//Flavor Text
					g.setFont(new Font("Georgia", Font.PLAIN, 14));
					g.drawString(swordShop.get(Main.update.drawInfoIndx).getDescription(), swordShop.get(Main.update.drawInfoIndx).getX() + 200, swordShop.get(Main.update.drawInfoIndx).getY() + 65);
					g.setFont(new Font("Georgia", Font.PLAIN, 18));
				}
			}
		}
		else if(Main.update.speakingWith.getID() == "shop"){
			for(int i = 0; i < itemShop.size(); i++){
				g.drawImage(itemShop.get(i).getImage(), itemShop.get(i).getX(), itemShop.get(i).getY(), itemShop.get(i).getHeight(), itemShop.get(i).getWidth(), null);
				if(Main.update.drawInfo){
					g.setColor(Color.white);
					//Price
					g.drawString(itemShop.get(Main.update.drawInfoIndx).getID() + "     Price: " + itemShop.get(Main.update.drawInfoIndx).getValue(),itemShop.get(Main.update.drawInfoIndx).getX() + 200, itemShop.get(Main.update.drawInfoIndx).getY() + 25);
					//Info
					g.drawString(itemShop.get(Main.update.drawInfoIndx).getInfo(), itemShop.get(Main.update.drawInfoIndx).getX() + 200, itemShop.get(Main.update.drawInfoIndx).getY() + 45);
					//Flavor Text
					g.setFont(new Font("Georgia", Font.PLAIN, 14));
					g.drawString(itemShop.get(Main.update.drawInfoIndx).getDescription(), itemShop.get(Main.update.drawInfoIndx).getX() + 200, itemShop.get(Main.update.drawInfoIndx).getY() + 65);
					g.setFont(new Font("Georgia", Font.PLAIN, 18));
				}
			}
		}
		if(Main.update.insufficientGold > 0){
			g.setColor(Color.red);
			g.drawString("Insufficient funds for item.", 200, GraphicsMain.HEIGHT - 80);
			Main.update.insufficientGold--;
		}
		g.setColor(Color.yellow);
		g.drawString("Gold: " + Main.update.PC.getGold(), GraphicsMain.WIDTH - 140, 60);
	}
	
	private void drawBounds(Graphics2D g){
		Rectangle2D PlayerCharacter = Main.update.PC.getBoundbox();
		g.setColor(Color.blue);
		g.draw(PlayerCharacter);
	}
	
	private void drawPrompts(Graphics2D g){
		g.setFont(new Font("Georgia", Font.PLAIN, 18));
		g.setColor(Color.white);
		if(Main.update.dialogueOptions > 0){
			g.drawString("Press R to Interact", 440, 120);
			Main.update.dialogueOptions--;
		}
	}
	
	private void drawNPC(Graphics2D g) {
		LinkedList<NPC> NPCs = Main.update.NPCs;
		for(int i = 0; i < NPCs.size(); i++) {
			NPC e = NPCs.get(i);
			e.draw(g);
			if(e.hasDialogue()){
				g.drawImage(talkBubble, (e.getX()), (e.getY() - 4), 18, 14, null);
			}
		}
	}

	private void drawBackground(Graphics2D g){
		g.drawImage(background, 0, 0, GraphicsMain.WIDTH, GraphicsMain.HEIGHT, null);
	}
	
	private void drawPlayer(Graphics2D g){
		Main.update.PC.draw(g);
	}

	private void drawDeadScreen(Graphics2D g) {
		//g.drawImage(deadScreen, 0, 0, GraphicsMain.WIDTH, GraphicsMain.HEIGHT, null);
	}
}

package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import main.Update;
import res.Armor;
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
	BufferedImage talkBubble, dialogueBox, interactBox;
	BufferedImage shop, inventory, bPortal, rPortal, hook, hook2;
	BufferedImage qAbility, wAbility, meditateAura;
	BufferedImage sword[] = new BufferedImage[7];
	BufferedImage Alexton, TurandalForest;
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
			background = ImageIO.read(getClass().getClassLoader().getResource("Backdrops/resolution1.png"));
			talkBubble = ImageIO.read(getClass().getClassLoader().getResource("Icons/talkBubble.png"));
			dialogueBox = ImageIO.read(getClass().getClassLoader().getResource("Icons/dialogueBox.png"));
			shop = ImageIO.read(getClass().getClassLoader().getResource("Equip/shop.png"));
			interactBox = ImageIO.read(getClass().getClassLoader().getResource("Icons/interactBubble.png"));
			inventory = ImageIO.read(getClass().getClassLoader().getResource("Icons/inventory.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			hook = ImageIO.read(getClass().getClassLoader().getResource("Icons/hook.png"));
			hook2 = ImageIO.read(getClass().getClassLoader().getResource("Icons/hook2.png"));
			qAbility = ImageIO.read(getClass().getClassLoader().getResource("Icons/Qability.png"));
			wAbility = ImageIO.read(getClass().getClassLoader().getResource("Icons/Wability.png"));
			meditateAura = ImageIO.read(getClass().getClassLoader().getResource("Icons/meditateAura.png"));
			bPortal = ImageIO.read(getClass().getClassLoader().getResource("Icons/brokenPortal.png"));
			rPortal = ImageIO.read(getClass().getClassLoader().getResource("Icons/repairedPortal.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Alexton = ImageIO.read(getClass().getClassLoader().getResource("Backdrops/Alexton.png"));
			TurandalForest = ImageIO.read(getClass().getClassLoader().getResource("Backdrops/Turandal Forest.png"));
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
		if(Main.update.splashScreenTime == 0){
			drawBackground(g);
			drawNPC(g);
			drawAbilities(g);
			drawPortal(g);
			drawPlayer(g);
			//drawBounds(g);
			drawPrompts(g);
			drawDialogue(g);
			drawInventory(g);
			drawCooldowns(g);
		}
		else{
			drawSplashscreen(g);
		}
		if(Main.appState == Main.DEAD_STATE) {
			drawDeadScreen(g);
		}
		dblBuffer.add(screen);
		if(dblBuffer.size() == 2) {
			this.g.drawImage(dblBuffer.poll(), 0, 0, GraphicsMain.WIDTH, GraphicsMain.HEIGHT, null);
		}
	}
	
	private void drawSplashscreen(Graphics2D g) {
		if(Main.update.mapID == "Alexton"){
			g.setFont(new Font("Rockwell", Font.BOLD, 48));
			g.setColor(Color.WHITE);
			g.drawImage(Alexton, 0,0,GraphicsMain.WIDTH, GraphicsMain.HEIGHT, null);
			g.drawString("ALEXTON", 375, 720);
		}
		Main.update.splashScreenTime--;
	}

	private void drawCooldowns(Graphics2D g) {
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		g.setColor(Color.white);
		g.drawImage(qAbility, 3, 25, 32, 48, null);
		g.drawString(""+Main.update.qCD, 8, 70);
		g.drawImage(wAbility, 35, 25, 32, 48, null);
		g.drawString(""+Main.update.wCD, 38, 70);
	}

	private void drawAbilities(Graphics2D g) {
		Line2D grapple = Main.update.grapple;
		if(grapple != null){
			g.draw(grapple);
			if(grapple.getY2() <= Main.update.PC.getY())
				g.drawImage(hook, (int)grapple.getX2(), (int)grapple.getY2()-20, 30, 30, null);
			else
				g.drawImage(hook2, (int)grapple.getX2()-20, (int)grapple.getY2()-10, 30, 30, null);
		}
		if(Main.update.healingTime > 0){
			g.drawImage(meditateAura, Main.update.PC.getX(), Main.update.PC.getY()+20, Main.update.PC.getWidth()+10, Main.update.PC.getHeight(), null);
			Main.update.healingTime--;
		}
	}

	private void drawPortal(Graphics2D g) {
		if(Main.update.portalOnline){
			g.drawImage(rPortal, 800, 300, 100, 100, null);
		}
		else
			g.drawImage(bPortal, 800, 300, 100, 100, null);		
	}

	private void drawInventory(Graphics2D g) {
		if(Main.update.invScreen){
			g.drawImage(inventory, 0, 30, 1024, 700, null);
			g.setFont(new Font("Rockwell", Font.BOLD, 22));
			//Drawing Gold count
			g.setColor(Color.yellow);
			g.drawString("" + Main.update.PC.getGold(), 250, 670);
			//Drawing Attack count
			g.setColor(Color.black);
			g.drawString("" + Main.update.PC.getWeapon().getDamage(), 90, 206);
			//Drawing Armor count
			g.drawString("" + Main.update.PC.getArmor().getArmor(), 90, 253);
			//Draws the HP of the player character (PC) and Location
			g.setColor(Color.red);
			g.drawString("HP               " + Main.update.PC.getHealth() + "/100", 70, 85);
			g.setColor(Color.black);
			g.drawString("Location     " + Main.update.mapID, 70, 110);
			int timeElapsed = (int)(Main.update.currentTime / 1000);
			String time = "";
			if(timeElapsed < 10){
				time = "00:0" + timeElapsed;
			}
			else if(timeElapsed < 60){
				time = "00:" + timeElapsed;
			}
			else if(timeElapsed >= 60){
				int mins = (int)(timeElapsed / 60);
				int secs = timeElapsed - (mins)*60;
				if(mins < 10){
					time = "0";
				}
				if(secs < 10){
					time += mins + ":0" + secs;
				}
				else
					time += mins + ":" + secs;
			}
			g.drawString("Playtime     " + time, 70, 135);
			//Draws the equipped weapon
			g.drawImage(Main.update.PC.getWeapon().getImage(), 252, 257, 91, 91, null);
			//Draws the equipped armor
			g.drawImage(Main.update.PC.getArmor().getImage(), 252, 421, 91, 91, null);
			//Draws all the items in inventory
			g.drawString("Items -", 400, 80);
			for(int i = 0; i < Main.update.PC.invItems.size(); i++){
				g.drawImage(Main.update.PC.invItems.get(i).getImage(), 400+(65 * i), 90, 50, 50, null);
			}
			//Draws all the swords in inventory
			g.drawString("Weapons -", 400, 210);
			for(int i = 0; i < Main.update.PC.invSwords.size(); i++){
				g.drawImage(Main.update.PC.invSwords.get(i).getImage(), 400+(65 * i), 220, 50, 50, null);
			}
			//Draws all the armor in inventory
			g.drawString("Armor -", 400, 340);
			for(int i = 0; i < Main.update.PC.invArmor.size(); i++){
				g.drawImage(Main.update.PC.invArmor.get(i).getImage(), 400+(65 * i), 350, 50, 50, null);
			}
			//Draws all the Quest Items in inventory
			g.drawString("Quest -", 400, 470);
			for(int i = 0; i < Main.update.PC.qItems.size(); i++){
				g.drawImage(Main.update.PC.qItems.get(i).getImage(), 400+(65 * i), 480, 50, 50, null);
			}
			if(Main.update.drawInvIndx >= 0 && Main.update.drawWhich > 0){
				if(Main.update.drawWhich == 1){
					g.setFont(new Font("Rockwell", Font.BOLD, 20));
					if(!Main.update.PC.invItems.isEmpty()){
						System.out.println(Main.update.PC.invItems.size() + " " + Main.update.drawInvIndx);
						g.drawString(Main.update.PC.invItems.get(Main.update.drawInvIndx).getID(), 400, 590);
						g.setFont(new Font("Rockwell", Font.PLAIN, 13));
						g.drawString(Main.update.PC.invItems.get(Main.update.drawInvIndx).getInfo(), 400, 630);
						g.setFont(new Font("Rockwell", Font.PLAIN, 12));
						g.drawString(Main.update.PC.invItems.get(Main.update.drawInvIndx).getDescription(), 430, 670);
					}
				}
				else if (Main.update.drawWhich == 2){
					g.setFont(new Font("Rockwell", Font.BOLD, 20));
					if(!Main.update.PC.invSwords.isEmpty()){
						g.drawString(Main.update.PC.invSwords.get(Main.update.drawInvIndx).getID(), 400, 590);
						g.setFont(new Font("Rockwell", Font.PLAIN, 13));
						g.drawString(Main.update.PC.invSwords.get(Main.update.drawInvIndx).getInfo(), 400, 630);
						g.setFont(new Font("Rockwell", Font.PLAIN, 12));
						g.drawString(Main.update.PC.invSwords.get(Main.update.drawInvIndx).getDescription(), 430, 670);
					}
				}
				else if(Main.update.drawWhich == 3){
					g.setFont(new Font("Rockwell", Font.BOLD, 20));
					if(!Main.update.PC.invArmor.isEmpty()){
						g.drawString(Main.update.PC.invArmor.get(Main.update.drawInvIndx).getID(), 400, 590);
						g.setFont(new Font("Rockwell", Font.PLAIN, 13));
						g.drawString(Main.update.PC.invArmor.get(Main.update.drawInvIndx).getInfo(), 400, 630);
						g.setFont(new Font("Rockwell", Font.PLAIN, 12));
						g.drawString(Main.update.PC.invArmor.get(Main.update.drawInvIndx).getDescription(), 430, 670);
					}
				}
				else if(Main.update.drawWhich == 4){
					if(!Main.update.PC.qItems.isEmpty()){
						g.setFont(new Font("Rockwell", Font.BOLD, 20));
						g.drawString(Main.update.PC.qItems.get(Main.update.drawInvIndx).getID(), 400, 590);
						g.setFont(new Font("Rockwell", Font.PLAIN, 13));
						g.drawString(Main.update.PC.qItems.get(Main.update.drawInvIndx).getInfo(), 400, 630);
						g.setFont(new Font("Rockwell", Font.PLAIN, 12));
						g.drawString(Main.update.PC.qItems.get(Main.update.drawInvIndx).getDescription(), 430, 670);
					}
				}
			}
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
			if(Main.update.commenceDialogue == 1 && (speak.getID() == "shop" || speak.getID() == "blacksmith" || speak.getID() == "armorsmith")){
				drawShop(g);
			}
			if(Main.update.commenceDialogue == 1 && (speak.getID() == "stranger")){
				Item soulGem = new Item(0, 0, "Soul Gem");
				if(!Main.update.gem){
					Main.update.PC.addQuestItem(soulGem);
					Main.update.gem = true;
				}
			}
		}
	}
	
	private void drawShop(Graphics2D g){
		g.drawImage(shop,  0, 28, 1024, 550, null);
		Main.update.shopping = true;
		LinkedList<Sword> swordShop = Main.update.shopSwords;
		LinkedList<Item> itemShop = Main.update.shopItems;
		LinkedList<Armor> armorShop = Main.update.shopArmor;
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
		else if(Main.update.speakingWith.getID() == "armorsmith"){
			for(int i = 0; i < armorShop.size(); i++){
				g.drawImage(armorShop.get(i).getImage(), armorShop.get(i).getX(), armorShop.get(i).getY(), armorShop.get(i).getHeight(), armorShop.get(i).getWidth(), null);
				if(Main.update.drawInfo){
					g.setColor(Color.white);
					//Price
					g.drawString(armorShop.get(Main.update.drawInfoIndx).getID() + "     Price: " + armorShop.get(Main.update.drawInfoIndx).getValue(),armorShop.get(Main.update.drawInfoIndx).getX() + 200, armorShop.get(Main.update.drawInfoIndx).getY() + 25);
					//Info
					g.drawString(armorShop.get(Main.update.drawInfoIndx).getInfo(), armorShop.get(Main.update.drawInfoIndx).getX() + 200, armorShop.get(Main.update.drawInfoIndx).getY() + 45);
					//Flavor Text
					g.setFont(new Font("Georgia", Font.PLAIN, 14));
					g.drawString(armorShop.get(Main.update.drawInfoIndx).getDescription(), armorShop.get(Main.update.drawInfoIndx).getX() + 200, armorShop.get(Main.update.drawInfoIndx).getY() + 65);
					g.setFont(new Font("Georgia", Font.PLAIN, 18));
				}
			}
		}
		if(Main.update.insufficientGold > 0){
			g.setColor(Color.red);
			g.drawString("Insufficient funds for item.",  420, 60);
			Main.update.insufficientGold--;
			if(Main.update.purchased > Main.update.insufficientGold){
				Main.update.purchased = 0;
			}
		}
		if(Main.update.alreadyHave > 0){
			g.setColor(Color.red);
			g.drawString("You already have this item.",  420, 60);
			Main.update.alreadyHave--;
			if(Main.update.purchased > Main.update.alreadyHave){
				Main.update.purchased = 0;
			}
		}
		else if(Main.update.purchased > 0 && Main.update.insufficientGold == 0){
			g.setColor(Color.green);
			g.drawString("Item Purchased!", 420, 60);
			Main.update.purchased--;
		}
		g.setColor(Color.yellow);
		g.drawString("Gold: " + Main.update.PC.getGold(), GraphicsMain.WIDTH - 140, 60);
		//Item description
	}
	
	private void drawBounds(Graphics2D g){
		Rectangle2D PlayerCharacter = Main.update.PC.getBoundbox();
		g.setColor(Color.blue);
		g.draw(PlayerCharacter);
		g.setColor(Color.white);
		Rectangle2D nb = Main.update.nb;
		g.draw(nb);
		for(int i = 0; i < Main.update.NPCs.size(); i++){
			g.draw(Main.update.NPCs.get(i).getSmall());
		}
	}
	
	private void drawPrompts(Graphics2D g){
		if(Main.update.dialogueOptions > 0){
			g.setFont(new Font("Georgia", Font.PLAIN, 18));
			g.setColor(Color.black);
			g.drawImage(interactBox, 413, 90, 200, 75, null);
			g.drawString("Press R to Interact", 440, 130);
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

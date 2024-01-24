package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SettingsScreen extends GameOptionsScreen {
	private Map<String, Object> CONFIG;
	private final Map<String, String[]> COMMENTS;
	
	private final List<List<ClickableWidget>> pages;
	private int lastY;
	private int distFromLeft;
	private int distFromRight;
	private int distFromBottom;
	private int currentPage;
	private long prevRenderTime;
	
	
	private final List<Ball> balls;
	
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public SettingsScreen(Screen previous) {
		super(previous, CLIENT.options, Text.of("Fabricated Exchange"));
		CONFIG = ModDataFiles.MAIN_CONFIG_FILE.getCopy();
		COMMENTS = ModDataFiles.MAIN_CONFIG_FILE.getComments();
		pages = new ArrayList<List<ClickableWidget>>();
		currentPage = 0;
		prevRenderTime = System.nanoTime();
		
		balls = new ArrayList<Ball>();
		
	}

	@Override
	protected void init() {
		super.init();
		this.lastY = 35;
		this.distFromLeft = 30;
		this.distFromRight = width - 30;
		this.distFromBottom = 30;
		this.balls.clear();
		
		for (int i = 0; i < 8; i++) { 
			balls.add(new Ball(
					new Vector2d(this.width / 2 + 50 + Math.random() * 50d - 25d, this.height / 2 + Math.random() * 50d - 25d), 
					new Vector2d(Math.random() * 2d - 1d, Math.random() * 2d - 1d)));
		}
		this.pages.clear();
		pages.add(new ArrayList<ClickableWidget>());
		if (this.client.world == null || MinecraftClient.getInstance().isIntegratedServerRunning()) {
			Text resetToDefaultText = Text.of("Reset To Default");
			ButtonWidget resetToDefault = new ButtonWidget.Builder(resetToDefaultText, (widget) -> {
				CONFIG = ModDataFiles.MAIN_CONFIG_FILE.copyDefaultValue();
				clearAndInit();
			}).dimensions(distFromLeft, height - distFromBottom, textRenderer.getWidth(resetToDefaultText) + 20, 20).build();
			
			addDrawableChild(resetToDefault);
			
			Text closeWithoutSavingText = Text.of("Close Without Saving");
			ButtonWidget closeWithoutSaving = new ButtonWidget.Builder(closeWithoutSavingText, (widget) -> {
				close();
			}).dimensions(distFromLeft + resetToDefault.getWidth() + 11, height - distFromBottom, textRenderer.getWidth(closeWithoutSavingText) + 20, 20).build();
			
			addDrawableChild(closeWithoutSaving);
			
			Text doneText = Text.of("Done");
			ButtonWidget done = new ButtonWidget.Builder(doneText, (widget) -> {
				ModDataFiles.MAIN_CONFIG_FILE.setValueAndSave(CONFIG);
				close();
			}).dimensions(distFromLeft + resetToDefault.getWidth() + closeWithoutSaving.getWidth() + 22, height - distFromBottom, closeWithoutSaving.getWidth(), 20).build();
			
			addDrawableChild(done);
			
			

		}
		
		addBooleanValue("showItemEmcOrigin");
		addBooleanValue("showEnchantedBookRepairCost");
		addSuperNumberValue("enchantmentEmcConstant");
		addSuperNumberValue("emcInMultiplier");
		addSuperNumberValue("emcOutMultiplier");
		addBooleanValue("mapper.enabled");
		addBooleanValue("transmutationTable.animated");
		addBooleanValue("transmutationTable.floorButton");
		
		if (pages.get(pages.size() - 1).isEmpty())
			pages.remove(pages.size() - 1);
		if (pages.size() > 1) {
			final TextWidget pageTextWidget = new TextWidget(
					distFromRight - 40, this.height / 2 - distFromBottom, 
					13, 13, Text.of(Integer.toString(currentPage)), textRenderer);
			addDrawable(pageTextWidget);
			
			addDrawableChild(ButtonWidget.builder(
				Text.of("<"), button -> switchPage(currentPage - 1, pageTextWidget))
				.dimensions(distFromRight - 60, this.height / 2 - distFromBottom, 13, 13)
				.build());
	
			addDrawableChild(ButtonWidget.builder(
				Text.of(">"), button -> switchPage(currentPage + 1, pageTextWidget))
				.dimensions(distFromRight - 20, this.height / 2 - distFromBottom, 13, 13)
				.build());
			
			if (currentPage != 0) {
				int remember = currentPage;
				currentPage = 0;
				switchPage(remember, pageTextWidget);
			}
		}

	}

	private void switchPage(int page, TextWidget pageTextWidget) {
		if ((page <= -1) || (page >= pages.size()))
			return;
		for (ClickableWidget widget : pages.get(currentPage)) {
			widget.active = false;
			widget.visible = false;
		}
		currentPage = page;
		for (ClickableWidget widget : pages.get(currentPage)) {
			widget.active = true;
			widget.visible = true;
		}
		pageTextWidget.setMessage(Text.of(Integer.toString(currentPage)));
	}
	
	@Override
	public void removed() {
		
	}
	
	private void addSuperNumberValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		

		
		
		Text nameText = Text.of(key + ":");
		int width = textRenderer.getWidth(nameText);
		int height = 12;
		TextWidget nameWidget = new TextWidget(distFromLeft, lastY + height / 2, width, height, nameText, textRenderer);
		nameWidget.setTooltip(Tooltip.of(commentsText));
		nameWidget.active = (pages.size() == 1);
		nameWidget.visible = (pages.size() == 1);
		addDrawableChild(nameWidget);
		pages.get(pages.size() - 1).add(nameWidget);
	

		
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, distFromLeft + width + 10, lastY + height / 2 - 1, 50, height, Text.empty());

		Text invalidText = Text.of("Invalid number!!");
		int invalidWidth = textRenderer.getWidth(invalidText);
		TextWidget invalidTextWidget = new TextWidget(
				distFromLeft + nameWidget.getWidth() + field.getWidth() + 20, 
				lastY + height / 2, invalidWidth, height, 
				Text.empty(), textRenderer);
		
		addDrawable(invalidTextWidget);
		
		pages.get(pages.size() - 1).add(invalidTextWidget);
		
		field.setChangedListener((str) -> {
			boolean isNumberValid = SuperNumber.isValidNumberString(str);
			invalidTextWidget.setMessage(isNumberValid ? Text.empty() : invalidText);
			if (isNumberValid)
				CONFIG.put(key, str);
		});
		field.setText((String)CONFIG.get(key));
		field.active = (pages.size() == 1);
		field.visible = (pages.size() == 1);
		addDrawableChild(field);
		pages.get(pages.size() - 1).add(field);
		
		increaseY();
	}
	
	
	private void addBooleanValue(String key) {
		String[] comments = COMMENTS.get(key);
		MutableText commentsText = Text.empty();
		for (String comment : comments) {
			commentsText.append("// ");
			commentsText.append(Text.literal(comment));
			commentsText.append("\n");
		}
		
		Text f = Text.of(key + ": OFF");
		Text t = Text.of(key + ": ON");
		int width = textRenderer.getWidth(f) + 15;
		//TextWidget value = new TextWidget(distFromLeft + width + 20, lastY + 10, 0, 0, (boolean)CONFIG.get(key) ? t : f, textRenderer);
		//addDrawable(value);
		ButtonWidget button = new ButtonWidget.Builder((boolean)CONFIG.get(key) ? t : f, (widget) -> {
			boolean newValue = !(boolean)CONFIG.get(key);
			CONFIG.put(key, newValue);
			widget.setMessage(newValue ? t : f);
		}).dimensions(distFromLeft, lastY, width, 20).tooltip(Tooltip.of(commentsText)).build();
		button.active = (pages.size() == 1);
		button.visible = (pages.size() == 1);
		
		addDrawableChild(button);
		pages.get(pages.size() - 1).add(button);
		
		increaseY();
	}
	
	private void increaseY() {
		lastY += 25;
		if (height - 60 < lastY) {
			lastY = 35;
			pages.add(new ArrayList<ClickableWidget>());
		}
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		
		super.render(matrices, mouseX, mouseY, delta);
		itemRenderer.renderGuiItemIcon(matrices, new ItemStack(ModItems.DARK_MATTER_HOE), mouseX - 8, mouseY - 8);
		GameOptionsScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
		drawBalls(matrices, mouseX, mouseY);

	}
	
	private void drawBalls(MatrixStack matrices, int mouseX, int mouseY) {
		long currentTime = System.nanoTime();
		
		double dotX = this.width / 2 + 50;
		double dotY = this.height / 2; //+ Math.sin(currentTime / 1000000000.0) * 10;
		
		double dt = (currentTime - prevRenderTime) / 10000000.0;
		for (Ball ball : balls) {
			itemRenderer.renderGuiItemIcon(matrices, new ItemStack(ball.item), (int)ball.pos.x - 8, (int)ball.pos.y - 8);

			
			
			ball.addVelocity(new Vector2d(
					dotX - ball.pos.x, 
					dotY - ball.pos.y)
					.div(2000d)
					.mul(Math.random() / 2 + 1).mul(dt));
			
			double dx = (ball.pos.x - mouseX);
			double dy = (ball.pos.y - mouseY);
			double distToMouse = Math.sqrt(dx * dx + dy * dy);
			double maxDist = 40d;
			if (distToMouse < maxDist) {
				ball.addVelocity(new Vector2d(
						mouseX - ball.pos.x, 
						mouseY - ball.pos.y)
						.div(1000d)
						.mul((maxDist - distToMouse) / maxDist * 30d)
						.mul(dt)
						.negate());
			}
			ball.tick(dt);
		}
		prevRenderTime = currentTime;
	}
	
	private static class Ball {
		private Vector2d pos;
		private Vector2d vel;
		private Item item;
		public Ball(Vector2d pos, Vector2d vel) {
			this.pos = pos;
			this.vel = vel;
			this.item = choose(ModItems.PHILOSOPHERS_STONE, ModItems.DARK_MATTER, ModItems.RED_MATTER);
		}
		
		private Item choose(Item... items) {
			return items[new Random().nextInt(items.length)];
		}
		
		public void addVelocity(Vector2d vel) {
			this.vel.add(vel);
		}
		
		public void tick(double dt) {
			System.out.println(dt);
			if (vel.length() > 2)
				vel.fma(-0.1 * dt, vel.normalize(new Vector2d()));
			pos.add(new Vector2d(vel).mul(dt));
			
		}
	}
}

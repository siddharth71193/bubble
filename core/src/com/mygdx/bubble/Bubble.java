package com.mygdx.bubble;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Bubble extends ApplicationAdapter {
	private Texture dropImage1;
	private Texture dropImage2;
	private Texture dropImage3;
	private Texture handImage;
	private Sound dropSound;
	private Music rainMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle hand;
	private Array<Rectangle> bubbles;
	private Array<Integer> value;
	private long lastBubbleTime;
	private long firstBubbleTime;
	private long score = 0;

	@Override
	public void create() {
		// load the images for the bubbles and the hand, 64x64 pixels each
		dropImage1 = new Texture(Gdx.files.internal("red.png"));
		dropImage2 = new Texture(Gdx.files.internal("orange.png"));
		dropImage3 = new Texture(Gdx.files.internal("grey.png"));
		handImage = new Texture(Gdx.files.internal("hand.png"));

		//Intialize the array to score the points
		value = new Array<Integer>();

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the hand
		hand = new Rectangle();
		hand.x = 800 / 2 - 64 / 2; // center the hand horizontally
		hand.y = 20; // bottom left corner of the hand is 20 pixels above the bottom screen edge
		hand.width = 64;
		hand.height = 64;

		// create the bubbles array and spawn the first bubble
		bubbles = new Array<Rectangle>();
		spawnBubble();

		//Get first drop time
		firstBubbleTime = TimeUtils.nanoTime();
	}

	//Create a bubble
	private void spawnBubble() {
		Rectangle bubble = new Rectangle();
		bubble.x = MathUtils.random(0, 800-64);
		bubble.y = 480;
		bubble.width = 64;
		bubble.height = 64;
		bubbles.add(bubble);
		int randValue = MathUtils.random(0, 2);;
		value.add(randValue);
		lastBubbleTime = TimeUtils.nanoTime();
	}

	@Override
	public void render() {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the hand and
		// all bubbles
		long maxTime = 300000000;
		if((TimeUtils.nanoTime() - firstBubbleTime) > (long)(maxTime*100))
		{
			batch.begin();
			BitmapFont font = new BitmapFont();
			font.getData().setScale(2f);
			font.draw(batch, "Score:"+score, 320,240);
			batch.end();
		}
		else {
			batch.begin();
			batch.draw(handImage, hand.x, hand.y);
			//Create the bubbles color on the basis of the value that we get randomly
			int c = 0;
			for (Rectangle bubble : bubbles) {
				if (value.get(c) == 0) {
					batch.draw(dropImage1, bubble.x, bubble.y);
				} else if (value.get(c) == 1) {
					batch.draw(dropImage2, bubble.x, bubble.y);
				} else {
					batch.draw(dropImage3, bubble.x, bubble.y);
				}
				c++;
			}
			batch.end();

			// process user input
			if (Gdx.input.isTouched()) {
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);
				hand.x = touchPos.x - 64 / 2;
				hand.y = touchPos.y - 64 / 2;
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)) hand.x -= 200 * Gdx.graphics.getDeltaTime();
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) hand.x += 200 * Gdx.graphics.getDeltaTime();

			// make sure the hand stays within the screen bounds
			if (hand.x < 0) hand.x = 0;
			if (hand.x > 800 - 64) hand.x = 800 - 64;

			// check if we need to create a new bubble
			if (TimeUtils.nanoTime() - lastBubbleTime > 1000000000) spawnBubble();

			// move the bubbles, remove any that are beneath the bottom edge of
			// the screen or that hit the hand. In the latter case we play back
			// a sound effect as well.
			Iterator<Rectangle> iter = bubbles.iterator();
			Iterator<Integer> iterValue = value.iterator();
			while (iter.hasNext()) {
				Rectangle bubble = iter.next();
				Integer val = iterValue.next();
				bubble.y -= 200 * Gdx.graphics.getDeltaTime();
				if (bubble.y + 64 < 0) {
					iter.remove();
					iterValue.remove();
				}
				if (bubble.overlaps(hand)) {
					dropSound.play();
					iter.remove();
					iterValue.remove();
					if(val == 0) {
						score += 5;
					}
					else if(val == 1) {
						score += 10;
					}
					else {
						score += 15;
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		dropImage1.dispose();
		dropImage2.dispose();
		dropImage3.dispose();
		handImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
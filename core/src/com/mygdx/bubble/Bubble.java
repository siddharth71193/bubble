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
	private Array<Rectangle> raindrops;
	private Array<Integer> value;
	private long lastDropTime;
	private long firstDropTime;
	private long score = 0;

	@Override
	public void create() {
		// load the images for the bubbles and the hand, 64x64 pixels each
		dropImage1 = new Texture(Gdx.files.internal("red.png"));
		dropImage2 = new Texture(Gdx.files.internal("orange.png"));
		dropImage3 = new Texture(Gdx.files.internal("grey.png"));
		handImage = new Texture(Gdx.files.internal("hand.png"));

		//Intialize the array for score points
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

		// create a Rectangle to logically represent the bucket
		hand = new Rectangle();
		hand.x = 800 / 2 - 64 / 2; // center the bucket horizontally
		hand.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
		hand.width = 64;
		hand.height = 64;

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();

		//Get first drop time
		firstDropTime = TimeUtils.nanoTime();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		int randValue = MathUtils.random(0, 2);;
		value.add(randValue);
		lastDropTime = TimeUtils.nanoTime();
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

		// begin a new batch and draw the bucket and
		// all drops
		long maxTime = 300000000;
		if((TimeUtils.nanoTime() - firstDropTime) > (long)(maxTime*100))
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

			int c = 0;
			for (Rectangle raindrop : raindrops) {
				if (value.get(c) == 0) {
					batch.draw(dropImage1, raindrop.x, raindrop.y);
				} else if (value.get(c) == 1) {
					batch.draw(dropImage2, raindrop.x, raindrop.y);
				} else {
					batch.draw(dropImage3, raindrop.x, raindrop.y);
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

			// make sure the bucket stays within the screen bounds
			if (hand.x < 0) hand.x = 0;
			if (hand.x > 800 - 64) hand.x = 800 - 64;

			// check if we need to create a new raindrop
			if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

			// move the raindrops, remove any that are beneath the bottom edge of
			// the screen or that hit the bucket. In the latter case we play back
			// a sound effect as well.
			Iterator<Rectangle> iter = raindrops.iterator();
			Iterator<Integer> iterValue = value.iterator();
			while (iter.hasNext()) {
				Rectangle raindrop = iter.next();
				Integer val = iterValue.next();
				raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
				if (raindrop.y + 64 < 0) {
					iter.remove();
					iterValue.remove();
				}
				if (raindrop.overlaps(hand)) {
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
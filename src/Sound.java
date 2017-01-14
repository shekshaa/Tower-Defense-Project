import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class Sound {
	private MediaPlayer mediaPlayer;

	public Sound(String name) {
		URL resource;
		Media media;

		resource = getClass().getResource("resources/Sounds/" + name + ".mp3");
		media = new Media(resource.toString());
		mediaPlayer = new MediaPlayer(media);
	}

	public void play() {
		if (Main.isSoundOn.getValue()) //if the game is not mute
			mediaPlayer.play();
	}

	public void pause() {
		mediaPlayer.pause();
	}

	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
}

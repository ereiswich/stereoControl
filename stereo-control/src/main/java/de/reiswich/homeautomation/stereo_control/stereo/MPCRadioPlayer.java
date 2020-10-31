package de.reiswich.homeautomation.stereo_control.stereo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.bff.javampd.MPD;
import org.bff.javampd.Playlist;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDResponseException;
import org.bff.javampd.objects.MPDSong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPCRadioPlayer {

	private Logger logger = LoggerFactory.getLogger(MPCRadioPlayer.class.getName());

	private String ip;
	private int port;

	public MPCRadioPlayer(String ip, int port) {
		logger.info("Initializing MPCPlayer with ip: " + ip + " and port: " + port);
		this.ip = ip;
		this.port = port;

	}

	public void playSong() {
		try {
			MPD mpd = new MPD.Builder().server(ip).port(port).build();
			
			String abcLoungeStream = "centauri.shoutca.st";
			Playlist playlist = mpd.getPlaylist();
			List<MPDSong> songList = playlist.getSongList();
			for (MPDSong mpdSong : songList) {
				if(mpdSong.getFile().contains(abcLoungeStream)) {
					logger.debug("Abc Lounge im MPD-Player gefunden. Spiele nun ab");
					mpd.getPlayer().playId(mpdSong);
					break;
				}
			}

			mpd.close();

		} catch (Exception e) {
			logger.error("MPD-Error: " + e.getMessage());
		}
	}

	public void stopPlaying() {
		try {
			MPD mpd = new MPD.Builder().server(ip).port(port).build();

			mpd.getPlayer().stop();

			mpd.close();

		} catch (MPDConnectionException e) {
			logger.error("MPD-Error: " + e.getMessage());
		} catch (UnknownHostException e) {
			logger.error("MPD-Error: " + e.getMessage());
		} catch (MPDResponseException e) {
			logger.error("MPD-Error: " + e.getMessage());
		}
	}
}
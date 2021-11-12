package tones;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conductor class is run to play a song. The conductor runs all the notes for each song one 
 * at a time. The conductor also stores the full song and assembles the threads for note playing.
 * Some methods and formatting is from Matthew Bushnell and Nate Williams.
 */


public class Conductor {

    private List<BellNote> song; //holds notes and lengths
    private final AudioFormat af; //audio source for playing sound
    private boolean error; //flag if error occurred, will not play songs if true.

    /**
     * Conductor reads in song, then assembles group of Bellboys to hold one note and play it
     * at their given time for their given length. Instantiates audio source so we can hear
     * the notes being played.
     */
    Conductor() {
        af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        
    }

    /**
     * Takes in string of filename for song. Then, create a list that houses all song pieces.
     * Once song is loaded, return list of pieces.
     * 
     * @param filename String, filename of song to play
     */
    private static List<BellNote> loadSong(String filename) {
		final List<BellNote> notes = new ArrayList<>();
		final File file = new File(filename);
		if (file.exists()) {
			try (FileReader fileReader = new FileReader(file);
					BufferedReader br = new BufferedReader(fileReader)) {
				String line = null;
				while ((line = br.readLine()) != null) {
					final BellNote b = parseNote(line);
					if (b != null) {
						notes.add(b);
					} else {
						System.err.println("Error: Invalid note: '" + line + "'");
					}
				}
			} catch (IOException ignored) {}
		} else {
			System.err.println("File '" + filename + "' not found");
		}
		return notes;
	}

	private static BellNote parseNote(String line) {
		final String[] fields = line.split("\\s+");
		if (fields.length == 2) {
			return new BellNote(parseTone(fields[0]), parseLength(fields[1]));
		}
		return null;
	}

	private static Note parseTone(String tone) {
		if (tone == null) {
			return Note.REST;
		}

		switch (tone.toUpperCase().trim()) {
		case "REST":
			return Note.REST;
		case "A4":
			return Note.A4;
		case "A4S":
			return Note.A4S;
		case "B4":
			return Note.B4;
		case "C4":
			return Note.C4;
		case "C4S":
			return Note.C4S;
		case "D4":
			return Note.D4;
		case "D4S":
			return Note.D4S;
		case "E4":
			return Note.E4;
		case "F4":
			return Note.F4;
		case "F4S":
			return Note.F4S;
		case "G4":
			return Note.G4;
		case "G4S":
			return Note.G4S;
		case "A5":
			return Note.A5;
		case "A5S":
			return Note.A5S;
		case "B5":
			return Note.B5;
		case "C5":
			return Note.C5;
		case "C5S":
			return Note.C5S;
		case "D5":
			return Note.D5;
		case "D5S":
			return Note.D5S;
		case "E5":
			return Note.E5;
		case "F5":
			return Note.F5;
		case "F5S":
			return Note.F5S;
		case "G5":
			return Note.G5;
		case "G5S":
			return Note.G5S;
		case "A6":
			return Note.A6;

		default:
			return Note.REST;
		}
	}

	private static NoteLength parseLength(String len) {
		if (len == null) {
			return NoteLength.WHOLE;
		}

		switch (Integer.parseInt(len)) {
		case 8:
			return NoteLength.EIGTH;
		case 4:
			return NoteLength.QUARTER;
		case 2:
			return NoteLength.HALF;
		case 1:
			return NoteLength.WHOLE;

		default:
			return NoteLength.WHOLE;
		}
    }

    /**
     * Plays song. Throws error if song is bad, threads have issue, or audio line 
     * encounters issue. Creates a map of notes and associated Bellboys to play said note on first
     * read of song list. Then, reads list again and calls Bellboys to play associated notes
     * for associated times in order. Then, all Bellboys return home and leave the group.
     */
    void playSong() {
        if (!error) {
            Map<Note, Bellboy> bells = new HashMap<>();
            try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
                line.open();
                line.start();
                for (BellNote bn : song) {
                    Note note = bn.getNote();
                    if (!bells.containsKey(note)) {
                        bells.put(note, new Bellboy(line, bn.getNote()));
                    }
                }
                for (Bellboy b : bells.values()) { b.start(); }
                for (BellNote bn : song) {
                    Bellboy b = bells.get(bn.getNote());
                    b.playNow(bn.length);
                    Thread.sleep(bn.length.timeMs());
                }
                line.drain();
                for (Bellboy b : bells.values()) {
                    b.line.drain();
                    b.stop();
                }
            } catch (InterruptedException e) {
                System.err.println("Thread Error");
            } catch (LineUnavailableException e) {
                System.err.println("No Line for Audio");
            }
        } else {
            System.err.println("You should fire whoever wrote this song! Try another one.");
        }
    }

    /*
     * Creates conductor, loads song and creates Bellboys to play each note, then plays song.
     */
    
    public static void main(String[] args) throws Exception {
    	Conductor conduct = new Conductor();
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				conduct.song = loadSong(args[i]);
				conduct.playSong();
				break;
			}
		}
	}
}

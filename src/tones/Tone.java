package tones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/*
 * Slightly modified Tone.java, written originally by Nate Williams, modified by Robert Hereth
 */


public class Tone {

	/*
	 * Single-threaded song player. Reads file given in parameter, converts it to notes, and plays
	 * song in order. 
	 */
	
	public static void main(String[] args) throws Exception {
		final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
		Tone t = new Tone(af);
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				List<BellNote> mySong = loadSong(args[i]);
				t.playSong(mySong);
				break;
			}
		}
	}

	/*
	 * Creates List of BellNotes from given file.
	 * 
	 * @param filename String, filename of song
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
						System.err.println("Error: Invalid move '" + line + "'");
					}
				}
			} catch (IOException ignored) {}
		} else {
			System.err.println("File '" + filename + "' not found");
		}
		return notes;
	}

	/*
	 * Reads line by line, returns BellNote from given line 
	 */
	
	private static BellNote parseNote(String line) {
		final String[] fields = line.split("\\s+");
		if (fields.length == 2) {
			return new BellNote(parseTone(fields[0]), parseLength(fields[1]));
		}
		return null;
	}
	
	/*
	 * Takes in String and interprets it as the tone of a given note
	 * 
	 * @param tone String, tone of given note
	 */

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

	/*
	 * Takes in string of note length, outputs NoteLength associated with string
	 * 
	 * @param len String, note length
	 */
	
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

	private final AudioFormat af;

	Tone(AudioFormat af) {
		this.af = af;
	}

	/*
	 * Plays list of notes through a SourceDataLine.
	 * 
	 * @param song List<BellNote>, list of notes that make up song
	 */
	
	void playSong(List<BellNote> song) throws LineUnavailableException {
		try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
			line.open();
			line.start();

			for (BellNote bn : song) {
				playNote(line, bn);
			}
			line.drain();
		}
	}
	
	/*
	 * Grab individual note and play it by transposing length to ms
	 * 
	 * @param line SourceDataLine, where to play note to
	 * @param bn BellNote, note to be played
	 */

	private void playNote(SourceDataLine line, BellNote bn) {
		final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
		final int length = Note.SAMPLE_RATE * ms / 1000;
		line.write(bn.note.sample(), 0, length);
		line.write(Note.REST.sample(), 0, 50);
	}
}

class BellNote {
	final Note note;
	final NoteLength length;

	BellNote(Note note, NoteLength length) {
		this.note = note;
		this.length = length;
	}

	public Note getNote() {
		return this.note;
	}
}

enum NoteLength {
	WHOLE(1.0f), HALF(0.5f), QUARTER(0.25f), EIGTH(0.125f);

	private final int timeMs;

	private NoteLength(float length) {
		timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
	}

	public int timeMs() {
		return timeMs;
	}
}

enum Note {
	// REST Must be the first 'Note'
	REST, A4, A4S, B4, C4, C4S, D4, D4S, E4, F4, F4S, G4, G4S, A5, A5S, B5, C5, C5S, D5, D5S, E5, F5, F5S, G5, G5S, A6;

	public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
	public static final int MEASURE_LENGTH_SEC = 1;

	// Circumference of a circle divided by # of samples
	private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

	private final double FREQUENCY_A_HZ = 440.0d;
	private final double MAX_VOLUME = 127.0d;

	private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

	private Note() {
		int n = this.ordinal();
		if (n > 0) {
			// Calculate the frequency!
			final double halfStepUpFromA = n - 1;
			final double exp = halfStepUpFromA / 12.0d;
			final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

			// Create sinusoidal data sample for the desired frequency
			final double sinStep = freq * step_alpha;
			for (int i = 0; i < sinSample.length; i++) {
				sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
			}
		}
	}

	public byte[] sample() {
		return sinSample;
	}
}
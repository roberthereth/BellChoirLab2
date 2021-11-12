package tones;

import javax.sound.sampled.SourceDataLine;

/**
 * Bellboy is the individual thread/bell that plays an associated note.
 * General formatting and method function from Matthew Bushnell and Nate Williams.
 */

public class Bellboy implements Runnable {

    public final SourceDataLine line; // line for audio
    public volatile boolean waiting; // flag for note playing
    public volatile boolean playing; // flag for note playing
    public final Note myBell; // note to play
    public final Thread thread;
    private volatile NoteLength currentL; // length to play

    /**
     * Constructor for Bellboy. Takes note and SourceDataLine, constructs a thread where
     * the note passed is played. Each Bellboy can only play one note.
     *
     * @param dl SourceDataLine
     * @param n Note
     */
     Bellboy(SourceDataLine dl, Note n) {
        line = dl;
        waiting = true;
        playing = false;
        thread = new Thread(this);
        myBell = n;
    }

    /**
     * Run function for the member thread. Will wait until signaled by playNow or stopped.
     */
    public synchronized void run() {
        while(playing) {
            while(waiting) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
            if(!playing) {break;}
            System.out.println(myBell);
            playNote();
            waiting = true;
        }
    }

    /**
     * A running member will wait until signaled by playNow to play their note. The note will
     * be played for the length passed and the go back to waiting.
     *
     * @param l NoteLength, tells the member how long to play the note
     */
    public synchronized void playNow(NoteLength len) {
        waiting = false;
        currentL = len;
        notify();
    }

    /**
     * Start the thread and put the member into a running state waiting for the playNow function
     * to be called.
     */
    public void start() {
        playing = true;
        waiting = true;
        thread.start();
    }

    /**
     * Will set playing and waiting flags to false and let the thread die
     */
    public synchronized void stop() {
        playing = false;
        waiting = false;
        notify();
    }

    /**
     * Uses the line that the Member has been passed to play the note that the Member owns.
     */
    private void playNote(){
            final int ms = Math.min(currentL.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
            final int length = Note.SAMPLE_RATE * ms / 1000;
            line.write(myBell.sample(), 0, length);
            line.write(Note.REST.sample(), 0, 50);
    }
}
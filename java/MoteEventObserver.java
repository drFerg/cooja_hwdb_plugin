import se.sics.cooja.Mote;
import se.sics.cooja.interfaces.Radio;
public interface MoteEventObserver {
	public void radioEvent(Radio radio);
}
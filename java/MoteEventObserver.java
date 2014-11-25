import org.contikios.cooja.Mote;
import org.contikios.cooja.interfaces.Radio;
import se.sics.mspsim.core.MSP430;

public interface MoteEventObserver {
	public void radioEventHandler(Radio radio, Mote mote);
    public void cpuEventHandler(MSP430 cpu, Mote mote);
}
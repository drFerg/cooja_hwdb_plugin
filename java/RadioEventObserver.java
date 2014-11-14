import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.jdom.Element;

import se.sics.cooja.ClassDescription;
import se.sics.cooja.GUI;
import se.sics.cooja.Mote;
import se.sics.cooja.interfaces.Radio;
import se.sics.cooja.MoteInterface;

public class RadioEventObserver extends InterfaceEventObserver {

	public RadioEventObserver(MoteObserver parent, Mote mote,
        Observable interfaceToObserve){
		super(parent, mote, interfaceToObserve);
		logger.info("Created radio observer");
	}

	@Override
	public void update(Observable obs, Object obj) {
    final Radio radio = (Radio) obs;
    int moteID = mote.getID();

    logger.info("'" + GUI.getDescriptionOf(radio.getClass())
        + "'" + " of mote '" + (moteID > 0 ? Integer.toString(moteID) : "?")
        + "'");
    logger.info(moteID + ", " + "Radio, " + radio.getLastEvent() + 
    						", " + mote.getSimulation().getSimulationTime());
    parent.radioEvent(radio);
  }
}
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
    parent.radioEventHandler((Radio) interfaceToObserve);
	}

  @Override
	public void update(Observable radio, Object obj) {
    parent.radioEventHandler((Radio) radio);
  }
}
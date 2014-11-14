import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.jdom.Element;

import se.sics.cooja.ClassDescription;
import se.sics.cooja.GUI;
import se.sics.cooja.Mote;
import se.sics.cooja.MoteInterface;
import se.sics.cooja.MoteInterfaceHandler;
import se.sics.cooja.interfaces.Radio;

@ClassDescription("Mote Observer")
public class MoteObserver {
    protected Mote mote = null;
    protected MoteEventObserver parent = null;
    protected ArrayList<InterfaceEventObserver> observers;
    private static Logger logger = Logger.getLogger(MoteObserver.class);

    public MoteObserver(MoteEventObserver parent, Mote moteToObserve) {
      this.parent = parent;
      this.mote = moteToObserve;
      observers = new ArrayList<InterfaceEventObserver>();
      observeAll();
    }

    public void observeAll(){
      logger.info("Adding interfaces for mote: " + mote.getID());
      for (MoteInterface mi : mote.getInterfaces().getInterfaces()) {
        if (mi != null) {
          if (mi instanceof Radio)
            observers.add(new RadioEventObserver(this, mote, mi));
          else
            observers.add(new InterfaceEventObserver(this, mote, mi));
        }
          
      }
    }

    public void deleteAllObservers(){
      logger.info("Removing interfaces for mote: " + mote.getID());
      for (InterfaceEventObserver intObserver : observers) {
        intObserver.getInterfaceObservable().deleteObserver(intObserver);
      }
    }
    public void radioEventHandler(Radio radio){
      parent.radioEventHandler(radio);
    }
  }

  
  

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.jdom.Element;

import se.sics.cooja.ClassDescription;
import se.sics.cooja.GUI;
import se.sics.cooja.Mote;
import se.sics.cooja.MoteInterface;

@ClassDescription("Interface Event Observer")
public class InterfaceEventObserver implements Observer {
    protected Mote mote = null;
    private Observable interfaceObservable;
    private MoteObserver parent;
    protected static Logger logger = Logger.getLogger(InterfaceEventObserver.class);

    public InterfaceEventObserver(MoteObserver parent, Mote mote,
        Observable interfaceToObserve) {
      interfaceObservable = interfaceToObserve;
      this.parent = parent;
      this.mote = mote;
      interfaceObservable.addObserver(this);
    }

    public Observable getInterfaceObservable(){
      return interfaceObservable;
    }

    public void update(Observable obs, Object obj) {
      final MoteInterface moteInterface = (MoteInterface) obs;
      int moteID = mote.getID();

      logger.info("'" + GUI.getDescriptionOf(moteInterface.getClass())
          + "'" + " of mote '" + (moteID > 0 ? Integer.toString(moteID) : "?")
          + "'");
    }
  }
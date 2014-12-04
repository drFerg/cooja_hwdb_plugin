import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteInterface;
import org.contikios.cooja.MoteInterfaceHandler;
import org.contikios.cooja.interfaces.Radio;
import org.contikios.cooja.mspmote.MspMote;
import se.sics.mspsim.core.MSP430;

/* MoteObserver 
 * 
 * Contains all the interface observers for each interface within a mote 
 */
@ClassDescription("Mote Observer")
public class MoteObserver {
    protected Mote mote = null;
    protected MoteEventObserver parent = null;
    protected CPUEventObserver cpu;
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
      cpu = new CPUEventObserver(this, mote);
      // for (MoteInterface mi : mote.getInterfaces().getInterfaces()) {
      //   if (mi != null) {
      //     if (mi instanceof Radio)
      //       observers.add(new RadioEventObserver(this, mote, mi));
      //     // else
      //     //   observers.add(new InterfaceEventObserver(this, mote, mi));
      //   } 
      //}
    }

    public void deleteAllObservers(){
      logger.info("Removing interfaces for mote: " + mote.getID());
      // for (InterfaceEventObserver intObserver : observers) {
      //   intObserver.getInterfaceObservable().deleteObserver(intObserver);
      // }
      cpu.removeListener();
    }
    public void radioEventHandler(Radio radio, Mote mote){
      parent.radioEventHandler(radio, mote);
    }

    public void cpuEventHandler(MSP430 cpu, Mote mote){
      parent.cpuEventHandler(cpu, mote);
    }
  }

  
  

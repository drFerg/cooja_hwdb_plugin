import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Cooja;
import se.sics.mspsim.core.MSP430;
import se.sics.mspsim.core.MSP430Constants;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteInterface;
import org.contikios.cooja.MoteInterfaceHandler;
import org.contikios.cooja.PluginType;
import org.contikios.cooja.RadioConnection;
import org.contikios.cooja.RadioMedium;
import org.contikios.cooja.RadioPacket;
import org.contikios.cooja.SimEventCentral.MoteCountListener;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.TimeEvent;
import org.contikios.cooja.VisPlugin;
import org.contikios.cooja.interfaces.Radio;

/**
 * 
 * This project must be loaded in COOJA before the plugin can be used:
 * Menu>Settings>Manage project directories>Browse>..>OK
 * 
 * @author Fergus Leahy
 */

/* 
 * Cooja HWDB plugin
 * 
 * Pipes interface events (radio, CPU) from motes in Cooja into HWDB for analysis using automata.
 * 
 */
@ClassDescription("Cooja HWDB") /* Description shown in menu */
@PluginType(PluginType.SIM_PLUGIN)
public class CoojaHWDB extends VisPlugin implements MoteEventObserver{
  private static final long serialVersionUID = 4368807123350830772L;
  private static Logger logger = Logger.getLogger(CoojaHWDB.class);


  private Simulation sim;
  private HWDBClient hwdb;
  private RadioMedium radioMedium;
  private Observer radioMediumObserver;
  private MoteCountListener moteCountListener;
  private ArrayList<MoteObserver> moteObservers;
  private boolean initialised = false;
  private Cooja gui;


  /**
   * @param simulation Simulation object
   * @param gui GUI object 
   */
  public CoojaHWDB(Simulation simulation, Cooja gui) {
    super("Cooja HWDB", gui, false);
    sim = simulation;
    radioMedium = sim.getRadioMedium();
    this.gui = gui;
    hwdb = new HWDBClient("localhost", 1234, "Cooja");
    
    /* Initialise Observers button */
    JButton button = new JButton("Observe");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!initialised) initObservers();
      }
    });
    this.getContentPane().add(BorderLayout.NORTH, button);
    setSize(300,100);
  }

  public void initObservers() {
    /* Check for class loaders, if not the same class casting won't work, reload to fix */
    if ((sim.getMotes()[0].getClass().getClassLoader() != this.getClass().getClassLoader()) &&
        (sim.getMotes()[0].getClass().getClassLoader() != gui.getClass().getClassLoader())) {
          logger.info("Different class loaders");
          return;
    }
    initialised = true;
    /* Create observers for each mote */
    moteObservers = new ArrayList<MoteObserver>();
    for(Mote mote : sim.getMotes()) {
      addMote(mote);
    }

    /* Listens for any new nodes added during runtime */
    sim.getEventCentral().addMoteCountListener(moteCountListener = new MoteCountListener() {
      public void moteWasAdded(Mote mote) {
        /* Add mote's radio to observe list */
        addMote(mote);
        logger.info("Added a mote");
      }
      public void moteWasRemoved(Mote mote) {
        /* Remove motes radio from observe list */
        logger.info("Removed a mote");
      }
    });
  }

  /* Adds a new mote to the observed set of motes 
   * Needed for use in listener, to access /this/ context */
  public void addMote(Mote mote){
    moteObservers.add(new MoteObserver(this, mote));
  }

  public void closePlugin() {
    /* Clean up plugin resources */
    logger.info("Tidying up CoojaHWDB listeners/observers");
    if (!initialised) return;
    sim.getEventCentral().removeMoteCountListener(moteCountListener); 
    //radioMedium.deleteRadioMediumObserver(radioMediumObserver);
    for(MoteObserver mote : moteObservers) {
      mote.deleteAllObservers();
    }
    logger.info("CoojaHWDB cleaned up");
  }

  public void radioEventHandler(Radio radio, Mote mote) {
    hwdb.insert("radio", String.format("('%d', '%d',\"%s\", '%d', '%1.1f', '%1.1f')", 
      sim.getSimulationTime(), mote.getID(), 
      radio.getLastEvent(), (radio.isRadioOn() ? 1 : 0), 
      radio.getCurrentSignalStrength(), radio.getCurrentOutputPower()));
  }

  public void cpuEventHandler(MSP430 cpu, Mote mote){
     hwdb.insert("cpu", String.format("('%d', '%d', '%d', \"%s\")", sim.getSimulationTime(), mote.getID(),
                 cpu.getMode(), MSP430Constants.MODE_NAMES[cpu.getMode()]));
  }


}
    /* Subscribes to all events on the radio medium */
    // radioMedium.addRadioMediumObserver(radioMediumObserver = new Observer() {
    //   public void update(Observable obs, Object obj) {
    //     RadioConnection conn = radioMedium.getLastConnection();
    //     if (conn == null) return;
    //     String s = "Mote: " + conn.getSource().getMote().getID() + "sent to Mote(s): ";
    //     for(Radio radio : conn.getDestinations()) {
    //       s += radio.getMote().getID() + " ";
    //     }
    //     logger.info(s);
    //   }
    // });
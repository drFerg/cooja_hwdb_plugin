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

import se.sics.cooja.ClassDescription;
import se.sics.cooja.GUI;
import se.sics.cooja.Mote;
import se.sics.cooja.MoteInterface;
import se.sics.cooja.MoteInterfaceHandler;
import se.sics.cooja.PluginType;
import se.sics.cooja.RadioConnection;
import se.sics.cooja.RadioMedium;
import se.sics.cooja.RadioPacket;
import se.sics.cooja.SimEventCentral.MoteCountListener;
import se.sics.cooja.Simulation;
import se.sics.cooja.TimeEvent;
import se.sics.cooja.VisPlugin;

import se.sics.cooja.interfaces.Radio;

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


  /**
   * @param simulation Simulation object
   * @param gui GUI object 
   */
  public CoojaHWDB(Simulation simulation, GUI gui) {
    super("Cooja HWDB", gui);
    sim = simulation;
    radioMedium = sim.getRadioMedium();
    hwdb = new HWDBClient("localhost", 1234, "Cooja");
    
    /* Initialise Observers button */
    JButton button = new JButton("Observe");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!initialised) initObservers();
      }
    });
    add(BorderLayout.NORTH, button);
    setSize(300,100);
  }

  public void initObservers() {
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

  public void radioEventHandler(Radio radio) {
    hwdb.insert("radio", String.format("('%d', '%d',\"%s\", '%d', '%1.1f', '%1.1f')", 
      sim.getSimulationTime(), radio.getMote().getID(), 
      radio.getLastEvent(), (radio.isRadioOn() ? 1 : 0), 
      radio.getCurrentSignalStrength(), radio.getCurrentOutputPower()));
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
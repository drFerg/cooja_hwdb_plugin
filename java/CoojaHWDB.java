import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
@ClassDescription("Cooja HWDB") /* Description shown in menu */
@PluginType(PluginType.SIM_PLUGIN)
public class CoojaHWDB extends VisPlugin {
  private static final long serialVersionUID = 4368807123350830772L;
  private static Logger logger = Logger.getLogger(CoojaHWDB.class);


  private Simulation sim;
  private RadioMedium radioMedium;
  private Observer radioMediumObserver;
  private Observer radioObserver;
  private ArrayList<Observer> radioObservers;
  private Observer msObserver;
  private MoteCountListener moteCountListener;
  private ArrayList<MoteObserver> moteObservers;
  private JTextField textField;


  /**
   * @param simulation Simulation object
   * @param gui GUI object 
   */
  public CoojaHWDB(Simulation simulation, GUI gui) {
    super("Cooja HWDB", gui);
    sim = simulation;
    radioMedium = sim.getRadioMedium();
    //radioObservers = new ArrayList<Observer>();
    moteObservers = new ArrayList<MoteObserver>();

    for(Mote mote : sim.getMotes()) {
      moteObservers.add(new MoteObserver(this, mote));
      //mote.getInterfaces().getRadio().addObserver(radioObserver);
    }

    /* Button */
    JButton button = new JButton("button");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logger.info("Button clicked");
      }
    });
    add(BorderLayout.NORTH, button);

    /* Text field */
    textField = new JTextField("enter text to save");
    add(BorderLayout.SOUTH, textField);

    /* Subscribes to all motes radio's events */
    radioObserver = new Observer() {
      public void update(Observable obs, Object obj) {
        if (obs != null) logger.info("Got obs");
        radioEvent((Radio) obs);
      }
    };

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

    /* Listens for any new nodes added during runtime */
    sim.getEventCentral().addMoteCountListener(moteCountListener = new MoteCountListener() {
      public void moteWasAdded(Mote mote) {
        /* Add mote's radio to observe list */
        logger.info("Added a mote");
        mote.getInterfaces().getRadio().addObserver(radioObserver);
      }
      public void moteWasRemoved(Mote mote) {
        /* Remove motes radio from observe list */
        logger.info("Removed a mote");
        mote.getInterfaces().getRadio().deleteObserver(radioObserver);
      }
    });

    /* Register as millisecond observer: print time every millisecond */
    // simulation.addMillisecondObserver(msObserver = new Observer() {
    //   public void update(Observable obs, Object obj) {
    //     logger.info("Millisecond observer: simulation time is now: " + sim.getSimulationTimeMillis() + " ms");
    //   }
    // });
    /* Register self-repeating event in simulation thread */
    // simulation.invokeSimulationThread(new Runnable() {
    //   public void run() {
    //      This is called from the simulation thread; we can safely schedule events 
    //     sim.scheduleEvent(repeatEvent, sim.getSimulationTime());
    //   }
    // });

    setSize(300,100);
  }

  private TimeEvent repeatEvent = new TimeEvent(0) {
    public void execute(long t) {
      logger.info("Repeat event: simulation time is now: " + sim.getSimulationTimeMillis() + " ms");

      /* This is called from the simulation thread; we can safely schedule events */
      sim.scheduleEvent(this, t + 10*Simulation.MILLISECOND);
    }
  };

  public void closePlugin() {
    /* Clean up plugin resources */
    logger.info("Tidying up CoojaHWDB listeners/observers");
    //sim.deleteMillisecondObserver(msObserver);
    //repeatEvent.remove();
    sim.getEventCentral().removeMoteCountListener(moteCountListener);
    radioMedium.deleteRadioMediumObserver(radioMediumObserver);
    for(Mote mote : sim.getMotes()) {
      mote.getInterfaces().getRadio().deleteObserver(radioObserver);
    }
    for(MoteObserver mote : moteObservers) {
      mote.deleteAllObservers();
    }
    logger.info("CoojaHWDB cleaned up");
  }

  public Collection<Element> getConfigXML() {
    ArrayList<Element> config = new ArrayList<Element>();
    Element element;

    /* Save text field */
    element = new Element("textfield");
    element.setText(textField.getText());
    config.add(element);

    return config;
  }

  private void radioEvent(Radio radio) {
    if (radio == null) {
      logger.info("No radio obj");
      return;
    }
    logger.info("Last event from mote " + radio.getMote().getID() + " : " + radio.getLastEvent());

    // lastEventLabel.setText("Last event: " + radio.getLastEvent());
    // ssLabel.setText("Signal strength (not auto-updated): "
    //     + String.format("%1.1f", radio.getCurrentSignalStrength()) + " dBm");
    // if (radio.getChannel() == -1) {
    //   channelLabel.setText("Current channel: ALL");
    // } else {
    //   channelLabel.setText("Current channel: " + radio.getChannel());
    // }
  }



  public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
    for (Element element : configXML) {
      if (element.getName().equals("textfield")) {
        final String text = element.getText();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            textField.setText(text);
          }
        });
      }
    }
    return true;
  }

}

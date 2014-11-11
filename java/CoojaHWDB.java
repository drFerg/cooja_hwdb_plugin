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
import se.sics.cooja.PluginType;
import se.sics.cooja.Simulation;
import se.sics.cooja.TimeEvent;
import se.sics.cooja.VisPlugin;
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
  private Observer msObserver;
  private JTextField textField;


  /**
   * @param simulation Simulation object
   * @param gui GUI object 
   */
  public CoojaHWDB(Simulation simulation, GUI gui) {
    super("Cooja HWDB", gui);
    this.sim = simulation;

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

    /* Register as millisecond observer: print time every millisecond */
    simulation.addMillisecondObserver(msObserver = new Observer() {
      public void update(Observable obs, Object obj) {
        logger.info("Millisecond observer: simulation time is now: " + sim.getSimulationTimeMillis() + " ms");
      }
    });

    /* Register self-repeating event in simulation thread */
    simulation.invokeSimulationThread(new Runnable() {
      public void run() {
        /* This is called from the simulation thread; we can safely schedule events */
        sim.scheduleEvent(repeatEvent, sim.getSimulationTime());
      }
    });

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
    logger.info("Deleting millisecond observer");
    sim.deleteMillisecondObserver(msObserver);

    logger.info("Unschedule repeat event");
    repeatEvent.remove();
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

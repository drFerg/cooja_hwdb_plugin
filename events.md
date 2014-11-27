WSN Simulation Events
=====================

Within the simulation each mote contains a set of interfaces distinct to its type (program code).
Each interface allows observers to be notified of when its state changes, after the fact.

Most data can be both read and written to, allowing dynamic alteration of the motes and their radio environment at run time.

Radio
-----

Available data:
 - Last event (which alerted the observer) (enum)
 - Last packet transmitted/received (byte[])
 - Transmitting/Receiving (Booleans)
 - On/Off (Boolean)
 - Current Output Power (double)
 - Current Signal Strength (double)
 - Radio Channel (int)

Position
--------

Available data:
 - x, y, z (double)

Radio medium
------------

The medium handles all actual transmissions and receptions of packets based on the positions of motes and the current radio model.

Available data:
 - Last connection (source, destinations, packet, interfered destinations)

CPU
---

Available data:
 - Power mode (int/string) e.g., active, low power mode 1, etc.

Memory
------

Available actions:
 - Read/write variables

LED
---

Available data:
 - On/Off for Any/Red/Green/Yellow (booleans)

Button
------

Available data:
 - Pressed (boolean)
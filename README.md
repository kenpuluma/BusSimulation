# BusSimulation
This is a java multithread project example of simulating bus system.

The purpose of the simulation is to observe the behavior of the system and the waiting queue at each stop.
Buses will continuous go through each bus stops and pick up passengers (if there are).

User can decide following parameters at the beginning of the simulation:
     number of bus stops                                 (15 by default)
     number of buses                                     (5 by default)
     time to drive between two contiguous stops          (5 minutes by default)
     mean arrival rate of passengers at each stop        (5 persons/min by default)
     boarding time for each passenger                    (2 seconds by default)
     program running time                                (8 hours by default)

The program will pop-up a simple visible window that let user see clearly what happens to each bus and bus stops,
and will store the waiting queue size and bus location every 30 minutes to save.txt. You can find this file under
the project folder. The program will automatically exit after the time user defined or manually stopped by click
at the close button. Before exit it will also save all the parameters for this time to the output file.

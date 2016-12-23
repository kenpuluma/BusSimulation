import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jiazhen Liu on 10/4/2016.
 *
 * This is a bus simulation system. Buses will continuous go through each bus stops and pick up passengers (if there
 * are). The program allows user to observe the behavior of the buses and the waiting queue at each stop.
 *
 * User can decide following parameters at the beginning of the simulation:
 *      number of bus stops                                 (15 by default)
 *      number of buses                                     (5 by default)
 *      time to drive between two contiguous stops          (5 minutes by default)
 *      mean arrival rate of passengers at each stop        (5 persons/min by default)
 *      boarding time for each passenger                    (2 seconds by default)
 *      program running time                                (8 hours by default)
 *
 * The program will pop-up a simple visible window that let user see clearly what happens to each bus and bus stops,
 * and will store the waiting queue size and bus location every 30 minutes to save.txt. You can find this file under
 * the project folder. The program will automatically exit after the time user defined or manually stopped by click
 * at the close button. Before exit it will also save all the parameters for this time to the output file.
 *
 */
public class BusServiceSimulate extends JPanel {
    // use arraylist to store the global variables so that all the functions can read them
    private static ArrayList<BusStop> _bus_stops;
    private static ArrayList<Bus> _buses;
    private static ArrayList<JProgressBar> _progress_bars;
    private double seed; // seed for random number generation
    private long startTime; // record running time
    private long curTime;

    // initialization reads all the parameters and create program needed objects
    public void Initial(int bus_stop_num, int bus_num, int driving_time, int board_time, int person_arrival_rate, int running_time) {
        // initialize the arraylists
        _bus_stops = new ArrayList<BusStop>();
        _buses = new ArrayList<Bus>();
        _progress_bars = new ArrayList<JProgressBar>();
        seed = 100;
        startTime = System.nanoTime();

        JPanel left = new JPanel(new GridLayout(0, 1)); // use two containers to hold the bars
        JPanel right = new JPanel(new GridLayout(0, 1)); // use Gridlayout to manage objects size
        JPanel panel = new JPanel(new GridLayout(0, 2));   // hold two panels
        Border border;  // draw a border for each progress bar

        // create temporary class object to add to the arraylist
        Bus bus;
        BusStop bus_stop;

        // initialize Bus and BusStop objects according to input size from user
        for (int i = 0; i < bus_num; i++) {
            // each Bus object need bus name, index of bus, bus initial position, driving time between stops,
            // and passenger board time to consctruct.
            bus = new Bus(("bus " + i), i, (bus_stop_num / bus_num) * i, driving_time, board_time);
            _buses.add(bus);
            _progress_bars.add(new JProgressBar()); // add a new progress bar
            _progress_bars.get(i).setMaximum(bus_stop_num); // set max value of the bar to number of bus stops
            _progress_bars.get(i).setStringPainted(true);
            _progress_bars.get(i).setValue((bus_stop_num / bus_num) * i);   // set value to initial position
            _progress_bars.get(i).setString(Integer.toString((bus_stop_num / bus_num) * i));
            border = BorderFactory.createTitledBorder(_buses.get(i)._bus_name);   // draw a border that displays bus name
            _progress_bars.get(i).setBorder(border);
            right.add(_progress_bars.get(i)); // add to container
        }

        for (int i = 0; i < bus_stop_num; i++) {
            // each BusStop object need bus stop name, index of bus stop, and mean passenger arrival rate to construct
            bus_stop = new BusStop(("busstop " + i), i, person_arrival_rate);
            _bus_stops.add(bus_stop);
            // add a progress bar for each waiting queue
            _progress_bars.add(new JProgressBar()); // add a new progress bar
            _progress_bars.get(i + bus_num).setMaximum(200);
            _progress_bars.get(i + bus_num).setStringPainted(true); // skip first bus_num of items created for buses
            _progress_bars.get(i + bus_num).setString("0"); // display initial value
            border = BorderFactory.createTitledBorder(_bus_stops.get(i)._bus_stop_name);   // displays bus stop name
            _progress_bars.get(i + bus_num).setBorder(border);
            left.add(_progress_bars.get(i + bus_num));   // add to container
        }

        panel.add(left);    // add to the container
        panel.add(right);
        panel.add(new JScrollPane(left, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));  // add two scroll bars
        panel.add(new JScrollPane(right, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        JFrame f = new JFrame("Thread");    // create a new frame
        f.add(panel);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // set operation when close the window
        f.setSize(1200, 975);    // set window size
        f.setVisible(true); // make the frame visible

        // exit action implement, store needed info when exit.
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveOnExit(bus_stop_num, bus_num, driving_time, board_time, person_arrival_rate);
                System.exit(0);
            }
        };
        f.addWindowListener(exitListener);

        // start recording
        generatePeriodic(bus_stop_num, bus_num, driving_time, board_time, person_arrival_rate, running_time);
    }

    // save current waiting queue and bus location to a txt file
    public void saveToFile(int bus_stop_num, int bus_num, int driving_time, int board_time, int person_arrival_rate, int running_time) {
        File file = new File("save.txt");   // load the file
        // if file doesn't exists, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // write something to the file
        try {
            FileWriter out = new FileWriter(file, true);    // append to it
            curTime = System.nanoTime();
            //out.write(Long.toString((curTime - startTime) / 1000000000 / 60));  // save running time to file
            out.write(Long.toString((curTime - startTime) / 1000000000));  // for test
            out.write('\n');
            for (int i = 0; i < _bus_stops.size(); i++) {
                out.write(_bus_stops.get(i).get_passenger() + " "); // save waiting queue at each bus stops
            }
            out.write('\n');
            for (int i = 0; i < _buses.size(); i++) {
                out.write(_buses.get(i).get_bus_location() + " ");  // save bus locations
            }
            out.write('\n');
            out.close();    // close the file
            if (((curTime - startTime) / 1000000000) >= (running_time * 60)) {
                saveOnExit(bus_stop_num, bus_num, driving_time, board_time, person_arrival_rate);
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOnExit(int bus_stop_num, int bus_num, int driving_time, int board_time, int person_arrival_rate) {
        File file = new File("save.txt");   // load the file
        // if file doesn't exists, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // when the program exits, it will store all the parameters used this time,
        // as well as max size of waiting queue at each bus stop
        try {
            FileWriter out = new FileWriter(file, true);    // append to it
            out.write("Simulation complete. See parameters below:" + '\n');
            out.write("bus stops:" + bus_stop_num + " buses:" + bus_num + " driving time:" + driving_time + " board time:" + board_time + " person arrival rate:" + person_arrival_rate + '\n');   // store parameters
            out.write("max waiting queue at each stop: ");
            for (int i = 0; i < _bus_stops.size(); i++) {
                out.write(_bus_stops.get(i)._max_passenger + " ");  // store max
            }
            out.write('\n');
            out.write('\n');
            out.close();    // close the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generatePeriodic(int bus_stop_num, int bus_num, int driving_time, int board_time, int person_arrival_rate, int running_time) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveToFile(bus_stop_num, bus_num, driving_time, board_time, person_arrival_rate, running_time);
            }
        }, 0, 30 * 1000);   // for test
        //  }, 0, 30 * 60 * 1000);
    }

    // exponentially distributed random number generation referenced from lecture slides
    public double randomNumber(int person_arrival_rate) {
        double x;
        x = -(1.0 / person_arrival_rate) * Math.log(((seed + 1.0) / 65536.0));
        seed = (25173.0 * seed + 13849.0) % 65536.0;
        return x;
    }

    // BusStop class implementation
    public class BusStop implements Runnable {
        int _bus_stop_index;
        int _passenger;
        int _max_passenger;
        int _person_arrival_rate;
        String _bus_stop_name;

        // default constructor
        public BusStop(String bus_stop_name, int bus_stop_index, int person_arrival_rate) {
            _passenger = 0;
            _max_passenger = 0;
            _bus_stop_index = bus_stop_index;
            _person_arrival_rate = person_arrival_rate;
            _bus_stop_name = bus_stop_name;
        }

        public int get_passenger() {
            return _passenger;
        }

        // generate or pick up a passenger by demand
        public void set_passenger(int i) {
            _passenger += i;
        }

        // person event
        // define thread process
        public void run() {
            int event_time = 0; // keep track of next event's time
            while (true) {
                try {
                    event_time = (int) (randomNumber(_person_arrival_rate) * 1000);    // for test
                    //event_time = (int) (randomNumber(_person_arrival_rate) * 1000 * 60);    // convert from minutes to milliseconds
                    Thread.sleep(event_time);   // wait until next person come
                    set_passenger(1);   // generate a passenger
                    if (_max_passenger < _passenger)
                        _max_passenger = _passenger;
                    _progress_bars.get(_bus_stop_index + _buses.size()).setValue(_passenger);    // update the progress bar
                    _progress_bars.get(_bus_stop_index + _buses.size()).setString(Integer.toString(_passenger));
                } catch (InterruptedException e) {
                    System.out.println("Interrupted!");
                }
            }
        }
    }

    // Bus class implementation
    public class Bus implements Runnable {
        int _bus_index;
        int _bus_pos;
        int _driving_time;
        int _board_time;
        String _bus_name;

        // default constructor
        public Bus(String bus_name, int bus_index, int bus_pos, int driving_time, int board_time) {
            _bus_name = bus_name;
            _bus_index = bus_index;
            _bus_pos = bus_pos;
            _driving_time = driving_time;
            _board_time = board_time;
        }

        public int get_bus_location() {
            return _bus_pos;
        }

        // boarder event
        public void boarder(ArrayList<BusStop> bus_stops) {
            bus_stops.get(_bus_pos).set_passenger(-1);  // pick up a passenger
            _progress_bars.get(_bus_pos + _buses.size()).setValue(bus_stops.get(_bus_pos).get_passenger()); // update the progress bar
            _progress_bars.get(_bus_pos + _buses.size()).setString(Integer.toString(bus_stops.get(_bus_pos).get_passenger()));
        }

        // arrival event
        public void arrival(ArrayList<BusStop> bus_stops) {
            if (_bus_pos == bus_stops.size() - 1)    // if it's not the last stop then go to next bus stop
                _bus_pos = 0; // go to the first stop (assuming the bus route is a circle)
            else
                _bus_pos++; // go to the next stop
            _progress_bars.get(_bus_index).setValue(_bus_pos);  // update the progress bar
            _progress_bars.get(_bus_index).setString(Integer.toString(_bus_pos));
            System.out.println(_bus_name + " arrives at stop");
        }

        // define thread process
        public void run() {
            int event_time = 0; // keep track of next event time
            while (true) {
                try {
                    while (_bus_stops.get(_bus_pos).get_passenger() > 0) {  // pick up all passengers when the waiting
                        boarder(_bus_stops);                                // queue is not empty
                        event_time = _board_time * 1000 / 60;    // for test
                        //event_time = _board_time * 1000;    // convert from seconds to milliseconds
                        Thread.sleep(event_time);   // wait until next passenger board
                    }
                    event_time = _driving_time * 1000; // for test
                    //event_time = _driving_time * 60 * 1000; // convert from minutes to milliseconds
                    Thread.sleep(event_time);   // wait until arrive at next stop
                    arrival(_bus_stops);    // after pick up passengers the bus goes to the next bus stop
                } catch (InterruptedException e) {
                    System.out.println("Interrupted!");
                }
            }
        }
    }

    public static void main(String[] args) {
        BusServiceSimulate simulation = new BusServiceSimulate();
        // some input
        int bus_stop_num = 15;
        int bus_num = 5;
        int driving_time = 5;
        int board_time = 2;
        int person_arrival_rate = 5;
        int running_time = 8;
        // read from user defined file
        try {
            Scanner input = new Scanner(System.in);
            System.out.println("Please see the guide below and modify your input file:");
            System.out.println("The input file can only have integer numbers followed one by one, and separated by each other with at least one space.");
            System.out.println("The order of input is:");
            System.out.println("bus stop number, bus number, driving time (in minute), boarding time (in second), person arrival rate (person/minutes), programming running time (in hour)");
            System.out.println("Here is an example of what should be contained in an input file: 15 5 5 2 5 8");
            System.out.println("Now please enter the input file name with extention (Notice that for test purpose I speed up the program 60 times than original):");

            File file = new File(input.nextLine());
            input = new Scanner(file);

            bus_stop_num = input.nextInt();
            bus_num = input.nextInt();
            driving_time = input.nextInt();
            board_time = input.nextInt();
            person_arrival_rate = input.nextInt();
            running_time = input.nextInt();

            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialization
        simulation.Initial(bus_stop_num, bus_num, driving_time, board_time, person_arrival_rate, running_time);
        // start threads
        for (int i = 0; i < _bus_stops.size(); i++) {
            new Thread(_bus_stops.get(i)).start();
        }
        for (int i = 0; i < _buses.size(); i++) {
            new Thread(_buses.get(i)).start();
        }
    }
}

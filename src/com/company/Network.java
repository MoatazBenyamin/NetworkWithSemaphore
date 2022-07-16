package com.company;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import static java.lang.Thread.sleep;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Semaphore
{
    protected int value = 0 ;
    protected Semaphore() { value = 0 ; }
    protected Semaphore(int initial) { value = initial ; }

    public synchronized void P(Device device) throws InterruptedException, IOException {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("C:\\Users\\moataz\\Desktop\\Network\\src\\test.txt", true));
        value-- ;
        if (value < 0) {
            try {
                writer.write(" ( "+device.deviceName+" ) " + " ( " + device.deviceType + " ) " + " arrived and waiting"+'\n' );
                System.out.println(" ( "+device.deviceName+" ) " + " ( " + device.deviceType + " ) " + " arrived and waiting"+'\n' );
                wait ();
            } catch (InterruptedException e)
            {
                e.printStackTrace ();
            }
        }
        else
        {
            writer.write (" ( "+device.deviceName+" ) " + "(" + device.deviceType + " ) " + " arrived"+'\n' );
            System.out.println(" ( "+device.deviceName+" ) " + "(" + device.deviceType + " ) " + " arrived"+'\n' );
        }
        writer.close();
    }

    public synchronized void V(Device device) {
        value++ ; if (value <= 0) {notify();}
    }
}

class Router {

    public int numOfDevices, currentConnectedDevices;
    public Semaphore s;
    public boolean[] connection;
    //constructor
    Router(int Devices) {
        numOfDevices = Devices;
        s = new Semaphore(Devices);
        connection = new boolean[Devices];
    }

    public int connect(Device device) throws InterruptedException {
        Object ob1=new Object();
        synchronized(ob1){
            for (int i = 0; i < numOfDevices; i++) {
                if(connection[i]==false){   //if conection is false do wait
                    currentConnectedDevices++;
                    device.id = i + 1;
                    connection[i] = true;
                    sleep(100);
                    break;
                }
            }
            return device.id;
        }
    }


    public synchronized void disconnect(Device device) throws IOException{
        BufferedWriter writer = new BufferedWriter(
                new FileWriter("C:\\Users\\moataz\\Desktop\\Network\\src\\test.txt", true));
        currentConnectedDevices--;
        connection[device.id-1] = false;
        notify();//for wake up threads that are waiting

        writer.write("Connection " + device.id + ": " + device.deviceName + "  Logged out"+'\n');
        System.out.println("Connection " + device.id + ": " + device.deviceName + "  Logged out"+'\n');
        writer.close();
    }

}



class Device extends Thread {
    String deviceType;
    String deviceName;
    int id;
    Router router;

    public Device ( Router r,String Na ,String Ty ) {
        this.deviceType = Ty;
        this.deviceName = Na;
        this.id = 1;
        this.router = r ;
    }
    //Connection 1: C1 Occupied
    @Override
    public void run () {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("C:\\Users\\moataz\\Desktop\\Network\\src\\test.txt", true));
            router.s.P ( this );
            id = router.connect ( this );
            writer.write ( "connection " + id + ": " + deviceName + " Occupied\n" );
            System.out.println( "connection " + id + ": " + deviceName + " Occupied\n" );

            writer.write ( "connection " + id + ": " + deviceName + " LogIn\n" );
            System.out.println( "connection " + id + ": " + deviceName + " LogIn\n" );

            writer.write ("connection " + id + ": " + deviceName + " performs online activity\n" );
            System.out.println("connection " + id + ": " + deviceName + " performs online activity\n" );

            router.disconnect ( this );
            router.s.V ( this );

            writer.close();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
        catch (IOException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

public class Network {

    public static void main ( String[] args ) throws InterruptedException {
        ArrayList<Device> devices = new ArrayList<> ();
        System.out.println ( "What is the number of WI-FI Connections?" );
        Scanner scan = new Scanner ( System.in );
        int input = scan.nextInt ();
        Router router = new Router (input);

        System.out.println ( "What is the number of devices Clients want to connect?" );
        int num = scan.nextInt ();
        for (int i = 0; i < num; i++) {
            String name = scan.next ();
            String type = scan.next ();
            Device newDevice = new Device (router , type ,name );
            devices.add (newDevice);
        }

        for (int i = 0; i < num; i++) {
            devices.get(i).start();
        }

    }
}
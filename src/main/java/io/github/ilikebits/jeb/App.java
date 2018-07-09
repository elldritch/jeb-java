package io.github.ilikebits.jeb;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, RPCException {
        System.out.println("Hello World!");
        Connection connection = Connection.newInstance();
        KRPC krpc = KRPC.newInstance(connection);
        System.out.println("Connected to kRPC version " + krpc.getStatus().getVersion());

        SpaceCenter sc = SpaceCenter.newInstance(connection);
        Vessel v = sc.getActiveVessel();
        Flight f = v.flight(v.getSurfaceReferenceFrame());

        while (true) {
            System.out.println(f.getSurfaceAltitude());
        }
    }
}

package io.github.ilikebits.jeb;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Control;
import krpc.client.services.SpaceCenter.Engine;
import krpc.client.services.SpaceCenter.Part;
import krpc.client.services.SpaceCenter.Parts;
import krpc.client.services.SpaceCenter.Resource;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

/**
 * Hello world!
 */
public class App {
  public static void main(String[] args) {
    // Attempt to connect to kRPC server.
    Connection connection = null;
    try {
      connection = Connection.newInstance();
    } catch (IOException e) {
      System.out.println("Could not connect to kRPC server: " + e.getLocalizedMessage());
      System.exit(1);
    }

    KRPC krpc = KRPC.newInstance(connection);
    try {
      System.out.println("Connected to kRPC version " + krpc.getStatus().getVersion());

      // Get maximum (i.e. current) decouple stage.
      SpaceCenter sc = SpaceCenter.newInstance(connection);
      Vessel v = sc.getActiveVessel();
      Parts parts = v.getParts();
      int maxDecoupleStage = parts
        .getAll()
        .stream()
        .map(p -> {
          try {
            return p.getDecoupleStage();
          } catch (RPCException e) {
            return -1;
          }
        })
        .max(Comparator.naturalOrder())
        .orElse(-1);

      // Get current stage.
      Control c = v.getControl();
      int stage = c.getCurrentStage();

      // Get resources for current decouple stage.
      System.out.println("Next stage: " + (maxDecoupleStage - 1));
      Resources resources = v.resourcesInDecoupleStage(maxDecoupleStage - 1, false);
      List<Resource> resourceList = resources.getAll();
      System.out.println("Resources in stage: " + resourceList);
      Map<String, Resource> resourceMap = new HashMap<>();
      for (Resource r : resourceList) {
        String name = r.getName();
        System.out.println("Found resource: " + name);
        System.out.println("Quantity: " + r.getAmount());
        resourceMap.put(name, r);
      }

      // Iterate over parts.
      float ispNumer = 0;
      float ispDenom = 0;
      double mass = 0.0;
      double dryMass = 0.0;
      for (Part p : parts.getAll()) {
        int pStage = p.getStage();
        double pMass = p.getMass();
        // Compute mass for stage.
        mass += pMass;
        if (pStage == stage - 1) {
          dryMass += p.getDryMass();
        } else {
          dryMass += pMass;
        }

        // Compute specific impulse for engines.
        Engine e = p.getEngine();
        if (e == null) {
          continue;
        }
        if (pStage == stage - 1) {
          System.out.println("Found ready engine.");
          float thrust = e.getMaxThrust();
          float isp = e.getKerbinSeaLevelSpecificImpulse();
          System.out.println("Thrust: " + thrust);
          System.out.println("I_sp: " + isp);
          ispNumer += thrust;
          ispDenom += thrust / isp;
        }
      }
      float isp = ispNumer / ispDenom;
      System.out.println("Combined I_sp: " + isp);
      double dv = Math.log(mass / dryMass) * isp * 9.81;
      System.out.println("Stage delta V: " + dv);
    } catch (RPCException e) {
      System.out.println("RPC exception occurred: " + e.getLocalizedMessage());
    }

    System.out.println("Exit.");
    System.exit(0);
  }
}

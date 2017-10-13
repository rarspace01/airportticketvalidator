package org.rarspace01.airportticketvalidator;

import java.util.Date;

public class Flight {
    public String fromAirport;
    public String toAirport;
    public String flightName;
    public String flightCarrierMarketed;
    public int flightNumberMarketed = -1;
    public String flightCarrierOperated;
    public int flightNumberOperated = -1;
    public Date flightTime;
    public String aircraft;

    @Override
    public String toString() {
        return flightTime + " " + (flightCarrierMarketed == null ? flightCarrierOperated : flightCarrierMarketed) +
               (flightCarrierMarketed == null ? flightNumberOperated : flightNumberMarketed) + " " + fromAirport + "->" + toAirport;
    }
}

package org.rarspace01.airportticketvalidator;

import java.text.DecimalFormat;
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
        return getUnifiedFlightName() + " - " + fromAirport + "->" + toAirport + " @" + flightTime;
    }

    public String getUnifiedFlightName() {
        return getFlightCarrierUnified() + getFlightNumberUnified();
    }

    private int getFlightNumberUnified() {
        return flightCarrierMarketed == null ? flightNumberOperated : flightNumberMarketed;
    }

    private String getFlightCarrierUnified() {
        return flightCarrierMarketed == null ? flightCarrierOperated : flightCarrierMarketed;
    }

    public String getUnifiedFlightNameBCBP() {
        return (getFlightCarrierUnified() + "   ").substring(0, 3) + (new DecimalFormat("0000").format(getFlightNumberUnified()));
    }
}

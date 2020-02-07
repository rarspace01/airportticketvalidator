package org.rarspace01.airportticketvalidator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class Flight implements Comparable{
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
        return getUnifiedFlightName() + " - " + fromAirport + "->" + toAirport + " @" + new SimpleDateFormat("HH:mm").format(flightTime);
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

    @Override
    public int compareTo(Object o) {
        if(o instanceof Flight){
            Flight otherFlight = (Flight) o;
            int compareFlightTime = this.flightTime.compareTo(otherFlight.flightTime);
            int compareFlightNumber = this.getUnifiedFlightName().compareTo(otherFlight.getUnifiedFlightName());
            return compareFlightTime == 0 ? compareFlightNumber : compareFlightTime;
        }
        return 0;
    }
}

package org.rarspace01.airportticketvalidator;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class AirportTicketValidatorApplication extends Application {

    private static AirportTicketValidatorApplication singleton;

    public List<Flight> getFlightCache() {
        return flightCache;
    }

    public void addToFlightCache(List<Flight> flightCache) {
        this.flightCache.addAll(flightCache);
    }

    private List<Flight> flightCache;

    public static AirportTicketValidatorApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        flightCache = new ArrayList<Flight>();
    }
}

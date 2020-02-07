package org.rarspace01.airportticketvalidator;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightFactoryTest {

    @Test
    void createFlightsFromJSONSource() {

    // Given
        Calendar currentDate = Calendar.getInstance();
        String airportCode = "HAM";
        String requestUrl = String.format("https://www.flightstats.com/v2/api-next/flight-tracker/dep/%s/%d/%d/%d/0?carrierCode=&numHours=12", airportCode, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.DAY_OF_MONTH));
        String page = new HttpHelper().getPage(requestUrl);

        // When
        List<Flight> flightsFromJSONSource = FlightFactory.createFlightsFromJSONSource(page, airportCode);

        // Then
        assertTrue(flightsFromJSONSource.size() > 0);

    }
}
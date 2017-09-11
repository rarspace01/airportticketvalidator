package org.rarspace01.airportticketvalidator;

import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class FlightUtil {


	public static boolean isFlightInList(Flight flightToBeSearched, List<Flight> listOfFlights) {
		boolean isFound = false;

		for (Flight currentFlight : listOfFlights) {

			if (currentFlight.fromAirport.equals(flightToBeSearched.fromAirport) &&
					currentFlight.toAirport.equals(flightToBeSearched.toAirport) &&
					isFLightNumberMatching(currentFlight, flightToBeSearched) &&
					isTimeCloseBy(currentFlight, flightToBeSearched)) {
				isFound = true;
				break;
			}


		}

		return isFound;
	}

	private static boolean isFLightNumberMatching(Flight currentFlight, Flight flightToBeSearched) {
		boolean isFLightNumberMatching = false;

		if (isMarketedCarrierMatching(currentFlight, flightToBeSearched)) {
			if (isFlightNumberLiteralMatching(currentFlight, flightToBeSearched)) {
				isFLightNumberMatching = true;
			}
		} else if (isOperatedCarrierMatching(currentFlight, flightToBeSearched)) {
			// on operator match
			if (isFlightNumberLiteralMatching(currentFlight, flightToBeSearched)) {
				isFLightNumberMatching = true;
			}
		}

		return isFLightNumberMatching;
	}

	private static boolean isFlightNumberLiteralMatching(Flight currentFlight, Flight flightToBeSearched) {
		return currentFlight.flightNumberMarketed == flightToBeSearched.flightNumberMarketed;
	}

	private static boolean isOperatedCarrierMatching(Flight flightToBeSearched, Flight flightCarrierOperated) {
		return flightToBeSearched.flightCarrierMarketed.equals(flightCarrierOperated.flightCarrierOperated);
	}

	private static boolean isMarketedCarrierMatching(Flight flightToBeSearched, Flight flightMarketedOperated) {
		return flightToBeSearched.flightCarrierMarketed.equals(flightMarketedOperated.flightCarrierMarketed);
	}

	private static boolean isTimeCloseBy(Flight currentFlight, Flight flightToBeSearched) {
		boolean isCloseBy = false;
		Calendar current = Calendar.getInstance();
		Calendar search = Calendar.getInstance();

		current.setTime(currentFlight.flightTime);
		search.setTime(flightToBeSearched.flightTime);

		if (current.get(Calendar.YEAR) == search.get(Calendar.YEAR) && current.get(Calendar.MONTH) == search.get(Calendar.MONTH) && current.get(Calendar.DAY_OF_MONTH) == search.get(Calendar.DAY_OF_MONTH)) {
			isCloseBy = true;
		} else {
			Log.d("FlightUtil", current.toString() + " vs " + search.toString());
		}

		return isCloseBy;
	}

}

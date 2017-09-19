package org.rarspace01.airportticketvalidator;

import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class FlightUtil {


	public static boolean isFlightInList(Flight flightToBeSearched, List<Flight> listOfFlights) {
		boolean isFound = false;

		for (Flight currentFlight : listOfFlights) {

			if (currentFlight != null && flightToBeSearched != null && currentFlight.fromAirport.equals(flightToBeSearched.fromAirport) &&
					currentFlight.toAirport.equals(flightToBeSearched.toAirport) &&
					isFlightNumberMatching(currentFlight, flightToBeSearched) &&
					isTimeCloseBy(currentFlight, flightToBeSearched)) {
				isFound = true;
				break;
			}


		}

		return isFound;
	}

	private static boolean isFlightNumberMatching(Flight currentFlight, Flight flightToBeSearched) {
		boolean isFLightNumberMatching = false;

		if (isMarketedCarrierMatching(currentFlight, flightToBeSearched)) {
			if (isFlightNumberLiteralMatchingMarketed(currentFlight, flightToBeSearched)) {
				isFLightNumberMatching = true;
			}
		} else if (isOperatedCarrierMatching(currentFlight, flightToBeSearched)) {
			// on operator match
			if (isFlightNumberLiteralMatchingOperated(currentFlight, flightToBeSearched)) {
				isFLightNumberMatching = true;
			}
		}

		return isFLightNumberMatching;
	}

	private static boolean isFlightNumberLiteralMatchingMarketed(Flight currentFlight, Flight flightToBeSearched) {
		return currentFlight.flightNumberMarketed == flightToBeSearched.flightNumberMarketed;
	}

	private static boolean isFlightNumberLiteralMatchingOperated(Flight currentFlight, Flight flightToBeSearched) {
		return currentFlight.flightNumberOperated == flightToBeSearched.flightNumberOperated;
	}

	private static boolean isOperatedCarrierMatching(Flight flightToBeSearched, Flight flightCarrierOperated) {
		return flightToBeSearched.flightCarrierMarketed.trim().equals(flightCarrierOperated.flightCarrierOperated.trim());
	}

	private static boolean isMarketedCarrierMatching(Flight flightToBeSearched, Flight flightMarketedOperated) {
		if(flightToBeSearched.flightCarrierMarketed != null && flightMarketedOperated.flightCarrierMarketed != null) {
			return flightToBeSearched.flightCarrierMarketed.trim().equals(flightMarketedOperated.flightCarrierMarketed.trim());
		} else {
			return false;
		}
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

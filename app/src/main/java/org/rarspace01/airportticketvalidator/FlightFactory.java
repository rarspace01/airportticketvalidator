package org.rarspace01.airportticketvalidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rarspace01.airportticketvalidator.bcbp.model.IataCode;

import java.util.ArrayList;
import java.util.List;

public class FlightFactory {

	public static Flight createFlightFromBCBP(IataCode inputBCBP) {
		Flight returnFlight = null;

		if (inputBCBP != null) {
			returnFlight = new Flight();
			returnFlight.fromAirport = inputBCBP.getFirstFlightSegment().getFromCity();
			returnFlight.toAirport = inputBCBP.getFirstFlightSegment().getFromCity();
			returnFlight.flightName = inputBCBP.getFirstFlightSegment().getFlightNumber().replaceAll("[0-9]", "");
			returnFlight.flightNumber = Integer.parseInt(inputBCBP.getFirstFlightSegment().getFlightNumber().replaceAll("[A-Za-z]", ""));
//			returnFlight.
		}

		return returnFlight;
	}

	public static List<Flight> createFlightsFromJSONArray(JSONArray inputJSON) {

		List<Flight> localList = new ArrayList<>();

		for (int i = 0; i < inputJSON.length(); i++) {
			try {
				JSONObject localFlight = inputJSON.getJSONObject(i);

				Flight readFlight = flightFromJSONObject(localFlight);
				localList.add(readFlight);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return localList;
	}

	private static Flight flightFromJSONObject(JSONObject jsonObject) {
		Flight returnFlight = null;

		try {
			jsonObject = new JSONObject("{\"Departure\":{\"AirportCode\":\"HAM\",\"ScheduledTimeLocal\":{\"DateTime\":\"2017-09-05T17:15\"},\"ScheduledTimeUTC\":{\"DateTime\":\"2017-09-05T15:15Z\"},\"TimeStatus\":{\"Code\":\"OT\",\"Definition\":\"Flight On Time\"},\"Terminal\":{\"Name\":2,\"Gate\":\"A18\"}},\"Arrival\":{\"AirportCode\":\"STR\",\"ScheduledTimeLocal\":{\"DateTime\":\"2017-09-05T18:30\"},\"ScheduledTimeUTC\":{\"DateTime\":\"2017-09-05T16:30Z\"},\"TimeStatus\":{\"Code\":\"OT\",\"Definition\":\"Flight On Time\"},\"Terminal\":{\"Name\":1}},\"MarketingCarrier\":{\"AirlineID\":\"4U\",\"FlightNumber\":2041},\"OperatingCarrier\":{\"AirlineID\":\"4U\",\"FlightNumber\":2041},\"Equipment\":{\"AircraftCode\":319},\"FlightStatus\":{\"Code\":\"NA\",\"Definition\":\"No status\"}}");

			jsonObject.getJSONObject("Departure");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnFlight;
	}

}

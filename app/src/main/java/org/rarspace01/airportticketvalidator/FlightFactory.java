package org.rarspace01.airportticketvalidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FlightFactory {


	public static void main(String[] args) {

	}

	List<Flight> createFlightsFromJSONArray(JSONArray inputJSON) {

		List<Flight> localList = new ArrayList<>();

		for (int i = 0; i < inputJSON.length(); i++) {
			try {
				JSONObject localFlight = inputJSON.getJSONObject(i);


			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return localList;
	}

	private Flight flightFromJSONObject(JSONObject jsonObject) {
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

package org.rarspace01.airportticketvalidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rarspace01.airportticketvalidator.bcbp.model.IataCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FlightFactory {

	private static SimpleDateFormat parserDate = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm");

	public static Flight createFlightFromBCBP(IataCode inputBCBP) {
		Flight returnFlight = null;

		if (inputBCBP != null) {
			returnFlight = new Flight();
			returnFlight.fromAirport = inputBCBP.getFirstFlightSegment().getFromCity();
			returnFlight.toAirport = inputBCBP.getFirstFlightSegment().getFromCity();
			returnFlight.flightCarrierOperated = inputBCBP.getFirstFlightSegment().getOperatingCarrierDesignator();
			returnFlight.flightNumberMarketed = Integer.parseInt(inputBCBP.getFirstFlightSegment().getFlightNumber().replaceAll("[A-Za-z]", ""));
			returnFlight.flightTime = inputBCBP.getFirstFlightSegment().getDateOfFlight().getTime();
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
			returnFlight = new Flight();

			JSONObject departure = jsonObject.getJSONObject("Departure");
			JSONObject arrival = jsonObject.getJSONObject("Arrival");

			returnFlight.fromAirport = departure.getString("AirportCode");
			returnFlight.flightTime = parserDate.parse(departure.getString("ScheduledTimeLocal"));
			returnFlight.toAirport = arrival.getString("AirportCode");
			returnFlight.flightCarrierMarketed = jsonObject.getJSONObject("MarketingCarrier").getString("AirlineID");
			returnFlight.flightNumberMarketed = Integer.parseInt(jsonObject.getJSONObject("MarketingCarrier").getString("FlightNumber"));
			returnFlight.flightCarrierOperated = jsonObject.getJSONObject("OperatingCarrier").getString("AirlineID");
			returnFlight.flightNumberOperated = Integer.parseInt(jsonObject.getJSONObject("OperatingCarrier").getString("FlightNumber"));
			returnFlight.aircraft = jsonObject.getJSONObject("Equipment").getString("AircraftCode");
		} catch (JSONException | ParseException e) {
			e.printStackTrace();
		}

		return returnFlight;
	}

	public static List<Flight> createFlightsFromXMLSource(String response) {
		List<Flight> localList = new ArrayList<>();

		if (!response.contains("<table")) {
			response = "<table>" + response + "</table>";
		}

		Document doc = Jsoup.parse(response);
		Elements trElements = doc.body().getElementsByTag("tr");
		for (Element element : trElements) {
			Flight flight = new Flight();
			Elements dataElements = element.getElementsByTag("td");
			for (Element tdElement : dataElements) {
				if (tdElement.text().matches("[0-9]{2,2}:[0-9]{2,2}")) {
					//flight.flightTime = new Date
				} else if (tdElement.text().matches(".*[-]\\s{0,1}[A-Za-z]{1,4}")) {
					flight.fromAirport = "HAM";
					flight.toAirport = tdElement.text().replaceAll(".*[-]", "").trim();
				} else if (tdElement.attributes().get("data-title").contains("Flight")) {
					String[] splittedFlightnumber = tdElement.text().split(" ");
					if (splittedFlightnumber.length == 2) {
						flight.flightCarrierMarketed = splittedFlightnumber[0];
						flight.flightNumberMarketed = Integer.parseInt(splittedFlightnumber[1]);
					}
				}
			}
			localList.add(flight);
		}

		return localList;
	}

}

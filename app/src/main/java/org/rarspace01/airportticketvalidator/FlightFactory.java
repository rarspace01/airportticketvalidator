package org.rarspace01.airportticketvalidator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.rarspace01.airportticketvalidator.bcbp.model.IataCode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class FlightFactory {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

	public static Flight createFlightFromBCBP(IataCode inputBCBP) {
		Flight returnFlight = null;

		if (inputBCBP != null) {
			returnFlight = new Flight();
			returnFlight.fromAirport = inputBCBP.getFirstFlightSegment().getFromCity();
			returnFlight.toAirport = inputBCBP.getFirstFlightSegment().getToCity();
			returnFlight.flightCarrierOperated = inputBCBP.getFirstFlightSegment().getOperatingCarrierDesignator();
			returnFlight.flightNumberOperated = Integer.parseInt(inputBCBP.getFirstFlightSegment().getFlightNumber().replaceAll("[A-Za-z]", ""));
			returnFlight.flightTime = inputBCBP.getFirstFlightSegment().getDateOfFlight().getTime();
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
				String elementText = tdElement.text();
				if (elementText.matches("[0-9]{2,2}:[0-9]{2,2}")) {
					String timeString = elementText;
					try {
						Date parseTime = DATE_FORMAT.parse(timeString);
						Calendar timeCalendar = Calendar.getInstance();
						timeCalendar.setTime(parseTime);
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
						calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
						flight.flightTime = calendar.getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					//flight.flightTime = new Date
				} else if (elementText.matches(".*[-]\\s{0,1}[A-Za-z]{1,4}")) {
					flight.fromAirport = "HAM";
					flight.toAirport = elementText.replaceAll(".*[-]", "").trim();
				} else if (tdElement.attributes().get("data-title").contains("Flight")) {
					String[] splittedFlightnumber = elementText.split(" ");
					if (splittedFlightnumber.length == 2) {
						flight.flightCarrierMarketed = splittedFlightnumber[0];
						flight.flightNumberMarketed = Integer.parseInt(splittedFlightnumber[1]);
					} else if (elementText.matches("[A-Z-a-z0-9]+[0-9]+")) {
						// search last character & everythign behind is flightnumber && needs to be verified
						int lastCharacter = -1;
						for (int i = 0; i < elementText.length(); i++) {
							if ((elementText.charAt(i) + "").matches("[A-Za-z]")) {
								lastCharacter = i;
							}
						}
						if (lastCharacter != -1) {
							flight.flightCarrierMarketed = elementText.substring(0, lastCharacter < elementText.length() ? lastCharacter + 1 : elementText.length() - 1);
							flight.flightNumberMarketed = Integer.parseInt(elementText.substring(lastCharacter < elementText.length() ? lastCharacter + 1 : elementText.length() - 1));
						}
					}
				}
			}
			if(flight.fromAirport != null && flight.toAirport != null){
				localList.add(flight);
			}
		}

		return localList;
	}

	@NotNull
	public static List<Flight> createFlightsFromJSONSource(@Nullable String response, final String departureAirport) {
		final List<Flight> localList = new ArrayList<>();
		JsonElement jsonElement = JsonParser.parseString(response);
		JsonArray jsonFlights = jsonElement.getAsJsonObject().get("data").getAsJsonObject().get("flights").getAsJsonArray();
		jsonFlights.iterator().forEachRemaining(new Consumer<JsonElement>() {
			@Override
			public void accept(JsonElement jsonElement) {
				Flight flight = new Flight();
				try {
					flight.flightTime = DATE_FORMAT.parse(jsonElement.getAsJsonObject().get("departureTime").getAsJsonObject().get("time24").getAsString());
				} catch (ParseException ignored) {
				}
				flight.fromAirport = departureAirport;
				flight.toAirport = jsonElement.getAsJsonObject().get("airport").getAsJsonObject().get("fs").getAsString();
				flight.flightCarrierMarketed = jsonElement.getAsJsonObject().get("carrier").getAsJsonObject().get("fs").getAsString();
				flight.flightNumberMarketed = Integer.parseInt(jsonElement.getAsJsonObject().get("carrier").getAsJsonObject().get("flightNumber").getAsString());
				flight.flightCarrierOperated = jsonElement.getAsJsonObject().get("operatedBy") instanceof JsonNull ? null : jsonElement.getAsJsonObject().get("operatedBy").getAsString();

				localList.add(flight);
			}
		});

		return localList;
	}
}

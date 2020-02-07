package org.rarspace01.airportticketvalidator

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.rarspace01.airportticketvalidator.bcbp.Parser
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val cachedFlightList: ArrayList<Flight> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btn_Scan)
        val btnResult = findViewById<Button>(R.id.btn_result)
        val textAirport = findViewById<EditText>(R.id.txtAirport)
        setProgressBar(0)

        btnScan.setOnClickListener({
            IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).initiateScan()
        })

        btnScan.setOnLongClickListener {
            showCode()
            true
        }

        btnResult.setOnLongClickListener {
            resetBackgroundSuccess()
            getDepartingFlights(textAirport.text.toString());
            true
        }

    }

    private fun showCode() {
        val txtAirport = findViewById<EditText>(R.id.txtAirport)
        var flightList: Array<String>?
        var flightListString: ArrayList<String> = ArrayList()

        AirportTicketValidatorApplication.getInstance().flightCache.sort()

        for (flight: Flight in AirportTicketValidatorApplication.getInstance().flightCache) {
            flightListString.add(flight.toString());
        }
        flightList = flightListString.toArray(arrayOfNulls<String>(0))

        if (flightList != null) {
            AlertDialog.Builder(this).setTitle("Choose flight")
                    .setItems(flightList, DialogInterface.OnClickListener { dialog, which ->
                        val flightFromString = getFlightFromString(flightList[which])
                        // TODO: set the date according to flight
                        var dayOfTheYear = "000" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                        dayOfTheYear = dayOfTheYear.substring(dayOfTheYear.length - 3, dayOfTheYear.length)
                        var defaultString = "M1FOUNDTHE/EASTEREGG"
                        if (txtAirport != null && txtAirport.text != null) {
                            val emptyString = "                    "
                            defaultString = (txtAirport.text.toString() + emptyString).substring(0, 20)
                        }
                        val bcbpRawData = defaultString + "        AAAAAH " + flightFromString.fromAirport + flightFromString.toAirport +
                                flightFromString.unifiedFlightNameBCBP + " " +
                                dayOfTheYear + "Y001A0018 147>1181  7250BEW 0000000000000291040000000000 0   LH 992003667193035    Y"
                        val context = this
                        val intent = Intent("com.google.zxing.client.android.ENCODE")
                        intent.putExtra("ENCODE_TYPE", "Text")
                        intent.putExtra("ENCODE_DATA", bcbpRawData)
                        intent.putExtra("ENCODE_FORMAT", "AZTEC")
                        startActivity(intent)
                    })
                    .create()
                    .show()
        }
    }

    private fun getFlightFromString(listString: String): Flight {
        var returnFlight = Flight()
        for (flight: Flight in AirportTicketValidatorApplication.getInstance().flightCache) {
            if (listString.contains(flight.unifiedFlightName)) {
                returnFlight = flight
                break;
            }
        }
        return returnFlight
    }

    private fun setProgressBar(progress: Int) {
        val progressbar = findViewById<ProgressBar>(R.id.databasePogressBar)
        progressbar.progress = progress
    }

    private fun setBackgroundSuccess(isSuccessfull: Boolean) {
        val resultButton = findViewById<Button>(R.id.btn_result)
        if (isSuccessfull) {
            resultButton.setBackgroundColor(Color.GREEN)
        } else {
            resultButton.setBackgroundColor(Color.RED)
        }
    }

    private fun resetBackgroundSuccess() {
        val resultButton = findViewById<Button>(R.id.btn_result)
        resultButton.setBackgroundColor(Color.GRAY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d("MainActivity", "Scanned")
                Log.d("MainActivity", result.contents)
                //Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                val bcbpParser = Parser()
                val readTicket = bcbpParser.parse(result.contents)

                Log.d("City from: ", readTicket.firstFlightSegment.fromCity)
                var airportText = findViewById<EditText>(R.id.txtAirport)
                //getDepartingFlights(airportText.text.toString())
                // wait till Flights are requested

                val bcbpFlight = FlightFactory.createFlightFromBCBP(readTicket)

                if (FlightUtil.isFlightInList(bcbpFlight, AirportTicketValidatorApplication.getInstance().flightCache)) {
                    Toast.makeText(this, "Valid Ticket!", Toast.LENGTH_LONG).show()
                    setBackgroundSuccess(true)
                    //postOnSlack(readTicket.passengerName + " had an valid Ticket!\uE312")
                } else {
                    Toast.makeText(this, bcbpFlight.toString(), Toast.LENGTH_LONG).show()
                    setBackgroundSuccess(false)
                    //postOnSlack(readTicket.passengerName + " had an invalid Ticket")
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getDepartingFlights(airportCode: String): List<Flight> {
        cachedFlightList.clear()
        var isProccessed = false;
        val flights = ArrayList<Flight>()
        val currentDate = Calendar.getInstance()
        var departedPage = String.format("https://www.flightstats.com/v2/api-next/flight-tracker/dep/%s/%d/%d/%d/0?carrierCode=&numHours=12",airportCode,currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH)+1, currentDate.get(Calendar.DAY_OF_MONTH))
        val queue = Volley.newRequestQueue(this)

        val req = object : StringRequest(Request.Method.GET, departedPage,
                Response.Listener<String> { response ->
                    //Log.d("Response", response)

                    // parse Flights to Array of Flights
                    cachedFlightList.addAll(FlightFactory.createFlightsFromJSONSource(response,airportCode))

                    AirportTicketValidatorApplication.getInstance().addToFlightCache(cachedFlightList);

                    val btnScan = findViewById<Button>(R.id.btn_Scan)
                    btnScan.setText(resources.getString(R.string.scan) + String.format("[%d]",cachedFlightList.size))

                    isProccessed = true;
                    setProgressBar(100)
                }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)
            isProccessed = true;
            Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
        }) {}

        var departedPage2 = String.format("https://www.flightstats.com/v2/api-next/flight-tracker/dep/%s/%d/%d/%d/12?carrierCode=&numHours=12",airportCode,currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH)+1, currentDate.get(Calendar.DAY_OF_MONTH))
        val req2 = object : StringRequest(Request.Method.GET, departedPage2,
                Response.Listener<String> { response ->
                    //Log.d("Response", response)

                    // parse Flights to Array of Flights
                    cachedFlightList.addAll(FlightFactory.createFlightsFromJSONSource(response,airportCode))

                    AirportTicketValidatorApplication.getInstance().addToFlightCache(cachedFlightList);

                    val btnScan = findViewById<Button>(R.id.btn_Scan)
                    btnScan.setText(resources.getString(R.string.scan) + String.format("[%d]",cachedFlightList.size))

                    isProccessed = true;
                    setProgressBar(100)
                }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)
            isProccessed = true;
            Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
        }) {}

        queue.add(req)
        queue.add(req2)

        return flights
    }

}

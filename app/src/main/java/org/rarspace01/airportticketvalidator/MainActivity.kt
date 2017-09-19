package org.rarspace01.airportticketvalidator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONArray
import org.rarspace01.airportticketvalidator.bcbp.Parser
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val cachedFlightList: ArrayList<Flight> = ArrayList<Flight>();

    var jsonArrayDepartingFlights = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btn_Scan)
        btnScan.setOnClickListener({ view ->
            IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).initiateScan()
        })

        getDepartingFlights("HAM");
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d("MainActivity", "Scanned")
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                val bcbpParser = Parser()
                val readTicket = bcbpParser.parse(result.contents)

                Log.d("City from: ", readTicket.firstFlightSegment.fromCity)
                var airportText = findViewById<EditText>(R.id.txtAirport)
                //getDepartingFlights(airportText.text.toString())
                // wait till Flights are requested

                val bcbpFlight = FlightFactory.createFlightFromBCBP(readTicket)

                if (FlightUtil.isFlightInList(bcbpFlight, cachedFlightList)) {
                    Toast.makeText(this, "Valid Ticket!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Non valid Ticket!", Toast.LENGTH_LONG).show()
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private var jsonAuth: String = "";

    fun getDepartingFlights(airportCode: String): List<Flight> {
        var isProccessed = false;
        val flights = ArrayList<Flight>();
        val currentDate = Date()
        val dateFormatter = SimpleDateFormat("YYYY-MM-dd'T'HH:mm")
        var departedPage = "https://www.hamburg-airport.de/tools/flightplan/flightplan_detail.php/en/departures/today/"
        val queue = Volley.newRequestQueue(this)

        val req = object : StringRequest(Request.Method.GET, departedPage,
                Response.Listener<String> { response ->
                    Log.d("Response", response)

                    // parse Flights to Array of Flights
                    cachedFlightList.addAll(FlightFactory.createFlightsFromXMLSource(response))

                    isProccessed = true;

                }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)
            isProccessed = true;
            Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
        }) {}

        queue.add(req)

        return flights
    }
}

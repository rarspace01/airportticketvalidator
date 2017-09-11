package org.rarspace01.airportticketvalidator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
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

        //val btnSearch = findViewById<Button>(R.id.btn_Scan)
        btn_Scan.setOnClickListener({ view ->
            IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).initiateScan()
        })

        cachedFlightList.addAll(getDepartingFlights("HAM"));
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
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getDepartingFlights(airportCode: String): List<Flight> {
        var isProccessed = false;
        val flights = ArrayList<Flight>();
        val currentDate = Date()
        val dateFormatter = SimpleDateFormat("YYYY-MM-dd'T'HH:mm")
        var departedPage = "https://api.lufthansa.com/v1/operations/flightstatus/departures/" + airportCode + "/" + dateFormatter.format(currentDate) + "?limit=100"
        val queue = Volley.newRequestQueue(this)

        val req = object : JsonObjectRequest(Request.Method.GET, departedPage,
                null, Response.Listener<JSONObject> { response ->
            Log.d("Response", response.toString())
            jsonArrayDepartingFlights = response.getJSONObject("FlightStatusResource").getJSONObject("Flights").getJSONArray("Flight")

            // parse Flights to Array of Flights
            cachedFlightList.addAll(FlightFactory.createFlightsFromJSONArray(jsonArrayDepartingFlights))

            isProccessed = true;

        }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)
            isProccessed = true;
            Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer 83kffu3m88khw3bhmer8fgp7")
                return headers
            }
        }

        queue.add(req)

        return flights
    }
}

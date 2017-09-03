package org.rarspace01.airportticketvalidator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.rarspace01.airportticketvalidator.bcbp.Parser


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).initiateScan()
        var airportText = findViewById <EditText>(R.id.txtAirport)
        getDepartingFlights(airportText.text.toString())
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
                Log.d("City from: ", readTicket.firstFlightSegment.fromCity);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getDepartingFlights(airportCode: String) {
        //
        /*

        5q8w245p9an8dzr9c7daz3dz
         */
        var departedPage = "https://api.lufthansa.com/v1/operations/flightstatus/departures/" + airportCode + "/2017-09-01T16:00?limit=100"
        val queue = Volley.newRequestQueue(this)
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, departedPage,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d(this.localClassName, response)
                }, Response.ErrorListener { Log.e(this.localClassName, "error") })
        queue.add(stringRequest)

    }

}

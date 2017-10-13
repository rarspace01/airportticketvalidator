package org.rarspace01.airportticketvalidator

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONArray
import org.rarspace01.airportticketvalidator.bcbp.Parser
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val cachedFlightList: ArrayList<Flight> = ArrayList<Flight>()

    var jsonArrayDepartingFlights = JSONArray()
    var longClicks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnScan = findViewById<Button>(R.id.btn_Scan)
        setProgressBar(0)

        btnScan.setOnClickListener({ view ->
            IntentIntegrator(this).setBeepEnabled(false).setOrientationLocked(false).initiateScan()
        })

        btnScan.setOnLongClickListener({ view ->
            val bcbpCode = findViewById<ImageView>(R.id.bcbpCode)
            /*val barcodeBitmap = createBarcodeBitmap(bcbpRawData, 250, 250)
            bcbpCode.setImageBitmap(barcodeBitmap)
            bcbpCode.setBackgroundColor(Color.WHITE)
            true*/
            showCode()
            true
        })

        getDepartingFlights("HAM");
    }

    private fun showCode() {
        var flightList: Array<String>? = null
        var flightListString: ArrayList<String> = ArrayList<String>();

        for (flight: Flight in AirportTicketValidatorApplication.getInstance().flightCache) {
            flightListString.add(flight.toString());
        }
        flightList = flightListString.toArray(arrayOfNulls<String>(0))

        if (flightList != null) {
            AlertDialog.Builder(this).setTitle("Choose flight")
                    .setItems(flightList, DialogInterface.OnClickListener { dialog, which ->
                        val flightFromString = getFlightFromString(flightList!![which])
                        // TODO: set the date according to flight
                        var dayOfTheYear = "000" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                        dayOfTheYear = dayOfTheYear.substring(dayOfTheYear.length - 3, dayOfTheYear.length)
                        val bcbpRawData = "M1FOUNDTHE/EASTEREGG        AAAAAH " + flightFromString.fromAirport + flightFromString.toAirport +
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

    private fun getActivity(): Activity {
        return getActivity()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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
                    Toast.makeText(this, "Non valid Ticket!", Toast.LENGTH_LONG).show()
                    setBackgroundSuccess(false)
                    //postOnSlack(readTicket.passengerName + " had an invalid Ticket")
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
        var departedPage = "https://www.hamburg-airport.de/tools/flightplan/flightplan_detail.php/en/departures/today/"
        val queue = Volley.newRequestQueue(this)

        val req = object : StringRequest(Request.Method.GET, departedPage,
                Response.Listener<String> { response ->
                    //Log.d("Response", response)

                    // parse Flights to Array of Flights
                    cachedFlightList.addAll(FlightFactory.createFlightsFromXMLSource(response))

                    AirportTicketValidatorApplication.getInstance().addToFlightCache(cachedFlightList);

                    val btnScan = findViewById<Button>(R.id.btn_Scan)
                    btnScan.setText(resources.getString(R.string.scan) + "[" + cachedFlightList.size + "]")

                    isProccessed = true;
                    setProgressBar(100)

                }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)
            isProccessed = true;
            Toast.makeText(this@MainActivity, "" + error.message, Toast.LENGTH_SHORT).show()
        }) {}

        queue.add(req)

        return flights
    }

    private fun createBarcodeBitmap(data: String, width: Int, height: Int): Bitmap {
        val writer = MultiFormatWriter()
        val finalData = Uri.encode(data)
        // Use 1 as the height of the matrix as this is a 1D Barcode.
        val bm = writer.encode(finalData, BarcodeFormat.AZTEC, width, height)
        val imageBitmap = Bitmap.createBitmap(width * 2, height * 2, Bitmap.Config.ARGB_8888)

        for (i in 0..(width - 1)) {//width
            for (j in 0..(height - 1)) {//height
                imageBitmap.setPixel(i * 2, j * 2, if (bm.get(i, j)) Color.BLACK else Color.WHITE)
            }
        }
        return imageBitmap
    }

    private fun postOnSlack(message: String) {
        var slackHookPage = "https://hooks.slack.com/services/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"
        val queue = Volley.newRequestQueue(this)

        val req = object : StringRequest(Request.Method.POST, slackHookPage,
                Response.Listener<String> { response ->

                }, Response.ErrorListener { error ->
            VolleyLog.d("Error", "Error: " + error.message)

        }) {

            override fun getBody(): ByteArray {
                var fakeJSON = "{\"text\":\"" + message + "\"}"
                return fakeJSON.toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }

        }

        queue.add(req)
    }

}

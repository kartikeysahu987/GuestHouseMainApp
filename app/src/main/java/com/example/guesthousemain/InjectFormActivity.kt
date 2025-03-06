package com.example.guesthousemain.ui

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.guesthousemain.util.SessionManager

class InjectFormActivity : AppCompatActivity() {

    // Change this to the actual URL of your reservation form
    private val targetUrl = "https://example.com/guest-house-reservation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Load tokens from SessionManager (if not already loaded).
        //    This ensures SessionManager.accessToken and SessionManager.refreshToken are set.
        SessionManager.loadTokens(this)

        // 2) Create a WebView programmatically (no XML).
        val webView = WebView(this)

        // 3) Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            // If the site requires you to store cookies for session-based auth, enable them:
            // setAppCacheEnabled(true) etc. as needed
        }

        // 4) If the site requires cookies (e.g., a session cookie that includes the tokens),
        //    you can set them before loading the URL. For example:
        //    (Only do this if the website actually expects tokens as cookies!)
        val domain = "example.com" // The domain of your site
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setCookie(domain, "accessToken=${SessionManager.accessToken}")
        CookieManager.getInstance().setCookie(domain, "refreshToken=${SessionManager.refreshToken}")
        CookieManager.getInstance().flush()

        // 5) Set a WebViewClient to inject JS after the page loads
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // JavaScript code that fills out and submits the form.
                // Replace the element IDs with those from your actual webpage.
                val jsCode = """
                    (function() {
                        // Fill the fields
                        document.getElementById("name_of_guest").value = "as";
                        document.getElementById("address").value = "sd";
                        document.getElementById("number_of_guests").value = "1";
                        document.getElementById("number_of_rooms").value = "1";
                        
                        document.getElementById("arrival_date").value = "06-03-2025";
                        document.getElementById("arrival_time").value = "18:51";
                        document.getElementById("departure_date").value = "07-03-2025";
                        document.getElementById("departure_time").value = "03:51";
                        
                        document.getElementById("purpose_of_booking").value = "staff";
                        document.getElementById("category").value = "DS";
                        document.getElementById("room_type").value = "Single Occupancy (Rs 3000/- only)";
                        document.getElementById("payment").value = "Paid by guest";

                        // Applicant/Proposer details
                        document.getElementById("applicant_name").value = "c";
                        document.getElementById("designation").value = "c";
                        document.getElementById("department").value = "c";
                        document.getElementById("employee_code").value = "2892zsc";
                        document.getElementById("mobile_number").value = "825679889";
                        document.getElementById("email").value = "2@iitrpr.ac.in";

                        // Finally, submit the form automatically
                        document.querySelector("form").submit();
                    })();
                """.trimIndent()

                view?.evaluateJavascript(jsCode, null)
            }
        }

        // 6) Set the WebView as the Activity's content view
        setContentView(webView)

        // 7) Optionally pass custom headers if the site needs an Authorization header
        //    If the site expects "Bearer <accessToken>", do something like:
        val extraHeaders = mapOf("Authorization" to "Bearer ${SessionManager.accessToken}")

        // 8) Load the target URL with optional headers
        webView.loadUrl(targetUrl, extraHeaders)
    }
}

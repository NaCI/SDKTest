package com.naci.sdktestlibrary

import android.content.Context
import android.util.Log
import com.android.volleyx.Header
import com.android.volley.Response
import com.android.volley.VolleyError
import com.naci.sdktestlibraryconstants.Constants

class SDKStarter {

    companion object {

        private const val TAG = "SDKStarter"

        lateinit var consumeTokenListener: ConsumeTokenListener

        fun startSDKForSubmitConsumer(
            context: Context,
            merchantToken: String?,
            listener: ConsumeTokenListener
        ) {
            val asd = Header("asd", "asd")
            val response = Response.error<VolleyError>(VolleyError())
            Log.d(TAG, "startSDKForSubmitConsumer: END_POINT : ${Constants.END_POINT}")
            consumeTokenListener = listener
            context.startActivity(MainActivity.newInstance(context))
        }

    }

}
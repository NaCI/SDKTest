package com.naci.sdktestlibrary

import android.content.Context

public class SDKStarter {

    companion object {

        lateinit var consumeTokenListener: ConsumeTokenListener

        fun startSDKForSubmitConsumer(
            context: Context,
            merchantToken: String?,
            listener: ConsumeTokenListener
        ) {
            consumeTokenListener = listener
            context.startActivity(MainActivity.newInstance(context))
        }

    }

}
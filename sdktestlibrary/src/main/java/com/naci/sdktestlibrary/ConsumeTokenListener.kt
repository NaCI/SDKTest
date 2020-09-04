package com.naci.sdktestlibrary

interface ConsumeTokenListener {
    fun onSuccess(first6: String, last2: String)
    fun onCancelled()
    fun onFailure(errId: Int, errMessage: String)
}
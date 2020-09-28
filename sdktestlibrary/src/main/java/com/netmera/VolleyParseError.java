package com.netmera;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

class VolleyParseError extends VolleyError {
    VolleyParseError(NetworkResponse response) {
        super(response);
    }
}

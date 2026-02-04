package com.example.autolog_20.ui.theme.data.api

import java.io.IOException

class RefreshTokenFailedException(
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)
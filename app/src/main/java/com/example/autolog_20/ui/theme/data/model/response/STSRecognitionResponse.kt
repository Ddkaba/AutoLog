package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.STSRecognitionData

data class STSRecognitionResponse(
    val success: Boolean,
    val data: STSRecognitionData?,
    val error: String?
)

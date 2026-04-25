package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.BrandAndModel
import com.example.autolog_20.ui.theme.data.model.Manufacturer
import com.example.autolog_20.ui.theme.data.model.VinData
import com.example.autolog_20.ui.theme.data.model.VinReport

data class VinInfoResponse(
    val status: String,
    val vin: VinData,
    val reports: List<VinReport>,
    val manufacturers: List<Manufacturer>,
    val brandsAndModels: List<BrandAndModel>,
    val errors: String?,
    val messages: List<String?>
)
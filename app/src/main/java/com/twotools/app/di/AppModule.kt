package com.twotools.app.di

import com.twotools.app.core.storage.StorageManager
import com.twotools.app.data.datastore.PreferencesManager
import com.twotools.app.features.calculators.dateage.DateAgeViewModel
import com.twotools.app.features.calculators.emi.EmiViewModel
import com.twotools.app.features.calculators.percentage.PercentageViewModel
import com.twotools.app.features.converters.unit.UnitConverterViewModel
import com.twotools.app.features.home.HomeViewModel
import com.twotools.app.features.image.compress.ImageCompressorViewModel
import com.twotools.app.features.image.converter.FormatConverterViewModel
import com.twotools.app.features.image.exif.ExifInspectorViewModel
import com.twotools.app.features.image.resize.ImageResizerViewModel
import com.twotools.app.features.pdf.imagestopdf.ImagesToPdfViewModel
import com.twotools.app.features.pdf.pdftoimages.PdfToImagesViewModel
import com.twotools.app.features.qr.generator.QrGeneratorViewModel
import com.twotools.app.features.qr.scanner.QrScannerViewModel
import com.twotools.app.features.text.caseconverter.CaseConverterViewModel
import com.twotools.app.features.text.hash.HashGeneratorViewModel
import com.twotools.app.features.settings.SettingsViewModel
import com.twotools.app.features.text.inspector.TextInspectorViewModel
import com.twotools.app.features.vault.VaultRepository
import com.twotools.app.features.vault.VaultViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Singletons
    single { PreferencesManager(androidContext()) }
    single { StorageManager(androidContext()) }
    single { VaultRepository(androidContext()) }

    // ViewModels
    viewModel { HomeViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { TextInspectorViewModel() }
    viewModel { CaseConverterViewModel() }
    viewModel { HashGeneratorViewModel() }
    viewModel { PercentageViewModel() }
    viewModel { DateAgeViewModel() }
    viewModel { EmiViewModel() }
    viewModel { UnitConverterViewModel() }
    viewModel { QrGeneratorViewModel(get()) }
    viewModel { QrScannerViewModel() }
    viewModel { ImageCompressorViewModel(get()) }
    viewModel { ImageResizerViewModel(get()) }
    viewModel { FormatConverterViewModel(get()) }
    viewModel { ExifInspectorViewModel(get()) }
    viewModel { ImagesToPdfViewModel(get()) }
    viewModel { PdfToImagesViewModel(get()) }
    viewModel { VaultViewModel(get()) }
}

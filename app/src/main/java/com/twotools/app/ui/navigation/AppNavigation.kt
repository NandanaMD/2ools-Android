package com.twotools.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.twotools.app.features.calculators.dateage.DateAgeCalculatorScreen
import com.twotools.app.features.calculators.dateage.DateAgeViewModel
import com.twotools.app.features.calculators.emi.EmiCalculatorScreen
import com.twotools.app.features.calculators.emi.EmiViewModel
import com.twotools.app.features.calculators.percentage.PercentageCalculatorScreen
import com.twotools.app.features.calculators.percentage.PercentageViewModel
import com.twotools.app.features.converters.unit.UnitConverterScreen
import com.twotools.app.features.converters.unit.UnitConverterViewModel
import com.twotools.app.features.home.HomeScreen
import com.twotools.app.features.home.HomeViewModel
import com.twotools.app.features.image.compress.ImageCompressorScreen
import com.twotools.app.features.image.compress.ImageCompressorViewModel
import com.twotools.app.features.image.converter.FormatConverterScreen
import com.twotools.app.features.image.converter.FormatConverterViewModel
import com.twotools.app.features.image.exif.ExifInspectorScreen
import com.twotools.app.features.image.exif.ExifInspectorViewModel
import com.twotools.app.features.image.resize.ImageResizerScreen
import com.twotools.app.features.image.resize.ImageResizerViewModel
import com.twotools.app.features.pdf.imagestopdf.ImagesToPdfScreen
import com.twotools.app.features.pdf.imagestopdf.ImagesToPdfViewModel
import com.twotools.app.features.pdf.pdftoimages.PdfToImagesScreen
import com.twotools.app.features.pdf.pdftoimages.PdfToImagesViewModel
import com.twotools.app.features.qr.generator.QrGeneratorScreen
import com.twotools.app.features.qr.generator.QrGeneratorViewModel
import com.twotools.app.features.qr.scanner.QrScannerScreen
import com.twotools.app.features.qr.scanner.QrScannerViewModel
import com.twotools.app.features.vault.VaultScreen
import com.twotools.app.features.vault.VaultViewModel
import com.twotools.app.features.text.caseconverter.CaseConverterScreen
import com.twotools.app.features.text.caseconverter.CaseConverterViewModel
import com.twotools.app.features.settings.SettingsScreen
import com.twotools.app.features.settings.SettingsViewModel
import com.twotools.app.features.text.hash.HashGeneratorScreen
import com.twotools.app.features.text.hash.HashGeneratorViewModel
import com.twotools.app.features.text.inspector.TextInspectorScreen
import com.twotools.app.features.text.inspector.TextInspectorViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                targetOffset = { it / 4 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                targetAlpha = 0.5f,
                animationSpec = tween(200)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { -it / 4 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                initialAlpha = 0.5f,
                animationSpec = tween(220)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onToolClick = { tool ->
                    navController.navigate(tool.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = koinViewModel()
            SettingsScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("tool/text_inspector") {
            val vm: TextInspectorViewModel = koinViewModel()
            TextInspectorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/case_converter") {
            val vm: CaseConverterViewModel = koinViewModel()
            CaseConverterScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/hash_generator") {
            val vm: HashGeneratorViewModel = koinViewModel()
            HashGeneratorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/password_vault") {
            val vm: VaultViewModel = koinViewModel()
            VaultScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable("tool/percentage_calc") {
            val vm: PercentageViewModel = koinViewModel()
            PercentageCalculatorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/date_age_calc") {
            val vm: DateAgeViewModel = koinViewModel()
            DateAgeCalculatorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/emi_calc") {
            val vm: EmiViewModel = koinViewModel()
            EmiCalculatorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable("tool/unit_converter") {
            val vm: UnitConverterViewModel = koinViewModel()
            UnitConverterScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable("tool/qr_generator") {
            val vm: QrGeneratorViewModel = koinViewModel()
            QrGeneratorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/qr_scanner") {
            val vm: QrScannerViewModel = koinViewModel()
            QrScannerScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable("tool/image_compressor") {
            val vm: ImageCompressorViewModel = koinViewModel()
            ImageCompressorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/image_resizer") {
            val vm: ImageResizerViewModel = koinViewModel()
            ImageResizerScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/image_converter") {
            val vm: FormatConverterViewModel = koinViewModel()
            FormatConverterScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/exif_inspector") {
            val vm: ExifInspectorViewModel = koinViewModel()
            ExifInspectorScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable("tool/images_to_pdf") {
            val vm: ImagesToPdfViewModel = koinViewModel()
            ImagesToPdfScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
        composable("tool/pdf_to_images") {
            val vm: PdfToImagesViewModel = koinViewModel()
            PdfToImagesScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
    }
}

package com.perksls.inesai

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.perksls.inesai.data.local.AppDatabase
import com.perksls.inesai.data.local.PreferencesManager
import com.perksls.inesai.data.repository.ConversationRepository
import com.perksls.inesai.data.repository.ProviderRepository
import com.perksls.inesai.ui.screens.ApiSetupScreen
import com.perksls.inesai.ui.screens.ChatScreen
import com.perksls.inesai.ui.screens.ProvidersScreen
import com.perksls.inesai.ui.screens.ProviderOrderScreen
import com.perksls.inesai.ui.screens.SettingsScreen
import com.perksls.inesai.ui.theme.InesAITheme
import com.perksls.inesai.ui.viewmodel.ChatViewModel
import com.perksls.inesai.ui.viewmodel.ProviderViewModel

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Chat : Screen("chat")
    object Settings : Screen("settings")
    object Providers : Screen("providers")
    object ProviderOrder : Screen("provider_order")
}

@Composable
fun InesAIApp() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val conversationRepository = remember {
        ConversationRepository(database.conversationDao(), database.messageDao())
    }
    val providerRepository = remember { ProviderRepository(database.providerDao()) }

    val theme by preferencesManager.theme.collectAsState(initial = "system")

    val darkTheme = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    InesAITheme(darkTheme = darkTheme) {
        val navController = rememberNavController()

        // Navega para setup se não houver providers configurados
        // (a lógica de verificação fica no ChatViewModel)
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route
        ) {
            composable(Screen.Setup.route) {
                val providerViewModel: ProviderViewModel = viewModel {
                    ProviderViewModel(providerRepository)
                }
                ApiSetupScreen(
                    providerViewModel = providerViewModel,
                    onSetupComplete = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Chat.route) {
                val chatViewModel: ChatViewModel = viewModel {
                    ChatViewModel(preferencesManager, conversationRepository, providerRepository)
                }
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToProviders = { navController.navigate(Screen.Providers.route) },
                    isDarkTheme = darkTheme
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    preferencesManager = preferencesManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Providers.route) {
                val providerViewModel: ProviderViewModel = viewModel {
                    ProviderViewModel(providerRepository)
                }
                ProvidersScreen(
                    viewModel = providerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrder = { navController.navigate(Screen.ProviderOrder.route) }
                )
            }

            composable(Screen.ProviderOrder.route) {
                val providerViewModel: ProviderViewModel = viewModel {
                    ProviderViewModel(providerRepository)
                }
                ProviderOrderScreen(
                    viewModel = providerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

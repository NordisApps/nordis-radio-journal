package com.nordisapps.nordisradiojournal.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nordisapps.nordisradiojournal.ui.settings.SettingsMenu
import com.nordisapps.nordisradiojournal.data.model.UiState
import com.nordisapps.nordisradiojournal.tools.AdminPanelScreen
import com.nordisapps.nordisradiojournal.tools.EditAnnouncementScreen
import com.nordisapps.nordisradiojournal.tools.EditStationScreen
import com.nordisapps.nordisradiojournal.ui.settings.AboutScreen
import com.nordisapps.nordisradiojournal.ui.settings.PlayerSettingsScreen
import com.nordisapps.nordisradiojournal.ui.theme.ThemeMode
import com.nordisapps.nordisradiojournal.viewmodel.AdminViewModel
import com.nordisapps.nordisradiojournal.viewmodel.AnnouncementsViewModel
import com.nordisapps.nordisradiojournal.viewmodel.FavouritesViewModel
import com.nordisapps.nordisradiojournal.viewmodel.PlayerViewModel
import com.nordisapps.nordisradiojournal.viewmodel.RadioFactsViewModel
import com.nordisapps.nordisradiojournal.viewmodel.RecentlyPlayedViewModel
import com.nordisapps.nordisradiojournal.viewmodel.StationsViewModel

@UnstableApi
@Composable
fun AppNavigation(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    stationsViewModel: StationsViewModel,
    recentlyPlayedViewModel: RecentlyPlayedViewModel,
    favouritesViewModel: FavouritesViewModel,
    adminViewModel: AdminViewModel,
    announcementsViewModel: AnnouncementsViewModel,
    factsViewModel: RadioFactsViewModel,
    uiState: UiState,
    selectedTab: Int,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    context: Context,
    userPhotoUrl: String?,
    userName: String?,
    userEmail: String?,
    isAdmin: Boolean,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAdminPanelClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it })
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it })
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        }
    ) {
        composable("home") {
            MainScreen(
                playerViewModel = playerViewModel,
                stationsViewModel = stationsViewModel,
                recentlyPlayedViewModel = recentlyPlayedViewModel,
                favouritesViewModel = favouritesViewModel,
                announcementsViewModel = announcementsViewModel,
                factsViewModel = factsViewModel,
                selectedTab = selectedTab,
                currentLanguage = currentLanguage,
                userPhotoUrl = userPhotoUrl,
                userName = userName,
                userEmail = userEmail,
                onSignInClick = onSignInClick,
                onSignOutClick = onSignOutClick,
                isAdmin = isAdmin,
                onAdminPanelClick = onAdminPanelClick
            )
        }
        composable("settings") {
            SettingsMenu(
                currentLanguage = currentLanguage,
                onLanguageChange = { langCode ->
                    onLanguageChange(langCode)
                },
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onAboutClick = { navController.navigateSingleTop("about") },
                onPlayerSettingsClick = { navController.navigateSingleTop("player_settings") }
            )
        }
        composable("about") {
            AboutScreen()
        }
        composable("player_settings") {
            PlayerSettingsScreen()
        }
        composable("admin_panel") {
            LaunchedEffect(Unit) {
                announcementsViewModel.loadAllAnnouncementsForAdmin()
            }
            AdminPanelScreen(
                uiState = uiState,
                onDeleteStationClicked = { station ->
                    adminViewModel.deleteStation(
                        station = station,
                        onSuccess = {
                            Toast.makeText(
                                context, "Станция \"${station.name}\" удалена",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "Ошибка удаления: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                onAddStationClicked = {
                    val nextId = (uiState.stations.maxOfOrNull { it.displayId ?: 0 }
                        ?: 0) + 1
                    navController.navigateSingleTop("edit_station_screen?nextDisplayId=$nextId")
                },
                onEditStationClicked = { station ->
                    navController.navigateSingleTop("edit_station_screen?stationId=${station.id}&nextDisplayId=null")
                },
                onAddAnnouncementClicked = {
                    navController.navigateSingleTop("edit_announcement_screen?announcementId=null")
                },
                onEditAnnouncementClicked = { announcement ->
                    navController.navigateSingleTop("edit_announcement_screen?announcementId=${announcement.id}")
                },
                onDeleteAnnouncementClicked = { announcement ->
                    announcementsViewModel.deleteAnnouncement(
                        announcement = announcement,
                        onSuccess = {
                            Toast.makeText(
                                context, "Анонс \"${announcement.title}\" удалён",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "Ошибка удаления: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )
        }
        composable(
            route = "edit_station_screen?stationId={stationId}&nextDisplayId={nextDisplayId}",
            arguments = listOf(
                navArgument("stationId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("nextDisplayId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val stationId = backStackEntry.arguments?.getString("stationId")
            val nextDisplayId =
                backStackEntry.arguments?.getString("nextDisplayId")?.toIntOrNull()
            EditStationScreen(
                stationId = stationId,
                uiState = uiState,
                nextDisplayId = nextDisplayId,
                onSaveStation = { stationToSave ->
                    adminViewModel.saveStation(
                        station = stationToSave,
                        onSuccess = {
                            navController.popBackStack()
                            Toast.makeText(
                                context,
                                "Станция сохранена!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "Ошибка сохранения: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )
        }
        composable(
            route = "edit_announcement_screen?announcementId={announcementId}",
            arguments = listOf(
                navArgument("announcementId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val announcementId = backStackEntry.arguments?.getString("announcementId")
            EditAnnouncementScreen(
                announcementId = announcementId,
                uiState = uiState,
                onSaveAnnouncement = { announcementToSave ->
                    announcementsViewModel.saveAnnouncement(
                        announcement = announcementToSave,
                        onSuccess = {
                            navController.popBackStack()
                            Toast.makeText(
                                context,
                                "Анонс сохранён!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                "Ошибка сохранения: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )
        }
    }
}

fun NavController.navigateSingleTop(route: String) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route) {
            launchSingleTop = true
        }
    }
}
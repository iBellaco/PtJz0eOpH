package com.example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.AppThemeManager
import com.example.util.tr

/** Barra original de Coach, con los colores configurados y el aviso de la pestaña Usuario. */
@Composable
fun OriginalCoachNavBar(items: List<CoachNavItem>, selectedIndex: Int, onSelected: (Int) -> Unit,
                        modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = AppThemeManager.getNavBarBackgroundColor()) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                modifier = Modifier.testTag("coach_nav_item_$index"),
                icon = {
                    if (index == 4 && items.size == 5) UserNotificationIcon(userPanelNotificationCount())
                    else Icon(item.icon, contentDescription = tr(item.title))
                },
                label = { Text(tr(item.title)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppThemeManager.getNavBarSelectedIconColor(),
                    selectedTextColor = AppThemeManager.getNavBarSelectedTextColor(),
                    indicatorColor = AppThemeManager.getNavBarIndicatorColor(),
                    unselectedIconColor = AppThemeManager.getNavBarUnselectedColor(),
                    unselectedTextColor = AppThemeManager.getNavBarUnselectedColor()
                )
            )
        }
    }
}

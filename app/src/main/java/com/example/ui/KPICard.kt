package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * A reusable, highly customizable KPI card component representing operational or financial metrics.
 *
 * @param title The label/description of the metric (e.g. "Today Sales").
 * @param value The value text of the metric (e.g. "৳ 6,50,000").
 * @param targetRoute The route identifier to navigate to upon click.
 * @param modifier Custom modifier for layout adjustments.
 * @param icon The leading vector icon inside the badge.
 * @param iconColor Color applied to the icon and its subtle background container.
 * @param userRole The current logged-in role of the user (e.g. OWNER, ADMIN, STAFF).
 * @param permission Optional permission tag to restrict card visibility based on the user's role.
 * @param loading Boolean to control the rendering of a modern pulsating shimmer/skeleton loader.
 * @param routeParams Optional map of routing parameters for target destination.
 * @param onNavigate Lambda callback executed when card is tapped, passing target route and parameters.
 */
@Composable
fun KPICard(
    title: String,
    value: String,
    targetRoute: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.TrendingUp,
    iconColor: Color = PrimaryBlue,
    userRole: String? = null,
    permission: String? = null,
    loading: Boolean = false,
    routeParams: Map<String, String>? = null,
    onNavigate: (String, Map<String, String>?) -> Unit = { _, _ -> }
) {
    // Permission-based Visibility Check
    val isVisible = remember(userRole, permission) {
        if (permission.isNullOrBlank()) true
        else {
            when (userRole?.uppercase()) {
                "OWNER" -> true
                "ADMIN" -> {
                    // Admin can see everything except OWNER_ONLY metrics
                    permission != "OWNER_ONLY"
                }
                "STAFF" -> {
                    // Staff can only see items explicitly allowed for Staff
                    permission == "STAFF_ALLOWED" || 
                    permission == "VIEW_TASKS" || 
                    permission == "VIEW_SERVICE" || 
                    permission == "VIEW_INVENTORY"
                }
                else -> false
            }
        }
    }

    if (!isVisible) return

    Card(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !loading) {
                onNavigate(targetRoute, routeParams)
            }
            .testTag("kpi_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (loading) {
                // Pulsating Shimmer Skeleton state
                val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(pulseAlpha),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * An overloaded reusable KPICard component that accepts icon as the 3rd positional argument,
 * supporting existing dashboard rows with actions/navigation callbacks.
 */
@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    userRole: String? = null,
    permission: String? = null,
    loading: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Permission-based Visibility Check
    val isVisible = remember(userRole, permission) {
        if (permission.isNullOrBlank()) true
        else {
            when (userRole?.uppercase()) {
                "OWNER" -> true
                "ADMIN" -> {
                    permission != "OWNER_ONLY"
                }
                "STAFF" -> {
                    permission == "STAFF_ALLOWED" || 
                    permission == "VIEW_TASKS" || 
                    permission == "VIEW_SERVICE" || 
                    permission == "VIEW_INVENTORY"
                }
                else -> false
            }
        }
    }

    if (!isVisible) return

    Card(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !loading) {
                onClick()
            }
            .testTag("kpi_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (loading) {
                val infiniteTransition = rememberInfiniteTransition(label = "shimmer_b")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha_b"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(pulseAlpha),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


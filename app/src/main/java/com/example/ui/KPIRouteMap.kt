package com.example.ui

data class KpiRoute(
    val route: String,
    val params: Map<String, String> = emptyMap()
)

object KpiRouteMap {
    val routes = mapOf(
        "today_sales" to KpiRoute(
            route = "/reports/sales",
            params = mapOf("period" to "today")
        ),
        "today_collection" to KpiRoute(
            route = "/reports/collections",
            params = mapOf("period" to "today")
        ),
        "customer_due" to KpiRoute(
            route = "/reports/customer-due"
        ),
        "open_service_tickets" to KpiRoute(
            route = "/service/tickets",
            params = mapOf("status" to "open")
        ),
        "low_stock" to KpiRoute(
            route = "/inventory/products",
            params = mapOf("filter" to "low_stock")
        ),
        "present_today" to KpiRoute(
            route = "/attendance/admin",
            params = mapOf("date" to "today", "status" to "present")
        ),
        "unread_messages" to KpiRoute(
            route = "/messages",
            params = mapOf("filter" to "unread")
        )
    )
}

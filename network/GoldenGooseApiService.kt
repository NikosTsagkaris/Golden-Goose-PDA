package com.ntvelop.goldengoosepda.network

import retrofit2.Response
import retrofit2.http.*

interface GoldenGooseApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @GET("tables")
    suspend fun getTables(): Response<List<TableResponse>>

    @GET("orders/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: String): Response<OrderResponse>

    @POST("tables/{table_id}/orders")
    suspend fun openTable(
        @Path("table_id") tableId: String
    ): Response<OpenTableResponse>

    @GET("orders/open")
    suspend fun getOpenOrders(): Response<List<OrderResponse>>

    @GET("orders/paid")
    suspend fun getPaidOrders(): Response<List<OrderResponse>>

    @POST("orders/{order_id}/lines")
    suspend fun addOrderLine(
        @Path("order_id") orderId: String,
        @Body request: OrderLineCreateRequest
    ): Response<OrderLineResponse>

    @POST("orders/{order_id}/submit")
    suspend fun submitOrder(
        @Path("order_id") orderId: String,
        @Query("note") note: String? = null,
        @Query("waiter_name") waiterName: String? = null,
        @Query("printer_ip") printerIp: String? = null
    ): Response<Map<String, String>>

    @POST("orders/{order_id}/pay")
    suspend fun payOrderLines(
        @Path("order_id") orderId: String,
        @Body request: PayLinesRequest
    ): Response<Map<String, Any>>

    @GET("menu/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @DELETE("orders/{order_id}")
    suspend fun deleteOrder(@Path("order_id") orderId: String): Response<DeleteResponse>

    @GET("shifts/totals")
    suspend fun getShiftTotals(): Response<ShiftTotalsResponse>

    @POST("shifts/end")
    suspend fun endShift(): Response<Map<String, Any>>

    @POST("shifts/print-summary")
    suspend fun printShiftSummary(): Response<Map<String, Any>>

    // Admin endpoints
    @GET("admin/totals/waiters")
    suspend fun getWaiterTotals(): Response<List<WaiterTotalResponse>>

    @GET("admin/logs")
    suspend fun getActionLogs(): Response<List<ActionLogResponse>>

    @DELETE("admin/logs")
    suspend fun clearLogs(): Response<Map<String, Any>>

    @POST("setup/tables/sync")
    suspend fun syncTables(@Body request: TableSyncRequest): Response<Map<String, Any>>

    @GET("pos/options/{group_code}")
    suspend fun getOptions(@Path("group_code") groupCode: Int): Response<List<OptionResponse>>
}

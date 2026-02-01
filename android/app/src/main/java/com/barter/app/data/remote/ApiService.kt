package com.barter.app.data.remote

import com.barter.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== 认证 ==========
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    // ========== 用户 ==========
    @GET("api/users/me")
    suspend fun getMyProfile(): Response<ApiResponse<User>>

    @GET("api/users/{id}")
    suspend fun getProfile(@Path("id") userId: Long): Response<ApiResponse<User>>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun updateAvatar(@Part avatar: MultipartBody.Part): Response<ApiResponse<User>>

    @POST("api/users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @GET("api/users/me/settings")
    suspend fun getSettings(): Response<ApiResponse<UserSettings>>

    @PUT("api/users/me/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<ApiResponse<UserSettings>>

    @GET("api/users/me/login-records")
    suspend fun getLoginRecords(): Response<ApiResponse<List<LoginRecord>>>
    
    @GET("api/users/me/ratings")
    suspend fun getMyRatings(): Response<ApiResponse<List<Rating>>>

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Long): Response<ApiResponse<PublicProfile>>

    @POST("api/users/{id}/rate")
    suspend fun rateUser(
        @Path("id") userId: Long,
        @Body request: RateUserRequest
    ): Response<ApiResponse<UserRatingResponse>>

    @GET("api/users/{id}/ratings")
    suspend fun getUserRatings(
        @Path("id") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<UserRatingResponse>>>

    @GET("api/users/admin")
    suspend fun getAdminUser(): Response<ApiResponse<PublicProfile>>

    // ========== 物品 ==========
    @GET("api/items/list")
    suspend fun getItems(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ItemListItem>>>

    @GET("api/items/search")
    suspend fun searchItems(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ItemListItem>>>

    @GET("api/items/{id}")
    suspend fun getItem(@Path("id") itemId: Long): Response<ApiResponse<Item>>

    @GET("api/items/my")
    suspend fun getMyItems(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ItemListItem>>>

    @GET("api/items/my/available")
    suspend fun getMyAvailableItems(): Response<ApiResponse<List<ItemListItem>>>

    @Multipart
    @POST("api/items")
    suspend fun createItem(
        @Part("item") item: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Response<ApiResponse<Item>>

    @PUT("api/items/{id}")
    suspend fun updateItem(
        @Path("id") itemId: Long,
        @Body request: UpdateItemRequest
    ): Response<ApiResponse<Item>>

    @DELETE("api/items/{id}")
    suspend fun deleteItem(@Path("id") itemId: Long): Response<ApiResponse<Unit>>
    
    @POST("api/items/{id}/wish")
    suspend fun toggleWish(@Path("id") itemId: Long): Response<ApiResponse<WishResponse>>
    
    @GET("api/items/wishes")
    suspend fun getMyWishes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ItemListItem>>>

    // ========== 交换 ==========
    @POST("api/trades")
    suspend fun createTrade(@Body request: CreateTradeRequest): Response<ApiResponse<TradeRequest>>

    @GET("api/trades/{id}")
    suspend fun getTrade(@Path("id") tradeId: Long): Response<ApiResponse<TradeRequest>>

    @PUT("api/trades/{id}/status")
    suspend fun updateTradeStatus(
        @Path("id") tradeId: Long,
        @Body request: UpdateTradeStatusRequest
    ): Response<ApiResponse<TradeRequest>>

    @GET("api/trades/sent")
    suspend fun getSentTrades(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<TradeRequest>>>

    @GET("api/trades/received")
    suspend fun getReceivedTrades(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<TradeRequest>>>

    // ========== 聊天 ==========
    @POST("api/chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResponse<Message>>

    @GET("api/chat/conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<Conversation>>>

    @GET("api/chat/conversations/{id}")
    suspend fun getConversationDetail(
        @Path("id") conversationId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<ConversationDetail>>

    // ========== 评价 ==========
    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<Review>>

    @GET("api/reviews/user/{userId}")
    suspend fun getUserReviews(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<Review>>>

    // ========== 管理员 ==========
    @GET("api/admin/config")
    suspend fun getAdminConfig(): Response<ApiResponse<SystemConfigResponse>>

    @POST("api/admin/config/allow-user-chat")
    suspend fun setAllowUserChat(@Body request: AllowRequest): Response<ApiResponse<Unit>>

    @POST("api/admin/config/allow-user-view-items")
    suspend fun setAllowUserViewItems(@Body request: AllowRequest): Response<ApiResponse<Unit>>
}

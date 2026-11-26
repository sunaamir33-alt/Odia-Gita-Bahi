package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BaserowApiService {

    @GET("api/database/rows/table/{tableId}/")
    suspend fun getSongsPage(
        @Path("tableId") tableId: String,
        @Query("user_field_names") userFieldNames: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 200
    ): BaserowPageResponse

    @GET("api/database/rows/table/{tableId}/")
    suspend fun searchSongs(
        @Path("tableId") tableId: String,
        @Query("user_field_names") userFieldNames: Boolean = true,
        @Query("search") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): BaserowPageResponse
}

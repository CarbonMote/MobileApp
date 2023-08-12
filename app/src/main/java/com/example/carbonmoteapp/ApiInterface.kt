package com.example.carbonmoteapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiInterface {
    @GET("api/cru-data-table")
    fun getData(@Query("id") id: Int?): Call<List<DataDAO>>

    @POST("api/cru-data-table")
    fun postData(@Body data: DataDAO): Call<DataDAO>

    @PUT("api/cru-data-table")
    fun putData(@Body data: DataDAO): Call<DataDAO>
}

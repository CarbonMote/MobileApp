package com.example.carbonmoteapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {
    @GET("api/crud")
    fun getData(@Query("id") id: Int?): Call<List<DataDAO>>

    @POST("api/crud")
    fun postData(@Body data: DataDAO): Call<DataDAO>
}

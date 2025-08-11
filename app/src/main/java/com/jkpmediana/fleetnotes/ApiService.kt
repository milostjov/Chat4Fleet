
package com.jkpmediana.fleetnotes

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("wp-json/konto/v1/list")
    fun getKonta(): Call<List<Konto>>

    @GET("wp-json/konto/v1/comments/{brojKonta}")
    fun getKomentari(
        @Path("brojKonta", encoded = false) brojKonta: String
    ): Call<List<Komentar>>


    @DELETE("wp-json/konto/v1/delete/{id}")
    fun deleteComment(@Path("id") id: Int): Call<Void>

    @POST("wp-json/konto/v1/add")
    fun addComment(@Body newComment: NewComment): Call<ApiResponse>

    @PUT("wp-json/konto/v1/edit/{id}")
    fun editComment(
        @Path("id") id: Int,
        @Body updatedComment: UpdatedComment
    ): Call<ApiResponse>
}

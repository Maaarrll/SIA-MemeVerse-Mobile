package com.example.memeverseapp.network;


import com.example.memeverseapp.models.ApiResponse;
import com.example.memeverseapp.models.CommentBody;
import com.example.memeverseapp.models.CommentDeleteBody;
import com.example.memeverseapp.models.CommentsResponse;
import com.example.memeverseapp.models.ConversationsResponse;
import com.example.memeverseapp.models.LoginResponse;
import com.example.memeverseapp.models.MessagesResponse;
import com.example.memeverseapp.models.NotificationsResponse;
import com.example.memeverseapp.models.PostDetailResponse;
import com.example.memeverseapp.models.PostsResponse;
import com.example.memeverseapp.models.ReportBody;
import com.example.memeverseapp.models.SearchResponse;
import com.example.memeverseapp.models.SendMessageBody;
import com.example.memeverseapp.models.UnreadCountResponse;
import com.example.memeverseapp.models.UserPostsResponse;
import com.example.memeverseapp.models.UserResponse;
import com.example.memeverseapp.models.VoteBody;
import com.example.memeverseapp.models.VoteResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @FormUrlEncoded
    @POST("api/login.php")
    Call<LoginResponse> login(
            @Field("login") String login,
            @Field("password") String password
    );

    @POST("api/logout.php")
    Call<ApiResponse> logout();

    @FormUrlEncoded
    @POST("api/register.php")
    Call<ApiResponse> register(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("confirm_password") String confirm
    );

    // Posts
    @GET("api/posts.php")
    Call<PostsResponse> getPosts(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("api/posts.php")
    Call<UserPostsResponse> getUserPosts(
            @Query("user_id") int userId
    );

    @GET("api/posts.php")
    Call<PostsResponse> getPostsByCategory(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category_slug") String categorySlug
    );

    @GET("api/post.php")
    Call<PostDetailResponse> getPost(
            @Query("id") int postId
    );

    @POST("api/vote.php")
    Call<VoteResponse> vote(
            @Body VoteBody body
    );

    @FormUrlEncoded
    @POST("api/edit_post.php")
    Call<ApiResponse> editPost(
            @Field("post_id") int postId,
            @Field("title") String title,
            @Field("description") String description,
            @Field("category_id") int categoryId
    );

    @FormUrlEncoded
    @POST("api/delete_post.php")
    Call<ApiResponse> deletePost(
            @Field("post_id") int postId
    );

    // Comments
    @GET("api/comments.php")
    Call<CommentsResponse> getComments(
            @Query("post_id") int postId
    );

    @POST("api/comments.php")
    Call<ApiResponse> addComment(
            @Body CommentBody body
    );

    @POST("api/delete_comment.php")
    Call<ApiResponse> deleteComment(
            @Body CommentDeleteBody body
    );

    // Messages
    @GET("api/get_conversations.php")
    Call<ConversationsResponse> getConversations(
            @retrofit2.http.Header("X-User-Id") int userId
    );

    @GET("api/get_messages.php")
    Call<MessagesResponse> getMessages(
            @retrofit2.http.Header("X-User-Id") int userId,
            @Query("with") int withUserId
    );

    @POST("api/send_message.php")
    Call<ApiResponse> sendMessage(
            @retrofit2.http.Header("X-User-Id") int userId,
            @Body SendMessageBody body
    );

    @FormUrlEncoded
    @POST("api/delete_conversation.php")
    Call<ApiResponse> deleteConversation(
            @retrofit2.http.Header("X-User-Id") int userId,
            @Field("user_id") int otherUserId
    );

    // Notifications
    @GET("api/latest_notifications.php")
    Call<NotificationsResponse> getLatestNotifications();

    @POST("api/mark_all_notifications_read.php")
    Call<ApiResponse> markAllNotificationsRead();

    @GET("api/unread_notifications.php")
    Call<UnreadCountResponse> getUnreadNotifications();

    @GET("api/unread_messages.php")
    Call<UnreadCountResponse> getUnreadMessages();

    // Profile
    @GET("api/get_user.php")
    Call<UserResponse> getUser(
            @Query("id") int userId
    );

    @Multipart
    @POST("api/update_profile.php")
    Call<ApiResponse> updateProfile(
            @Part("nickname") String nickname,
            @Part("bio") String bio,
            @Part MultipartBody.Part avatar
    );

    // Follow / Report
    @FormUrlEncoded
    @POST("api/follow.php")
    Call<ApiResponse> follow(
            @Field("user_id") int userId,
            @Field("action") String action
    );

    @POST("api/report.php")
    Call<ApiResponse> report(
            @Body ReportBody body
    );

    // Search
    @GET("api/search.php")
    Call<SearchResponse> search(
            @Query("q") String query
    );

    // Upload
    @Multipart
    @POST("api/upload.php")
    Call<ApiResponse> uploadPost(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("category_id") RequestBody categoryId,
            @Part MultipartBody.Part image
    );
}
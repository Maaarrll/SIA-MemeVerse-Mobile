package com.example.memeverseapp.network;

import com.example.memeverseapp.models.PostsResponse;

import com.example.memeverseapp.models.*;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
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

    @GET("api/get_post.php")
    Call<PostDetailResponse> getPost(
            @Query("id") int postId
    );

    @GET("api/user_posts.php")
    Call<UserPostsResponse> getUserPosts(
            @Query("user_id") int userId
    );

    @FormUrlEncoded
    @POST("api/vote.php")
    Call<VoteResponse> vote(
            @Field("post_id") int postId,
            @Field("vote") String vote
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
    @FormUrlEncoded
    @POST("api/comment.php")
    Call<ApiResponse> addComment(
            @Field("post_id") int postId,
            @Field("comment") String comment,
            @Field("parent_id") int parentId
    );

    @HTTP(method = "DELETE", path = "api/comment.php", hasBody = true)
    Call<ApiResponse> deleteComment(
            @Body CommentDeleteBody body
    );

    // Messages
    @GET("api/get_conversations.php")
    Call<ConversationsResponse> getConversations();

    @GET("api/get_messages.php")
    Call<MessagesResponse> getMessages(
            @Query("with") int withUserId
    );

    @POST("api/send_message.php")
    Call<ApiResponse> sendMessage(
            @Body SendMessageBody body
    );

    @FormUrlEncoded
    @POST("api/delete_conversation.php")
    Call<ApiResponse> deleteConversation(
            @Field("user_id") int userId
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
            @Part("title") okhttp3.RequestBody title,
            @Part("description") okhttp3.RequestBody description,
            @Part("category_id") okhttp3.RequestBody categoryId,
            @Part MultipartBody.Part image
    );
}
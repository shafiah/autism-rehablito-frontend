
package com.autismcentre.api;

import com.autismcentre.model.AdminAssignedTherapistResponse;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminChildCreateRequest;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.model.AdminChildUpdateRequest;
import com.autismcentre.model.AdminCommunicationMessageResponse;
import com.autismcentre.model.AdminCommunicationSummaryResponse;
import com.autismcentre.model.AdminCreateNotificationRequest;
import com.autismcentre.model.AdminFeedbackResponse;
import com.autismcentre.model.AdminHomeworkResponse;
import com.autismcentre.model.AdminLoginRequest;
import com.autismcentre.model.AdminLoginResponse;
import com.autismcentre.model.AdminMessageRequest;
import com.autismcentre.model.AdminNotificationCampaignResponse;
import com.autismcentre.model.AdminNotificationRecipientResponse;
import com.autismcentre.model.AdminParentOptionResponse;
import com.autismcentre.model.AdminPaymentResponse;
import com.autismcentre.model.AdminPaymentSummaryResponse;
import com.autismcentre.model.AdminReportResponse;
import com.autismcentre.model.AdminReportReviewRequest;
import com.autismcentre.model.AdminScheduleNotificationRequest;
import com.autismcentre.model.NotificationResponse;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentChildTherapistResponse;
import com.autismcentre.model.ParentHomeworkResponse;
import com.autismcentre.model.ParentLoginRequest;
import com.autismcentre.model.ParentLoginResponse;
import com.autismcentre.model.ParentPaymentDetailsResponse;
import com.autismcentre.model.ParentRegisterRequest;
import com.autismcentre.model.ParentRegisterResponse;
import com.autismcentre.model.AdminTherapistCreateRequest;
import com.autismcentre.model.AdminTherapistCreateResponse;
import com.autismcentre.model.AdminTherapistResponse;
import com.autismcentre.model.ParentReportResponse;
import com.autismcentre.model.ParentHomeworkRemarkRequest;
import com.autismcentre.model.ParentSessionResponse;
import com.autismcentre.model.ProgressRecordResponse;
import com.autismcentre.model.TherapistAssignedChildResponse;
import com.autismcentre.model.TherapistChildResponse;
import com.autismcentre.model.TherapistFeedbackResponse;
import com.autismcentre.model.TherapistHomeworkCreateRequest;
import com.autismcentre.model.TherapistHomeworkRemarksRequest;
import com.autismcentre.model.TherapistLoginRequest;
import com.autismcentre.model.TherapistLoginResponse;

import com.autismcentre.model.AdminChildTherapistResponse;

import com.autismcentre.model.AdminSessionCreateRequest;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.model.AdminSessionUpdateRequest;
import com.autismcentre.model.TherapistNoteRequest;
import com.autismcentre.model.TherapistNoteResponse;
import com.autismcentre.model.TherapistProgressRequest;
import com.autismcentre.model.TherapistReportCreateRequest;
import com.autismcentre.model.TherapistReportResponse;
import com.autismcentre.model.TherapistSessionResponse;
import com.autismcentre.model.TherapistHomeworkResponse;
import com.autismcentre.model.TherapistHomeworkStatusUpdateRequest;
import com.autismcentre.model.TherapistHomeworkUpdateRequest;
import com.autismcentre.model.TherapistMessageRequest;
import com.autismcentre.model.TherapistMessageResponse;
import com.autismcentre.model.TherapistUnreadMessageResponse;
import com.autismcentre.model.ParentMessageRequest;
import com.autismcentre.model.ParentMessageResponse;
import com.autismcentre.model.ParentMessageReadResponse;
import com.autismcentre.model.ParentUnreadMessageResponse;

import com.autismcentre.model.ParentCreatePaymentRequest;
import com.autismcentre.model.ParentCreatePaymentResponse;
import com.autismcentre.model.ParentPaymentOptionResponse;
import com.autismcentre.model.ParentPaymentSummaryResponse;
import com.autismcentre.model.ParentVerifyPaymentRequest;
import com.autismcentre.model.ParentVerifyPaymentResponse;
import com.autismcentre.model.ParentFeedbackRequest;
import com.autismcentre.model.ParentFeedbackResponse;

import com.autismcentre.model.ParentForgotPasswordRequest;
import com.autismcentre.model.ParentForgotPasswordResponse;
import com.autismcentre.model.ParentVerifyOtpRequest;
import com.autismcentre.model.ParentVerifyOtpResponse;
import com.autismcentre.model.ParentResetPasswordRequest;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import retrofit2.http.Query;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface ApiService {

    // Parent Register API
    @POST("api/parent/auth/register")
    Call<ParentRegisterResponse> registerParent(
            @Body ParentRegisterRequest request
    );

    // Parent Login API
    @POST("api/parent/auth/login")
    Call<ParentLoginResponse> loginParent(
            @Body ParentLoginRequest request
    );


    // ==============================
    // Parent Forgot Password APIs
    // ==============================

    @POST("api/parent/auth/forgot-password")
    Call<ParentForgotPasswordResponse> forgotParentPassword(
            @Body ParentForgotPasswordRequest request
    );

    @POST("api/parent/auth/verify-otp")
    Call<ParentVerifyOtpResponse> verifyParentOtp(
            @Body ParentVerifyOtpRequest request
    );

    @POST("api/parent/auth/reset-password")
    Call<Void> resetParentPassword(
            @Body ParentResetPasswordRequest request
    );


    // Get children linked to the authenticated parent
    @GET("api/parent/children")
    Call<List<ParentChildResponse>> getMyChildren();


    // ==============================
    // Parent Schedule / Session APIs
    // ==============================

    // Get all sessions for a specific child
    @GET("api/parent/children/{childId}/sessions")
    Call<List<ParentSessionResponse>> getMyChildSessions(
            @Path("childId") Long childId
    );

    // Get details of a specific session belonging to a child
    @GET("api/parent/children/{childId}/sessions/{sessionId}")
    Call<ParentSessionResponse> getMyChildSessionById(
            @Path("childId") Long childId,
            @Path("sessionId") Long sessionId
    );

    // ==============================
// Parent Progress API
// ==============================

    // Get progress records for a child linked to the logged-in parent
    @GET("api/parent/children/{childId}/progress")
    Call<List<ProgressRecordResponse>> getMyChildProgress(
            @Path("childId") Long childId
    );


    // ==============================
    // Parent Reports APIs
    // ==============================

    // Get published reports for a child linked to the logged-in parent
    @GET("api/parent/children/{childId}/reports")
    Call<List<ParentReportResponse>> getMyChildReports(
            @Path("childId") Long childId
    );

    // Get details of one published report
    @GET("api/parent/children/{childId}/reports/{reportId}")
    Call<ParentReportResponse> getMyChildReportById(
            @Path("childId") Long childId,
            @Path("reportId") Long reportId
    );



    // Get a specific child profile belonging to the authenticated parent
    @GET("api/parent/children/{childId}")
    Call<ParentChildResponse> getMyChildById(
            @Path("childId") Long childId
    );

    // Get therapists assigned to a child belonging to the authenticated parent
    @GET("api/parent/children/{childId}/therapists")
    Call<List<ParentChildTherapistResponse>> getMyChildTherapists(
            @Path("childId") Long childId
    );


    // ==============================
    // Parent Homework APIs
    // ==============================

    // Get homework assigned to a child linked to the logged-in parent
    @GET("api/parent/children/{childId}/homework")
    Call<List<ParentHomeworkResponse>> getMyChildHomework(
            @Path("childId") Long childId
    );

    // Get details of a specific homework
    @GET("api/parent/children/{childId}/homework/{homeworkId}")
    Call<ParentHomeworkResponse> getMyChildHomeworkById(
            @Path("childId") Long childId,
            @Path("homeworkId") Long homeworkId
    );

    // Add or update parent remarks for homework
    @PUT("api/parent/children/{childId}/homework/{homeworkId}/remark")
    Call<ParentHomeworkResponse> updateMyChildHomeworkRemark(
            @Path("childId") Long childId,
            @Path("homeworkId") Long homeworkId,
            @Body ParentHomeworkRemarkRequest request
    );


    @POST("api/admin/auth/login")
    Call<AdminLoginResponse> loginAdmin(
            @Body AdminLoginRequest request
    );

    @GET("api/admin/branches")
    Call<List<AdminBranchResponse>> getAdminBranches();

    @GET("api/admin/parents")
    Call<List<AdminParentOptionResponse>> getAdminParents();

    // Get active parents registered under selected branch
    @GET("api/admin/children/parents")
    Call<List<AdminParentOptionResponse>> getAdminParentsByBranch(
            @Query("branchId") Long branchId
    );

    @POST("api/admin/children")
    Call<AdminChildResponse> createAdminChild(
            @Body AdminChildCreateRequest request
    );

    // Upload optional child profile image
    @Multipart
    @POST("api/admin/children/{childId}/image")
    Call<AdminChildResponse> uploadAdminChildImage(
            @Path("childId") Long childId,
            @Part MultipartBody.Part image
    );

    // Get all children for Admin Child Management
    @GET("api/admin/children")
    Call<List<AdminChildResponse>> getAdminChildren();

    @GET("api/admin/children/{childId}")
    Call<AdminChildResponse> getAdminChildById(
            @Path("childId") Long childId
    );

    // Admin creates a therapist account
    @POST("api/admin/therapists")
    Call<AdminTherapistCreateResponse> createAdminTherapist(
            @Body AdminTherapistCreateRequest request
    );

    // Get all therapists or filter by branch
    @GET("api/admin/therapists")
    Call<List<AdminTherapistResponse>> getAdminTherapists(
            @retrofit2.http.Query("branchId") Long branchId
    );



    // Therapist Login API
    @POST("api/therapist/auth/login")
    Call<TherapistLoginResponse> loginTherapist(
            @Body TherapistLoginRequest request
    );

    @GET("api/admin/child-therapists/therapists/{therapistId}/children")
    Call<List<TherapistAssignedChildResponse>> getTherapistAssignedChildren(
            @Path("therapistId") Long therapistId
    );

    // Get therapists assigned to a child
    @GET("api/admin/child-therapists/children/{childId}/therapists")
    Call<List<AdminChildTherapistResponse>> getTherapistsByChild(
            @Path("childId") Long childId
    );

    // Assign a therapist to a child
    @POST("api/admin/child-therapists/children/{childId}/therapists/{therapistId}")
    Call<AdminChildTherapistResponse> assignTherapistToChild(
            @Path("childId") Long childId,
            @Path("therapistId") Long therapistId
    );

    // Unassign a therapist from a child
    @PUT("api/admin/child-therapists/{assignmentId}/unassign")
    Call<AdminChildTherapistResponse> unassignTherapistFromChild(
            @Path("assignmentId") Long assignmentId
    );

    // Update child details
    @PUT("api/admin/children/{childId}")
    Call<AdminChildResponse> updateAdminChild(
            @Path("childId") Long childId,
            @Body AdminChildUpdateRequest request
    );

    // Activate child
    @PUT("api/admin/children/{childId}/activate")
    Call<AdminChildResponse> activateAdminChild(
            @Path("childId") Long childId
    );

    // Deactivate child
    @PUT("api/admin/children/{childId}/deactivate")
    Call<AdminChildResponse> deactivateAdminChild(
            @Path("childId") Long childId
    );

    // ==============================
    // Admin Session APIs
    // ==============================

    // Create a new session
    @POST("api/admin/sessions")
    Call<AdminSessionResponse> createAdminSession(
            @Body AdminSessionCreateRequest request
    );

    // Get all sessions, optionally filtered by branch
    @GET("api/admin/sessions")
    Call<List<AdminSessionResponse>> getAdminSessions(
            @Query("branchId") Long branchId
    );

    // Get session details by ID
    @GET("api/admin/sessions/{sessionId}")
    Call<AdminSessionResponse> getAdminSessionById(
            @Path("sessionId") Long sessionId
    );

    // Update a scheduled session
    @PUT("api/admin/sessions/{sessionId}")
    Call<AdminSessionResponse> updateAdminSession(
            @Path("sessionId") Long sessionId,
            @Body AdminSessionUpdateRequest request
    );

    // Cancel a scheduled session
    @PUT("api/admin/sessions/{sessionId}/cancel")
    Call<AdminSessionResponse> cancelAdminSession(
            @Path("sessionId") Long sessionId
    );


    // ==============================
    // Therapist Session APIs
    // ==============================

    // Get sessions assigned to the logged-in therapist
    @GET("api/therapist/sessions")
    Call<List<TherapistSessionResponse>> getMyTherapistSessions();

    // Get details of an assigned session
    @GET("api/therapist/sessions/{sessionId}")
    Call<TherapistSessionResponse> getMyTherapistSessionById(
            @Path("sessionId") Long sessionId
    );

    // Start a scheduled session
    @PUT("api/therapist/sessions/{sessionId}/start")
    Call<TherapistSessionResponse> startTherapistSession(
            @Path("sessionId") Long sessionId
    );

    // Complete an in-progress session
    @PUT("api/therapist/sessions/{sessionId}/complete")
    Call<TherapistSessionResponse> completeTherapistSession(
            @Path("sessionId") Long sessionId
    );

    @GET("api/therapist/children")
    Call<List<TherapistChildResponse>> getMyTherapistChildren();

    @GET("api/therapist/children/{childId}")
    Call<TherapistChildResponse> getMyTherapistChildById(
            @Path("childId") Long childId
    );


    // ==============================
    // Therapist Progress APIs
    // ==============================

    // Save therapy progress for a session
    @POST("api/therapist/progress/sessions/{sessionId}")
    Call<com.autismcentre.model.TherapistProgressResponse> createTherapistProgress(
            @Path("sessionId") Long sessionId,
            @Body TherapistProgressRequest request
    );


    // ==============================
    // Therapist Reports APIs
    // ==============================

    // Create a draft report for a child
    @POST("api/therapist/reports/children/{childId}")
    Call<TherapistReportResponse> createTherapistReport(
            @Path("childId") Long childId,
            @Body TherapistReportCreateRequest request
    );

    // Submit an existing draft report
    @PUT("api/therapist/reports/{reportId}/submit")
    Call<TherapistReportResponse> submitTherapistReport(
            @Path("reportId") Long reportId
    );

    // Get reports belonging to the logged-in therapist
    @GET("api/therapist/reports")
    Call<List<TherapistReportResponse>> getMyTherapistReports();


    // Get progress history for a child assigned to the logged-in therapist
    @GET("api/therapist/progress/children/{childId}")
    Call<List<com.autismcentre.model.TherapistProgressResponse>>
    getTherapistChildProgress(
            @Path("childId") Long childId
    );

    // ==============================
// Therapist Note APIs
// ==============================

    // Save a note for a session
    @POST("api/therapist/notes/sessions/{sessionId}")
    Call<TherapistNoteResponse> createTherapistNote(
            @Path("sessionId") Long sessionId,
            @Body TherapistNoteRequest request
    );

    // Get saved notes for a child
    @GET("api/therapist/notes/children/{childId}")
    Call<List<TherapistNoteResponse>> getTherapistChildNotes(
            @Path("childId") Long childId
    );


    // ==============================
    // Admin Therapy Report APIs
    // ==============================

    // Get reports filtered by status
    @GET("api/admin/reports")
    Call<List<AdminReportResponse>> getAdminReports(
            @Query("status") String status
    );

    // Review a submitted report with admin remarks
    @PUT("api/admin/reports/{reportId}/review")
    Call<AdminReportResponse> reviewAdminReport(
            @Path("reportId") Long reportId,
            @Body AdminReportReviewRequest request
    );

    // Publish a reviewed report
    @PUT("api/admin/reports/{reportId}/publish")
    Call<AdminReportResponse> publishAdminReport(
            @Path("reportId") Long reportId
    );

    // ==============================
    // Therapist Homework APIs
    // ==============================

    // Create homework for an assigned child
    @POST("api/therapist/homework/children/{childId}")
    Call<TherapistHomeworkResponse> createTherapistHomework(
            @Path("childId") Long childId,
            @Body TherapistHomeworkCreateRequest request
    );

    // Get homework assigned by the logged-in therapist
    @GET("api/therapist/homework")
    Call<List<TherapistHomeworkResponse>> getMyTherapistHomework();

    // Get homework details by ID
    @GET("api/therapist/homework/{homeworkId}")
    Call<TherapistHomeworkResponse> getMyTherapistHomeworkById(
            @Path("homeworkId") Long homeworkId
    );

    // Update homework status
    @PUT("api/therapist/homework/{homeworkId}/status")
    Call<TherapistHomeworkResponse> updateTherapistHomeworkStatus(
            @Path("homeworkId") Long homeworkId,
            @Body TherapistHomeworkStatusUpdateRequest request
    );


    // Update existing therapist homework
    @PUT("api/therapist/homework/{homeworkId}")
    Call<TherapistHomeworkResponse> updateTherapistHomework(
            @Path("homeworkId") Long homeworkId,
            @Body TherapistHomeworkUpdateRequest request
    );


    // Update therapist remarks for homework
    @PUT("api/therapist/homework/{homeworkId}/remarks")
    Call<TherapistHomeworkResponse> updateTherapistHomeworkRemarks(
            @Path("homeworkId") Long homeworkId,
            @Body TherapistHomeworkRemarksRequest request
    );


    // Delete existing therapist homework
    @DELETE("api/therapist/homework/{homeworkId}")
    Call<Void> deleteTherapistHomework(
            @Path("homeworkId") Long homeworkId
    );


    // Admin: Get homework list with optional filters
    @GET("api/admin/homework")
    Call<List<AdminHomeworkResponse>> getAdminHomework(
            @Query("branchId") Long branchId,
            @Query("childId") Long childId,
            @Query("status") String status,
            @Query("therapyType") String therapyType
    );

    // Admin: Get homework details by ID
    @GET("api/admin/homework/{homeworkId}")
    Call<AdminHomeworkResponse> getAdminHomeworkById(
            @Path("homeworkId") Long homeworkId
    );


    // ==============================
    // Therapist Parent Communication APIs
    // ==============================

    // Send a message to the parent of an assigned child
    @POST("api/therapist/communication/children/{childId}/messages")
    Call<TherapistMessageResponse> sendTherapistMessage(
            @Path("childId") Long childId,
            @Body TherapistMessageRequest request
    );

    // Get conversation history for an assigned child
    @GET("api/therapist/communication/children/{childId}/messages")
    Call<List<TherapistMessageResponse>> getTherapistConversation(
            @Path("childId") Long childId
    );

    // Mark a received parent message as read
    @PUT("api/therapist/communication/children/{childId}/messages/{messageId}/read")
    Call<com.google.gson.JsonObject> markTherapistMessageAsRead(
            @Path("childId") Long childId,
            @Path("messageId") Long messageId
    );

    // Get unread message count
    @GET("api/therapist/communication/children/{childId}/messages/unread-count")
    Call<TherapistUnreadMessageResponse> getTherapistUnreadMessageCount(
            @Path("childId") Long childId
    );

    // Admin Parent Communication Summary
    @GET("api/admin/communication")
    Call<List<AdminCommunicationSummaryResponse>>
    getAdminCommunicationSummaries();

    // Admin: Get conversation messages for a child
    @GET("api/admin/communication/children/{childId}/messages")
    Call<List<AdminCommunicationMessageResponse>> getAdminChildConversation(
            @Path("childId") Long childId
    );

    // Admin: Send a message to the parent or assigned therapist
    @POST("api/admin/communication/children/{childId}/messages")
    Call<AdminCommunicationMessageResponse> sendAdminCommunicationMessage(
            @Path("childId") Long childId,
            @Body AdminMessageRequest request
    );

    // Admin: Get active therapists assigned to a child
    @GET("api/admin/communication/children/{childId}/therapists")
    Call<List<AdminAssignedTherapistResponse>> getAdminAssignedTherapists(
            @Path("childId") Long childId
    );

    @GET("api/admin/communication/children/{childId}/therapists/{therapistUserId}/messages")
    Call<List<AdminCommunicationMessageResponse>> getAdminTherapistConversation(
            @Path("childId") Long childId,
            @Path("therapistUserId") Long therapistUserId
    );

    // ==============================
// Parent Therapist Communication APIs
// ==============================

    // Send message to a selected assigned therapist
    @POST("api/parent/communication/children/{childId}/therapists/{therapistId}/messages")
    Call<ParentMessageResponse> sendParentMessage(
            @Path("childId") Long childId,
            @Path("therapistId") Long therapistId,
            @Body ParentMessageRequest request
    );

    // Get conversation with a selected therapist
    @GET("api/parent/communication/children/{childId}/therapists/{therapistId}/messages")
    Call<List<ParentMessageResponse>> getParentConversation(
            @Path("childId") Long childId,
            @Path("therapistId") Long therapistId
    );

    // Mark therapist message as read
    @PUT("api/parent/communication/children/{childId}/therapists/{therapistId}/messages/{messageId}/read")
    Call<ParentMessageReadResponse> markParentMessageAsRead(
            @Path("childId") Long childId,
            @Path("therapistId") Long therapistId,
            @Path("messageId") Long messageId
    );

    // Get unread messages count from selected therapist
    @GET("api/parent/communication/children/{childId}/therapists/{therapistId}/messages/unread-count")
    Call<ParentUnreadMessageResponse> getParentUnreadMessageCount(
            @Path("childId") Long childId,
            @Path("therapistId") Long therapistId
    );

    // ==============================
    // Parent Payment / Razorpay APIs
    // ==============================

    // Get available therapy payment options for a child
    @GET("api/parent/payments/children/{childId}/options")
    Call<List<ParentPaymentOptionResponse>> getParentPaymentOptions(
            @Path("childId") Long childId
    );

    // Get payment summary for selected therapy
    @GET("api/parent/payments/children/{childId}/summary")
    Call<ParentPaymentSummaryResponse> getParentPaymentSummary(
            @Path("childId") Long childId,
            @Query("therapyType") String therapyType
    );

    // Create Razorpay order
    @POST("api/parent/payments/create-order")
    Call<ParentCreatePaymentResponse> createParentPaymentOrder(
            @Body ParentCreatePaymentRequest request
    );

    // Verify Razorpay payment
    @POST("api/parent/payments/verify")
    Call<ParentVerifyPaymentResponse> verifyParentPayment(
            @Body ParentVerifyPaymentRequest request
    );

    @GET("api/parent/payments/history")
    Call<List<ParentPaymentDetailsResponse>> getParentPaymentHistory();

    @GET("api/parent/payments/{paymentId}/receipt")
    @Streaming
    Call<ResponseBody> getParentPaymentReceipt(
            @Path("paymentId") Long paymentId
    );

    // Admin Payment Summary - All Branches
    @GET("api/admin/payments/summary")
    Call<AdminPaymentSummaryResponse> getAdminPaymentSummary();

    // Admin Payment Summary - Specific Branch
    @GET("api/admin/payments/branch/{branchId}/summary")
    Call<AdminPaymentSummaryResponse> getAdminPaymentSummaryByBranch(
            @Path("branchId") Long branchId
    );

    // ==============================
    // Admin Payment Management APIs
    // ==============================

    // Get all successful payments
    @GET("api/admin/payments/paid")
    Call<List<AdminPaymentResponse>> getAdminPaidPayments();

    // Get all non-successful payments
    @GET("api/admin/payments/unpaid")
    Call<List<AdminPaymentResponse>> getAdminUnpaidPayments();

    // Get successful payments by branch
    @GET("api/admin/payments/branch/{branchId}/paid")
    Call<List<AdminPaymentResponse>> getAdminPaidPaymentsByBranch(
            @Path("branchId") Long branchId
    );

    // Get non-successful payments by branch
    @GET("api/admin/payments/branch/{branchId}/unpaid")
    Call<List<AdminPaymentResponse>> getAdminUnpaidPaymentsByBranch(
            @Path("branchId") Long branchId
    );


    // Admin: Get campaign history with optional filters
    @GET("api/admin/notifications/campaigns")
    Call<List<AdminNotificationCampaignResponse>>
    getAdminNotificationCampaigns(
            @Query("branchId") Long branchId,
            @Query("status") String status
    );


    // Admin: Get all recipients of a notification campaign
    @GET("api/admin/notifications/campaigns/{campaignId}/recipients")
    Call<List<AdminNotificationRecipientResponse>>
    getAdminNotificationCampaignRecipients(
            @Path("campaignId") Long campaignId
    );

    // Admin: Cancel a scheduled notification campaign
    @DELETE("api/admin/notifications/schedule/{campaignId}")
    Call<Void> cancelAdminScheduledNotification(
            @Path("campaignId") Long campaignId
    );

    // Admin: Send notification immediately
    @POST("api/admin/notifications")
    Call<ResponseBody> createAdminNotification(
            @Body AdminCreateNotificationRequest request
    );

    // Admin: Schedule notification
    @POST("api/admin/notifications/schedule")
    Call<ResponseBody> scheduleAdminNotification(
            @Body AdminScheduleNotificationRequest request
    );


    // ==============================
    // Parent Notification APIs
    // ==============================

    // Get all notifications of logged-in parent
    @GET("api/parent/notifications")
    Call<List<NotificationResponse>> getParentNotifications();

    // Get unread notification count
    @GET("api/parent/notifications/unread-count")
    Call<com.google.gson.JsonObject> getParentUnreadNotificationCount();

    // Get a single notification
    @GET("api/parent/notifications/{notificationId}")
    Call<NotificationResponse> getParentNotificationById(
            @Path("notificationId") Long notificationId
    );

    // Mark a notification as read
    @PUT("api/parent/notifications/{notificationId}/read")
    Call<com.google.gson.JsonObject> markParentNotificationAsRead(
            @Path("notificationId") Long notificationId
    );

    // Mark all notifications as read
    @PUT("api/parent/notifications/read-all")
    Call<Void> markAllParentNotificationsAsRead();

    // ==============================
// Therapist Notification APIs
// ==============================

    // Get all notifications of logged-in therapist
    @GET("api/therapist/notifications")
    Call<List<NotificationResponse>> getTherapistNotifications();

    // Get unread notification count
    @GET("api/therapist/notifications/unread-count")
    Call<com.google.gson.JsonObject> getTherapistUnreadNotificationCount();

    // Get a single notification
    @GET("api/therapist/notifications/{notificationId}")
    Call<NotificationResponse> getTherapistNotificationById(
            @Path("notificationId") Long notificationId
    );

    // Mark a notification as read
    @PUT("api/therapist/notifications/{notificationId}/read")
    Call<com.google.gson.JsonObject> markTherapistNotificationAsRead(
            @Path("notificationId") Long notificationId
    );

    // Mark all notifications as read
    @PUT("api/therapist/notifications/read-all")
    Call<Void> markAllTherapistNotificationsAsRead();

    // ==============================
// Admin Notification APIs
// ==============================

    // ==============================
// Admin Notification APIs
// ==============================

    // Get all notifications of logged-in Admin
    @GET("api/admin/notifications")
    Call<List<NotificationResponse>> getAdminNotifications();

    // Get unread notification count
    @GET("api/admin/notifications/unread-count")
    Call<com.google.gson.JsonObject> getAdminUnreadNotificationCount();

    // Get a single notification
    @GET("api/admin/notifications/{notificationId}")
    Call<NotificationResponse> getAdminNotificationById(
            @Path("notificationId") Long notificationId
    );

    // Mark a notification as read
    @PUT("api/admin/notifications/{notificationId}/read")
    Call<com.google.gson.JsonObject> markAdminNotificationAsRead(
            @Path("notificationId") Long notificationId
    );

    // Mark all notifications as read
    @PUT("api/admin/notifications/read-all")
    Call<Void> markAllAdminNotificationsAsRead();


    // ==============================
    // Parent Feedback APIs
    // ==============================

    // Submit feedback for an assigned therapist
    @POST("api/parent/feedback")
    Call<ParentFeedbackResponse> submitParentFeedback(
            @Body ParentFeedbackRequest request
    );

    // Get feedback history of logged-in parent
    @GET("api/parent/feedback")
    Call<List<ParentFeedbackResponse>> getParentFeedbackHistory();

    // ==============================
// Admin Feedback APIs
// ==============================

    // Get all parent feedback
    @GET("api/admin/feedback")
    Call<List<AdminFeedbackResponse>> getAdminFeedback();

    // Publish submitted parent feedback
    @PUT("api/admin/feedback/{feedbackId}/publish")
    Call<AdminFeedbackResponse> publishAdminFeedback(
            @Path("feedbackId") Long feedbackId
    );

    // ==============================
// Therapist Feedback API
// ==============================

    // Get published feedback for logged-in therapist
    @GET("api/therapist/feedback")
    Call<List<TherapistFeedbackResponse>> getMyTherapistFeedback();

    // Download authenticated child profile image
    @GET("api/parent/children/{childId}/image")
    Call<ResponseBody> getMyChildImage(
            @Path("childId") Long childId
    );

    @GET("api/admin/children/{childId}/image")
    Call<ResponseBody> getAdminChildImage(
            @Path("childId") Long childId
    );


}

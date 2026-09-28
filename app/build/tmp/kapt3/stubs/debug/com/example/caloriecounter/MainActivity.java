package com.example.caloriecounter;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00da\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u000e\u0018\u0000 v2\u00020\u0001:\u0002vwB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010F\u001a\u00020GH\u0002J\b\u0010H\u001a\u00020GH\u0002J\b\u0010I\u001a\u00020GH\u0002J\b\u0010J\u001a\u00020GH\u0002J\b\u0010K\u001a\u00020GH\u0002J\b\u0010L\u001a\u00020GH\u0002J\u0012\u0010M\u001a\u0004\u0018\u00010N2\u0006\u0010O\u001a\u00020PH\u0002J\b\u0010Q\u001a\u00020GH\u0002J\b\u0010R\u001a\u00020GH\u0002J\u0010\u0010S\u001a\u00020\u00192\u0006\u0010T\u001a\u00020/H\u0002J\u001a\u0010U\u001a\u0004\u0018\u00010V2\u0006\u0010W\u001a\u00020X2\u0006\u0010T\u001a\u00020/H\u0002J\"\u0010Y\u001a\u0004\u0018\u00010V2\u0006\u0010W\u001a\u00020X2\u0006\u0010T\u001a\u00020/2\u0006\u0010Z\u001a\u00020\u0019H\u0002J\u0012\u0010[\u001a\u00020G2\b\u0010\\\u001a\u0004\u0018\u00010]H\u0014J\b\u0010^\u001a\u00020GH\u0014J-\u0010_\u001a\u00020G2\u0006\u0010`\u001a\u00020\u00192\u000e\u0010a\u001a\n\u0012\u0006\b\u0001\u0012\u00020/0b2\u0006\u0010c\u001a\u00020dH\u0016\u00a2\u0006\u0002\u0010eJ\b\u0010f\u001a\u00020GH\u0003J \u0010g\u001a\u00020\u00172\u0006\u0010h\u001a\u00020/2\u0006\u0010i\u001a\u00020j2\u0006\u0010k\u001a\u00020\bH\u0002J\u0016\u0010l\u001a\u00020G2\f\u0010m\u001a\b\u0012\u0004\u0012\u0002010%H\u0002J\u001a\u0010n\u001a\u00020G2\u0006\u0010O\u001a\u00020P2\b\u0010o\u001a\u0004\u0018\u00010/H\u0002J\b\u0010p\u001a\u00020GH\u0002J\u0010\u0010q\u001a\u00020\b2\u0006\u0010r\u001a\u00020\u001eH\u0002J\b\u0010s\u001a\u00020GH\u0002J\b\u0010t\u001a\u00020GH\u0002J\f\u0010u\u001a\u00020\u0019*\u00020\u0019H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010#\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00150%X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010'\u001a\u00020(8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b+\u0010,\u001a\u0004\b)\u0010*R\u000e\u0010-\u001a\u00020\u001bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010.\u001a\u0004\u0018\u00010/X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u00100\u001a\b\u0012\u0004\u0012\u0002010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00102\u001a\u0004\u0018\u000103X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00104\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u00105\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u00106\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u00107\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u00108\u001a\u00020\u001bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u00109\u001a\u0004\u0018\u00010:X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010;\u001a\u0004\u0018\u00010<X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010=\u001a\u00020>X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010?\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010@\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010A\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00190%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010C\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010D\u001a\u00020\u0019X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010E\u001a\u00020\u0017X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006x"}, d2 = {"Lcom/example/caloriecounter/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "bottomNav", "Lcom/google/android/material/bottomnavigation/BottomNavigationView;", "cameraDevice", "Landroid/hardware/camera2/CameraDevice;", "cameraEnabled", "", "cameraHandler", "Landroid/os/Handler;", "cameraPlaceholder", "Landroid/widget/Button;", "cameraSession", "Landroid/hardware/camera2/CameraCaptureSession;", "cameraTexture", "Landroid/view/TextureView;", "cameraThread", "Landroid/os/HandlerThread;", "captureButton", "carbsDot", "Landroid/view/View;", "carbsLabel", "Landroid/widget/TextView;", "carbsValue", "", "chartContainer", "Landroid/widget/LinearLayout;", "chartTitle", "currentPage", "Lcom/example/caloriecounter/MainActivity$Page;", "dailyLimit", "fatDot", "fatLabel", "fatValue", "historyContentContainer", "homeContentViews", "", "isDarkTheme", "mealRepository", "Lcom/example/caloriecounter/data/MealRepository;", "getMealRepository", "()Lcom/example/caloriecounter/data/MealRepository;", "mealRepository$delegate", "Lkotlin/Lazy;", "pageContainer", "pendingPhotoNote", "", "photoHistory", "Lcom/example/caloriecounter/data/PhotoHistoryEntity;", "photoImageReader", "Landroid/media/ImageReader;", "proteinDot", "proteinLabel", "proteinValue", "remainingValue", "rootLayout", "scanDetailsEditText", "Lcom/google/android/material/textfield/TextInputEditText;", "scanDetailsInputLayout", "Lcom/google/android/material/textfield/TextInputLayout;", "themeToggle", "Landroid/widget/ImageButton;", "titleText", "todayConsumed", "todayValue", "weekCalories", "weekValue", "yesterdayConsumed", "yesterdayValue", "buildChart", "", "buildHistoryPage", "buildScanPage", "buildSettingsPage", "capturePhoto", "closeCamera", "decodeOrientedPhoto", "Landroid/graphics/Bitmap;", "imageBytes", "", "disableCameraForNote", "enableCameraPreview", "getJpegOrientation", "cameraId", "getJpegSize", "Landroid/util/Size;", "manager", "Landroid/hardware/camera2/CameraManager;", "getPreviewSize", "viewWidth", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "openCamera", "pageText", "text", "size", "", "bold", "renderPhotoHistory", "photos", "saveCapturedPhoto", "note", "showCameraUnavailable", "showPage", "page", "updateDashboard", "updateTheme", "dp", "Companion", "Page", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private final int dailyLimit = 2200;
    private final int todayConsumed = 1545;
    private final int yesterdayConsumed = 1880;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.Integer> weekCalories = null;
    private final int proteinValue = 78;
    private final int carbsValue = 190;
    private final int fatValue = 60;
    private boolean isDarkTheme = false;
    private android.widget.LinearLayout rootLayout;
    private android.widget.TextView titleText;
    private android.widget.TextView chartTitle;
    private android.widget.ImageButton themeToggle;
    private android.widget.TextView todayValue;
    private android.widget.TextView yesterdayValue;
    private android.widget.TextView weekValue;
    private android.widget.TextView remainingValue;
    private android.widget.LinearLayout chartContainer;
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;
    private android.view.View proteinDot;
    private android.view.View carbsDot;
    private android.view.View fatDot;
    private android.widget.TextView proteinLabel;
    private android.widget.TextView carbsLabel;
    private android.widget.TextView fatLabel;
    private android.widget.LinearLayout pageContainer;
    private java.util.List<? extends android.view.View> homeContentViews;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.camera2.CameraDevice cameraDevice;
    @org.jetbrains.annotations.Nullable()
    private android.hardware.camera2.CameraCaptureSession cameraSession;
    @org.jetbrains.annotations.Nullable()
    private android.os.HandlerThread cameraThread;
    @org.jetbrains.annotations.Nullable()
    private android.os.Handler cameraHandler;
    @org.jetbrains.annotations.Nullable()
    private android.view.TextureView cameraTexture;
    @org.jetbrains.annotations.Nullable()
    private android.media.ImageReader photoImageReader;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.material.textfield.TextInputLayout scanDetailsInputLayout;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.material.textfield.TextInputEditText scanDetailsEditText;
    @org.jetbrains.annotations.Nullable()
    private android.widget.Button cameraPlaceholder;
    @org.jetbrains.annotations.Nullable()
    private android.widget.Button captureButton;
    @org.jetbrains.annotations.Nullable()
    private android.widget.LinearLayout historyContentContainer;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.example.caloriecounter.data.PhotoHistoryEntity> photoHistory;
    private boolean cameraEnabled = true;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String pendingPhotoNote;
    @org.jetbrains.annotations.NotNull()
    private com.example.caloriecounter.MainActivity.Page currentPage = com.example.caloriecounter.MainActivity.Page.HOME;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy mealRepository$delegate = null;
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.caloriecounter.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super();
    }
    
    private final com.example.caloriecounter.data.MealRepository getMealRepository() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final boolean showPage(com.example.caloriecounter.MainActivity.Page page) {
        return false;
    }
    
    private final void buildScanPage() {
    }
    
    private final void buildHistoryPage() {
    }
    
    private final void renderPhotoHistory(java.util.List<com.example.caloriecounter.data.PhotoHistoryEntity> photos) {
    }
    
    private final void buildSettingsPage() {
    }
    
    private final android.widget.TextView pageText(java.lang.String text, float size, boolean bold) {
        return null;
    }
    
    private final android.graphics.Bitmap decodeOrientedPhoto(byte[] imageBytes) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    private final void openCamera() {
    }
    
    private final android.util.Size getJpegSize(android.hardware.camera2.CameraManager manager, java.lang.String cameraId) {
        return null;
    }
    
    private final void capturePhoto() {
    }
    
    private final void saveCapturedPhoto(byte[] imageBytes, java.lang.String note) {
    }
    
    private final int getJpegOrientation(java.lang.String cameraId) {
        return 0;
    }
    
    private final void disableCameraForNote() {
    }
    
    private final void enableCameraPreview() {
    }
    
    private final void showCameraUnavailable() {
    }
    
    private final android.util.Size getPreviewSize(android.hardware.camera2.CameraManager manager, java.lang.String cameraId, int viewWidth) {
        return null;
    }
    
    private final void closeCamera() {
    }
    
    private final void updateDashboard() {
    }
    
    private final void updateTheme() {
    }
    
    private final void buildChart() {
    }
    
    private final int dp(int $this$dp) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/example/caloriecounter/MainActivity$Companion;", "", "()V", "CAMERA_PERMISSION_REQUEST", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/example/caloriecounter/MainActivity$Page;", "", "(Ljava/lang/String;I)V", "HOME", "SCAN", "HISTORY", "SETTINGS", "app_debug"})
    static enum Page {
        /*public static final*/ HOME /* = new HOME() */,
        /*public static final*/ SCAN /* = new SCAN() */,
        /*public static final*/ HISTORY /* = new HISTORY() */,
        /*public static final*/ SETTINGS /* = new SETTINGS() */;
        
        Page() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.example.caloriecounter.MainActivity.Page> getEntries() {
            return null;
        }
    }
}
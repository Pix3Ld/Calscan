package com.example.caloriecounter;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010#\u001a\u00020$H\u0002J\u0012\u0010%\u001a\u00020$2\b\u0010&\u001a\u0004\u0018\u00010'H\u0014J\b\u0010(\u001a\u00020$H\u0002J\b\u0010)\u001a\u00020$H\u0002J\f\u0010*\u001a\u00020\n*\u00020\nH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\n0\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/example/caloriecounter/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "bottomNav", "Lcom/google/android/material/bottomnavigation/BottomNavigationView;", "carbsDot", "Landroid/view/View;", "carbsLabel", "Landroid/widget/TextView;", "carbsValue", "", "chartContainer", "Landroid/widget/LinearLayout;", "chartTitle", "dailyLimit", "fatDot", "fatLabel", "fatValue", "isDarkTheme", "", "proteinDot", "proteinLabel", "proteinValue", "remainingValue", "rootLayout", "themeToggle", "Landroid/widget/ImageButton;", "titleText", "todayConsumed", "todayValue", "weekCalories", "", "weekValue", "yesterdayConsumed", "yesterdayValue", "buildChart", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "updateDashboard", "updateTheme", "dp", "app_release"})
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
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
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
}
package com.example.caloriecounter.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\t\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00130\u0012H'J\u0016\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\f\u00a8\u0006\u0015"}, d2 = {"Lcom/example/caloriecounter/data/MealDao;", "", "deleteSummary", "", "summaryId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getJob", "Lcom/example/caloriecounter/data/AnalysisJobEntity;", "jobId", "insertJob", "job", "(Lcom/example/caloriecounter/data/AnalysisJobEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertSummary", "summary", "Lcom/example/caloriecounter/data/MealSummaryEntity;", "(Lcom/example/caloriecounter/data/MealSummaryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeSummaries", "Lkotlinx/coroutines/flow/Flow;", "", "updateJob", "app_debug"})
@androidx.room.Dao()
public abstract interface MealDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertJob(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.AnalysisJobEntity job, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM analysis_jobs WHERE id = :jobId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getJob(long jobId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.caloriecounter.data.AnalysisJobEntity> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateJob(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.AnalysisJobEntity job, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertSummary(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.MealSummaryEntity summary, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM meal_summaries ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.caloriecounter.data.MealSummaryEntity>> observeSummaries();
    
    @androidx.room.Query(value = "DELETE FROM meal_summaries WHERE id = :summaryId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSummary(long summaryId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}
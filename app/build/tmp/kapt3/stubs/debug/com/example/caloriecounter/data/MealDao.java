package com.example.caloriecounter.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00052\u0006\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0014\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00190\u0018H'J\u0014\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00190\u0018H'J\u0016\u0010\u001b\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u001c"}, d2 = {"Lcom/example/caloriecounter/data/MealDao;", "", "deletePhotoHistory", "", "photoId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSummary", "summaryId", "getJob", "Lcom/example/caloriecounter/data/AnalysisJobEntity;", "jobId", "insertJob", "job", "(Lcom/example/caloriecounter/data/AnalysisJobEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertPhotoHistory", "photo", "Lcom/example/caloriecounter/data/PhotoHistoryEntity;", "(Lcom/example/caloriecounter/data/PhotoHistoryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertSummary", "summary", "Lcom/example/caloriecounter/data/MealSummaryEntity;", "(Lcom/example/caloriecounter/data/MealSummaryEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observePhotoHistory", "Lkotlinx/coroutines/flow/Flow;", "", "observeSummaries", "updateJob", "app_debug"})
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
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertPhotoHistory(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.PhotoHistoryEntity photo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM photo_history ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.caloriecounter.data.PhotoHistoryEntity>> observePhotoHistory();
    
    @androidx.room.Query(value = "DELETE FROM photo_history WHERE id = :photoId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePhotoHistory(long photoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}
package com.example.caloriecounter.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0012\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00160\u0015J\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00160\u0015J \u0010\u001a\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0012H\u0086@\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/example/caloriecounter/data/MealRepository;", "", "dao", "Lcom/example/caloriecounter/data/MealDao;", "(Lcom/example/caloriecounter/data/MealDao;)V", "deletePhoto", "", "photoId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSummary", "summaryId", "enqueuePhoto", "imageBytes", "", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enqueueText", "text", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observePhotoHistory", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/caloriecounter/data/PhotoHistoryEntity;", "observeSummaries", "Lcom/example/caloriecounter/data/MealSummaryEntity;", "savePhoto", "note", "([BLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MealRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.caloriecounter.data.MealDao dao = null;
    
    public MealRepository(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.MealDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.caloriecounter.data.MealSummaryEntity>> observeSummaries() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.caloriecounter.data.PhotoHistoryEntity>> observePhotoHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object savePhoto(@org.jetbrains.annotations.NotNull()
    byte[] imageBytes, @org.jetbrains.annotations.Nullable()
    java.lang.String note, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deletePhoto(long photoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object enqueueText(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object enqueuePhoto(@org.jetbrains.annotations.NotNull()
    byte[] imageBytes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteSummary(long summaryId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}
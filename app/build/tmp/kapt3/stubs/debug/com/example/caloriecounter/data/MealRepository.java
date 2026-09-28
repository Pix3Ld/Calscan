package com.example.caloriecounter.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/example/caloriecounter/data/MealRepository;", "", "dao", "Lcom/example/caloriecounter/data/MealDao;", "(Lcom/example/caloriecounter/data/MealDao;)V", "deleteSummary", "", "summaryId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enqueuePhoto", "imageBytes", "", "([BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enqueueText", "text", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeSummaries", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/caloriecounter/data/MealSummaryEntity;", "app_debug"})
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
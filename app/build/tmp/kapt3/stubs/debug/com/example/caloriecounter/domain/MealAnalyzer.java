package com.example.caloriecounter.domain;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/example/caloriecounter/domain/MealAnalyzer;", "", "analyze", "Lcom/example/caloriecounter/domain/AnalysisResult;", "input", "Lcom/example/caloriecounter/domain/AnalysisInput;", "(Lcom/example/caloriecounter/domain/AnalysisInput;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface MealAnalyzer {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object analyze(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.domain.AnalysisInput input, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.caloriecounter.domain.AnalysisResult> $completion);
}
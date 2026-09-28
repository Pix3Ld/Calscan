package com.example.caloriecounter.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0004H\u0007J\u0010\u0010\n\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\u000b"}, d2 = {"Lcom/example/caloriecounter/data/DatabaseConverters;", "", "()V", "inputTypeToString", "", "value", "Lcom/example/caloriecounter/data/InputType;", "jobStatusToString", "Lcom/example/caloriecounter/data/JobStatus;", "stringToInputType", "stringToJobStatus", "app_debug"})
public final class DatabaseConverters {
    
    public DatabaseConverters() {
        super();
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String inputTypeToString(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.InputType value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final com.example.caloriecounter.data.InputType stringToInputType(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String jobStatusToString(@org.jetbrains.annotations.NotNull()
    com.example.caloriecounter.data.JobStatus value) {
        return null;
    }
    
    @androidx.room.TypeConverter()
    @org.jetbrains.annotations.NotNull()
    public final com.example.caloriecounter.data.JobStatus stringToJobStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
}
package com.example.caloriecounter.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MealDao_Impl implements MealDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AnalysisJobEntity> __insertionAdapterOfAnalysisJobEntity;

  private final DatabaseConverters __databaseConverters = new DatabaseConverters();

  private final EntityInsertionAdapter<MealSummaryEntity> __insertionAdapterOfMealSummaryEntity;

  private final EntityInsertionAdapter<PhotoHistoryEntity> __insertionAdapterOfPhotoHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<AnalysisJobEntity> __updateAdapterOfAnalysisJobEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSummary;

  private final SharedSQLiteStatement __preparedStmtOfDeletePhotoHistory;

  public MealDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAnalysisJobEntity = new EntityInsertionAdapter<AnalysisJobEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `analysis_jobs` (`id`,`inputType`,`textInput`,`imageBytes`,`status`,`errorMessage`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AnalysisJobEntity entity) {
        statement.bindLong(1, entity.getId());
        final String _tmp = __databaseConverters.inputTypeToString(entity.getInputType());
        if (_tmp == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, _tmp);
        }
        if (entity.getTextInput() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTextInput());
        }
        if (entity.getImageBytes() == null) {
          statement.bindNull(4);
        } else {
          statement.bindBlob(4, entity.getImageBytes());
        }
        final String _tmp_1 = __databaseConverters.jobStatusToString(entity.getStatus());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getErrorMessage());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
      }
    };
    this.__insertionAdapterOfMealSummaryEntity = new EntityInsertionAdapter<MealSummaryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `meal_summaries` (`id`,`jobId`,`title`,`caloriesKcal`,`proteinGrams`,`carbohydratesGrams`,`fatGrams`,`fiberGrams`,`vitaminsJson`,`imageBytes`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MealSummaryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getJobId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        statement.bindLong(4, entity.getCaloriesKcal());
        statement.bindDouble(5, entity.getProteinGrams());
        statement.bindDouble(6, entity.getCarbohydratesGrams());
        statement.bindDouble(7, entity.getFatGrams());
        statement.bindDouble(8, entity.getFiberGrams());
        if (entity.getVitaminsJson() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getVitaminsJson());
        }
        if (entity.getImageBytes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindBlob(10, entity.getImageBytes());
        }
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfPhotoHistoryEntity = new EntityInsertionAdapter<PhotoHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `photo_history` (`id`,`imageBytes`,`note`,`createdAt`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PhotoHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getImageBytes() == null) {
          statement.bindNull(2);
        } else {
          statement.bindBlob(2, entity.getImageBytes());
        }
        if (entity.getNote() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNote());
        }
        statement.bindLong(4, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfAnalysisJobEntity = new EntityDeletionOrUpdateAdapter<AnalysisJobEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `analysis_jobs` SET `id` = ?,`inputType` = ?,`textInput` = ?,`imageBytes` = ?,`status` = ?,`errorMessage` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AnalysisJobEntity entity) {
        statement.bindLong(1, entity.getId());
        final String _tmp = __databaseConverters.inputTypeToString(entity.getInputType());
        if (_tmp == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, _tmp);
        }
        if (entity.getTextInput() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTextInput());
        }
        if (entity.getImageBytes() == null) {
          statement.bindNull(4);
        } else {
          statement.bindBlob(4, entity.getImageBytes());
        }
        final String _tmp_1 = __databaseConverters.jobStatusToString(entity.getStatus());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getErrorMessage());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteSummary = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM meal_summaries WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePhotoHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM photo_history WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertJob(final AnalysisJobEntity job,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAnalysisJobEntity.insertAndReturnId(job);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSummary(final MealSummaryEntity summary,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMealSummaryEntity.insert(summary);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPhotoHistory(final PhotoHistoryEntity photo,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPhotoHistoryEntity.insertAndReturnId(photo);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateJob(final AnalysisJobEntity job,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAnalysisJobEntity.handle(job);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSummary(final long summaryId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSummary.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, summaryId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSummary.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePhotoHistory(final long photoId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePhotoHistory.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, photoId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePhotoHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getJob(final long jobId,
      final Continuation<? super AnalysisJobEntity> $completion) {
    final String _sql = "SELECT * FROM analysis_jobs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, jobId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AnalysisJobEntity>() {
      @Override
      @Nullable
      public AnalysisJobEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInputType = CursorUtil.getColumnIndexOrThrow(_cursor, "inputType");
          final int _cursorIndexOfTextInput = CursorUtil.getColumnIndexOrThrow(_cursor, "textInput");
          final int _cursorIndexOfImageBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBytes");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final AnalysisJobEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final InputType _tmpInputType;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfInputType)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfInputType);
            }
            _tmpInputType = __databaseConverters.stringToInputType(_tmp);
            final String _tmpTextInput;
            if (_cursor.isNull(_cursorIndexOfTextInput)) {
              _tmpTextInput = null;
            } else {
              _tmpTextInput = _cursor.getString(_cursorIndexOfTextInput);
            }
            final byte[] _tmpImageBytes;
            if (_cursor.isNull(_cursorIndexOfImageBytes)) {
              _tmpImageBytes = null;
            } else {
              _tmpImageBytes = _cursor.getBlob(_cursorIndexOfImageBytes);
            }
            final JobStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            _tmpStatus = __databaseConverters.stringToJobStatus(_tmp_1);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new AnalysisJobEntity(_tmpId,_tmpInputType,_tmpTextInput,_tmpImageBytes,_tmpStatus,_tmpErrorMessage,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MealSummaryEntity>> observeSummaries() {
    final String _sql = "SELECT * FROM meal_summaries ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meal_summaries"}, new Callable<List<MealSummaryEntity>>() {
      @Override
      @NonNull
      public List<MealSummaryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfJobId = CursorUtil.getColumnIndexOrThrow(_cursor, "jobId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCaloriesKcal = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesKcal");
          final int _cursorIndexOfProteinGrams = CursorUtil.getColumnIndexOrThrow(_cursor, "proteinGrams");
          final int _cursorIndexOfCarbohydratesGrams = CursorUtil.getColumnIndexOrThrow(_cursor, "carbohydratesGrams");
          final int _cursorIndexOfFatGrams = CursorUtil.getColumnIndexOrThrow(_cursor, "fatGrams");
          final int _cursorIndexOfFiberGrams = CursorUtil.getColumnIndexOrThrow(_cursor, "fiberGrams");
          final int _cursorIndexOfVitaminsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "vitaminsJson");
          final int _cursorIndexOfImageBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBytes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MealSummaryEntity> _result = new ArrayList<MealSummaryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealSummaryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpJobId;
            _tmpJobId = _cursor.getLong(_cursorIndexOfJobId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final int _tmpCaloriesKcal;
            _tmpCaloriesKcal = _cursor.getInt(_cursorIndexOfCaloriesKcal);
            final float _tmpProteinGrams;
            _tmpProteinGrams = _cursor.getFloat(_cursorIndexOfProteinGrams);
            final float _tmpCarbohydratesGrams;
            _tmpCarbohydratesGrams = _cursor.getFloat(_cursorIndexOfCarbohydratesGrams);
            final float _tmpFatGrams;
            _tmpFatGrams = _cursor.getFloat(_cursorIndexOfFatGrams);
            final float _tmpFiberGrams;
            _tmpFiberGrams = _cursor.getFloat(_cursorIndexOfFiberGrams);
            final String _tmpVitaminsJson;
            if (_cursor.isNull(_cursorIndexOfVitaminsJson)) {
              _tmpVitaminsJson = null;
            } else {
              _tmpVitaminsJson = _cursor.getString(_cursorIndexOfVitaminsJson);
            }
            final byte[] _tmpImageBytes;
            if (_cursor.isNull(_cursorIndexOfImageBytes)) {
              _tmpImageBytes = null;
            } else {
              _tmpImageBytes = _cursor.getBlob(_cursorIndexOfImageBytes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MealSummaryEntity(_tmpId,_tmpJobId,_tmpTitle,_tmpCaloriesKcal,_tmpProteinGrams,_tmpCarbohydratesGrams,_tmpFatGrams,_tmpFiberGrams,_tmpVitaminsJson,_tmpImageBytes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PhotoHistoryEntity>> observePhotoHistory() {
    final String _sql = "SELECT * FROM photo_history ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"photo_history"}, new Callable<List<PhotoHistoryEntity>>() {
      @Override
      @NonNull
      public List<PhotoHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfImageBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "imageBytes");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PhotoHistoryEntity> _result = new ArrayList<PhotoHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PhotoHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final byte[] _tmpImageBytes;
            if (_cursor.isNull(_cursorIndexOfImageBytes)) {
              _tmpImageBytes = null;
            } else {
              _tmpImageBytes = _cursor.getBlob(_cursorIndexOfImageBytes);
            }
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PhotoHistoryEntity(_tmpId,_tmpImageBytes,_tmpNote,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

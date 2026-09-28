package com.example.caloriecounter.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MealDao _mealDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `analysis_jobs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `inputType` TEXT NOT NULL, `textInput` TEXT, `imageBytes` BLOB, `status` TEXT NOT NULL, `errorMessage` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `meal_summaries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `jobId` INTEGER NOT NULL, `title` TEXT NOT NULL, `caloriesKcal` INTEGER NOT NULL, `proteinGrams` REAL NOT NULL, `carbohydratesGrams` REAL NOT NULL, `fatGrams` REAL NOT NULL, `fiberGrams` REAL NOT NULL, `vitaminsJson` TEXT NOT NULL, `imageBytes` BLOB, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `photo_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `imageBytes` BLOB NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '06646c42317793e08d0f81a12b33c27e')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `analysis_jobs`");
        db.execSQL("DROP TABLE IF EXISTS `meal_summaries`");
        db.execSQL("DROP TABLE IF EXISTS `photo_history`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAnalysisJobs = new HashMap<String, TableInfo.Column>(8);
        _columnsAnalysisJobs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("inputType", new TableInfo.Column("inputType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("textInput", new TableInfo.Column("textInput", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("imageBytes", new TableInfo.Column("imageBytes", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAnalysisJobs.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAnalysisJobs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAnalysisJobs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAnalysisJobs = new TableInfo("analysis_jobs", _columnsAnalysisJobs, _foreignKeysAnalysisJobs, _indicesAnalysisJobs);
        final TableInfo _existingAnalysisJobs = TableInfo.read(db, "analysis_jobs");
        if (!_infoAnalysisJobs.equals(_existingAnalysisJobs)) {
          return new RoomOpenHelper.ValidationResult(false, "analysis_jobs(com.example.caloriecounter.data.AnalysisJobEntity).\n"
                  + " Expected:\n" + _infoAnalysisJobs + "\n"
                  + " Found:\n" + _existingAnalysisJobs);
        }
        final HashMap<String, TableInfo.Column> _columnsMealSummaries = new HashMap<String, TableInfo.Column>(11);
        _columnsMealSummaries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("jobId", new TableInfo.Column("jobId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("caloriesKcal", new TableInfo.Column("caloriesKcal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("proteinGrams", new TableInfo.Column("proteinGrams", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("carbohydratesGrams", new TableInfo.Column("carbohydratesGrams", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("fatGrams", new TableInfo.Column("fatGrams", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("fiberGrams", new TableInfo.Column("fiberGrams", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("vitaminsJson", new TableInfo.Column("vitaminsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("imageBytes", new TableInfo.Column("imageBytes", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMealSummaries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMealSummaries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMealSummaries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMealSummaries = new TableInfo("meal_summaries", _columnsMealSummaries, _foreignKeysMealSummaries, _indicesMealSummaries);
        final TableInfo _existingMealSummaries = TableInfo.read(db, "meal_summaries");
        if (!_infoMealSummaries.equals(_existingMealSummaries)) {
          return new RoomOpenHelper.ValidationResult(false, "meal_summaries(com.example.caloriecounter.data.MealSummaryEntity).\n"
                  + " Expected:\n" + _infoMealSummaries + "\n"
                  + " Found:\n" + _existingMealSummaries);
        }
        final HashMap<String, TableInfo.Column> _columnsPhotoHistory = new HashMap<String, TableInfo.Column>(4);
        _columnsPhotoHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoHistory.put("imageBytes", new TableInfo.Column("imageBytes", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoHistory.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoHistory.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPhotoHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPhotoHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPhotoHistory = new TableInfo("photo_history", _columnsPhotoHistory, _foreignKeysPhotoHistory, _indicesPhotoHistory);
        final TableInfo _existingPhotoHistory = TableInfo.read(db, "photo_history");
        if (!_infoPhotoHistory.equals(_existingPhotoHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "photo_history(com.example.caloriecounter.data.PhotoHistoryEntity).\n"
                  + " Expected:\n" + _infoPhotoHistory + "\n"
                  + " Found:\n" + _existingPhotoHistory);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "06646c42317793e08d0f81a12b33c27e", "2f517f6bc326888fd79837ad163279f1");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "analysis_jobs","meal_summaries","photo_history");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `analysis_jobs`");
      _db.execSQL("DELETE FROM `meal_summaries`");
      _db.execSQL("DELETE FROM `photo_history`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MealDao.class, MealDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MealDao mealDao() {
    if (_mealDao != null) {
      return _mealDao;
    } else {
      synchronized(this) {
        if(_mealDao == null) {
          _mealDao = new MealDao_Impl(this);
        }
        return _mealDao;
      }
    }
  }
}

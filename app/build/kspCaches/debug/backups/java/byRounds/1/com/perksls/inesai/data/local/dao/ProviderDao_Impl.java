package com.perksls.inesai.data.local.dao;

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
import com.perksls.inesai.data.local.entity.ProviderEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class ProviderDao_Impl implements ProviderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProviderEntity> __insertionAdapterOfProviderEntity;

  private final EntityDeletionOrUpdateAdapter<ProviderEntity> __updateAdapterOfProviderEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSortOrder;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProviderById;

  private final SharedSQLiteStatement __preparedStmtOfClearPrimary;

  private final SharedSQLiteStatement __preparedStmtOfSetPrimary;

  private final SharedSQLiteStatement __preparedStmtOfSetActiveModel;

  public ProviderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProviderEntity = new EntityInsertionAdapter<ProviderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `providers` (`id`,`name`,`baseUrl`,`apiKey`,`models`,`activeModel`,`isOpenAICompatible`,`sortOrder`,`isPrimary`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProviderEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getBaseUrl());
        statement.bindString(4, entity.getApiKey());
        statement.bindString(5, entity.getModels());
        statement.bindString(6, entity.getActiveModel());
        final int _tmp = entity.isOpenAICompatible() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getSortOrder());
        final int _tmp_1 = entity.isPrimary() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfProviderEntity = new EntityDeletionOrUpdateAdapter<ProviderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `providers` SET `id` = ?,`name` = ?,`baseUrl` = ?,`apiKey` = ?,`models` = ?,`activeModel` = ?,`isOpenAICompatible` = ?,`sortOrder` = ?,`isPrimary` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProviderEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getBaseUrl());
        statement.bindString(4, entity.getApiKey());
        statement.bindString(5, entity.getModels());
        statement.bindString(6, entity.getActiveModel());
        final int _tmp = entity.isOpenAICompatible() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getSortOrder());
        final int _tmp_1 = entity.isPrimary() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getCreatedAt());
        statement.bindString(11, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateSortOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE providers SET sortOrder = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteProviderById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM providers WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearPrimary = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE providers SET isPrimary = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetPrimary = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE providers SET isPrimary = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetActiveModel = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE providers SET activeModel = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProvider(final ProviderEntity provider,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProviderEntity.insert(provider);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProvider(final ProviderEntity provider,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProviderEntity.handle(provider);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSortOrder(final String id, final int order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSortOrder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, order);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateSortOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProviderById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProviderById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteProviderById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPrimary(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPrimary.acquire();
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
          __preparedStmtOfClearPrimary.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setPrimary(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetPrimary.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetPrimary.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setActiveModel(final String id, final String model,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetActiveModel.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, model);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetActiveModel.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProviderEntity>> getAllProviders() {
    final String _sql = "SELECT * FROM providers ORDER BY sortOrder ASC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"providers"}, new Callable<List<ProviderEntity>>() {
      @Override
      @NonNull
      public List<ProviderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBaseUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "baseUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModels = CursorUtil.getColumnIndexOrThrow(_cursor, "models");
          final int _cursorIndexOfActiveModel = CursorUtil.getColumnIndexOrThrow(_cursor, "activeModel");
          final int _cursorIndexOfIsOpenAICompatible = CursorUtil.getColumnIndexOrThrow(_cursor, "isOpenAICompatible");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ProviderEntity> _result = new ArrayList<ProviderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProviderEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBaseUrl;
            _tmpBaseUrl = _cursor.getString(_cursorIndexOfBaseUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModels;
            _tmpModels = _cursor.getString(_cursorIndexOfModels);
            final String _tmpActiveModel;
            _tmpActiveModel = _cursor.getString(_cursorIndexOfActiveModel);
            final boolean _tmpIsOpenAICompatible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOpenAICompatible);
            _tmpIsOpenAICompatible = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpIsPrimary;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ProviderEntity(_tmpId,_tmpName,_tmpBaseUrl,_tmpApiKey,_tmpModels,_tmpActiveModel,_tmpIsOpenAICompatible,_tmpSortOrder,_tmpIsPrimary,_tmpCreatedAt);
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
  public Object getProviderById(final String id,
      final Continuation<? super ProviderEntity> $completion) {
    final String _sql = "SELECT * FROM providers WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProviderEntity>() {
      @Override
      @Nullable
      public ProviderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBaseUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "baseUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModels = CursorUtil.getColumnIndexOrThrow(_cursor, "models");
          final int _cursorIndexOfActiveModel = CursorUtil.getColumnIndexOrThrow(_cursor, "activeModel");
          final int _cursorIndexOfIsOpenAICompatible = CursorUtil.getColumnIndexOrThrow(_cursor, "isOpenAICompatible");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ProviderEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBaseUrl;
            _tmpBaseUrl = _cursor.getString(_cursorIndexOfBaseUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModels;
            _tmpModels = _cursor.getString(_cursorIndexOfModels);
            final String _tmpActiveModel;
            _tmpActiveModel = _cursor.getString(_cursorIndexOfActiveModel);
            final boolean _tmpIsOpenAICompatible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOpenAICompatible);
            _tmpIsOpenAICompatible = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpIsPrimary;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ProviderEntity(_tmpId,_tmpName,_tmpBaseUrl,_tmpApiKey,_tmpModels,_tmpActiveModel,_tmpIsOpenAICompatible,_tmpSortOrder,_tmpIsPrimary,_tmpCreatedAt);
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
  public Object getPrimaryProvider(final Continuation<? super ProviderEntity> $completion) {
    final String _sql = "SELECT * FROM providers WHERE isPrimary = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProviderEntity>() {
      @Override
      @Nullable
      public ProviderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBaseUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "baseUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModels = CursorUtil.getColumnIndexOrThrow(_cursor, "models");
          final int _cursorIndexOfActiveModel = CursorUtil.getColumnIndexOrThrow(_cursor, "activeModel");
          final int _cursorIndexOfIsOpenAICompatible = CursorUtil.getColumnIndexOrThrow(_cursor, "isOpenAICompatible");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ProviderEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpBaseUrl;
            _tmpBaseUrl = _cursor.getString(_cursorIndexOfBaseUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModels;
            _tmpModels = _cursor.getString(_cursorIndexOfModels);
            final String _tmpActiveModel;
            _tmpActiveModel = _cursor.getString(_cursorIndexOfActiveModel);
            final boolean _tmpIsOpenAICompatible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOpenAICompatible);
            _tmpIsOpenAICompatible = _tmp != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final boolean _tmpIsPrimary;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp_1 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ProviderEntity(_tmpId,_tmpName,_tmpBaseUrl,_tmpApiKey,_tmpModels,_tmpActiveModel,_tmpIsOpenAICompatible,_tmpSortOrder,_tmpIsPrimary,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

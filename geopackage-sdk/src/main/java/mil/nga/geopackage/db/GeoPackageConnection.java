package mil.nga.geopackage.db;

import android.database.Cursor;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPackage Android Connection wrapper
 *
 * @author osbornb
 */
public class GeoPackageConnection extends GeoPackageCoreConnection {

    /**
     * Name column
     */
    private static final String NAME_COLUMN = "name";

    /**
     * Database connection
     */
    private final GeoPackageDatabase db;

    /**
     * Connection source
     */
    private final ConnectionSource connectionSource;

    /**
     * Constructor
     *
     * @param db GeoPackage connection
     */
    public GeoPackageConnection(GeoPackageDatabase db) {
        this.db = db;
        this.connectionSource = new AndroidConnectionSource(db.getDb());
    }

    /**
     * Get the database connection
     *
     * @return GeoPackage database
     */
    public GeoPackageDatabase getDb() {
        return db;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count(String table, String where, String[] args) {

        StringBuilder countQuery = new StringBuilder();
        countQuery.append("select count(*) from ").append(CoreSQLUtils.quoteWrap(table));
        if (where != null) {
            countQuery.append(" where ").append(where);
        }
        String sql = countQuery.toString();

        int count = singleResultQuery(sql, args);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer min(String table, String column, String where, String[] args) {

        Integer min = null;
        if (count(table, where, args) > 0) {
            StringBuilder minQuery = new StringBuilder();
            minQuery.append("select min(").append(CoreSQLUtils.quoteWrap(column)).append(") from ")
                    .append(CoreSQLUtils.quoteWrap(table));
            if (where != null) {
                minQuery.append(" where ").append(where);
            }
            String sql = minQuery.toString();

            min = singleResultQuery(sql, args);
        }

        return min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer max(String table, String column, String where, String[] args) {

        Integer max = null;
        if (count(table, where, args) > 0) {
            StringBuilder maxQuery = new StringBuilder();
            maxQuery.append("select max(").append(CoreSQLUtils.quoteWrap(column)).append(") from ")
                    .append(CoreSQLUtils.quoteWrap(table));
            if (where != null) {
                maxQuery.append(" where ").append(where);
            }
            String sql = maxQuery.toString();

            max = singleResultQuery(sql, args);
        }

        return max;
    }

    /**
     * Query the SQL for a single integer result
     *
     * @param sql
     * @param args
     * @return int result
     */
    private int singleResultQuery(String sql, String[] args) {

        Cursor countCursor = db.rawQuery(sql, args);

        int result = 0;
        try {
            if (countCursor.moveToFirst()) {
                result = countCursor.getInt(0);
            }
        } finally {
            countCursor.close();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        connectionSource.closeQuietly();
        db.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean columnExists(String tableName, String columnName) {

        boolean exists = false;

        Cursor cursor = rawQuery("PRAGMA table_info(" + CoreSQLUtils.quoteWrap(tableName) + ")", null);
        try {
            int nameIndex = cursor.getColumnIndex(NAME_COLUMN);
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                if (columnName.equals(name)) {
                    exists = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String querySingleStringResult(String sql, String[] args) {

        Cursor cursor = db.rawQuery(sql, args);

        String result = null;
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer querySingleIntResult(String sql, String[] args) {

        Cursor cursor = db.rawQuery(sql, args);

        Integer result = null;
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> querySingleColumnStringResults(String sql, String[] args) {

        Cursor cursor = db.rawQuery(sql, args);

        List<String> results = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                results.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> queryStringResults(String sql, String[] args) {
        return queryStringResults(sql, args, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] querySingleRowStringResults(String sql, String[] args) {
        List<String[]> results = queryStringResults(sql, args, 1);
        String[] singleRow = null;
        if (!results.isEmpty()) {
            singleRow = results.get(0);
        }
        return singleRow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> queryStringResults(String sql, String[] args,
                                             Integer limit) {

        Cursor cursor = rawQuery(sql, args);

        List<String[]> results = new ArrayList<>();
        try {
            int columns = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                String[] row = new String[columns];
                for (int i = 0; i < columns; i++) {
                    row[i] = cursor.getString(i);
                }
                results.add(row);
                if (limit != null && results.size() >= limit) {
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        return results;
    }

    /**
     * Perform a raw database query
     *
     * @param sql  sql command
     * @param args arguments
     * @return cursor
     * @since 1.2.1
     */
    public Cursor rawQuery(String sql, String[] args) {
        return db.rawQuery(sql, args);
    }

}

package org.rain.vertx.app.base.rocksdb;

/**
 * Custom exception for RocksDb operations.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.8.0
 */
public class RocksDbException extends RuntimeException {

    private static final long serialVersionUID = "0.8.0".hashCode();

    public RocksDbException() {
    }

    public RocksDbException(String message) {
        super(message);
    }

    public RocksDbException(Throwable cause) {
        super(cause);
    }

    public RocksDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public RocksDbException(String message, Throwable cause, boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /*----------------------------------------------------------------------*/

    /**
     * Thrown to indicate the specified column family does not exist.
     *
     * @author Thanh Nguyen <btnguyen2k@gmail.com>
     */
    public static class ColumnFamilyNotExists extends RocksDbException {
        private static final long serialVersionUID = "0.8.0".hashCode();

        public ColumnFamilyNotExists() {
            super("The specified column family does not exist!");
        }

        public ColumnFamilyNotExists(String cfName) {
            super("Column family [" + cfName + "] does not exist!");
        }
    }

    /*----------------------------------------------------------------------*/

    /**
     * Thrown to indicate the write/delete operation is not permitted because
     * the DB is opened in read-only mode.
     *
     * @author Thanh Nguyen <btnguyen2k@gmail.com>
     */
    public static class ReadOnlyException extends RocksDbException {
        private static final long serialVersionUID = "0.8.0".hashCode();

        public ReadOnlyException() {
            super("Modification operation is not permitted in read-only mode!");
        }

        public ReadOnlyException(String operation) {
            super("Modification operation [" + operation + "] is not permitted in read-only mode!");
        }
    }
}

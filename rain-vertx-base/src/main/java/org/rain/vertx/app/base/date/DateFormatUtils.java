package org.rain.vertx.app.base.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Date/Time format utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.2.2
 */
public class DateFormatUtils {

    /**
     * @since 0.7.0
     */
    public final static String DF_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * @since 0.2.2.3
     */
    private final static class MyDateFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;
        private final UUID id = UUID.randomUUID();

        public MyDateFormat(String format) {
            super(format);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MyDateFormat)) {
                return false;
            }

            MyDateFormat that = (MyDateFormat) o;
            if (id != null ? !id.equals(that.id) : that.id != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }

    private final static class DateFormatFactory extends BasePooledObjectFactory<DateFormat> {
        private String format;

        public DateFormatFactory(String format) {
            this.format = format;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DateFormat create() throws Exception {
            return new MyDateFormat(format);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PooledObject<DateFormat> wrap(DateFormat df) {
            return new DefaultPooledObject<DateFormat>(df);
        }
    }

    private final static LoadingCache<String, ObjectPool<DateFormat>> cachedDateFormat = CacheBuilder
            .newBuilder().maximumSize(100).expireAfterAccess(3600, TimeUnit.SECONDS)
            .concurrencyLevel(2)
            .removalListener(new RemovalListener<String, ObjectPool<DateFormat>>() {
                @Override
                public void onRemoval(RemovalNotification<String, ObjectPool<DateFormat>> event) {
                    event.getValue().close();
                }
            }).build(new CacheLoader<String, ObjectPool<DateFormat>>() {
                @Override
                public ObjectPool<DateFormat> load(String format) throws Exception {
                    DateFormatFactory dff = new DateFormatFactory(format);
                    ObjectPool<DateFormat> obj = new GenericObjectPool<DateFormat>(dff);
                    return obj;
                }
            });

    /**
     * Convert a {@link Date} to string, based on the specified {@code format}.
     *
     * @param date
     * @param format
     * @return
     * @see {@link SimpleDateFormat}
     */
    public static String toString(Date date, String format) {
        try {
            ObjectPool<DateFormat> pool = cachedDateFormat.get(format);
            try {
                DateFormat df = pool.borrowObject();
                try {
                    return df.format(date);
                } finally {
                    pool.returnObject(df);
                }
            } catch (Exception e) {
                throw e instanceof RuntimeException ? (RuntimeException) e
                        : new RuntimeException(e);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a {@link Calendar} to string, based on the specified {@code format}.
     *
     * @param calendar
     * @param format
     * @return
     * @see {@link SimpleDateFormat}
     * @since 0.9.1.1
     */
    public static String toString(Calendar calendar, String format) {
        return toString(calendar.getTime(), format);
    }

    /**
     * Parse a string to {@link Date}, based on the specified {@code format}.
     *
     * @param source
     * @param format
     * @return
     */
    public static Date fromString(String source, String format) {
        try {
            ObjectPool<DateFormat> pool = cachedDateFormat.get(format);
            try {
                DateFormat df = pool.borrowObject();
                try {
                    return df.parse(source);
                } finally {
                    pool.returnObject(df);
                }
            } catch (Exception e) {
                throw e instanceof RuntimeException ? (RuntimeException) e
                        : new RuntimeException(e);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
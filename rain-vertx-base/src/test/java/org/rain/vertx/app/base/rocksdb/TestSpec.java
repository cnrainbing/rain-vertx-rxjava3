package org.rain.vertx.app.base.rocksdb;

import org.apache.commons.io.FileUtils;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;

public class TestSpec {
    public static void main(String[] args) throws RocksDbException, IOException {
        // cleanup
        String dataDir = "/Users/fengbingbing/Downloads/tmp";
        //FileUtils.deleteDirectory(new File(dataDir));
        try (RocksDbWrapper rocksDb = RocksDbWrapper.openReadOnly(dataDir)) {
            // RocksDbWrapper instance has been initialized by RocksDbWrapper.openXXX() method.
            // Calling rocksDb.init() is not needed.
            rocksDb.getColumnFamilies().forEach(columnFamilyDescriptor -> {
                var cfName = new String(columnFamilyDescriptor.getName());
                System.out.println("cfName:" + cfName);
                // 打印全部[key - value]
                RocksIterator iter = rocksDb.getIterator(cfName);
                for (iter.seekToFirst(); iter.isValid(); iter.next()) {
                    System.out.println(new String(iter.key()) + ":" + new String(iter.value()));
                }
            });


            System.out.println("Number of keys: " + rocksDb.getEstimateNumKeys(RocksDbWrapper.DEFAULT_COLUMN_FAMILY));

            // fetch a value from default column family
            /*byte[] value = rocksDb.get("key");
            System.out.println("Value of [key]: " + (value != null ? new String(value) : "[null]"));

            // write a value
            rocksDb.put("key", "a value");
            System.out.println("Number of keys: " + rocksDb.getEstimateNumKeys(RocksDbWrapper.DEFAULT_COLUMN_FAMILY));

            // fetch it back
            value = rocksDb.get("key");
            System.out.println("Value of [key]: " + (value != null ? new String(value) : "[null]"));
            */
        }
    }

}

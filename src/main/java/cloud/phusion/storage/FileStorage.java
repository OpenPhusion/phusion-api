package cloud.phusion.storage;

import cloud.phusion.Context;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * File Storage.
 *
 * Public files can be accessed from the internet with a Web address.
 */
public interface FileStorage {

    public class FileProperties {
        public String name;
        public long size;
        public long updateTime;
        public boolean isFolder;

        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = df.format(new Date(updateTime));

            return isFolder ? name+"/" :
                    String.format("%s: %d bytes, updated at %s", name, size, strDate);
        };
    }

    boolean doesFileExist(String path) throws Exception;
    boolean doesPublicFileExist(String path) throws Exception;
    boolean doesFileExist(String path, Context ctx) throws Exception;
    boolean doesPublicFileExist(String path, Context ctx) throws Exception;

    long getFileSize(String path) throws Exception;
    long getPublicFileSize(String path) throws Exception;
    long getFileSize(String path, Context ctx) throws Exception;
    long getPublicFileSize(String path, Context ctx) throws Exception;

    FileProperties getFileProperties(String path, Context ctx) throws Exception;
    FileProperties getPublicFileProperties(String path, Context ctx) throws Exception;

    String[] listFolders(String path) throws Exception;
    String[] listPublicFolders(String path) throws Exception;
    String[] listFiles(String path) throws Exception;
    String[] listPublicFiles(String path) throws Exception;
    String[] listFolders(String path, Context ctx) throws Exception;
    String[] listPublicFolders(String path, Context ctx) throws Exception;
    String[] listFiles(String path, Context ctx) throws Exception;
    String[] listPublicFiles(String path, Context ctx) throws Exception;

    FileProperties[] listFilesWithProperties(String path, Context ctx) throws Exception;
    FileProperties[] listPublicFilesWithProperties(String path, Context ctx) throws Exception;

    void saveToFile(String path, InputStream content) throws Exception;
    void saveToPublicFile(String path, InputStream content) throws Exception;
    void saveToFile(String path, byte[] content) throws Exception;
    void saveToPublicFile(String path, byte[] content) throws Exception;
    void saveToFile(String path, InputStream content, Context ctx) throws Exception;
    void saveToPublicFile(String path, InputStream content, Context ctx) throws Exception;
    void saveToFile(String path, byte[] content, Context ctx) throws Exception;
    void saveToPublicFile(String path, byte[] content, Context ctx) throws Exception;

    InputStream readFromFile(String path) throws Exception;
    InputStream readFromPublicFile(String path) throws Exception;
    byte[] readAllFromFile(String path) throws Exception;
    byte[] readAllFromPublicFile(String path) throws Exception;
    InputStream readFromFile(String path, Context ctx) throws Exception;
    InputStream readFromPublicFile(String path, Context ctx) throws Exception;
    byte[] readAllFromFile(String path, Context ctx) throws Exception;
    byte[] readAllFromPublicFile(String path, Context ctx) throws Exception;

    void removeFile(String path) throws Exception;
    void removePublicFile(String path) throws Exception;
    void removeFile(String path, Context ctx) throws Exception;
    void removePublicFile(String path, Context ctx) throws Exception;

    /**
     * Remove all files and directories.
     */
    void removeAll() throws Exception;
    void removeAll(Context ctx) throws Exception;

    /**
     * Get the Web address for the file.
     */
    String getPublicFileUrl(String path) throws Exception;

}

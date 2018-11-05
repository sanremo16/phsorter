package org.san.home.phsorter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Created by User on 04.04.2018.
 */
public class FileUtils {

    public static DatePeriod getDatePeriodByPath(Path filePath) {
        //2012/02.14 (14 февраля)
        //2015/03.09-12 (самсинг)
        //2017/09.хх (дома)
        Path parentPath = filePath.getParent().getFileName();
        return DatePeriod.of(parentPath.toString(), filePath.getFileName().toString());
    }


    public static Path checkFilePath(String filePath, boolean checkExists, Boolean checkRead, Boolean checkWrite) {
        final File f = new File(filePath);
        if (f == null || !f.isDirectory())
            throw new IllegalArgumentException("Incorrect file path '" + filePath + "': not a directory");
        if (checkExists && !f.exists())
            throw new IllegalArgumentException("Incorrect file path '" + filePath + "': doesn't exist");
        if (f.exists() && ((checkRead && !f.canRead()) || (checkWrite && !f.canWrite())))
            throw new IllegalArgumentException("Incorrect file path '" + filePath + "': unsufficient permissions");
        return Paths.get(filePath);
    }

    public static void createDirIfNotExist(Path dirPath) throws IOException {
        if (!Files.exists(dirPath, LinkOption.NOFOLLOW_LINKS)) {
            //Files.createDirectory(dirPath, new FileAttribute[](new FileAttribute<String>()) PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
            dirPath.toFile().mkdirs();
            //Files.createDirectory(dirPath);
        }
    }
}

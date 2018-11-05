package org.san.home.phsorter;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Получение даты создания снимка
 *
 * Created by Sanremo on 29.04.2017.
 */
public class CreationDateGetter {
    private static final Logger logger = LoggerFactory.getLogger(CreationDateGetter.class);
    private static final String FILE_CREATED_AT_ATTR = "basic:createdAt";
    /**
     * IMG_20130502_133516.jpg
     * VID_20130805_205348.mp4
     */
    private static Pattern MY_NAME_WITH_DATE_PATTERN = Pattern.compile("(\\w{3})_(\\d{8})_(\\d{6}).(jpg|mp4|JPG|MP4|MOV|PNG)");
    //20171229_175922.mp4
    private static Pattern KSU_NAME_WITH_DATE_PATTERN = Pattern.compile("(\\d{8})_(\\d{6}).(jpg|mp4|JPG|MP4|MOV|PNG)");

    /**
     * дата из атрибутов файла
     * @param filePath
     * @return
     */
    @Nullable
    static LocalDate byFileAttribute(@NotNull Path filePath) {
        try {
            //Files.get
            BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            int compareTo = basicFileAttributes.creationTime().compareTo(basicFileAttributes.lastModifiedTime());
            FileTime ft = compareTo > 0 ? basicFileAttributes.lastModifiedTime() : basicFileAttributes.creationTime();
            LocalDateTime ldt = LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault());
            return ldt.toLocalDate();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * дата из имени файла
     * @param filePath
     * @return
     */
    @Nullable
    static LocalDate byFileName(@NotNull Path filePath) {
        String fileName = filePath.getFileName().toString();
        Matcher matcher1 = MY_NAME_WITH_DATE_PATTERN.matcher(fileName);
        Matcher matcher2 = KSU_NAME_WITH_DATE_PATTERN.matcher(fileName);
        if (matcher1.matches()) {
            return LocalDate.parse(matcher1.group(2), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else if (matcher2.matches()) {
            return LocalDate.parse(matcher2.group(1), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            //logger.error("File name doesn't match mobile name regexp: " + fileName);
            return null;
        }
    }

    /**
     * дата из свойств снимка
     * @param filePath
     * @return
     */
    @Nullable
    static LocalDate byImageAttribute(@NotNull Path filePath) {

        throw new NotImplementedException();
    }

    public static LocalDate getPhotoCreationDate(@NotNull Path filePath) {
        LocalDate res;
        return (res = byFileName(filePath)) != null ? res : byFileAttribute(filePath);
    }
}

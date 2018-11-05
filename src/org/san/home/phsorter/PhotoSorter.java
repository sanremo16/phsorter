package org.san.home.phsorter;

import static org.san.home.phsorter.FileUtils.*;

import com.google.common.collect.TreeMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;


public class PhotoSorter {
    private static final Logger logger = LoggerFactory.getLogger(PhotoSorter.class);
    static int IMAGES_PER_DAY_LIMIT = 10;
    private static PathMatcher MEDIA_PATH_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.{jpg,JPG,mp4,MP4,MOV,PNG}");

    static void doWork(String srcPathStr, String dstPathStr) {
        Path srcFolderPath = FileUtils.checkFilePath(srcPathStr, true, true, false);
        Path dstFolderPath = FileUtils.checkFilePath(dstPathStr, false, true, true);

        TreeMultimap<LocalDate, Path> srcPathByCreationDateMultimap = getSrcPathByCreationDateMultimap(srcFolderPath);
        TreeMap<DatePeriod, Path> dstPathByDatePeriodMultimap = getDstFolderByDatePeriodMultimap(dstFolderPath);
        srcPathByCreationDateMultimap.forEach((localDate, path) -> {
            DatePeriod suitablePeriod = findSuitablePeriod(dstPathByDatePeriodMultimap.navigableKeySet(), localDate);
            try {
                if (suitablePeriod != null) {
                    if (DatePeriod.DatePeriodType.MONTH == suitablePeriod.getType()
                            && srcPathByCreationDateMultimap.get(localDate).size() >= IMAGES_PER_DAY_LIMIT) {
                        //при больщом кол-ве файлов создаем папку за день
                        moveFile(localDate.getYear(), localDate.getMonth().getValue(), localDate.getDayOfMonth(), path, dstFolderPath);
                    } else {
                        logger.debug("Copy file: " + path + ", to " + dstPathByDatePeriodMultimap.get(suitablePeriod).resolve(path.getFileName()));
                        Files.move(path,
                                dstPathByDatePeriodMultimap.get(suitablePeriod).resolve(path.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING);


                    }
                } else {
                    if (srcPathByCreationDateMultimap.get(localDate).size() >= IMAGES_PER_DAY_LIMIT) {
                        //при больщом кол-ве файлов создаем папку за день
                        moveFile(localDate.getYear(), localDate.getMonth().getValue(), localDate.getDayOfMonth(), path, dstFolderPath);
                    } else {
                        //при небольшом кол-ве файлов копируем в папку месяца
                        moveFile(localDate.getYear(), localDate.getMonth().getValue(), 0, path, dstFolderPath);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static DatePeriod findSuitablePeriod(SortedSet<DatePeriod> periods, LocalDate date) {
        for (DatePeriod period : periods) {
            if (period.endDate.isBefore(date)) continue;
            if (period.startDate.isAfter(date)) return null;
            if (period.contain(date)) return period;
        }
        return null;
    }

    /**
     * заполнение карты "дата - путь"
     * @param srcFolderPath
     * @return
     */
    static TreeMultimap<LocalDate, Path> getSrcPathByCreationDateMultimap(Path srcFolderPath) {
        TreeMultimap<LocalDate, Path> srcPathByCreationDateMultimap = TreeMultimap.create();
        try (Stream<Path> srcPaths = Files.walk(srcFolderPath)) {
            srcPaths
                    .filter(srcFilePath -> MEDIA_PATH_MATCHER.matches(srcFilePath))
                    .forEach(srcFilePath -> {
                                srcPathByCreationDateMultimap.put(
                                        CreationDateGetter.getPhotoCreationDate(srcFilePath),
                                        srcFilePath);
                            }
                    );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return srcPathByCreationDateMultimap;
    }

    /**
     * заполнение карты "временной период - путь" для целевой директории
     * @param dstFolderPath
     * @return
     */
    static TreeMap<DatePeriod, Path> getDstFolderByDatePeriodMultimap(Path dstFolderPath) {
        TreeMap<DatePeriod, Path> dstPathByDatePeriodMultimap = new TreeMap<>();
        try (Stream<Path> dstPaths = Files.walk(dstFolderPath)) {
            dstPaths
                    .filter(filePath -> (filePath.toFile().isDirectory()
                            && DatePeriod.isYear(filePath.getParent().getFileName().toString())
                            && DatePeriod.instanceOf(filePath.getFileName().toString())))
                    .forEachOrdered(filePath -> {
                                dstPathByDatePeriodMultimap.put(
                                        FileUtils.getDatePeriodByPath(filePath),
                                        filePath);
                            }
                    );

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dstPathByDatePeriodMultimap;
    }

    private static void moveFile(int year, int month, int day, Path srcFilePath, Path dstFolderPath)
    throws IOException {
        Path dst = getDstFilePath(year, month, day, srcFilePath, dstFolderPath);
        logger.debug("Move file: " + srcFilePath + ", to " + dst);
        Files.move(srcFilePath,
                dst,
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * создание целевой директории на основании даты файла
     * @param year
     * @param month
     * @param day
     * @param srcFilePath
     * @param dstFolderPath
     * @return
     * @throws IOException
     */
    static Path getDstFilePath(int year, int month, int day, Path srcFilePath, Path dstFolderPath)
    throws IOException {
        String twoDecimalDay = getTwoDecimalValue(day);

        Path yearPath = Paths.get(dstFolderPath.toAbsolutePath().toString(),
                String.valueOf(year));
        Path monthPath = Paths.get(yearPath.toAbsolutePath().toString(),
                getTwoDecimalValue(month));
        Path dayPath = twoDecimalDay.length() == 0
                ? Paths.get(monthPath.toAbsolutePath().toString() + ".xx")
                : Paths.get(monthPath.toAbsolutePath().toString() + "." + twoDecimalDay);
        createDirIfNotExist(dayPath);

        return Paths.get(dayPath.toAbsolutePath().toString(), srcFilePath.getFileName().toString());
    }



    /**
     * x on input -> 0x on output
     * xy on input -> return xy
     * 0 on input -> return ""
     * @param val
     * @return
     */
    private static String getTwoDecimalValue(int val) {
        if (val == 0) {
            return "";
        } else if (val < 10) {
            return "0" + val;
        } else {
            return String.valueOf(val);
        }
    }

    public static void main(String[] args) {
	    if (args.length != 2) {
            System.out.println("Usage: <source folder> <destination folder>");
            System.exit(0);
        }
        //try {
            new PhotoSorter().doWork(args[0], args[1]);
        //} catch (IOException ex) {
        //    ex.printStackTrace();
        //}
    }


}

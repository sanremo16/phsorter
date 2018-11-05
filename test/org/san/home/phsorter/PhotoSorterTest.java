package org.san.home.phsorter;

import com.google.common.collect.TreeMultimap;
import junit.framework.TestCase;

import static org.san.home.phsorter.PhotoSorter.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

/**
 * Created by User on 28.09.2018.
 */
public class PhotoSorterTest extends TestCase {

    private static final String SRC_PHOTO = "test.jpg";
    private static final String RES_PATH = "test/resources/";
    private static final String SRC_PHOTO_FULL = RES_PATH + "src1/test.jpg";

    private static final String DST = RES_PATH + "dst1";

    private static String CREATION_DATE = "20160821";
    private static String IMAGE_BY_FILE_ATTR = RES_PATH + "src1/IMG_1968.JPG";

    public void testGetDstFilePath() throws Exception {
        assertEquals(Paths.get(DST, "2018", "09.28", SRC_PHOTO).toAbsolutePath(),
                getDstFilePath(2018, 9, 28, Paths.get(SRC_PHOTO_FULL), Paths.get(DST)));

        assertEquals(Paths.get(DST, "2018", "10.28", SRC_PHOTO).toAbsolutePath(),
                getDstFilePath(2018, 10, 28, Paths.get(SRC_PHOTO_FULL), Paths.get(DST)));

        assertEquals(Paths.get(DST, "2018", "10.01", SRC_PHOTO).toAbsolutePath(),
                getDstFilePath(2018, 10, 1, Paths.get(SRC_PHOTO_FULL), Paths.get(DST)));

        assertEquals(Paths.get(DST, "2018", "09.xx", SRC_PHOTO).toAbsolutePath(),
                getDstFilePath(2018, 9, 0, Paths.get(SRC_PHOTO_FULL), Paths.get(DST)));
    }

    public void testGetSrcPathByCreationDateMultimap() {
        TreeMultimap<LocalDate, Path> res = TreeMultimap.create();
        res.put(getLocalDate("20180928"), Paths.get(RES_PATH, "src1/test2.MP4"));
        res.put(getLocalDate("20180928"), Paths.get(RES_PATH, "src1/test.jpg"));
        res.put(getLocalDate(CREATION_DATE), Paths.get(IMAGE_BY_FILE_ATTR));
        assertEquals(res, getSrcPathByCreationDateMultimap(Paths.get(RES_PATH, "src1")));
    }

    public void testGetDstFolderByDatePeriodMultimap() {
        TreeMap<DatePeriod, Path> res = new TreeMap();
        res.put(new DatePeriod(getLocalDate("20161004"), getLocalDate("20161014"), DatePeriod.DatePeriodType.PERIOD),
                Paths.get(RES_PATH + "dst2/2016/10.04-14 (Кипр)"));
        res.put(new DatePeriod(getLocalDate("20160509"), getLocalDate("20160509"), DatePeriod.DatePeriodType.SINGLE_DAY),
                Paths.get(RES_PATH + "dst2/2016/05.09 (Дома)"));
        res.put(new DatePeriod(getLocalDate("20160101"), getLocalDate("20160128"), DatePeriod.DatePeriodType.MONTH),
                Paths.get(RES_PATH + "dst2/2016/01.XX"));

        assertEquals(res, getDstFolderByDatePeriodMultimap(Paths.get(RES_PATH + "dst2")));

    }

    public void testDoWork() throws IOException {
        PhotoSorter.IMAGES_PER_DAY_LIMIT = 5;
        PhotoSorter.doWork(RES_PATH + "src", RES_PATH + "dst");
        assertTrue(Paths.get(RES_PATH + "dst\\2016\\08.xx\\IMG_1968.JPG").toFile().exists());
        assertTrue(Paths.get(RES_PATH + "dst\\2018\\09.20-30\\test.jpg").toFile().exists());
        assertTrue(Paths.get(RES_PATH + "dst\\2018\\09.20-30\\test2.MP4").toFile().exists());
        assertTrue(Paths.get(RES_PATH + "dst\\2018\\10.xx\\test3.jpg").toFile().exists());
        assertTrue(Paths.get(RES_PATH + "dst\\2017\\12.xx\\20171228_194914.jpg").toFile().exists());
        assertTrue(Paths.get(RES_PATH + "dst\\2017\\12.25\\20171225_110459.jpg").toFile().exists());
        Files.walk(Paths.get(RES_PATH + "dst")).filter(p -> !p.toFile().isDirectory()).forEach(p -> p.toFile().delete());
        Paths.get(RES_PATH + "dst\\2018\\10.xx").toFile().delete();
        Paths.get(RES_PATH + "dst\\2017\\12.xx").toFile().delete();
        Paths.get(RES_PATH + "dst\\2017\\12.25").toFile().delete();
    }

    public void testClean() throws IOException {
        Files.walk(Paths.get(RES_PATH + "dst")).filter(p -> !p.toFile().isDirectory()).forEach(p -> p.toFile().delete());
    }

    public void test() {
        PhotoSorter.doWork("e:\\Mobile\\Саша\\", "e:\\Photo\\Общие\\");

    }

    private static LocalDate getLocalDate(String str) {
        return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}

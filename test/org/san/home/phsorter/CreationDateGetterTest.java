package org.san.home.phsorter;

import junit.framework.TestCase;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.san.home.phsorter.CreationDateGetter.*;

/**
 * Created by User on 28.09.2018.
 */
public class CreationDateGetterTest  extends TestCase {
    private static final String RES_PATH = "test/resources/";
    private static String MY_PHONE_PHOTO_NAME = RES_PATH + "IMG_20130502_133516.jpg";
    private static String KSU_PHONE_VIDEO_NAME = RES_PATH + "20171229_175922.mp4";

    private static String IMAGE_BY_FILE_ATTR = RES_PATH + "src1/IMG_1968.JPG";
    private static String CREATION_DATE = "20160821";

    public void testByFileName() {
        assertEquals(LocalDate.parse("20130502", DateTimeFormatter.ofPattern("yyyyMMdd")),
                byFileName(Paths.get(MY_PHONE_PHOTO_NAME)));
        assertEquals(LocalDate.parse("20171229", DateTimeFormatter.ofPattern("yyyyMMdd")),
                byFileName(Paths.get(KSU_PHONE_VIDEO_NAME)));
    }

    public void testByFileAttr() {
        assertEquals(LocalDate.parse(CREATION_DATE, DateTimeFormatter.ofPattern("yyyyMMdd")),
                byFileAttribute(Paths.get(IMAGE_BY_FILE_ATTR)));
    }
}

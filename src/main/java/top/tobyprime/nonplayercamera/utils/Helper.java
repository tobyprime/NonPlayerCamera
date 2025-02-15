package top.tobyprime.nonplayercamera.utils;

public class Helper {
    public static void log(Object str) {
        Global.LOGGER.info(str);
    }

    public static void warn(Object str) {
        Global.LOGGER.warn(str);
    }

    public static void err(Object str) {
        Global.LOGGER.error(str);
    }

    public static void dbg(Object str) {
        Global.LOGGER.warn(str);
    }


}

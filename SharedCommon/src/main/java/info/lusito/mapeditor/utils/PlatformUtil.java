package info.lusito.mapeditor.utils;

public class PlatformUtil {

    private final static String OS = System.getProperty("os.name").toLowerCase();
    public final static boolean IS_WINDOWS = OS.contains("win");
    public final static boolean IS_MAC = OS.contains("mac");
    public final static boolean IS_UNIX = OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    public final static boolean IS_SOLARIS = OS.contains("sunos");
}

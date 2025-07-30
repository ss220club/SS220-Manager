package club.ss220.manager.util;

public class CkeyUtils {

    public static String sanitizeCkey(String ckey) {
        if (ckey == null) {
            return null;
        }
        
        return ckey.toLowerCase().replaceAll("[^a-z0-9_]", "");
    }
}

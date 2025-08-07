package club.ss220.core.util;

import org.springframework.stereotype.Service;

@Service
public class ByondUtils {

    public String sanitizeCkey(String ckey) {
        if (ckey == null) {
            return null;
        }
        
        return ckey.toLowerCase().replaceAll("[^a-z0-9_]", "");
    }
}

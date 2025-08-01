package club.ss220.manager.util;

import org.springframework.stereotype.Service;

@Service
public class CkeyUtils {

    public String sanitizeCkey(String ckey) {
        if (ckey == null) {
            return null;
        }
        
        return ckey.toLowerCase().replaceAll("[^a-z0-9_]", "");
    }
}

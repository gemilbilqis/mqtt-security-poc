package ro.ase.iot.mqtt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class KeyLoader {
    public static byte[] loadKey(String path) throws IOException {
        String base64 = Files.readString(Paths.get(path)).trim();
        return Base64.getDecoder().decode(base64);
    }
}

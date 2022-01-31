package cz.tefek.ymd2.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class DirectoryUtil
{
    public static void ensureDirectoryExists(Path path) throws IOException
    {
        if (Files.isDirectory(path))
            return;

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS))
            throw new RuntimeException(String.format("The directory '%s/' is obstructed.", path));

        System.out.printf("Creating the %s directory.%n", path.toAbsolutePath());
        Files.createDirectories(path);
    }
}

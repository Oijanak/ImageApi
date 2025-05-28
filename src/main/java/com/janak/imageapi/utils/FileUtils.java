package com.janak.imageapi.utils;

public class FileUtils {

        public static String getReadableFileSize(long sizeInBytes) {
            if (sizeInBytes <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));
            return String.format("%.1f %s", sizeInBytes / Math.pow(1024, digitGroups), units[digitGroups]);
        }

    }



package pol.picfix;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoUtil {


    private static Set<String> photoFileExtensions;

    static {
        photoFileExtensions = new HashSet<String>();
        photoFileExtensions.add("jpg");
        photoFileExtensions.add("gif");
        photoFileExtensions.add("png");
        photoFileExtensions.add("mov");
        photoFileExtensions.add("mp4");
        photoFileExtensions.add("avi");
        photoFileExtensions.add("tif");
        photoFileExtensions.add("nef");
        photoFileExtensions.add("psd");
    }
    
    
    private static Map<String, String> monthMap;
    
    static {
        monthMap = new HashMap<String, String>(12);
        monthMap.put("01", "January");
        monthMap.put("02", "February");
        monthMap.put("03", "March");
        monthMap.put("04", "April");
        monthMap.put("05", "May");
        monthMap.put("06", "June");
        monthMap.put("07", "July");
        monthMap.put("08", "August");
        monthMap.put("09", "September");
        monthMap.put("10", "October");
        monthMap.put("11", "November");
        monthMap.put("12", "December");
    }
    
    public static boolean isPhotoExtension(String fileExtension) {
        return photoFileExtensions.contains(fileExtension);
    }
    
    public static boolean hasPhotoExtension(String fileName) {
        return photoFileExtensions.contains(getFileExtension(fileName));
    }
    
    public static boolean hasPhotoExtension(File file) {
        return photoFileExtensions.contains(getFileExtension(file));
    }
    
    public static String getFileExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }
        return "";
    }  
    
    public static String getFileExtension(File file) {
        return getFileExtension(file.getName());
    }
    
    public static String getMonthName(String twoCharacterMonth) {
        return monthMap.get(twoCharacterMonth);
    }
   
    public static boolean isPhotoNamingConvention(File file) {
        return isPhotoNamingConvention(file.getName());
    }

    public static boolean isPhotoNamingConvention(String fileName) {
        final String fileNamePatternString = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}-[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}.*\\..+";
        return fileName.matches(fileNamePatternString);
    }
    
    public static boolean doesFileNameHaveCounterAppendage(String fileName) {
        String fileNameWithCounterPatternString = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}-[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}-[0-9]+\\..+";
        return fileName.matches(fileNameWithCounterPatternString);
    }
    
    public static boolean doesFileNameHaveCounterAppendage(File file) {
        return doesFileNameHaveCounterAppendage(file.getName());
    }
    
    public static int getValueOfCounterAppendage(String fileName) {
        String fileNameWithCounterPatternString = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}-[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}-([0-9]+)\\..+";
        Pattern fileNamePattern = Pattern.compile(fileNameWithCounterPatternString);
        Matcher fileNameMatcher = fileNamePattern.matcher(fileName);
        if (fileNameMatcher.find()) {
            return Integer.parseInt(fileNameMatcher.group(1));
        }
        else {
            return -1;
        } 
    }
    
    public static int getValueOfCounterAppendage(File file) {
        return getValueOfCounterAppendage(file.getName());
    }
    
    public static String updateFileNameWithCounterAppendage(String fileName, int counter) {
        StringBuilder fileNameBuilder = new StringBuilder();
        
        // Add the first part
        fileNameBuilder.append(fileName.substring(0, 17));
        
        // Add the appendage
        fileNameBuilder.append('-');
        fileNameBuilder.append(counter);
        
        // Add the file extension
        fileNameBuilder.append('.');
        fileNameBuilder.append(getFileExtension(fileName));
        
        return fileNameBuilder.toString();
    }
    
}

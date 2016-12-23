package pol.picfix;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoCopier {
    
	private String sourcePhotoFolderPath;
	private String processedPhotoRootFolderPath;
	private List<File> photos;
	
	// Figures for reporting...
    private int numberOfFilesChecked;
    private int numberOfPhotoFilesFound;
    private int numberOfNonPhotoFilesFound;
    private int numberOfFilesCopied;
	

	public PhotoCopier(String sourcePhotoFolderPath, String processedPhotoRootFolderPath) {
		this.sourcePhotoFolderPath = sourcePhotoFolderPath;
		this.processedPhotoRootFolderPath = processedPhotoRootFolderPath;
	}

	public void run() {
	   
		try {
		    findAllPhotos(); 
            copyFilesToRelevantLocation();
            printStatistics();
		} 
		catch (IOException e) {
            e.printStackTrace();
        }
		
	}


	/**
	 * Iterate through folder and add found photos to the list
	 */
	protected void findAllPhotos() {
	    photos = new ArrayList<File>();
	    File sourcePhotoFolder = new File(sourcePhotoFolderPath);
	   
	    for (File file : sourcePhotoFolder.listFiles()) {
	        numberOfFilesChecked++;
	        
	        // Verify the format
	        if (!PhotoUtil.isPhotoNamingConvention(file)) {
	            numberOfNonPhotoFilesFound++;
	            System.out.println("Skipping " + file.getName());
	            continue;
	        } 
	         
	        // Verify the extension
	        if (!PhotoUtil.hasPhotoExtension(file)) {
                numberOfNonPhotoFilesFound++;
                System.out.println("Skipping " + file.getName());
                continue;
            } 
	        
	        // If we've made it to here, we're good...
	        numberOfPhotoFilesFound++;
	        photos.add(file);
	        
	    }
	    
	}

	
	protected void copyFilesToRelevantLocation() throws IOException {
	    
	    System.out.println("Copying files...");
        
        CopyOption[] options = new CopyOption[]{
            StandardCopyOption.COPY_ATTRIBUTES
        };
	    
		for (File photo : photos) {
		    File targetFolder = new File(getTargetFolderPathForPhoto(photo));
		    targetFolder.mkdirs();
		    File targetPhoto = new File(targetFolder, photo.getName());
		    if (targetPhoto.isFile()) {
		        targetPhoto = resolveFileNameConflict(targetPhoto);
		    }
		    // System.out.println("Copying " + photo + " to " + targetFilePath);
		    Files.copy(photo.toPath(), targetPhoto.toPath(), options);
		    numberOfFilesCopied++;
		}
		
		System.out.println("Copy files complete.");
	}
	
	protected String getTargetFolderPathForPhoto(File photo) {
	    // System.out.println("getTargetFolderPathForPhoto(): " + photo.getName());
	    String fileNamePatternString = "([0-9]{2})\\.([0-9]{2})\\.[0-9]{2}-[0-9]{2}\\.[0-9]{2}\\.[0-9]{2}.*\\..+";
	    Pattern fileNamePattern = Pattern.compile(fileNamePatternString);
	    Matcher fileNameMatcher = fileNamePattern.matcher(photo.getName());
	    if (fileNameMatcher.find()) {
	        String yearFolder = "20" + fileNameMatcher.group(1);
	        String monthFolder = fileNameMatcher.group(2) + "-" + PhotoUtil.getMonthName(fileNameMatcher.group(2));
	        return processedPhotoRootFolderPath + "/" + yearFolder + "/" + monthFolder;
	    }
	    return "";
	}

	/**
	 * Takes a file that already exists and appends a counter.
	 * Will keep incrementing the counter until a unique name is found.
	 * 
	 * @param file
	 * @return
	 */
	protected File resolveFileNameConflict(File file) {
	 
	    System.out.println("Resolving name :" + file.getName());
	    
	    String newFileName = null;
	    File newFile = null;
	    
	    // Determine the starting value for our counter
	    int counter = PhotoUtil.doesFileNameHaveCounterAppendage(file) ? PhotoUtil.getValueOfCounterAppendage(file) : 0;
	        
        do {
            counter++;
            newFileName = PhotoUtil.updateFileNameWithCounterAppendage(file.getName(), counter);
            // System.out.println(" - Trying: " + newFileName);
            newFile = new File(file.getParent(), newFileName);
        }
        while (newFile.isFile()); 
       
        System.out.println("Found new file name: " + newFileName);
        
        return newFile;
	}
	
    protected void printStatistics() {
        System.out.println("      Files checked: " + numberOfFilesChecked);
        System.out.println("    Non-photo files: " + numberOfNonPhotoFilesFound);
        System.out.println("        Photo files: " + numberOfPhotoFilesFound);
        System.out.println("       Copied files: " + numberOfFilesCopied);
    }
	
	
	/**
	 * Entry point 
	 * 
	 * @param args array containing one item with path to directory
	 */
	public static void main(String[] args) {
		System.out.println("Going to run photo copier...");
		PhotoCopier photoCopier = new PhotoCopier(args[0], args[1]);	
		photoCopier.run();
		System.out.println("Done.");
	}
}

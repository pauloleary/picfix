package pol.picfix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

public class PhotoRenamer {

	private String photoRootFolderPath;
	private String photoTargetFolderPath;
	private List<File> photos;
	private Map<String, File> targetNameSourceFileMap;
	
	// Figures for reporting...
	private int numberOfFilesChecked;
	private int numberOfFoldersChecked;
	private int numberOfPhotoFilesFound;
	private int numberOfNonPhotoFilesFound;
	private int numberOfFilesCopied;
	private int numberOfFilesWithNoCreationDate;
	private int numberOfFilesWithErrors;

	public PhotoRenamer(String photoRootFolderPath, String photoTargetFolderPath) {
		this.photoRootFolderPath = photoRootFolderPath;	
		this.photoTargetFolderPath = photoTargetFolderPath;
	}

	public void run() {
		try {
			addPhotosToList();
			buildTargetNameSourceFileMap();
			copyFilesToNewLocation();
			printStatistics();
		}
 		catch (IOException e) {
		}	
	}


	public void addPhotosToList() {
		photos = new ArrayList<File>();	
		File rootDirectory = new File(photoRootFolderPath);
		addPhotosInDirectoryToList(rootDirectory, photos);
	}
	
	/**
	 * Add photos to list recursively
     */
	public void addPhotosInDirectoryToList(File directory, List<File> photos) {
		for (File file : directory.listFiles()) {
			
			if (file.isFile()) {
			    numberOfFilesChecked++;
				if (file.getName().contains(".")) {
					String fileExtension = PhotoUtil.getFileExtension(file);
					// System.out.println("  Found photo ext: " + fileExtension);

					if (PhotoUtil.isPhotoExtension(fileExtension)) {
					    numberOfPhotoFilesFound++;
						// System.out.println(" Found photo file: " + file.getName());
						photos.add(file);
					}
					else {
					    numberOfNonPhotoFilesFound++;
						System.out.println("   Non-photo file: " + file.getName());
					}
				}
				else {
				    numberOfNonPhotoFilesFound++;
					System.out.println("   Non-photo file: " + file.getName());
				}
			}
			else if (file.isDirectory()) {
			    numberOfFoldersChecked++;
				addPhotosInDirectoryToList(file, photos);	
			}
		}
	}

	
	public void buildTargetNameSourceFileMap() throws IOException {
	    
		targetNameSourceFileMap = new HashMap<String, File>(photos.size());
		
		String creationDateAttributeName = "kMDItemContentCreationDate";
		String creationDateValuePatternString = "kMDItemContentCreationDate     = [0-9]{2}([0-9]{2})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2}).+";
		Pattern creationDateValuePattern = Pattern.compile(creationDateValuePatternString);
		// Loop over the list of found photos...
		for (File photo : photos) {
			// System.out.println(photo.getCanonicalPath());	
			Runtime runtime = Runtime.getRuntime();
			String[] execCommandParts = {"mdls", photo.getCanonicalPath()};
			Process process = runtime.exec(execCommandParts);
			
			// Get the input...
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			// Get the error stream...
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// read the output from the command
			String s = null;
			boolean foundCreationDate = false;
			while ((s = stdInput.readLine()) != null) {
				if (s.startsWith(creationDateAttributeName)) {
   					// System.out.println(s);
					Matcher matcher = creationDateValuePattern.matcher(s);
					if (matcher.find()) {
						String newFileName = matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3) + "-" +
                                             matcher.group(4) + "." + matcher.group(5) + "." + matcher.group(6);
						//System.out.println("New file name: " + newFileName);
						addPhotoToTargetNameSourceFileMap(newFileName, photo);
					}	
					foundCreationDate = true;
					break;
				}
			}
	
			// Check that we found what we were looking for...
			if (!foundCreationDate) {
			    numberOfFilesWithNoCreationDate++;
				System.out.println("No creation date: " + photo.getCanonicalPath());	
			}

			boolean errorFound = false;
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
   				System.out.println("****ERROR*****\n" + s);
   				errorFound = true;
			}
			
			if (errorFound) {
			   numberOfFilesWithErrors++; 
			}
		}	
	}
	
	protected void copyFilesToNewLocation() throws IOException {
	   
	    System.out.println("Copying files...");
	    
	    new File(photoTargetFolderPath).mkdirs();
	    CopyOption[] options = new CopyOption[]{
            StandardCopyOption.COPY_ATTRIBUTES
        };
	     
	    for (Map.Entry<String, File> targetNameSourceFilePair : targetNameSourceFileMap.entrySet()) {
	        String fileExtension = PhotoUtil.getFileExtension(targetNameSourceFilePair.getValue().getName());
	        String targetFilePath = photoTargetFolderPath + "/" + targetNameSourceFilePair.getKey() + "." + fileExtension;
	        Path target = Paths.get(targetFilePath);
	        Files.copy(targetNameSourceFilePair.getValue().toPath(), target, options);
	        numberOfFilesCopied++;
	    }
	   
	    System.out.println("Copying complete.");
	}
	
	protected void addPhotoToTargetNameSourceFileMap(String newFileName, File sourcePhotoFile) {
	    
	    // Check if we already have the target file name...
	    if (!targetNameSourceFileMap.containsKey(newFileName)) {
	        targetNameSourceFileMap.put(newFileName, sourcePhotoFile);
        }
        else {
            // We have a duplicate so add it...
            addPhotoWithDuplicateNameToTargetNameSourceFileMap(newFileName, sourcePhotoFile);
        }
	    
	}
	
	
	protected void addPhotoWithDuplicateNameToTargetNameSourceFileMap(String newFileName, File sourcePhotoFile) {
	    
	    // Add a '-' to the file name
	    newFileName += '-';
	    int counter = 0;
	   
	    do {
	        counter++;
	        
	        // increment the newly appended digit
	        newFileName = newFileName.substring(0, newFileName.lastIndexOf('-') + 1) + counter;
	    }
	    while (targetNameSourceFileMap.containsKey(newFileName)); 
	   
	    System.out.println("New new file name: " + newFileName);
	    
	    // When we're here we have a unique file name, so add it...
	    targetNameSourceFileMap.put(newFileName, sourcePhotoFile);
	    
	}

	protected void printStatistics() {
	    System.out.println("    Folders checked: " + numberOfFoldersChecked);
	    System.out.println("      Files checked: " + numberOfFilesChecked);
	    System.out.println("    Non-photo files: " + numberOfNonPhotoFilesFound);
	    System.out.println("        Photo files: " + numberOfPhotoFilesFound);
	    System.out.println("       Copied files: " + numberOfFilesCopied);
	    System.out.println("   No creation date: " + numberOfFilesWithNoCreationDate);
	    System.out.println("        Error files: " + numberOfFilesWithErrors);
	}
	
	/**
	 * Entry point 
	 * 
	 * @param args array containing one item with path to directory
	 */
	public static void main(String[] args) {
		System.out.println("Going to run photo renamer...");
		PhotoRenamer photoRenamer = new PhotoRenamer(args[0], args[1]);	
		photoRenamer.run();
		System.out.println("Done.");
	}
}

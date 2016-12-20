package pol.picfix;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

public class PhotoRenamer {

	private static Set<String> photoFileExtensions;
	private static Map<String, String> fileDateMap;
	private static List<File> photoFiles;

	static {
		photoFileExtensions = new HashSet<String>();
		photoFileExtensions.add("jpg");
		photoFileExtensions.add("gif");
		photoFileExtensions.add("png");
		photoFileExtensions.add("mov");
		photoFileExtensions.add("mp4");
		photoFileExtensions.add("avi");
	}

	private String directoryPath;
	private List<File> photos;

	public PhotoRenamer(String directoryPath) {
		this.directoryPath = directoryPath;	
	}

	public void run() {
		try {
			addPhotosToList();
			buildFilePathDateMap();
		}
 		catch (IOException e) {
		}	
	}


	public void addPhotosToList() {
		photos = new ArrayList<File>();	
		File rootDirectory = new File(directoryPath);
		addPhotosInDirectoryToList(rootDirectory, photos);
	}
	
	/**
	 * Add photos to list recursively
         */
	public void addPhotosInDirectoryToList(File directory, List<File> photos) {
		for (File file : directory.listFiles()) {
			
			if (file.isFile()) {
				if (file.getName().contains(".")) {
					String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
					System.out.println("  Found photo ext: " + fileExtension);

					if (photoFileExtensions.contains(fileExtension)) {
						System.out.println(" Found photo file: " + file.getName());
						photos.add(file);
					}
					else {
						System.out.println("   Non-photo file: " + file.getName());
					}
				}
				else {
					System.out.println("   Non-photo file: " + file.getName());
				}
			}
			else if (file.isDirectory()) {
				addPhotosInDirectoryToList(file, photos);	
			}
		}
	}

	public void buildFilePathDateMap() throws IOException {
		Map<String, String> dateMap;
		String creationDateAttributeName = "kMDItemContentCreationDate";
		String creationDateValuePatternString = "kMDItemContentCreationDate     = [0-9][0-9]([0-9][0-9])-([0-9][0-9])-([0-9][0-9]) ([0-9][0-9]):([0-9][0-9]):([0-9][0-9]).+";
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
   					System.out.println(s);
					Matcher matcher = creationDateValuePattern.matcher(s);
					if (matcher.find()) {
						String newFileName = matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3) + "-" +
                                                                     matcher.group(4) + "." + matcher.group(5) + "." + matcher.group(6) + "-";
						System.out.println("New file name: " + newFileName);
					}	
					foundCreationDate = true;
					break;
				}
			}
	
			// Check that we found what we were looking for...
			if (!foundCreationDate) {
				System.out.println("No creation date: " + photo.getCanonicalPath());	
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
   				System.out.println("****ERROR*****\n" + s);
			}
		}	
	}

	/**
	 * Entry point 
	 * 
	 * @param args array containing one item with path to directory
	 */
	public static void main(String[] args) {
		System.out.println("Going to run photo renamer...");
		PhotoRenamer photoRenamer = new PhotoRenamer(args[0]);	
		photoRenamer.run();
		System.out.println("Done.");
	}
}

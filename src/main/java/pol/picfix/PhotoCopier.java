package pol.picfix;

public class PhotoCopier {

	private String directoryPath;
	private String workingDirectory;

	public PhotoCopier(String path) {
		this.directoryPath = directoryPath;	
	}

	public void run() {
		findAllPhotos();	
	}


	public void findAllPhotos() {

	}
	
	public void findPhotosInFolder() {
		
	}

	/**
	 * Entry point 
	 * 
	 * @param args array containing one item with path to directory
	 */
	public static void main(String[] args) {
		System.out.println("Going to run photo copier...");
		PhotoCopier photoCopier = new PhotoCopier(args[0]);	
		photoCopier.run();
		System.out.println("Done.");
	}
}

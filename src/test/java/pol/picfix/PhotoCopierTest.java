package pol.picfix;

import static org.junit.Assert.*;

import org.junit.Test;

public class PhotoCopierTest {

    @Test
    public void testPhotoCopier() {
        String renamedPhotoFolderPath = "/Users/i079185/Desktop/Photos/Renamed/2003";
        String processedPhotoRootFolderPath = "/Users/i079185/Desktop/Photos/Processed";
        PhotoCopier photoCopier = new PhotoCopier(renamedPhotoFolderPath, processedPhotoRootFolderPath);
        photoCopier.run();
    }

}

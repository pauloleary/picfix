package pol.picfix;

import static org.junit.Assert.*;

import org.junit.Test;

public class PhotoRenamerTest {

    // @Test
    public void testPhotoRenamer() {
        String photoFolderRootPath = "/Users/i079185/Desktop/Photos/Raw/2003";
        String renamedPhotosTargetFolderPath = "/Users/i079185/Desktop/Photos/Renamed/2003";
        PhotoRenamer photoRenamer = new PhotoRenamer(photoFolderRootPath, renamedPhotosTargetFolderPath);
        photoRenamer.run();
    }

}

package org.swalsh.image

import com.google.inject.Inject
import groovy.io.FileType
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Mode
import ratpack.exec.ExecControl
import ratpack.exec.Promise
import ratpack.form.UploadedFile

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static java.util.UUID.randomUUID

@com.google.inject.Singleton
class ImageService {

    private final ExecControl execControl

    @Inject
    ImageService(ExecControl execControl) {
        this.execControl = execControl
    }

    Promise<List<String>> getUploadedImages(File imageDir) {
        execControl.blocking {
            imageDir.listFiles({ it.isFile() } as FileFilter).sort { it.lastModified() }*.name
        }
    }

    boolean isImageFile(UploadedFile file) {
        file.contentType.type.contains("image")
    }

    Promise<File> process(UploadedFile file, File imageDirectory, File thumbsDirectory) {
        String fileName = getUniqueFilename("png")
        BufferedImage image = readImage(file)

        execControl.blocking {
            saveThumb(image, fileName, thumbsDirectory)
            saveImage(image, fileName, imageDirectory)
        }
    }

    String getUniqueFilename(String extension) {
        "${randomUUID()}.$extension"
    }

    BufferedImage readImage(UploadedFile file) {
        ImageIO.read(file.inputStream)
    }

    File saveImage(BufferedImage image, String fileName, File directory) {

        File file = new File(directory, fileName)
        ImageIO.write(image, "png", file)

        file

    }

    File saveThumb(BufferedImage image, String fileName, File directory) {

        BufferedImage thumb = Scalr.resize(image, Mode.FIT_TO_HEIGHT, 100)
        saveImage(thumb, fileName, directory)

    }

}
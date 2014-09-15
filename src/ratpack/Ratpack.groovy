import org.swalsh.image.ImageService
import ratpack.form.Form
import ratpack.jackson.JacksonModule

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

def assetsPath = "public"
def imagesDirName = "uploaded-files"
def imagesPath = "$assetsPath/$imagesDirName"
def thumbsPath = "$imagesPath/thumb"

ratpack {

    bindings {
        add new JacksonModule()
        init {
            launchConfig.baseDir.file(thumbsPath).toFile().mkdirs()
        }
    }

    handlers {
        assets assetsPath, "index.html"
        prefix("image") { ImageService imageService ->
            def baseDir = launchConfig.baseDir
            def imagesDir = baseDir.file(imagesPath).toFile()
            def thumbsDir = baseDir.file(thumbsPath).toFile()

            get {
                imageService.getUploadedImages(imagesDir).then {
                    render json(imagePath: imagesDirName, images: it)
                }
            }
            post("upload") {
                def form = parse Form
                def uploaded = form.file("fileUpload")

                if (imageService.isImageFile(uploaded)) {
                    imageService.process(uploaded, imagesDir, thumbsDir).then {
                        render json(fileName: it.name)
                    }
                } else {
                    response.status(400).send "Invalid file type. Images only!"
                }
            }
        }
    }

}
package lanse.worldwarchess.shared.core.screens.lounge.diego

import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.Timer

object MainSpin {

    //ADD CODE HERE
    @JvmStatic
    fun show() {
        println("Creates the page")
        val frame = JFrame("Spinning GIF")
        frame.setSize(900, 1000)
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

        //creates a image
        val imageURL = "https://images.icon-icons.com/3685/PNG/512/github_logo_icon_229278.png"
        val backgroundImageURL = "https://img.freepik.com/free-vector/realistic-galaxy-background_23-2148991322.jpg?semt=ais_hybrid&w=740&q=80"
        val jamesURL = "https://avatars.githubusercontent.com/u/54397470?s=130&v=4"
        val lanceURL = "https://avatars.githubusercontent.com/u/187788301?s=130&v=4"
        val diegoURL = "https://avatars.githubusercontent.com/u/148488347?v=4"

        val image = loadImage(imageURL)
        val background = loadImage(backgroundImageURL)

        //the devs
        val jamesImage = loadImage(jamesURL)
        val lanceImage = loadImage(lanceURL)
        val diegoImage = loadImage(diegoURL)

        //will print the image into the frame
        if (image != null && background != null &&
            jamesImage != null && lanceImage != null && diegoImage != null) {
            val panel = SpinningPanel(image, background, jamesImage, lanceImage, diegoImage)

            //timer to animate the spin
            val timer = Timer(30) {
                panel.angle += 0.05 // speed of spin
                panel.repaint()
            }
            timer.start()

            frame.add(panel)
        } else { //image could not be loaded
            frame.add(JLabel("Could not load image from URL."))
        }

        //ALWAYS HAVE THIS AT THE END
        frame.isVisible = true
        frame.revalidate()
        frame.repaint()
    }

    //making the image system
    fun loadImage(imageURL: String): BufferedImage? {
        //loads the image
        return try {
            val url = URL(imageURL)
            ImageIO.read(url) //reads the url
        } catch (e: IOException) { //if no image can be loaded then it will have an error
            e.printStackTrace()
            System.err.println("Error loading image from URL: $imageURL")
            null
        }
        //returns the image now processed and ready to use
    }
}

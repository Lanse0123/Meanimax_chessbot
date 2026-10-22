package lanse.worldwarchess.shared.core.screens.lounge.diego

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

class SpinningPanel(
    private val image: BufferedImage,
    private val background_image: BufferedImage,
    //images of the ddevs to transfer to the nextPanel
    private val james: BufferedImage,
    private val lance: BufferedImage,
    private val diego: BufferedImage
) : JPanel() {

    var angle = 0.0
    private var imageBounds: Rectangle? = null //trackable image area
    private var fadeAlpha = 0f

    //calls the image from main
    init {
        println("gets the image and background image")
        println("getting the images of the devs")

        //mouse listener
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (imageBounds != null && imageBounds!!.contains(e.point)) {
                    println("Image Clicked")
                    transitionEffect()
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        /*Drawing the background*/
        g2d.drawImage(background_image, 0, 0, width, height, null)
//        System.out.println("background drawn");

        /*Spinning code*/
        //getting the width and height of the image
        val w = image.width
        val h = image.height

        //Used to center the item by getting the height and the width of the panel
        val cx = width / 2
        val cy = height / 2

        val scaleX = kotlin.math.cos(angle)

        //saving the transform
        val old: AffineTransform = g2d.transform

        g2d.translate(cx.toDouble(), cy.toDouble()) //moves the panel with the help of cy and cx
        g2d.scale(scaleX, 1.0) //spins horizantol
        g2d.drawImage(image, -w / 2, -h / 2, null) //gets the high and length of the image and draws it

        //restoring the orginal location
        g2d.transform = old

        //saving the clickable bounds
        imageBounds = Rectangle(cx - w / 2, cy - h / 2, w, h)

        //transition screen section
        if (fadeAlpha > 0f) {
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha)
            g2d.color = Color(61, 37, 135)
            g2d.fillRect(0, 0, width, height)
            g2d.composite = AlphaComposite.SrcOver // reset
        }
    }

    //transition effect
    fun transitionEffect() {
        val timer = Timer(30, null)
        timer.addActionListener {
            fadeAlpha = kotlin.math.min(1f, fadeAlpha + 0.05f)
            if (fadeAlpha >= 1f) {
                timer.stop()
                // Swap to next panel
                val topFrame = SwingUtilities.getWindowAncestor(this) as JFrame
                topFrame.contentPane = NextPanel(this, james, lance, diego)
                topFrame.revalidate()
            }
            repaint() // trigger paintComponent again
        }
        timer.start()
    }

    //making the reset fade so i can finally go back to the orginal screen
    fun resetFade() {
        fadeAlpha = 0f
    }

    //making a transiton in
    fun transitionIn() {
        fadeAlpha = 1f
        val timer = Timer(30, null)
        timer.addActionListener {
            fadeAlpha = kotlin.math.max(0f, fadeAlpha - 0.05f)
            if (fadeAlpha <= 0f) {
                timer.stop()
            }
            repaint()
        }
        timer.start()
    }
}

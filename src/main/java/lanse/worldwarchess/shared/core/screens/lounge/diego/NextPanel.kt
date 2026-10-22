package lanse.worldwarchess.shared.core.screens.lounge.diego

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.*

class NextPanel(
    private val orginal_panal: SpinningPanel,
    private val james_image: BufferedImage?,
    private val lance_image: BufferedImage?,
    private val diego_image: BufferedImage?
) : JPanel() {

    init {
        background = Color(61, 37, 135)
        layout = BorderLayout()

        val title = JLabel("The Devs", SwingConstants.CENTER)
        title.foreground = Color.WHITE
        title.font = title.font.deriveFont(Font.BOLD, 24f)

        //making "The Devs" a clickable button
        title.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                println("Going back to the orginal screen")
                // trigger to go back to the orginal panal
                val topFrame = SwingUtilities.getWindowAncestor(this@NextPanel) as? JFrame
                if (topFrame != null) {
                    topFrame.contentPane = orginal_panal
                    topFrame.revalidate()
                    orginal_panal.resetFade()
                    topFrame.contentPane = orginal_panal
                    topFrame.revalidate()
                    orginal_panal.transitionIn()
                }
            }
        })

        add(title, BorderLayout.NORTH)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D

        //drawing james
        if (james_image != null) {
            g2d.drawImage(james_image, 50, 250, 150, 150, this)
            g2d.color = Color.WHITE
            g2d.font = Font("SansSerif", Font.BOLD, 20)
            g2d.drawString("jamesMFelder", 50, 425)
        }

        if (lance_image != null) {
            g2d.drawImage(lance_image, 350, 250, 150, 150, this)
            g2d.color = Color.WHITE
            g2d.font = Font("SansSerif", Font.BOLD, 20)
            g2d.drawString("Lanse0123", 350, 425)
        }

        if (diego_image != null) {
            g2d.drawImage(diego_image, 650, 250, 150, 150, this)
            g2d.color = Color.WHITE
            g2d.font = Font("SansSerif", Font.BOLD, 20)
            g2d.drawString("Spiderasasen", 650, 425)
        }
    }
}

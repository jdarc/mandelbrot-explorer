import java.awt.*
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider

class MainFrame : JFrame("Mandelbrot Explorer") {
    private val palette = Palette(0x0AFC84, 0x3264F0, 0xE63C14, 0xE6AA00, 0xAFAF0A, 0x5A0032, 0xB45A78, 0xFF1428, 0x1E46C8, 0x0AFC84)
    private val config = Configuration(palette, 256)
    private val viewport = Viewport(config)

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        background = Color.BLACK
        ignoreRepaint = true

        contentPane.layout = BorderLayout()
        contentPane.add(viewport, BorderLayout.CENTER)
        contentPane.add(createIterationSlider(), BorderLayout.SOUTH)

        val screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        val bounds = screenDevices.first().defaultConfiguration.bounds

        size = Dimension(bounds.width * 80 / 100, bounds.height * 90 / 100)
        minimumSize = Dimension(360, 240)

        location = Point(bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2)
    }

    private fun createIterationSlider(): JPanel {
        val panel = JPanel(BorderLayout())
        val slider = JSlider(4, 8192, config.iterations)
        slider.isFocusable = false
        slider.majorTickSpacing = 256
        slider.minorTickSpacing = 32
        slider.addChangeListener {
            config.iterations = slider.value
            viewport.render()
        }
        panel.add(JLabel("Iterations"), BorderLayout.WEST)
        panel.add(slider, BorderLayout.CENTER)
        return panel
    }
}

import java.awt.*
import java.awt.event.*
import java.util.concurrent.ForkJoinPool
import javax.swing.JPanel
import kotlin.math.min

class Viewport(private val configuration: Configuration) : JPanel() {
    private var raster = Bitmap(64, 64)
    private var task: Generator? = null
    private var mouseDown = Point()
    private var mouseDrag = Point()
    private var dragging = false

    override fun paintComponent(g: Graphics) {
        g as Graphics2D
        raster.draw(g)

        if (dragging) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

            g.color = Color.YELLOW
            g.stroke = BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
            g.drawLine(mouseDown.x, mouseDown.y, mouseDrag.x, mouseDrag.y)

            g.color = Color.BLACK
            g.stroke = BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1F, floatArrayOf(2F), 2F)
            g.drawLine(mouseDown.x, mouseDown.y, mouseDrag.x, mouseDrag.y)
        }
    }

    fun render() {
        if (width < 1 || height < 1) return
        if (width != raster.width || height != raster.height) raster = Bitmap(width, height)
        task?.kill()
        ForkJoinPool.commonPool().execute {
            task = Generator(configuration, raster, bounds) { repaint() }
            ForkJoinPool.commonPool().invoke(task)
            repaint()
        }
    }

    private fun zoomIn(e: MouseEvent) {
        val point = configuration.point
        val r = point.z / min(raster.width, raster.height)
        val x = point.x + (e.x - raster.width * 0.25) * r
        val y = point.y + (e.y - raster.height * 0.25) * r
        val z = point.z * 0.5
        configuration.point = Vector3(x, y, z)
        render()
    }

    private fun zoomOut() {
        val point = configuration.point
        val r = 0.5 * point.z / min(raster.width, raster.height)
        val x = point.x - raster.width * r
        val y = point.y - raster.height * r
        val z = point.z * 2.0
        configuration.point = Vector3(x, y, z)
        render()
    }

    private fun reset() {
        configuration.reset()
        render()
    }

    init {
        isFocusable = true
        reset()

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ESCAPE -> reset()
                    KeyEvent.VK_MINUS -> zoomOut()
                }
            }
        })

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) = requestFocus()

            override fun mousePressed(e: MouseEvent) {
                mouseDown = Point(e.x, e.y)
                mouseDrag = Point(e.x, e.y)
                dragging = true
            }

            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount > 1) zoomIn(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                dragging = false
                if (e.x != mouseDown.x || e.y != mouseDown.y) {
                    val point = configuration.point
                    val r = point.z / min(raster.width, raster.height)
                    val x = point.x + (mouseDown.x - e.x) * r
                    val y = point.y + (mouseDown.y - e.y) * r
                    configuration.point = Vector3(x, y, point.z)
                    render()
                }
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                mouseDrag = Point(e.x, e.y)
                repaint()
            }
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                super.componentResized(e)
                render()
            }
        })
    }
}

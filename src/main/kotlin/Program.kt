import javax.swing.UIManager

object Program {
    @JvmStatic
    fun main(args: Array<String>) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        javax.swing.SwingUtilities.invokeLater {
            MainFrame().apply { isVisible = true }
        }
    }
}

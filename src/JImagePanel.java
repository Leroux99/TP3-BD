
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author Leroux
 */
public class JImagePanel extends JPanel {
    private Image img;

  public JImagePanel(String img) {
    this.img = new ImageIcon(img).getImage();
  }

    @Override
  public void paintComponent(Graphics g) {
    g.drawImage(img, 0, 0, null);
  }
}

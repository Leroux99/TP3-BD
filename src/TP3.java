/**
 * Créé par:
 * Jérémie Leroux
 * Joaquin Lee-Martinez
 */

import java.awt.*;
import java.util.ArrayList;

public class TP3 {
    public static void main(String[] args) {
        Menu menu = new Menu();
        ArrayList<Image> icones = new ArrayList<>();
        icones.add(new javax.swing.ImageIcon("Images\\Icone16.png").getImage());
        icones.add(new javax.swing.ImageIcon("Images\\Icone32.png").getImage());
        icones.add(new javax.swing.ImageIcon("Images\\Icone64.png").getImage());
        menu.setIconImages(icones);
        menu.setLocationRelativeTo(null);
        menu.setVisible(true);
    }
}

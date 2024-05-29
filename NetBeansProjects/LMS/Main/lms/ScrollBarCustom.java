package lms;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import lms.ModernScrollBarUI;

public class ScrollBarCustom extends JScrollBar {

    public ScrollBarCustom() {
        setUI(new ModernScrollBarUI());
        setPreferredSize(new Dimension(8, 8));
        setForeground(new Color(232,240,214));
        setBackground(new Color(232,240,214));
        setUnitIncrement(20);
    }
}

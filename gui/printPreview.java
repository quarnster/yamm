package org.gjt.fredde.yamm.gui;

import javax.swing.*;
import java.awt.*;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMM;

public class printPreview extends JDialog {

  YAMM frame;

  public printPreview(JFrame frame2, final int mail) {
    setBounds(0, 0, 592, 842);
    setResizable(false);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());

    frame = (YAMM)frame2;

    JPanel panel = new JPanel() {
      public void paint(Graphics g) {
        g.drawRect(0, 0, 592, 842);
        Mailbox.getMailForPrint(frame.selectedbox, mail, g);
      }
    };
    getContentPane().add("Center", new JScrollPane(panel));
    show();
  }
}

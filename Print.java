package org.gjt.fredde.yamm;

import java.awt.print.*;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JFrame;
import org.gjt.fredde.yamm.mail.Mailbox;
import org.gjt.fredde.yamm.YAMM;

public class Print {

  YAMM frame;
  int mail;

  public Print(JFrame frame2, int mail2) {
    frame = (YAMM)frame2;
    mail = mail2;
    // Get a PrinterJob
    PrinterJob job = PrinterJob.getPrinterJob();

    // Create a landscape page format
    PageFormat landscape = job.defaultPage();
    landscape.setOrientation(PageFormat.LANDSCAPE);

    // Set up a book
    Book bk = new Book();
 //   Mailbox.getMailForPrint(frame.selectedbox, mail, bk);
//    bk.append(new PaintCover(), job.defaultPage());

    // Pass the book to the PrinterJob
    job.setPageable(bk);

    // Put up the dialog box
    if (job.printDialog()) {

        // Print the job if the user didn't cancel printing
        try { job.print(); }
            catch (Exception exc) { System.err.println("error: " + exc); }
    }
  }
/*
  class PaintCover implements Printable {
    public int print(Graphics g, PageFormat pf, int pageIndex)
         throws PrinterException {
      g.setFont(new Font("Helvetica", Font.PLAIN, 10));
      g.setColor(Color.black);
//    g.drawString("Sample Shapes", 100, 200);
      Mailbox.getMailForPrint(frame.selectedbox, mail, g);

      return Printable.PAGE_EXISTS;
    }
  }
*/
}



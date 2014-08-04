package MoF;


import amidst.Amidst;
import amidst.Options;
import amidst.gui.menu.AmidstMenu;
import amidst.preferences.BooleanPrefModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class FinderWindow extends JFrame {
	private static final long serialVersionUID = 196896954675968191L;
	public static FinderWindow instance;
	private Container pane;
	public Project curProject;  //TODO
	public static boolean dataCollect;
	private final AmidstMenu menuBar;
    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();
    Project p;
	public FinderWindow() {
		//Initialize window
		super("Amidst v" + Amidst.version());
		
		setSize(1000,800);
		//setLookAndFeel();
		pane = getContentPane();
		//UI Manager:
		pane.setLayout(new BorderLayout());
		new UpdateManager(this, true).start();
		setJMenuBar(menuBar = new AmidstMenu(this));
		setVisible(true);
		setIconImage(Amidst.icon);
		instance = this;

        Runnable task = new Runnable() {
            public void run() {
                if (Options.instance.cliseed != null) {
                    setSize(800, 800);

                    p =new Project(Options.instance.cliseed, "default");

                    clearProject();
                    setProject(p);
                    //p.map.getMap().setZoom(0.6);
                    //p.map.getMap().width = 1024;
                    //Point mouse = new Point(790 >> 1, 790 >> 1);

                    //p.map.getMap().setZoom(0.65);
                    //p.map.centerAt(-512,0);
                    p.map.saveToFile(new File("/tmp/" + Options.instance.cliseed + ".png"));
                    //Point p1 = new Point(10,10);
                    //p.map.adjustZoom(p1, 5000);
                }
            }
        };
        Runnable task2 = new Runnable() {
            public void run() {
                if (Options.instance.cliseed != null) {
                    p.map.getMap().setZoom(0.6);
                    p.moveMapTo(0,0);
                    //p.map.saveToFile(new File("/tmp/" + Options.instance.cliseed + ".png"));
                    Container c = p.map;
                    BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    c.paint(im.getGraphics());
                    try {
                        ImageIO.write(im, "PNG", new File("/tmp/shot.png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        worker.schedule(task, 1, TimeUnit.SECONDS);
        worker.schedule(task2, 2, TimeUnit.SECONDS);


		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
	}

	public void clearProject() {
		// FIXME Release resources.
		if (curProject != null) {
			removeKeyListener(curProject.getKeyListener());
			curProject.dispose();
			pane.remove(curProject);
			System.gc();
		}
	}
	public void setProject(Project ep) {
		menuBar.mapMenu.setEnabled(true);
		curProject = ep;

		addKeyListener(ep.getKeyListener());
		pane.add(curProject, BorderLayout.CENTER);

		this.validate();
	}
}

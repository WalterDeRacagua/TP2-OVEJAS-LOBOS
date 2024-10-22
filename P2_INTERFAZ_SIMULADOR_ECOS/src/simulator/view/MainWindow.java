package simulator.view;

import javax.swing.JFrame;
import simulator.control.Controller;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		ControlPanel contrPanel = new ControlPanel(this._ctrl);
		mainPanel.add(contrPanel, BorderLayout.PAGE_START);

		StatusBar statsBar = new StatusBar(this._ctrl);
		mainPanel.add(statsBar, BorderLayout.PAGE_END);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		InfoTable speciesTable = new InfoTable("Species", new SpeciesTableModel(_ctrl));
		speciesTable.setPreferredSize(new Dimension(250, 250));
		contentPanel.add(speciesTable, BorderLayout.PAGE_START);
		
		InfoTable stateAnimal = new InfoTable("Animals State", new StateAnimalTableModel(_ctrl));
		stateAnimal.setPreferredSize(new Dimension(250, 250));
		contentPanel.add(stateAnimal, BorderLayout.PAGE_START);

		InfoTable regionsTable = new InfoTable("Regions", new RegionsTableModel(_ctrl));
		regionsTable.setPreferredSize(new Dimension(250, 250));
		contentPanel.add(regionsTable, BorderLayout.CENTER);


		InfoTable maxSpeedTable = new InfoTable("MaxSpeed", new MaxSpeedTableModel(_ctrl));
		maxSpeedTable.setPreferredSize(new Dimension(100, 250));
		contentPanel.add(maxSpeedTable, BorderLayout.PAGE_END);

		
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public JFrame getFrame() {
		return this;
	}

}

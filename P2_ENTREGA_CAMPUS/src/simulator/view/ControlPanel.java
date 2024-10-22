package simulator.view;

import simulator.control.Controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.*;

import org.json.JSONObject;
import org.json.JSONTokener;

class ControlPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;
	private JToolBar _toolaBar;
	private JFileChooser _fc;
	private boolean _stopped = true;
	private JButton _openButton;
	private JButton _viewerButton;
	private JButton _regionsButton;
	private JButton _runButton;
	private JButton _stopButton;
	private JButton _quitButton;
	private JSpinner _stepsSpinner;
	private JTextField _dtTextField;

	public ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		_toolaBar = new JToolBar();
		add(_toolaBar, BorderLayout.PAGE_START);

		this._fc = new JFileChooser();
		File dirDefault = new File(System.getProperty("user.dir") + "/resources/examples");
		this._fc.setCurrentDirectory(dirDefault);
		this._fc.setSelectedFile(new File(dirDefault, "ex1.json"));

// Open Button	
		this._openButton = new JButton();
		this._openButton.setToolTipText("Open");
		this._openButton.setIcon(new ImageIcon("resources/icons/open.png"));
		this._openButton.addActionListener((e) -> this.openFileButton());
		this._toolaBar.add(this._openButton);

// ViewerButton
		this._toolaBar.addSeparator();
		this._viewerButton = new JButton();
		this._viewerButton.setToolTipText("Viewer");
		this._viewerButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
		this._viewerButton.addActionListener((e) -> {
			try {
				if (this._fc.getSelectedFile() == null)
					throw new Exception();
				this.openWindowButton();
			} catch (Exception t) {
				ViewUtils.showErrorMsg("<html><p>Obligatory use input file.</p></html>");
			}
		});
		this._toolaBar.add(this._viewerButton);

//Regions Button
		this._regionsButton = new JButton();
		this._regionsButton.setToolTipText("Change Regions");
		this._regionsButton.setIcon(new ImageIcon("resources/icons/regions.png"));
		this._regionsButton.addActionListener((e) -> changeRegions());
		this._toolaBar.add(this._regionsButton);

// Run Button
		this._toolaBar.addSeparator();
		this._runButton = new JButton();
		this._runButton.setToolTipText("Run");
		this._runButton.setIcon(new ImageIcon("resources/icons/run.png"));
		this._runButton.addActionListener((e) -> {
			try {
				if (this._fc.getSelectedFile() == null)
					throw new Exception();
				this.runOperationButton();
			} catch (Exception t) {
				ViewUtils.showErrorMsg("<html><p>Obligatory use input file.</p></html>");
			}
		});

		this._toolaBar.add(this._runButton);

// Stop Button
		this._stopButton = new JButton();
		this._stopButton.setToolTipText("Stop");
		this._stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
		this._stopButton.addActionListener((e) -> this.stopOperationButton());
		this._toolaBar.add(this._stopButton);

// Steps Spinner:
		// initial value = 10000 (first parameter)
		// initial range value = 1 (second parameter)
		// final range value = 10000 (third parameter)
		// increment = 100 (fourth parameter)
		_toolaBar.addSeparator();
		_toolaBar.add(new JLabel(" Steps:"));
		_stepsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		_stepsSpinner.setToolTipText("Simulation steps to run: 1-10000");
		_stepsSpinner.setMaximumSize(new Dimension(80, 40));
		_stepsSpinner.setMinimumSize(new Dimension(80, 40));
		_stepsSpinner.setPreferredSize(new Dimension(80, 40));
		_toolaBar.add(_stepsSpinner);

// Delta-Time TextField
		_toolaBar.addSeparator();
		_toolaBar.add(new JLabel(" Delta-Time:"));
		_dtTextField = new JTextField("0.03", 5);
		_dtTextField.setToolTipText("Real time (seconds) corresponding to a step");
		_dtTextField.setMaximumSize(new Dimension(80, 40));
		_toolaBar.add(_dtTextField);

// Quit Button
		_toolaBar.add(Box.createGlue());
		_toolaBar.addSeparator();
		_quitButton = new JButton();
		_quitButton.setToolTipText("Quit");
		_quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
		_quitButton.addActionListener((e) -> ViewUtils.quit(this));
		_toolaBar.add(_quitButton);

	}

	private void run_sim(int n, double dt) {
		if (n > 0 && !_stopped) {
			try {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				_ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> {
					run_sim(n - 1, dt);
				});
			} catch (Exception e) {
				ViewUtils.showErrorMsg(e.getMessage());
				this.activateButtons();
				_stopped = true;
			}
		} else {
			this.activateButtons();
			_stopped = true;
		}
	}

	private void activateButtons() {
		this._quitButton.setEnabled(true);
		this._openButton.setEnabled(true);
		this._regionsButton.setEnabled(true);
		this._stopButton.setEnabled(true);
		this._viewerButton.setEnabled(true);

	}

	private void deactivateButtons() {
		this._quitButton.setEnabled(false);
		this._openButton.setEnabled(false);
		this._regionsButton.setEnabled(false);
		this._viewerButton.setEnabled(false);
	}

	private void openFileButton() {
		int isOpen = this._fc.showOpenDialog(ViewUtils.getWindow(this));
		if (isOpen == JFileChooser.APPROVE_OPTION) {

			File file = this._fc.getSelectedFile();

			try {
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				JSONObject jo = new JSONObject(new JSONTokener(sb.toString()));
				int cols = jo.getInt("cols");
				int rows = jo.getInt("rows");
				int width = jo.getInt("width");
				int height = jo.getInt("height");

				this._ctrl.reset(cols, rows, width, height);
				this._ctrl.load_data(jo);
			} catch (Exception e) {

				ViewUtils.showErrorMsg(e.getMessage());
			}

		}
	}

	private void openWindowButton() {
		new MapWindow((Frame) SwingUtilities.getWindowAncestor(this), this._ctrl);
	}

	private void runOperationButton() {
		try {
			double dt = 0.03;
			dt = Double.parseDouble(_dtTextField.getText());
			this.deactivateButtons();
			this._stopped = false;
			int steps = (Integer) _stepsSpinner.getValue();
			try {
				if (this._fc.getSelectedFile() == null)
					throw new Exception();
				this.run_sim(steps, dt);
			} catch (Exception e) {
				ViewUtils.showErrorMsg("<html><p>Obligatory use input file.</p></html>");
			}
		} catch (Exception e) {
			ViewUtils.showErrorMsg("<html><p>Format incorrect of delta-time.</p></html>");
			this._dtTextField.setText("0.03");
		}

	}

	private void stopOperationButton() {
		this._stopped = true;
	}

	private void changeRegions() {
		_changeRegionsDialog = new ChangeRegionsDialog(this._ctrl);
		_changeRegionsDialog.open(SwingUtilities.getWindowAncestor(this));
	}
}

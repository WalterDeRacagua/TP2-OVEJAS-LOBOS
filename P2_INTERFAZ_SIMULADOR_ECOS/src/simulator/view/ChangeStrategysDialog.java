package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

public class ChangeStrategysDialog extends JDialog implements EcoSysObserver{
	private static final long serialVersionUID = 1L;
	private DefaultComboBoxModel<String> _stateToStrategysModel;
	private DefaultComboBoxModel<String> _strategysModel;
	private DefaultComboBoxModel<String> _specieModel;
	private List<JSONObject> _strategiesInfo;
	private List<JSONObject> _speciesInfo;
	private Controller _ctrl;
	private String[] _headers = { "Specie", "Type Strategy", "Description" };
	private JSONObject data;
	private JSONObject joDataOut;
	private List<String> dataValues;
	private int _status;


	public ChangeStrategysDialog(Controller ctrl) {
		super((Frame) null, true);
		_ctrl = ctrl;
		_ctrl.addObserver(this);
		_status = 0;
		initGUI();
	}

	private void initGUI() {
		_strategiesInfo = Main._strategies.get_info();
		_speciesInfo=Main._animals.get_info();
		setTitle("[CHANGE STRATEGIES]");
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);
		setPreferredSize(new Dimension(700, 400));

		JLabel cabeceraLabel = new JLabel(
				"<html><p>Select a strategy type, to mate/hunger state ,and select specie of you want changes</html>");
		cabeceraLabel.setAlignmentX(CENTER_ALIGNMENT);
		cabeceraLabel.setPreferredSize(new Dimension(650, 50));


		JPanel panelInferior = new JPanel();
		initComboBoxAll();
		rellenarComboBoxAll();
		insertComponentsInPanelInferior(panelInferior);

		JPanel panelAceptar = new JPanel();
		insertComponentsInPanelAceptar(panelAceptar);

		mainPanel.add(cabeceraLabel);
		mainPanel.add(panelInferior);
		mainPanel.add(panelAceptar);

		pack();
		setResizable(false);
		setVisible(false);
	}

	

	private void insertComponentsInPanelAceptar(JPanel panelAceptar) {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> {
			_status = 0;
			setVisible(false);
		});
		panelAceptar.add(cancelButton);

		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> {
			if (_status == 1) {
				keepChanges();
				ChangeStrategysDialog.this.setVisible(false);
			} else {
				ViewUtils.showErrorMsg("No has hecho ningun cambio en las regiones");
				ChangeStrategysDialog.this.setVisible(false);
			}
		});
		panelAceptar.add(okButton);
	}


	private void keepChanges() {
		_status = 0;
		String specie,state,strategy;
		strategy=(String) _strategysModel.getSelectedItem();
		specie=(String) _specieModel.getSelectedItem();
		state=(String) _stateToStrategysModel.getSelectedItem();


		try {
			_ctrl.set_strategies(specie,state,strategy);
		} catch (Exception e) {
			ViewUtils.showErrorMsg("Error. " + e.getMessage());
		}
	}

	private void insertComponentsInPanelInferior(JPanel p) {
		JLabel strategiesLabel = new JLabel("Strategy Type: ");
		p.add(strategiesLabel);
		JComboBox strategiesModel = new JComboBox(_strategysModel);
		strategiesModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = strategiesModel.getSelectedIndex();
				_status = 1;
			}
		});
		p.add(strategiesModel);

		JLabel toHungerMateLabel = new JLabel("To Hunger/mate ");
		p.add(toHungerMateLabel);
		JComboBox state = new JComboBox(_stateToStrategysModel);
		state.addActionListener(e -> _status = 1);
		p.add(state);
		JLabel toSpecieLabel = new JLabel("To Specie: ");
		p.add(toSpecieLabel);
		JComboBox specie = new JComboBox(_specieModel);
		specie.addActionListener(e -> _status = 1);
		p.add(specie);

	}


	private void initComboBoxAll() {
		_stateToStrategysModel = new DefaultComboBoxModel<>();
		_strategysModel = new DefaultComboBoxModel<>();
		_specieModel = new DefaultComboBoxModel<>();

	}

	private void rellenarComboBoxAll() {
		rellenarComboBoxStates();
		rellenarComboBoxStrategies();
		rellenarComboBoxSpecies();
	}

	private void rellenarComboBoxStrategies() {
		for (JSONObject jo : _strategiesInfo) {
			String type = jo.getString("type");
			_strategysModel.addElement(type);
		}
	}

	private void rellenarComboBoxStates() {
		_stateToStrategysModel.addElement(State.HUNGER.name());
		_stateToStrategysModel.addElement(State.MATE.name());



	}

	private void rellenarComboBoxSpecies() {
		for (JSONObject jo : _speciesInfo) {
			String type = jo.getString("type");
			_specieModel.addElement(type);
		}

	}



	public void open(Window parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
	}

}

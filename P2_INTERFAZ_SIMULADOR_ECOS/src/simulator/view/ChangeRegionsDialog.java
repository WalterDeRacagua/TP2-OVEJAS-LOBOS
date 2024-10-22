package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;
import org.json.JSONArray;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<String> _fromRowModel;
	private DefaultComboBoxModel<String> _toRowModel;
	private DefaultComboBoxModel<String> _fromColModel;
	private DefaultComboBoxModel<String> _toColModel;
	private DefaultTableModel _dataTableModel;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;
	private String[] _headers = { "Key", "Value", "Description" };
	private JSONObject data;
	private JSONObject joDataOut;
	private List<String> dataValues;
	private int _status;

	private MapInfo map;

	public ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		_ctrl = ctrl;
		_ctrl.addObserver(this);
		_status = 0;
		initGUI();
	}

	private void initGUI() {
		_regionsInfo = Main._regions_factory.get_info();
		setTitle("[CHANGE REGIONS]");
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);
		setPreferredSize(new Dimension(700, 400));

		JLabel cabeceraLabel = new JLabel(
				"<html><p>Select a region type, the rows/cols interval,and provide values for the parameters in the <b>value column</b> (default values are used for parametres with no value).</p></html>");
		cabeceraLabel.setAlignmentX(CENTER_ALIGNMENT);
		cabeceraLabel.setPreferredSize(new Dimension(650, 50));

		JPanel panelCentral = new JPanel();
		panelCentral.setAlignmentX(CENTER_ALIGNMENT);
		JTable dataTable = initDefaultDataTableModel();
		dataTable.setAlignmentX(CENTER_ALIGNMENT);
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(300);
		for (int i = 0; i < dataTable.getColumnCount(); i++) {
			dataTable.getColumnModel().getColumn(i).setResizable(false);
		}

		JScrollPane scroll = new JScrollPane(dataTable);
		scroll.setAlignmentX(CENTER_ALIGNMENT);
		scroll.setPreferredSize(new Dimension(680, 250));
		panelCentral.add(scroll, BorderLayout.CENTER);

		JPanel panelInferior = new JPanel();
		initComboBoxAll();
		rellenarComboBoxAll();
		insertComponentsInPanelInferior(panelInferior);

		JPanel panelAceptar = new JPanel();
		insertComponentsInPanelAceptar(panelAceptar);

		mainPanel.add(cabeceraLabel);
		mainPanel.add(panelCentral);
		mainPanel.add(panelInferior);
		mainPanel.add(panelAceptar);

		pack();
		setResizable(false);
		setVisible(false);
	}

	private JTable initDefaultDataTableModel() {
		_dataTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}

			@Override
			public Object getValueAt(int row, int col) {
				List<String> keys = new ArrayList<>(data.keySet());
				String key = keys.get(row);
				String s = "";

				switch (col) {
				case 0:
					s = key;
					break;
				case 1:
					s = dataValues.get(row);
					break;
				case 2:
					s = data.getString(key);
					break;
				}

				return s;
			}

			@Override
			public void setValueAt(Object o, int row, int col) {
				if (col == 1)
					dataValues.add(row, o.toString());

			}
		};
		_dataTableModel.setNumRows(0);
		dataValues = new ArrayList<>();
		_dataTableModel.setColumnIdentifiers(_headers);
		return new JTable(_dataTableModel);
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
				ChangeRegionsDialog.this.setVisible(false);
			} else {
				ViewUtils.showErrorMsg("No has hecho ningun cambio en las regiones");
				ChangeRegionsDialog.this.setVisible(false);
			}
		});
		panelAceptar.add(okButton);
	}


	private void keepChanges() {
		_status = 0;
		joDataOut = new JSONObject();
		JSONObject joR = new JSONObject();
		JSONArray jaRegions = new JSONArray();
		JSONArray jaRow = new JSONArray();
		JSONArray jaCol = new JSONArray();
		JSONObject joSpec = new JSONObject();
		JSONObject joData = new JSONObject();
		jaRow.put(_fromRowModel.getSelectedItem());
		jaRow.put(_toRowModel.getSelectedItem());
		jaCol.put(_fromColModel.getSelectedItem());
		jaCol.put(_toColModel.getSelectedItem());
		if (data != null) {
			List<String> keys = new ArrayList<>(data.keySet());
			for (int i = 0; i < dataValues.size(); i++) {
				if (dataValues.get(i) != "") {
					joData.put(keys.get(i), dataValues.get(i));
				}
			}

		}
		joSpec.put("type", _regionsModel.getSelectedItem());

		joSpec.put("data", joData);
		joR.put("row", jaRow);
		joR.put("col", jaCol);
		joR.put("spec", joSpec);
		jaRegions.put(joR);
		joDataOut.put("regions", jaRegions);

		try {
			_ctrl.set_regions(joDataOut);
		} catch (Exception e) {
			ViewUtils.showErrorMsg("Error. " + e.getMessage());
		}
	}

	private void insertComponentsInPanelInferior(JPanel p) {
		JLabel regionsLabel = new JLabel("Region Type: ");
		p.add(regionsLabel);
		JComboBox regionsModel = new JComboBox(_regionsModel);
		regionsModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = regionsModel.getSelectedIndex();
				updateDataInDataTable(_regionsInfo.get(index));
				_status = 1;
			}
		});
		p.add(regionsModel);

		JLabel rowFromToLabel = new JLabel("Row from/to: ");
		p.add(rowFromToLabel);
		JComboBox fromrow = new JComboBox(_fromRowModel);
		fromrow.addActionListener(e -> _status = 1);
		p.add(fromrow);
		JComboBox torow = new JComboBox(_toRowModel);
		torow.addActionListener(e -> _status = 1);
		p.add(torow);

		JLabel colFromToLabel = new JLabel("Col from/to: ");
		p.add(colFromToLabel);
		JComboBox fromcol = new JComboBox(_fromColModel);
		fromcol.addActionListener(e -> _status = 1);
		p.add(fromcol);
		JComboBox tocol = new JComboBox(_toColModel);
		tocol.addActionListener(e -> _status = 1);
		p.add(tocol);
	}

	private void updateDataInDataTable(JSONObject info) {
		data = info.getJSONObject("data");
		dataValues = new ArrayList<>(Collections.nCopies(data.length(), ""));
		_dataTableModel.setNumRows(data.length());
		_dataTableModel.fireTableDataChanged();
	}

	private void initComboBoxAll() {
		_regionsModel = new DefaultComboBoxModel<>();
		_fromRowModel = new DefaultComboBoxModel<>();
		_toRowModel = new DefaultComboBoxModel<>();
		_fromColModel = new DefaultComboBoxModel<>();
		_toColModel = new DefaultComboBoxModel<>();

	}

	private void rellenarComboBoxAll() {
		rellenarComboBoxRegions();
		rellenarComboBoxRowFrom();
		rellenarComboBoxRowTo();
		rellenarComboBoxColumFrom();
		rellenarComboBoxColumTo();
	}

	private void rellenarComboBoxRegions() {
		for (JSONObject jo : _regionsInfo) {
			String type = jo.getString("type");
			_regionsModel.addElement(type);
		}
	}

	private void rellenarComboBoxRowFrom() {
		for (Integer i = 0; i < map.get_rows(); i++) {
			_fromRowModel.addElement(i.toString());
		}
	}

	private void rellenarComboBoxRowTo() {
		for (Integer i = 0; i < map.get_rows(); i++) {
			_toRowModel.addElement(i.toString());
		}
	}

	private void rellenarComboBoxColumFrom() {
		for (Integer i = 0; i < map.get_cols(); i++) {
			_fromColModel.addElement(i.toString());
		}
	}

	private void rellenarComboBoxColumTo() {
		for (Integer i = 0; i < map.get_cols(); i++) {
			_toColModel.addElement(i.toString());
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
		this.map = map;
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.map = map;
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this.map = map;
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
	}

}
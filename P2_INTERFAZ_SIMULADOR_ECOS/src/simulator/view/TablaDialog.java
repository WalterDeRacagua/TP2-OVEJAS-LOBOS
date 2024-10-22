package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

public class TablaDialog extends JDialog implements EcoSysObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Controller _ctrl;
	private JTable _table;
	private DefaultTableModel _dataTableModel;
	private JSpinner _spinner;
	private JButton _cancel;
	private JButton _reset_info;

	private MapInfo _regions;
	private static String[] _columns = { "Row", "Col", "Num Animals" };
	private Map<RegionData, Integer> _animals_per_region;

	/*
	 * El jspinner marcara el número de animales que debe tener como minimo la
	 * region para que se muestre la tabla.
	 */

	public TablaDialog(Controller ctrl) {
		super((Frame) null, true);
		/* ESTO ES PARA QUE SALGAN LAS REGIONES EN ORDEN. */
		this._animals_per_region = new LinkedHashMap<>();
		this._ctrl = ctrl;
		this.initGUI();
		this._ctrl.addObserver(this);
	}

	private void updateMap() {

		if (this._regions == null) {

			return;
		}

		this._animals_per_region.clear();
		Iterator<RegionData> it = this._regions.iterator();

		while (it.hasNext()) {

			RegionData r = it.next();
			if (r.r().getAnimalsInfo().size() >= (int) this._spinner.getValue()) {
				this._animals_per_region.put(r, r.r().getAnimalsInfo().size());
			}

		}

		this._dataTableModel.fireTableDataChanged();

	}

	private void initGUI() {

		this.setTitle("[SPECIAL TABLE]");
		// Panel principal
		JPanel mainPanel = new JPanel(new BorderLayout());
		// Panel informativo
		JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// Panel tabla
		JPanel panelTabla = new JPanel();
		// Panel de botones y JSpinner
		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JLabel cabeceraLabel = new JLabel(
				"<html><p>Table that shows the regions have <b>the quantity of animals</b> demanded by <b>JSpinner</b>.</p></html>");
		cabeceraLabel.setAlignmentX(CENTER_ALIGNMENT);
		cabeceraLabel.setPreferredSize(new Dimension(650, 50));
		panelInfo.add(cabeceraLabel);
		mainPanel.add(panelInfo, BorderLayout.PAGE_START);

		this._table = initDefaultDataTableModel();
		this._table.getColumnModel().getColumn(2).setPreferredWidth(300);
		for (int i = 0; i < this._table.getColumnCount(); i++) {
			this._table.getColumnModel().getColumn(i).setResizable(false);
		}

		JScrollPane scroll = new JScrollPane(this._table);
		scroll.setAlignmentX(CENTER_ALIGNMENT);
		scroll.setPreferredSize(new Dimension(680, 250));
		panelTabla.add(scroll);
		mainPanel.add(panelTabla, BorderLayout.CENTER);

		JLabel _numAnimals = new JLabel("Minimo numero de animales: ");
		panelBotones.add(_numAnimals);
		this._spinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		this._spinner.setToolTipText("Simulation animals: 1-50");
		this._spinner.setValue(1);
		this._spinner.setMaximumSize(new Dimension(80, 40));
		this._spinner.setMinimumSize(new Dimension(80, 40));
		this._spinner.setPreferredSize(new Dimension(80, 40));
		this._spinner.addChangeListener(a -> this.updateMap());
		panelBotones.add(this._spinner);

		this._reset_info = new JButton("Reset");
		this._reset_info.setToolTipText("Resets the value of the JSpinner");
		this._reset_info.addActionListener((a) -> this._spinner.setValue(1));
		panelBotones.add(this._reset_info);

		this._cancel = new JButton("Cancel");
		this._cancel.setToolTipText("Hides window");
		this._cancel.addActionListener((a) -> this.setVisible(false));
		panelBotones.add(this._cancel);

		mainPanel.add(panelBotones, BorderLayout.PAGE_END);

		this.setContentPane(mainPanel);
		this.pack();
		this.setResizable(false);

	}

	private JTable initDefaultDataTableModel() {
		_dataTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Object getValueAt(int row, int col) {
				List<RegionData> keys = new ArrayList<>(_animals_per_region.keySet());
				RegionData key = keys.get(row);
				Object s = "";

				switch (col) {
				case 0:
					s = key.row();
					break;
				case 1:
					s = key.col();
					break;
				case 2:
					s = _animals_per_region.get(key);
					break;
				}

				return s;
			}

			@Override
			public int getRowCount() {
				return _animals_per_region.size();
			}

			@Override
			public int getColumnCount() {
				return _columns.length;
			}

			@Override
			public String getColumnName(int col) {

				return _columns[col];
			}
		};
		_dataTableModel.setColumnIdentifiers(_columns);
		return new JTable(_dataTableModel);

	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {

		this._regions = map;
		this.updateMap();

	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._regions = map;
		this.updateMap();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

		this._regions = map;
		this.updateMap();
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this._regions = map;
		this.updateMap();
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this._regions = map;
		this.updateMap();
	}

	public void open(Window parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

}

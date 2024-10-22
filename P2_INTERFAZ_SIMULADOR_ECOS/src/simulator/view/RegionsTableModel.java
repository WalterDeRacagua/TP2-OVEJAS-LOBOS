package simulator.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;

	private Controller _ctrl;
	private MapInfo _regions;
	private Map<RegionData, Map<String, Integer>> _mapInfo;
	private static String[] columns;

	public RegionsTableModel(Controller ctrl) {
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.initCols();
		this._mapInfo = new LinkedHashMap<>();
		this.updateTable();
	}

	private void updateTable() {
		this._mapInfo.clear();
		String dietName = " ";
		Map<String, Integer> auxiliar = this.createNewMap();
		Iterator<RegionData> iterator = this._regions.iterator();

		while (iterator.hasNext()) {

			RegionData r = iterator.next();
			this._mapInfo.put(r, this.createNewMap());

			auxiliar = this._mapInfo.get(r);
			for (AnimalInfo a : r.r().getAnimalsInfo()) {
				dietName = a.get_diet().toString();
				auxiliar.put(dietName, auxiliar.getOrDefault(dietName, 0) + 1);
			}
			this._mapInfo.put(r, auxiliar);

		}

	}

	private Map<String, Integer> createNewMap() {

		Map<String, Integer> aux = new HashMap<>();

		for (int i = 0; i < Diet.values().length; i++) {
			aux.put(Diet.values()[i].toString(), 0);
		}
		return aux;
	}

	@Override
	public int getRowCount() {
		return this._regions.get_cols() * this._regions.get_rows();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		List<RegionData> keys = new ArrayList<>(this._mapInfo.keySet());
		RegionData key = keys.get(rowIndex);
		Map<String, Integer> auxiliar = this._mapInfo.get(key);

		if (columnIndex == 0) {
			return key.getRow();
		} else if (columnIndex == 1) {

			return key.getCol();
		} else if (columnIndex == 2) {

			return key.r().toString();
		} else {
			List<String> keyColumns = new ArrayList<>(auxiliar.keySet());
			String keyColumn = keyColumns.get(columnIndex - 3);
			int valor = auxiliar.get(keyColumn);
			return valor;
		}

	}

	@Override
	public String getColumnName(int col) {
		return columns[col];
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		this._regions = map;
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._regions = map;
		this.updateTable();
		this.fireTableDataChanged();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this._regions = map;
		this.updateTable();
		this.fireTableDataChanged();

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this._regions = map;
		this.updateTable();
		this.fireTableDataChanged();

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this._regions = map;
		this.updateTable();
		this.fireTableDataChanged();

	}

	private void initCols() {

		List<String> _columns = new ArrayList<>();
		_columns.add("Row");
		_columns.add("Col");
		_columns.add("Desc.");
		for (int i = 0; i < Diet.values().length; i++) {
			_columns.add(Diet.values()[i].toString());
		}

		columns = _columns.toArray(new String[0]);
	}
}

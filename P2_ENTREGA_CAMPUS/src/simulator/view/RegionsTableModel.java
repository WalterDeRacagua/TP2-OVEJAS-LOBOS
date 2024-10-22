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
	private Map<RegionData, Map<Diet, Integer>> _mapInfo;
	private static String[] columns;


	public RegionsTableModel(Controller ctrl) {
		this._ctrl = ctrl;
		this._mapInfo = new LinkedHashMap<>();
		this._ctrl.addObserver(this);
		this.initCols();
	}

	private void updateTable(MapInfo _regions) {
		this._mapInfo.clear();
		Diet diet;
		Map<Diet, Integer> auxiliar = this.createNewMap();
		Iterator<RegionData> iterator = _regions.iterator();

		while (iterator.hasNext()) {

			RegionData r = iterator.next();
			this._mapInfo.put(r, this.createNewMap());

			auxiliar = this._mapInfo.get(r);
			for (AnimalInfo a : r.r().getAnimalsInfo()) {
				diet = a.get_diet();
				auxiliar.put(diet, auxiliar.getOrDefault(diet, 0) + 1);
			}
			this._mapInfo.put(r, auxiliar);

		}

	}

	private Map<Diet, Integer> createNewMap() {

		Map<Diet, Integer> aux = new HashMap<>();
		
		for (int i = 0; i < Diet.values().length; i++) {
			aux.put(Diet.values()[i], 0);
		}
		return aux;
	}

	@Override
	public int getRowCount() {
		return _mapInfo.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		List<RegionData> keys = new ArrayList<>(this._mapInfo.keySet());
		RegionData key = keys.get(rowIndex);
		Map<Diet, Integer> auxiliar = this._mapInfo.get(key);

		if (columnIndex == 0) {
			return key.getRow();
		} else if (columnIndex == 1) {

			return key.getCol();
		} else if (columnIndex == 2) {

			return key.r().toString();
		} else {
			List<Diet> keyColumns = new ArrayList<>(auxiliar.keySet());
			Diet keyColumn = keyColumns.get(columnIndex - 3);
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
		this.updateTable(map);
		this.fireTableDataChanged();
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.updateTable(map);
		this.fireTableDataChanged();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this.updateTable(map);
		this.fireTableDataChanged();

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		this.updateTable(map);
		this.fireTableDataChanged();

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this.updateTable(map);
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

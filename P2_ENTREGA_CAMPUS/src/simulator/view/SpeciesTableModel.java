package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;

	private Controller _ctrl;
	private static String[] columns;
	private Map<String, Map<State, Integer>> _mapaInfo;

	public SpeciesTableModel(Controller ctrl) {
		this._mapaInfo = new HashMap<>();
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.initCols();
	}

	private void updateTable(List<AnimalInfo> animals) {
		this._mapaInfo.clear();
		State state;
		Map<State, Integer> auxiliar = this.createNewMap();

		for (AnimalInfo a : animals) {
			if (!this._mapaInfo.containsKey(a.get_genetic_code())) {
				this._mapaInfo.put(a.get_genetic_code(), this.createNewMap());
			}
			auxiliar = this._mapaInfo.get(a.get_genetic_code());
			state = a.get_state();
			auxiliar.put(state, auxiliar.getOrDefault(state, 0) + 1);
			this._mapaInfo.put(a.get_genetic_code(), auxiliar);
		}
	}

	private Map<State, Integer> createNewMap() {

		Map<State, Integer> aux = new LinkedHashMap<>();
		for (State key : State.values()) {
			aux.put(key, 0);
		}
		return aux;
	}

	@Override
	public int getRowCount() {
		return this._mapaInfo.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		List<String> keys = new ArrayList<>(this._mapaInfo.keySet());
		String key = keys.get(rowIndex);
		Map<State, Integer> auxiliar = this._mapaInfo.get(key);

		if (columnIndex == 0) {
			return key;
		}

		List<State> keysColumns = new ArrayList<>(auxiliar.keySet());
		State keyColumn = keysColumns.get(columnIndex-1);
		int valor = auxiliar.get(keyColumn);

		return valor;
	}

	@Override
	public String getColumnName(int col) {
		return columns[col];
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateTable(animals);
		this.fireTableDataChanged();
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.updateTable(animals);
		this.fireTableDataChanged();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this.updateTable(animals);
		this.fireTableDataChanged();
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this.updateTable(animals);
		this.fireTableDataChanged();
	}

	private void initCols() {
		List<String> _columns = new ArrayList<>();
		_columns.add("Species");
		for (int i = 0; i < State.values().length; i++) {
			_columns.add(State.values()[i].toString());
		}
		columns = _columns.toArray(new String[0]);
	}

}

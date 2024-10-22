
package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

public class StateAnimalTableModel extends AbstractTableModel implements EcoSysObserver {
	private static final long serialVersionUID = 1L;

	private Controller _ctrl;
	private static String[] columns;
	private Map<Integer, Map<State, Integer>> _mapaInfo;//herviboros en peligro  y carnivoros hambrientos en cada iteracion
	private int _iteracion;

	public StateAnimalTableModel(Controller ctrl) {
		this._mapaInfo = new HashMap<>();
		_iteracion=0;
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.initCols();
	}

	private void updateTable(List<AnimalInfo> animals) {
		this._mapaInfo.clear(); //TODO comentar para que muestre todas las iteraciones y no solo la actual
		State state;
		Diet dieta;
		Map<State, Integer> auxiliar = this.createNewMap();
		this._mapaInfo.put(_iteracion, this.createNewMap());
		for (AnimalInfo a : animals) {
			auxiliar = this._mapaInfo.get(_iteracion);
			state = a.get_state();
			dieta = a.get_diet();
			if(dieta.equals(Diet.HERBIVORE)&&state.equals(State.DANGER)) {
				auxiliar.put(state, auxiliar.getOrDefault(state, 0)+1);
			}else if(dieta.equals(Diet.CARNIVORE)&&state.equals(State.HUNGER)) {
				auxiliar.put(state, auxiliar.getOrDefault(state, 0)+1);				
			}
			this._mapaInfo.put(_iteracion, auxiliar);
		}
		_iteracion++;
	}

	private Map<State, Integer> createNewMap() {

		Map<State, Integer> aux = new LinkedHashMap<>();
		aux.put(State.DANGER, 0);
		aux.put(State.HUNGER, 0);

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
		List<Integer> keys = new ArrayList<>(this._mapaInfo.keySet());
		Integer key = keys.get(rowIndex);
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
		_columns.add("Num Iteracion");
		_columns.add("Danger Hervibores");
		_columns.add("Hunter Carnivores");
		columns = _columns.toArray(new String[0]);
	}

}


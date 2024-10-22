package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.MapInfo.RegionData;

public class MaxSpeedTableModel extends AbstractTableModel implements EcoSysObserver{
	
	private static final long serialVersionUID = 1L;
	
	private Controller _ctrl;
	private MapInfo _regions;
// private Map<RegionData, Map<String, Integer>> _mapInfo;
	private Map<RegionData, AnimalInfo> _mapInfo;
	private static String[] columns;
	
	public MaxSpeedTableModel(Controller ctrl) {
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.initCols();
		this._mapInfo = new LinkedHashMap<>();
		this.updateTable();
	}
	
	private void updateTable() {
		this._mapInfo.clear();
		Iterator<RegionData> iterator = this._regions.iterator();
		
		while (iterator.hasNext()) {
			
			RegionData r = iterator.next();
			this._mapInfo.put(r, animalInfoMaxSpeed(r));
			
		}
		
	}
	
	private AnimalInfo animalInfoMaxSpeed(RegionData r) {
		Optional<AnimalInfo> animal= r.r().getAnimalsInfo().stream().max((a1,a2)->Double.compare(a1.get_speed(), a2.get_speed()));
		if(animal.isPresent())
			return animal.get();
		else return null;
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
		AnimalInfo auxiliar = this._mapInfo.get(key);
		
		if (columnIndex == 0) {
			return key.getRow();
		} else if (columnIndex == 1) {
			
			return key.getCol();
			
		}  else if (columnIndex == 2) {
			String valor="";
			if(auxiliar!=null)
				valor = auxiliar.get_genetic_code().toString();
			return valor;
		}else {
			Double valor=0.0;
			if(auxiliar!=null)
				valor = auxiliar.get_speed();
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
		
		SwingUtilities.invokeLater(() -> {
			
			this._regions = map;
			this.updateTable();
			this.fireTableDataChanged();
			
		});
		
	}
	
	private void initCols() {
		
		List<String> _columns = new ArrayList<>();
		_columns.add("Row");
		_columns.add("Col");
		_columns.add("specie");
		_columns.add("max Speed");
		
		columns = _columns.toArray(new String[0]);
	}
}

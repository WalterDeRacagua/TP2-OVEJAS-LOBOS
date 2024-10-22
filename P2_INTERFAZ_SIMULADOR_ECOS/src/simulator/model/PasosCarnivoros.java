package simulator.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import simulator.control.Controller;
import simulator.model.MapInfo.RegionData;

public class PasosCarnivoros implements EcoSysObserver {

	private MapInfo regions;
	private Map<RegionData, Integer> _mapInfo;
	private Controller ctrl;

	public PasosCarnivoros(Controller ctrl) {
		this.ctrl = ctrl;
		this._mapInfo = new LinkedHashMap<>();
		this.ctrl.addObserver(this);
		this.init();
	}

	private void init() {

		this._mapInfo.clear();
		Iterator<RegionData> iterator = this.regions.iterator();

		while (iterator.hasNext()) {

			RegionData r = iterator.next();
			this._mapInfo.put(r, 0);

		}

	}

	private void update() {
		Iterator<RegionData> iterator = this.regions.iterator();

		while (iterator.hasNext()) {

			RegionData r = iterator.next();
			int contador = (int) r.r().getAnimalsInfo().stream().filter((a1) -> a1.get_diet() == Diet.CARNIVORE)
					.count();
			if (contador >= 3) {
				this._mapInfo.put(r, this._mapInfo.get(r).intValue() + 1);
			}

		}

	}

	public void mostrarPantalla() {
		
		Set<RegionData> conjunto = this._mapInfo.keySet();
		Iterator<RegionData> iterator = conjunto.iterator();

		while (iterator.hasNext()) {

			RegionData r = iterator.next();

			System.out.println("["+r.getRow()+" , "+ + r.getCol() +"] = "+ this._mapInfo.get(r).intValue());

		}

	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {

		this.regions = map;

	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {

		this.regions = map;
		this.init();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

		this.regions = map;
		this.update();
	}

}

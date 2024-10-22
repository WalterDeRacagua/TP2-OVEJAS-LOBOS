package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulator.control.Controller;
import simulator.misc.Vector2D;

public class AnimalesMovidosAlNorte implements EcoSysObserver {

	private Controller _ctrl;
	private List<AnimalInfo> _animals;
	private Map<Integer, Integer> animals_moved_north;
	private Map<AnimalInfo, Vector2D> _animals_pos;
	private int _iteracion;

	public AnimalesMovidosAlNorte(Controller ctrl) {

		this._iteracion = 1;
		this.animals_moved_north = new HashMap<>();
		this._animals_pos = new HashMap<>();
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
	}

	private void init() {

		this._animals_pos.clear();

		for (AnimalInfo a : this._animals) {

			this._animals_pos.put(a, a.get_position());

		}

	}

	private void update() {

		for (AnimalInfo a : this._animals) {

			if (this._animals_pos.get(a) != null && a.get_position().getY() > this._animals_pos.get(a).getY()) {

				this.animals_moved_north.put(this._iteracion,
						this.animals_moved_north.getOrDefault(this._iteracion, 0) + 1);

			}

			this._animals_pos.put(a, a.get_position());

		}

	}

	public void mostrar_animales_por_iteracion() {

		List<Integer> keys = new ArrayList<>(this.animals_moved_north.keySet());

		for (Integer key : keys) {

			System.out.println("Iteracion " + key + ": " + this.animals_moved_north.get(key));

		}

	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {

		this._animals = animals;
		this.init();

	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {

		this._iteracion = 0;
		this._animals = animals;
		this.init();
		this.animals_moved_north.clear();
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

		this._animals.add(a);
//		this.init();
		this._animals_pos.put(a, a.get_position());
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

		this._animals = animals;
		if (this._iteracion == 0) {
			this.init();
		} else {
			this.update();
		}
		this._iteracion++;

	}
}

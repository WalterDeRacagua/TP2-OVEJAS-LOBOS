package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.Iterator;

import org.json.JSONObject;
//import org.json.JSONArray;

import simulator.factories.Factory;

public class Simulator implements JSONable {
	private Factory<Animal> animals_factory;
	private Factory<Region> region_factory;
	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {
		if (cols < 0 || rows < 0)
			throw new IllegalArgumentException("Neither rows nor columns must be less tan zero or equal to zero.");
		if (width < 0 || height < 0)
			throw new IllegalArgumentException(
					"Neither the width nor the height must be less than zero or equal to zero.");

		this.regionManager = new RegionManager(cols, rows, width, height);
		this.animals = new ArrayList<Animal>();
		this.time = 0.0;
		this.animals_factory = animals_factory;
		this.region_factory = regions_factory;

	}

	private void set_region(int row, int col, Region r) {
		this.regionManager.set_region(row, col, r);
	}

	public void set_region(int row, int col, JSONObject r_json) {
		if (row < 0 || col < 0 || row >= regionManager.get_rows() || col >= this.regionManager.get_cols()) {
			throw new IllegalArgumentException(
					"The range of \"row\" and \"col\" must be within the bounds of the board.");
		}
		Region r = region_factory.create_instance(r_json);
		set_region(row, col, r);
	}

	private void add_animal(Animal a) {
		this.animals.add(a);
		this.regionManager.register_animal(a);
	}

	public void add_animal(JSONObject a_json) {
		Animal a = this.animals_factory.create_instance(a_json);
		add_animal(a);
	}

	public MapInfo get_map_info() {

		return this.regionManager;
	}

	public List<? extends AnimalInfo> get_animals() {
		return Collections.unmodifiableList(this.animals);
	}

	public double get_time() {
		return this.time;
	}

	public void advance(double dt) {
		this.time += dt;
		removeDeads();

		List<Animal> babys = new ArrayList<>();
		for (Animal a : this.animals) {
			a.update(dt);
			regionManager.update_animal_region(a);
	
		}
		
		this.regionManager.update_all_regions(dt);	
		
		for (Animal a : this.animals) {
		
		if (a.is_pregnant()) {
			babys.add(a.deliver_baby());
		}
	}
		this.addAllsBabys(babys);
	

	}

	private void addAllsBabys(List<Animal> babys) {
		for (Animal b : babys) {
			this.add_animal(b);
		}
	}

	private void removeDeads() {
		int i = this.animals.size() - 1;
		while (i >= 0) {
			Animal a = animals.get(i);
			if (!a.isAlive()) {
				remove(a);
				regionManager.unregister_animal(a);
			}
			i--;
		}
	}

	private void remove(Animal a) {
		animals.remove(a);
	}

	@Override
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		jo.put("time", this.get_time());
		jo.put("state", this.regionManager.as_JSON());
		return jo;
	}

}

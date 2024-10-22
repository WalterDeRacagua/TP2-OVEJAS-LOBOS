package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {

	protected List<Animal> animals;

	public Region() {
		this.animals = new ArrayList<Animal>();
	}

	public final void add_animal(Animal a) {
		this.animals.add(a);
	}

	public final void remove_animal(Animal a) {
		this.animals.remove(a);
	}

	public final List<Animal> getAnimals() {
		return Collections.unmodifiableList(this.animals);
	}

	protected int herviboresCount() {

		int count = 0;

		for (Animal a : this.animals) {

			if (a.get_diet() == Diet.HERBIVORE) {

				count++;
			}
		}

		return count;
	}

	@Override
	public JSONObject as_JSON() {

		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();

		for (AnimalInfo a : this.animals) {

			ja.put(a.as_JSON());
		}
		jo.put("animals", ja);

		return jo;
	}

}

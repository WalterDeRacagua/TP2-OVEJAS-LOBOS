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

	protected int herviboresCount() {//Es mejor hacer animals.filter.stream().filter((a)->a.get_diet()==HERVIBORE).count()

		int count = 0;

		for (Animal a : this.animals) {

			if (a.get_diet() == Diet.HERBIVORE) {

				count++;
			}
		}

		return count;
	}

	@Override
	public List<AnimalInfo> getAnimalsInfo() {

		return new ArrayList<>(this.animals);
		/*
		 * Si pongo this.animals unicamente da error porque creo que nosotros queremos
		 * pasarle la interfaz para no tener problemas de seguridad por ejemplo. Si le
		 * pasamos la lista normal, se podría modificar provocando problemas de
		 * seguridad(CREO)
		 */
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
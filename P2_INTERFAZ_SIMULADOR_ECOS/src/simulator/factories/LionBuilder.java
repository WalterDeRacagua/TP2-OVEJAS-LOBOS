package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Lion;

public class LionBuilder extends Builder<Animal>{
	private Factory<SelectionStrategy> strategy;

	public LionBuilder(Factory<SelectionStrategy> strategy) {
		super("lion", "Lion");
		if (strategy == null)
			throw new IllegalArgumentException("Strategy must not be null!");

		this.strategy = strategy;
	}

	@Override
	protected Animal create_instance(JSONObject data) {
		SelectionStrategy mate_strategy;
		SelectionStrategy hunt_strategy;
		JSONObject jodata = data;
		if (jodata.has("mate_strategy")) {
			JSONObject jomate = jodata.getJSONObject("mate_strategy");
			mate_strategy = strategy.create_instance(jomate);
		} else
			mate_strategy = new SelectFirst();
		if (jodata.has("hunt_strategy")) {
			JSONObject johunt = jodata.getJSONObject("hunt_strategy");
			hunt_strategy = strategy.create_instance(johunt);
		} else
			hunt_strategy = new SelectFirst();

		if (jodata.has("pos")) {
			JSONObject jopos = jodata.getJSONObject("pos");
			if (!jopos.has("x_range"))
				throw new IllegalArgumentException(
						"The \"x_range\" key does not exist inside \"pos\" in an element of the \"lion\" key.");
			JSONArray jax = jopos.getJSONArray("x_range");
			if (!jopos.has("y_range"))
				throw new IllegalArgumentException(
						"The \"y_range\" key does not exist inside \"pos\" in an element of the \"lion\" key.");
			JSONArray jay = jopos.getJSONArray("y_range");

			double min_x, max_x, min_y, max_y;
			try {
				min_x = jax.getDouble(0);
				max_x = jax.getDouble(1);
				min_y = jay.getDouble(0);
				max_y = jay.getDouble(1);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Any value of the \"pos\" key in an element of the \"lion\" key is not a numeric value.");
			}
			if (min_x < 0 || max_x < 0 || max_y < 0 || min_y < 0)
				throw new IllegalArgumentException(
						"To generate the random position of \"lion\", \"x_range\" and \"y_range\" must be positive values.");
			if (min_x > max_x || max_y < min_y)
				throw new IllegalArgumentException(
						"To generate the random position of \"lion\", \"x_range\" and \"y_range\" must be values sorted from lowest to highest.");

			Vector2D pos = Vector2D.get_random_vector(min_x, max_x, min_y, max_y);
			return new Lion(mate_strategy, hunt_strategy, pos);
		} else
			return new Lion(mate_strategy, hunt_strategy, null);

	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("hunt_strategy", new JSONObject());
		o.put("mate_strategy", new JSONObject());
		o.put("pos", new JSONArray());

	}
}

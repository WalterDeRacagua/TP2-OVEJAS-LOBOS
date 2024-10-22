package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.MapInfo;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	Simulator _sim;

	public Controller(Simulator sim) {
		this._sim = sim;
	}

	public void load_data(JSONObject data) {

		if (data.has("regions")) {
			loadRegions(data);
		}
		if (!data.has("animals"))
			throw new IllegalArgumentException("There is no \"animals\" key in the input file, and it is compulsory.");
		JSONArray jaAnimals = data.getJSONArray("animals");
		if (jaAnimals.length() == 0)
			throw new IllegalArgumentException("There is no element in the \"animals\" key. It is mandatory.");

		JSONObject joAnimal, joSpect;
		for (int i = 0; i < jaAnimals.length(); i++) {
			joAnimal = jaAnimals.getJSONObject(i);
			if (!joAnimal.has("amount"))
				throw new IllegalArgumentException(
						"The \"amount\" key does not exist in the " + i + " element of the \"animals\" key.");
			int amount = joAnimal.getInt("amount");
			if (amount <= 0)
				throw new IllegalArgumentException("The value of \"amount\" in the element " + i
						+ " of the \"animals\" key must be positive: " + amount);
			if (!joAnimal.has("spec"))
				throw new IllegalArgumentException(
						"There is no \"spec\" key in the " + i + " element of the \"animals\" key.");
			joSpect = joAnimal.getJSONObject("spec");

			for (int j = 0; j < amount; j++) {
				_sim.add_animal(joSpect);
			}

		}

	}

	private void loadRegions(JSONObject data) {
		JSONArray ja = data.getJSONArray("regions");
		if (ja.length() == 0) {
			throw new IllegalArgumentException(
					"There is no element in the \"regions\" key, is mandatory to include the elements if you decide to include \"regions\".");
		}

		for (int i = 0; i < ja.length(); i++) {
			JSONObject joRegion = ja.getJSONObject(i);
			if (!joRegion.has("row")) {
				throw new IllegalArgumentException(
						"There is no \"row\" key in the " + i + " element of the \"regions\" key.");
			}
			JSONArray jaRow = joRegion.getJSONArray("row");
			if (!joRegion.has("col")) {
				throw new IllegalArgumentException(
						"There is no \"col\" key in the " + i + " element of the \"regions\" key.");
			}
			JSONArray jaCol = joRegion.getJSONArray("col");
			if (!joRegion.has("spec")) {
				throw new IllegalArgumentException(
						"There is no \"spec\" key in the " + i + " element of the \"regions\" key.");
			}
			JSONObject joSpecRegion = joRegion.getJSONObject("spec");

			try {
				if (jaRow.getInt(0) > jaRow.getInt(1))
					throw new IllegalArgumentException(
							"The range introduced for \"row\" must be sorted from lowest to highest.");
				if (jaCol.getInt(0) > jaCol.getInt(1))
					throw new IllegalArgumentException(
							"The range introduced for \"col\" must be sorted from lowest to highest.");
				for (int j = jaRow.getInt(0); j <= jaRow.getInt(1); j++) {

					for (int k = jaCol.getInt(0); k <= jaCol.getInt(1); k++) {
						_sim.set_region(j, k, joSpecRegion);
					}
				}

			} catch (Exception e) {
				throw new IllegalArgumentException(e.getMessage());
			}

		}

	}

	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					(int) Math.round(a.get_age()) + 2));
		return ol;
	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = this._sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(this._sim.get_animals()), this._sim.get_time(), dt);
		}

		JSONObject jo = new JSONObject();
		jo.put("in", _sim.as_JSON());
		while (_sim.get_time() <= t) {

			this.advance(dt);
			if (sv)
				view.update(to_animals_info(this._sim.get_animals()), this._sim.get_time(), dt);
		}
		if (sv)
			view.close();
		jo.put("out", _sim.as_JSON());

		PrintStream p = new PrintStream(out);
		p.println(jo.toString());
		p.close();

	}

	public void reset(int cols, int rows, int width, int height) {

		this._sim.reset(cols, rows, width, height);
	}

	public void set_regions(JSONObject rs) {
		loadRegions(rs);

	}

	public void advance(double data) {

		this._sim.advance(data);

	}

	public void addObserver(EcoSysObserver o) {

		this._sim.addObserver(o);
	}

	public void removeObserver(EcoSysObserver o) {

		this._sim.removeObserver(o);
	}

	public void set_strategies(String specie, String state, String strategy) {
		this._sim.set_strategies(specie,state,strategy);
	}

}

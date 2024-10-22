package simulator.model;

import java.util.HashMap;
import java.util.Iterator;
//import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView {

	private Map<Animal, Region> _animal_region;
	private int _cols;
	private int _rows;
	private int _width;
	private int _height;
	private int _region_width;
	private int _region_height;
	private Region[][] _regions;

	public RegionManager(int cols, int rows, int width, int height) {
		this._cols = cols;
		this._rows = rows;
		this._width = width;
		this._height = height;
		this._region_height = height / rows;
		this._region_width = width / cols;
		initRegions();
		initAnimalRegion();
	}

	private void initRegions() {

		this._regions = new Region[this._rows][this._cols];
		for (int i = 0; i < this._rows; i++) {
			for (int j = 0; j < this._cols; j++) {
				this._regions[i][j] = new DefaultRegion();
			}
		}
	}

	private void initAnimalRegion() {
		this._animal_region = new HashMap<>();

	}

	public void set_region(int row, int col, Region r) {
		r.animals.addAll(this._regions[row][col].getAnimals());
		this._regions[row][col] = r;
	}

	public void register_animal(Animal a) {
		a.init(this);
		_animal_region.put(a, null);
		update_animal_region(a);
	}

	public void unregister_animal(Animal a) {
		_animal_region.get(a).remove_animal(a);
		_animal_region.remove(a);
	}

	private int get_index_row_region(Vector2D p) {
		double x = p.getY() / this._region_height;
		int px;
		px = (int) Math.floor(x);
		if (p.getY() % this._region_height == 0.0)
			px -= 1;
		if (px >= this._rows)
			px = _rows - 1;

		return px;
	}

	private int get_index_col_region(Vector2D p) {
		double y = p.getX() / this._region_width;
		int py;
		py = (int) Math.floor(y);
		if (p.getX() % this._region_width == 0.0)
			py -= 1;
		if (py >= this._cols)
			py = _cols - 1;

		return py;
	}

	public void update_animal_region(Animal a) {
		Vector2D p = a.get_position();
		int px = get_index_row_region(p);
		int py = get_index_col_region(p);

		if (_animal_region.get(a) == null) {
			_animal_region.put(a, _regions[px][py]);
			_regions[px][py].add_animal(a);
		} else if (!_animal_region.get(a).equals(_regions[px][py])) {
			_regions[px][py].add_animal(a);
			_animal_region.get(a).remove_animal(a);
			_animal_region.put(a, _regions[px][py]);
		}

	}

	public void update_all_regions(double dt) {
		for (int i = 0; i < this.get_rows(); i++) {
			for (int j = 0; j < this.get_cols(); j++) {
				this._regions[i][j].update(dt);
			}

		}
	}

	private Vector2D position_up_left_region(Animal o) {
		Vector2D p = o.get_position();
		double x = p.getX() - o.get_sight_range();
		double y = p.getY() - o.get_sight_range();
		p = new Vector2D(x, y);
		return p;
	}

	private Vector2D position_down_right_region(Animal o) {
		Vector2D p = o.get_position();
		double x = p.getX() + o.get_sight_range();
		double y = p.getY() + o.get_sight_range();
		p = new Vector2D(x, y);
		return p;
	}

	private Boolean isRegionOK(int i, int j) {
		if (0 <= i && i < _rows && 0 <= j && j < _rows)
			return true;
		else
			return false;
	}

	private Boolean isInRange(Animal e, Animal a) {
		double distance = e.get_position().distanceTo(a.get_position());
		if (distance <= e.get_sight_range())
			return true;
		else
			return false;
	}

	@Override
	public JSONObject as_JSON() {
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		for (int i = 0; i < this.get_rows(); i++) {
			for (int j = 0; j < this.get_cols(); j++) {

				JSONObject jor = new JSONObject();
				jor.put("row", i);
				jor.put("col", j);
				jor.put("data", this._regions[i][j].as_JSON());
				ja.put(jor);
			}
		}
		jo.put("regiones", ja);
		return jo;
	}

	@Override
	public int get_cols() {

		return this._cols;
	}

	@Override
	public int get_rows() {
		return this._rows;
	}

	@Override
	public int get_width() {
		return this._width;
	}

	@Override
	public int get_height() {

		return this._height;
	}

	@Override
	public int get_region_width() {
		return this._region_width;
	}

	@Override
	public int get_region_height() {
		return this._region_height;
	}

	@Override
	public double get_food(Animal a, double dt) {
		Region r = this._animal_region.get(a);
		return r.get_food(a, dt);
	}

	@Override
	public List<Animal> get_animals_in_range(Animal e, Predicate<Animal> filter) {
		List<Animal> animalsInRange = new ArrayList<>();
		Vector2D pl = position_up_left_region(e);
		Vector2D pr = position_down_right_region(e);

		int ci, cf, fi, ff;
		fi = get_index_row_region(pl);
		ci = get_index_col_region(pl);
		ff = get_index_row_region(pr);
		cf = get_index_col_region(pr);

		for (int i = fi; i <= ff; i++) {
			for (int j = ci; j <= cf; j++) {
				if (isRegionOK(i, j)) {
					for (Animal a : this._regions[i][j].getAnimals()) {

						if (e != a && isInRange(e, a)) {

							if (filter.test(a))
								animalsInRange.add(a);
						}
					}
				}
			}
		}

		return animalsInRange;
	}

	@Override
	public Iterator<RegionData> iterator() {

		return new Iterator<RegionData>() {

			private int row = 0;
			private int col = 0;

			@Override
			public boolean hasNext() {

				return row < _rows && col < _cols;
			}

			@Override
			public RegionData next() {

				RegionInfo rInfo = _regions[row][col];
				RegionData rData = new RegionData(row, col, rInfo);
				col++;
				if (col >= _cols && row < _rows) {
					col = 0;
					row++;
				}

				return rData;
			}

			@Override
			public void remove() {// Creo que ha dicho Pablo que debemos implementarlo, pero no estoy seguro. De
									// momento no lo usamos

				// TODO Creo que no es del todo correcto este método.
				// Supongo que este método se encarga de eliminar el elemento current de la
				// lista.

				for (int i = row; i < _rows; i++) {

					for (int j = col; j < _cols - 1; j++) {

						_regions[i][j] = _regions[i][j + 1];
					}

					_regions[i][_cols - 1] = _regions[i + 1][0];
				}

			}

		};
	}

}

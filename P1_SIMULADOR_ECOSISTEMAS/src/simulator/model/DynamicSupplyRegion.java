package simulator.model;

import java.util.List;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

	private static final double PARAM_1 = 60.0;
	private static final double PARAM_2 = 5.0;
	private static final double PARAM_3 = 2.0;

	private double _food;
	private double _factor;

	public DynamicSupplyRegion(List<Animal> animals, double food, double factor) {
		super();

		this._food = food;
		this._factor = factor;
	}

	@Override
	public void update(double dt) {
		if (Utils._rand.nextDouble(1) > 0.5) {
			this._factor = dt * _factor;
		}
	}

	@Override
	public double get_food(Animal a, double dt) {
		if (a.get_diet().equals(Diet.CARNIVORE)) {
			return 0.0;
		}
		int n = this.herviboresCount();
		double given_food = Math.min(_food, PARAM_1 * Math.exp(-Math.max(0, n - PARAM_2) * PARAM_3) * dt);
		this._food -= given_food;
		return given_food;
	}

}

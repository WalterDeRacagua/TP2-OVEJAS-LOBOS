package simulator.factories;

import org.json.JSONObject;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic Supply Region");
	}

	@Override
	protected Region create_instance(JSONObject data) {

		double factor = data.optDouble("factor", 2.0);
		double food = data.optDouble("food", 1000.0);
		if (factor < 0)
			throw new IllegalArgumentException(
					"The value of the \"factor\" key must be greater than zero or equal to zero.");
		if (food <= 0)
			throw new IllegalArgumentException("The value of the \"food\" key must be grater than zero.");

		return new DynamicSupplyRegion(null, food, factor);
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("food", 1);
		o.put("factor", 1);

	}

}

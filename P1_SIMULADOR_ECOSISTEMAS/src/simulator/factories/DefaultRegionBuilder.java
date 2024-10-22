package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {

	public DefaultRegionBuilder() {
		super("default_region", "Default Region");
	}

	@Override
	protected Region create_instance(JSONObject data) {
		return new DefaultRegion();
	}

}

package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectOldest;
import simulator.model.SelectionStrategy;

public class SelectOldestBuilder extends Builder<SelectionStrategy> {

	public SelectOldestBuilder() {
		super("oldest", "Select Oldest");
	}

	@Override
	protected SelectionStrategy create_instance(JSONObject data) {
		return new SelectOldest();
	}

}

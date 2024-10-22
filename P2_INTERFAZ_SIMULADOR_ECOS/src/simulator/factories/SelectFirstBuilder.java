package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

	public SelectFirstBuilder() {
		super("select_first", "Select First");
	}

	@Override
	protected SelectionStrategy create_instance(JSONObject data) {
		return new SelectFirst();
	}

}

package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {
		this._builders = new HashMap<>();
		this._builders_info = new LinkedList<JSONObject>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();

		if (builders == null || builders.isEmpty())
			throw new IllegalArgumentException("Builder list cannot be null or empty");

		for (Builder<T> b : builders) {
			this.add_builder(b);
		}

	}

	public void add_builder(Builder<T> b) {
		this._builders.put(b.get_type_tag(), b);

		this._builders_info.add(b.get_info());

	}

	@Override
	public T create_instance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("’info’ cannot be null");
		}
		if (!info.has("type"))
			throw new IllegalArgumentException(
					"The \"type\" key does not exist inside \"spec\" in an element of the \"animals\" key.");
		String type = info.getString("type");
		if (!this._builders.containsKey(type)) {
			throw new IllegalArgumentException("No builder found for the type we are searching: " + type);
		}
		JSONObject joData = info.has("data") ? info.getJSONObject("data") : new JSONObject();

		Builder<T> b = this._builders.get(type);

		T instance = b.create_instance(joData);

		if (instance == null) {
			throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());
		}

		return instance;
	}

	@Override
	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}
}

package simulator.model;

import java.util.List;

public interface RegionInfo extends JSONable {

	public List<AnimalInfo> getAnimalsInfo();

	// Iterable<AnimalInfo> animals;
	public String toString();
}

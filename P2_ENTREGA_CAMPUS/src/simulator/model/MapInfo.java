package simulator.model;

public interface MapInfo extends JSONable, Iterable<MapInfo.RegionData> {
	public int get_cols();

	public int get_rows();

	public int get_width();

	public int get_height();

	public int get_region_width();

	public int get_region_height();

	public record RegionData(int row, int col, RegionInfo r) {
		public int getRow() {
			return row;
		}

		public int getCol() {
			return col;
		}

		public RegionInfo r() {
			return r;
		}
	}

}

package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.*;
import simulator.misc.Utils;
import simulator.model.*;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0; // in seconds
	private final static Double _default_delta_time = 0.03; // in seconds

	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double _delta_time = null;
	private static Double _time = null;
	private static String _in_file = null;
	private static String _out_file = null;
	private static Boolean _visor = false;
	private static ExecMode _mode = ExecMode.BATCH;
	private static Controller _controller = null;
	private static Simulator _sim = null;
	private static JSONObject _jo_in = null;
	private static OutputStream out = null;

	private static Factory<Animal> _animals;
	private static Factory<Region> _regions;
	private static Factory<SelectionStrategy> _strategies;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_delta_time_option(line);
			parse_help_option(line, cmdLineOptions);
			parse_in_file_option(line);
			parse_out_file_option(line);
			parse_viewer_option(line);
			parse_time_option(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();
		// actual time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representig actual time, in seconds, per simulation step. Default value: 0.03.")
				.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions
				.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// simple viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());

		return cmdLineOptions;
	}

	private static void parse_delta_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta time: " + t);
		}
	}

	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_out_file_option(CommandLine line) {
		if (line.hasOption("o")) {
			_out_file = line.getOptionValue("o");
		} else
			_out_file = null;

	}

	private static void parse_viewer_option(CommandLine line) {
		if (line.hasOption("sv")) {
			_visor = true;
		} else
			_visor = false;
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void init_factories() {
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		selection_strategy_builders.add(new SelectOldestBuilder());
		_strategies = new BuilderBasedFactory<SelectionStrategy>(selection_strategy_builders);
		List<Builder<Animal>> animal_builders = new ArrayList<>();
		animal_builders.add(new SheepBuilder(_strategies));
		animal_builders.add(new WolfBuilder(_strategies));
		_animals = new BuilderBasedFactory<Animal>(animal_builders);

		List<Builder<Region>> region_builders = new ArrayList<>();
		region_builders.add(new DefaultRegionBuilder());
		region_builders.add(new DynamicSupplyRegionBuilder());
		_regions = new BuilderBasedFactory<Region>(region_builders);

	}

	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void start_batch_mode() throws Exception {
		loadInputFile();
		createOutputFile();
		initSimulator();
		runController();
	}

	private static void loadInputFile() throws FileNotFoundException {
		InputStream is = new FileInputStream(new File(_in_file));
		try {
			_jo_in = load_JSON_file(is);
		} catch (JSONException e) {
			throw new IllegalArgumentException("The structure of the " + _in_file
					+ " is not correct. Please, ensure that both keys and values are properly structured.");
		}
	}

	private static void createOutputFile() throws Exception {
		if (_out_file != null) {
			out = new FileOutputStream(new File(_out_file));
		} else
			out = System.out;
	}

	private static void initSimulator() {
		int cols,rows,width,height;
		try {
		cols=_jo_in.getInt("cols");
		rows= _jo_in.getInt("rows");
		width=_jo_in.getInt("width");
		height=_jo_in.getInt("height");
		} catch (Exception e) {
			String k = e.getMessage().split(" ")[0].split("JSONObject")[1];
			throw new IllegalArgumentException("The file " + _in_file + " contains an error in the key " + k + " when creating the board.");
		}
			_sim = new Simulator(cols,rows,width,height, _animals, _regions);

	}

	private static void runController() throws IOException {
		_controller = new Controller(_sim);
		_controller.load_data(_jo_in);
		_controller.run(_time, _delta_time, _visor, out);
		if (out != null)
			out.close();
	}

	private static void start_GUI_mode() throws Exception {
		throw new UnsupportedOperationException("GUI mode is not ready yet ...");
	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {

			System.err.println(e.getLocalizedMessage());
			System.err.println();
			System.err.println("Something went wrong ...");
			System.out.println();
			//e.printStackTrace();

		}
	}
}

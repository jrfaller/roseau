package com.github.maracas.roseau.combinatorial;

import com.github.maracas.roseau.api.SpoonAPIExtractor;
import com.github.maracas.roseau.combinatorial.pbt.GenerateNewVersionsAndLaunchBenchmark;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public final class PbtBenchmark {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		var maxParallelAnalysis = 8;
		try {
			int firstArgParsed = args.length >= 1 ? Integer.parseInt(args[0]) : maxParallelAnalysis;
			maxParallelAnalysis = Math.max(1, firstArgParsed);
		} catch (NumberFormatException ignored) {}

		String outputDir = args.length >= 2 ? args[1] : Constants.OUTPUT_FOLDER;
		var outputPath = Path.of(outputDir);

		try {
			SpoonAPIExtractor apiExtractor = new SpoonAPIExtractor();
			var api = apiExtractor.extractAPI(Path.of(Constants.API_FOLDER));
			var newVersionsAndBenchmarkStep = new GenerateNewVersionsAndLaunchBenchmark(api, maxParallelAnalysis, outputPath);
			newVersionsAndBenchmarkStep.run();
		} catch (Exception e) {
			LOGGER.error("Failed to run combinatorial benchmark");
			LOGGER.error(e.getMessage());

			System.exit(1);
		}
	}
}

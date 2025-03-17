package com.github.maracas.roseau.combinatorial.pbt;

import com.github.maracas.roseau.api.model.API;
import com.github.maracas.roseau.combinatorial.AbstractStep;
import com.github.maracas.roseau.combinatorial.Constants;
import com.github.maracas.roseau.combinatorial.StepExecutionException;
import com.github.maracas.roseau.combinatorial.utils.ExplorerUtils;
import com.github.maracas.roseau.combinatorial.v2.VisibilityChangeEnumerator;
import com.github.maracas.roseau.combinatorial.v2.benchmark.result.ResultsWriter;
import com.github.maracas.roseau.combinatorial.v2.compiler.InternalJavaCompiler;
import com.github.maracas.roseau.combinatorial.v2.queue.NewApiQueue;
import com.github.maracas.roseau.combinatorial.v2.queue.ResultsProcessQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GenerateNewVersionsAndLaunchBenchmark extends AbstractStep {
	private static final Logger LOGGER = LogManager.getLogger();

	private final API v1Api;
	private final int maxParallelAnalysis;

	private final NewApiQueue newApiQueue;
	private final ResultsProcessQueue resultsQueue;

	private final Map<Benchmark, Thread> benchmarkThreads = new HashMap<>();
	private ResultsWriter resultsWriter = null;

	private final InternalJavaCompiler compiler = new InternalJavaCompiler();

	private final Path v1SourcesPath;
	private final Path tmpPath;
	private final Path v1JarPath;

	public GenerateNewVersionsAndLaunchBenchmark(API v1Api, int maxParallelAnalysis, Path outputPath) {
		super(outputPath);

		this.v1Api = v1Api;
		this.maxParallelAnalysis = maxParallelAnalysis;

		newApiQueue = new NewApiQueue(maxParallelAnalysis);
		resultsQueue = new ResultsProcessQueue();

		v1SourcesPath = Path.of(Constants.API_FOLDER);
		tmpPath = Path.of(Constants.TMP_FOLDER);
		v1JarPath = tmpPath.resolve(Path.of(Constants.JAR_FOLDER, "v1.jar"));

		ExplorerUtils.cleanOrCreateDirectory(tmpPath);
	}

	public void run() throws StepExecutionException {
		checkSourcesArePresent();
		packageV1Api();

		try {
			initializeBenchmarkThreads();
			initializeResultsThread();

			var visitor = new VisibilityChangeEnumerator(v1Api, newApiQueue);
			visitor.$(v1Api).visit();

			informAllBenchmarksGenerationIsOver();
		} catch (Exception e) {
			throw new StepExecutionException(this.getClass().getSimpleName(), e.getMessage());
		}
	}

	private void checkSourcesArePresent() throws StepExecutionException {
		if (!ExplorerUtils.checkPathExists(v1SourcesPath))
			throw new StepExecutionException(this.getClass().getSimpleName(), "V1 API sources are missing");
	}

	private void packageV1Api() throws StepExecutionException {
		LOGGER.info("------- Packaging V1 API -------");

		var errors = compiler.packageApiToJar(v1SourcesPath, v1JarPath);

		if (!errors.isEmpty())
			throw new StepExecutionException(this.getClass().getSimpleName(), "Couldn't package V1 API: " + formatCompilerErrors(errors));

		LOGGER.info("-------- V1 API packaged -------\n");
	}

	private static String formatCompilerErrors(List<?> errors) {
		return errors.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator()));
	}

	private void initializeBenchmarkThreads() {
		LOGGER.info("-- Starting benchmark threads --");

		for (int i = 0; i < maxParallelAnalysis; i++) {
			var benchmark = new Benchmark(
					String.valueOf(i),
					newApiQueue, resultsQueue,
					v1SourcesPath, v1JarPath, tmpPath,
					v1Api
			);
			var thread = new Thread(benchmark);
			thread.start();

			benchmarkThreads.put(benchmark, thread);
		}

		LOGGER.info("--- All bench threads started --\n");
	}

	private void initializeResultsThread() {
		LOGGER.info("---- Starting results thread ---");

		resultsWriter = new ResultsWriter(resultsQueue);
		new Thread(resultsWriter).start();

		LOGGER.info("---- Results thread started ----\n");
	}

	private void informAllBenchmarksGenerationIsOver() {
		for (var benchmark : benchmarkThreads.keySet())
			benchmark.informsApisGenerationIsOver();

		for (var thread : benchmarkThreads.values())
			try { thread.join(); } catch (InterruptedException ignored) {}

		LOGGER.info("-- All bench threads finished --");
		int totalErrors = benchmarkThreads.keySet().stream().mapToInt(Benchmark::getErrorsCount).sum();
		LOGGER.info("Total benchmark errors: " + totalErrors);

//		if (totalErrors == 0)
//			ExplorerUtils.removeDirectory(tmpPath);

		informResultsThreadNoMoreResults();
	}

	private void informResultsThreadNoMoreResults() {
		LOGGER.info("----- Closing results file -----");

		if (resultsWriter != null) {
			resultsWriter.informNoMoreResults();
		}
	}
}

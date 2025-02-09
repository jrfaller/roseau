package com.github.maracas.roseau.api.extractors.jar;

import com.github.maracas.roseau.api.SpoonAPIFactory;
import com.github.maracas.roseau.api.extractors.APIExtractor;
import com.github.maracas.roseau.api.model.API;
import com.github.maracas.roseau.api.model.TypeDecl;
import com.github.maracas.roseau.api.model.reference.SpoonTypeReferenceFactory;
import com.github.maracas.roseau.api.model.reference.TypeReferenceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class JarAPIExtractor implements APIExtractor {
	private static final int ASM_VERSION = Opcodes.ASM9;
	private static final int PARSING_OPTIONS = ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES;// | ClassReader.SKIP_DEBUG;
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public API extractAPI(Path sources) {
		SpoonAPIFactory apiFactory = new SpoonAPIFactory();
		TypeReferenceFactory typeRefFactory = new SpoonTypeReferenceFactory(apiFactory);

		try (JarFile jar = new JarFile(sources.toFile())) {
			List<TypeDecl> typeDecls =
				jar.stream()
					.filter(entry -> entry.getName().endsWith(".class") && !entry.isDirectory())
					// Multi-release JARs store version-specific class files there, so we could have duplicates
					.filter(entry -> !entry.getName().startsWith("META-INF/"))
					.flatMap(entry -> {
						try (InputStream is = jar.getInputStream(entry)) {
							ClassReader reader = new ClassReader(is);
							APIClassVisitor visitor = new APIClassVisitor(ASM_VERSION, typeRefFactory);
							reader.accept(visitor, PARSING_OPTIONS);
							return Optional.ofNullable(visitor.getTypeDecl()).stream();
						} catch (IOException e) {
							LOGGER.error("Error processing JAR entry {}", entry.getName(), e);
							return Stream.empty();
						}
					})
					.toList();

			return new API(typeDecls, apiFactory);
		} catch (IOException e) {
			throw new RuntimeException("Error processing JAR file", e);
		}
	}
}

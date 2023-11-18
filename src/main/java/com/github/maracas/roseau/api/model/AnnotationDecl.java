package com.github.maracas.roseau.api.model;

import java.util.Collections;
import java.util.List;

public final class AnnotationDecl extends TypeDecl {
	public AnnotationDecl(String qualifiedName, AccessModifier visibility, boolean isExported, List<Modifier> modifiers, SourceLocation location, TypeReference<TypeDecl> containingType, List<FieldDecl> fields, List<MethodDecl> methods) {
		super(qualifiedName, visibility, isExported, modifiers, location, containingType, Collections.emptyList(), Collections.emptyList(), fields, methods);
	}

	@Override
	public boolean isAnnotation() {
		return true;
	}

	@Override
	public String toString() {
		return """
			annotation %s [%s] (%s)
			  %s
			  %s
			""".formatted(qualifiedName, visibility, location, fields, methods);
	}
}

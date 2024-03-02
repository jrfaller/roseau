package com.github.maracas.roseau.api.model;

import com.github.maracas.roseau.api.model.reference.ITypeReference;

import java.util.Objects;

public record ParameterDecl(
	String name,
	ITypeReference type,
	boolean isVarargs
) {
	public ParameterDecl {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
	}

	@Override
	public String toString() {
		return "%s%s %s".formatted(type, isVarargs ? "..." : "", name);
	}
}

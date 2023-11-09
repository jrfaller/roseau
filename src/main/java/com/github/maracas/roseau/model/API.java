package com.github.maracas.roseau.model;

import java.util.List;

/**
 * Represents the API of a library containing all the types, each of which may have methods, fields, constructors, and more information about the type.
 * This class encapsulates a list of {@link Type} instances, each representing distinct types identified by their respective qualified names.
 *
 * @param types The list of TypeDeclarations representing all the types in the library's API.
 */
public record API(List<Type> types) {
	/**
	 * Generates a string representation of the library's API.
	 *
	 * @return A formatted string containing all the API elements structured.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (Type typeDeclaration : types) {
			builder.append(typeDeclaration).append("\n");
			builder.append("    =========================\n\n");
		}

		return builder.toString();
	}
}

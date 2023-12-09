package com.github.maracas.roseau.api.model.reference;

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.maracas.roseau.api.model.APIFactory;
import com.github.maracas.roseau.api.model.TypeDecl;
import com.google.common.base.Objects;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;

public final class TypeReference<T extends TypeDecl> implements ITypeReference {
	private final String qualifiedName;
	private TypeFactory typeFactory;
	private T resolvedApiType;

	public TypeReference(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public TypeReference(String qualifiedName, TypeFactory typeFactory) {
		this.qualifiedName = qualifiedName;
		this.typeFactory = typeFactory;
	}

	@JsonValue
	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setTypeFactory(TypeFactory typeFactory) {
		this.typeFactory = typeFactory;
	}

	public Optional<T> getResolvedApiType() {
		if (resolvedApiType == null && typeFactory != null) {
			CtTypeReference<?> ref = typeFactory.createReference(qualifiedName);

			if (ref.getTypeDeclaration() != null)
				resolvedApiType = (T) new APIFactory(typeFactory).convertCtType(ref.getTypeDeclaration());
		}

		return Optional.ofNullable(resolvedApiType);
	}

	public void setResolvedApiType(T type) {
		resolvedApiType = type;
	}

	public boolean isSubtypeOf(TypeReference<T> other) {
		return this.equals(other) || getResolvedApiType().map(t -> t.getAllSuperTypes().contains(other)).orElse(false);
	}

	public boolean sameHierarchy(TypeReference<T> other) {
		return isSubtypeOf(other) || other.isSubtypeOf(this);
	}

	@Override
	public String toString() {
		return qualifiedName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeReference<?> other = (TypeReference<?>) o;
		return Objects.equal(qualifiedName, other.qualifiedName);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(qualifiedName);
	}
}

package com.github.maracas.roseau.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.maracas.roseau.api.model.reference.ITypeReference;
import com.github.maracas.roseau.api.model.reference.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Represents a type declaration in the library.
 * This class extends the {@link Symbol} class and contains information about the type's kind, fields, methods, constructors, and more.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "typeKind")
public abstract sealed class TypeDecl extends Symbol permits ClassDecl, InterfaceDecl, AnnotationDecl {
	protected final List<TypeReference<InterfaceDecl>> implementedInterfaces;

	/**
	 * List of formal type parameters for generic types.
	 */
	protected final List<FormalTypeParameter> formalTypeParameters;

	/**
	 * List of fields declared within the type.
	 */
	protected final List<FieldDecl> fields;

	/**
	 * List of methods declared within the type.
	 */
	protected final List<MethodDecl> methods;

	protected final TypeReference<TypeDecl> enclosingType;

	protected TypeDecl(String qualifiedName,
	                   AccessModifier visibility,
	                   List<Modifier> modifiers,
										 List<Annotation> annotations,
	                   SourceLocation location,
	                   List<TypeReference<InterfaceDecl>> implementedInterfaces,
	                   List<FormalTypeParameter> formalTypeParameters,
	                   List<FieldDecl> fields,
	                   List<MethodDecl> methods,
	                   TypeReference<TypeDecl> enclosingType) {
		super(qualifiedName, visibility, modifiers, annotations, location);
		this.implementedInterfaces = implementedInterfaces;
		this.formalTypeParameters = formalTypeParameters;
		this.fields = fields;
		this.methods = methods;
		this.enclosingType = enclosingType;
	}

	@JsonIgnore
	@Override
	public boolean isExported() {
		return (isPublic() || (isProtected() && !isEffectivelyFinal()))
			&& (enclosingType == null || enclosingType.getResolvedApiType().map(TypeDecl::isExported).orElse(true));
	}

	@JsonIgnore
	public boolean isNested() {
		return enclosingType != null;
	}

	@JsonIgnore
	public boolean isClass() {
		return false;
	}

	@JsonIgnore
	public boolean isInterface() {
		return false;
	}

	@JsonIgnore
	public boolean isEnum() {
		return false;
	}

	@JsonIgnore
	public boolean isRecord() {
		return false;
	}

	@JsonIgnore
	public boolean isAnnotation() {
		return false;
	}

	@JsonIgnore
	public boolean isCheckedException() {
		return false;
	}

	@JsonIgnore
	public boolean isStatic() {
		return modifiers.contains(Modifier.STATIC);
	}

	@JsonIgnore
	public boolean isFinal() {
		return modifiers.contains(Modifier.FINAL);
	}

	@JsonIgnore
	public boolean isSealed() {
		return modifiers.contains(Modifier.SEALED);
	}

	@JsonIgnore
	public boolean isEffectivelyFinal() {
		// FIXME: in fact, a sealed class may not be final if one of its permitted subclass
		//        is explicitly marked as non-sealed
		return !modifiers.contains(Modifier.NON_SEALED) && (isFinal() || isSealed());
	}

	@JsonIgnore
	public boolean isPublic() {
		return AccessModifier.PUBLIC == visibility;
	}

	@JsonIgnore
	public boolean isProtected() {
		return AccessModifier.PROTECTED == visibility;
	}

	@JsonIgnore
	public boolean isPrivate() {
		return AccessModifier.PRIVATE == visibility;
	}

	@JsonIgnore
	public boolean isPackagePrivate() {
		return AccessModifier.PACKAGE_PRIVATE == visibility;
	}

	@JsonIgnore
	public boolean isAbstract() {
		return modifiers.contains(Modifier.ABSTRACT);
	}

	@JsonIgnore
	public List<MethodDecl> getAllMethods() {
		List<MethodDecl> allMethods = Stream.concat(
			methods.stream(),
			getSuperMethods().stream()
		).toList();

		return allMethods.stream()
			.filter(m -> allMethods.stream().noneMatch(m2 -> !m2.equals(m) && m2.isOverriding(m)))
			.toList();
	}

	@JsonIgnore
	public List<TypeReference<? extends TypeDecl>> getAllSuperTypes() {
		return new ArrayList<>(getAllImplementedInterfaces());
	}

	protected List<MethodDecl> getSuperMethods() {
		return implementedInterfaces.stream()
			.map(TypeReference::getResolvedApiType)
			.flatMap(Optional::stream)
			.map(InterfaceDecl::getAllMethods)
			.flatMap(Collection::stream)
			.toList();
	}

	@JsonIgnore
	public List<FieldDecl> getAllFields() {
		return Stream.concat(
			fields.stream(),
			implementedInterfaces.stream()
				.map(TypeReference::getResolvedApiType)
				.flatMap(Optional::stream)
				.map(InterfaceDecl::getAllFields)
				.flatMap(Collection::stream)
		).toList();
	}

	/**
	 * Retrieves the superinterfaces of the type as typeDeclarations.
	 *
	 * @return Type's superinterfaces as typeDeclarations
	 */
	public List<TypeReference<InterfaceDecl>> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	/**
	 * Retrieves the list of formal type parameters for generic types.
	 *
	 * @return List of formal type parameters
	 */
	public List<FormalTypeParameter> getFormalTypeParameters() {
		return formalTypeParameters;
	}

	/**
	 * Retrieves the list of fields declared within the type.
	 *
	 * @return List of fields declared within the type
	 */
	public List<FieldDecl> getFields() {
		return fields;
	}

	public List<MethodDecl> getMethods() {
		return methods;
	}

	public Optional<TypeReference<TypeDecl>> getEnclosingType() {
		return Optional.ofNullable(enclosingType);
	}

	public Optional<FieldDecl> findField(String name) {
		return fields.stream()
			.filter(f -> f.getSimpleName().equals(name))
			.findFirst();
	}

	public Optional<MethodDecl> findMethod(String name, List<? extends ITypeReference> parameterTypes, boolean varargs) {
		return methods.stream()
			.filter(m -> m.hasSignature(name, parameterTypes, varargs))
			.findFirst();
	}

	public Optional<MethodDecl> findMethod(String name, List<? extends ITypeReference> parameterTypes) {
		return findMethod(name, parameterTypes, false);
	}

	public Optional<MethodDecl> findMethod(String name) {
		return methods.stream()
			.filter(m -> m.getSimpleName().equals(name))
			.findFirst();
	}

	@JsonIgnore
	public List<TypeReference<InterfaceDecl>> getAllImplementedInterfaces() {
		return Stream.concat(
			implementedInterfaces.stream(),
			implementedInterfaces.stream()
				.map(TypeReference::getResolvedApiType)
				.flatMap(Optional::stream)
				.map(InterfaceDecl::getAllImplementedInterfaces)
				.flatMap(Collection::stream)
		).distinct().toList();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		TypeDecl typeDecl = (TypeDecl) o;
		return Objects.equals(implementedInterfaces, typeDecl.implementedInterfaces)
			&& Objects.equals(formalTypeParameters, typeDecl.formalTypeParameters)
			&& Objects.equals(fields, typeDecl.fields)
			&& Objects.equals(methods, typeDecl.methods);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), implementedInterfaces, formalTypeParameters, fields, methods);
	}
}

package com.github.maracas.roseau.api.model.reference;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.maracas.roseau.api.model.TypeDecl;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "refKind")
public sealed interface ITypeReference
	permits TypeReference, ArrayTypeReference, PrimitiveTypeReference, TypeParameterReference {
	String getQualifiedName();
}

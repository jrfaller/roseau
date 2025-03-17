package com.github.maracas.roseau.combinatorial.pbt;

import com.github.maracas.roseau.api.model.API;
import com.github.maracas.roseau.api.model.AccessModifier;
import com.github.maracas.roseau.api.model.Symbol;
import com.github.maracas.roseau.api.visit.AbstractAPIVisitor;
import com.github.maracas.roseau.api.visit.Visit;
import com.github.maracas.roseau.combinatorial.Constants;
import com.github.maracas.roseau.combinatorial.v2.queue.NewApiQueue;

import java.nio.file.Path;

public final class VisibilityChangeActuator extends AbstractAPIVisitor {
	private API api;

	private String id;

	private Object target;

	public VisibilityChangeActuator(String id, Object target, API api) {
		this.id = id;
		this.api = api;
		this.target = target;
	}

	public Visit symbol(Symbol it) {

			if (it.toString().equals(id)) {
				it.visibility = (AccessModifier) target;
			}

		return () -> {};
	}

	public API getApi() {
		return this.api;
	}
}

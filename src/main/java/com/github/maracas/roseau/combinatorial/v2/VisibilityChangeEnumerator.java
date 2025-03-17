package com.github.maracas.roseau.combinatorial.v2;

import com.github.maracas.roseau.api.SpoonAPIFactory;
import com.github.maracas.roseau.api.model.*;
import com.github.maracas.roseau.api.visit.AbstractAPIVisitor;
import com.github.maracas.roseau.api.visit.Visit;
import com.github.maracas.roseau.combinatorial.Constants;
import com.github.maracas.roseau.combinatorial.pbt.VisibilityChangeActuator;
import com.github.maracas.roseau.combinatorial.v2.queue.NewApiQueue;

import java.io.IOException;
import java.nio.file.Path;

public final class VisibilityChangeEnumerator extends AbstractAPIVisitor {
	private final NewApiQueue queue;

	private String apiJson;

	public VisibilityChangeEnumerator(API api, NewApiQueue queue) {
		this.queue = queue;
		this.apiJson = initApiJson(api);
	}

	public Visit symbol(Symbol it) {
		try {
			if (it.getVisibility() == AccessModifier.PUBLIC) {
				String id = it.toString();
				API apiv1 = cloneApi();
				VisibilityChangeActuator v1 = new VisibilityChangeActuator(id, AccessModifier.PROTECTED, apiv1);
				v1.$(apiv1).visit();
				queue.put("visibility-reduced-to-protected", apiv1);
				API apiv2 = cloneApi();
				VisibilityChangeActuator v2 = new VisibilityChangeActuator(id, AccessModifier.PACKAGE_PRIVATE, apiv2);
				v2.$(apiv2).visit();
				queue.put("visibility-reduced-to-package", apiv2);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return () -> {};
	}

	private String initApiJson(API api) {
        try {
            return api.getJsonString();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
    }

	private API cloneApi() {
		try {
			return API.fromJson(apiJson, new SpoonAPIFactory());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}

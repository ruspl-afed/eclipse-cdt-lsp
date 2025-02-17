/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

final class ClangdPreferredOptions implements ClangdOptions {

	private final String qualifier;
	private final IScopeContext[] scopes;
	private final ClangdMetadata metadata;

	ClangdPreferredOptions(String qualifier, IScopeContext[] scopes, ClangdMetadata metadata) {
		this.qualifier = Objects.requireNonNull(qualifier);
		this.scopes = Objects.requireNonNull(scopes);
		this.metadata = Objects.requireNonNull(metadata);
	}

	@Override
	public String clangdPath() {
		return stringValue(metadata.clangdPath());
	}

	@Override
	public boolean useTidy() {
		return booleanValue(metadata.useTidy());
	}

	@Override
	public boolean useBackgroundIndex() {
		return booleanValue(metadata.useBackgroundIndex());
	}

	@Override
	public String completionStyle() {
		return stringValue(metadata.completionStyle());
	}

	@Override
	public boolean prettyPrint() {
		return booleanValue(metadata.prettyPrint());
	}

	@Override
	public String queryDriver() {
		return stringValue(metadata.queryDriver());
	}

	@Override
	public List<String> additionalOptions() {
		var options = stringValue(metadata.additionalOptions());
		if (options.isBlank()) {
			return new ArrayList<>();
		}
		return Arrays.asList(options.split("\\s+")); //$NON-NLS-1$
	}

	private String stringValue(PreferenceMetadata<?> meta) {
		String actual = String.valueOf(meta.defaultValue());
		for (int i = scopes.length - 1; i >= 0; i--) {
			IScopeContext scope = scopes[i];
			String previous = actual;
			actual = scope.getNode(qualifier).get(meta.identifer(), previous);
		}
		return actual;
	}

	private boolean booleanValue(PreferenceMetadata<Boolean> meta) {
		return Optional.of(meta)//
				.map(this::stringValue)//
				.map(Boolean::valueOf)//
				.orElseGet(meta::defaultValue);
	}

}

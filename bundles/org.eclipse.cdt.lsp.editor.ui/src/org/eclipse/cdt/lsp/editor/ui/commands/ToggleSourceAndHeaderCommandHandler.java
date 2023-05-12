/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.editor.ui.commands;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ResolveUri;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.services.ClangdLanguageServer;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;

public final class ToggleSourceAndHeaderCommandHandler {

	/*
	 * AF: here we inject the implementation as a field, 
	 * but we also can extract it from MPart 
	 * or declare it as another argument for `Execute` method 
	 */
	@Inject
	private ResolveUri resolve;
	
	@Execute
	public void toggle(@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		execute(part.getContext().get(IEditorPart.class));
		
	}

	@SuppressWarnings("restriction")
	private Object execute(IEditorPart activeEditor) {
		// Try to adapt to ITextEditor (e.g. to support editors embedded in
		// MultiPageEditorParts), otherwise use activeEditor.
		IEditorPart innerEditor = Optional.ofNullable((IEditorPart) Adapters.adapt(activeEditor, ITextEditor.class))
				.orElse(activeEditor);

		resolve.resolve(innerEditor).ifPresent(fileUri -> {
			IDocument document = org.eclipse.lsp4e.LSPEclipseUtils.getDocument(innerEditor.getEditorInput());
			org.eclipse.lsp4e.LanguageServers.forDocument(document)
					.computeFirst(
							server -> server instanceof ClangdLanguageServer
									? ((ClangdLanguageServer) server)
											.switchSourceHeader(new TextDocumentIdentifier(fileUri.toString()))
									: null)
					.thenAccept(otherFileUri -> otherFileUri
							.ifPresent(uri -> openEditor(innerEditor.getEditorSite().getPage(), URI.create(uri))));
		});

		return null;
	}

	private static void openEditor(IWorkbenchPage page, URI fileUri) {
		page.getWorkbenchWindow().getShell().getDisplay().asyncExec(() -> {
			try {
				IDE.openEditor(page, fileUri, LspPlugin.LSP_C_EDITOR_ID, true);
			} catch (PartInitException e) {
				StatusManager.getManager().handle(e, LspEditorUiPlugin.PLUGIN_ID);
			}
		});
	}

}
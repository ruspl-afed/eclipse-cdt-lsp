package org.eclipse.cdt.lsp.internal.editor;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.lsp.editor.ResolveUri;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.osgi.service.component.annotations.Component;

/*
 * AF: here is the service implementation to demonstrate IoC, 
 * it will be substituted by OSGi
 * but also it will be available for Eclipse DI
 * please note that it is defined in another bundle
 */
@Component
public final class EditorInputUri implements ResolveUri {

	@Override
	public Optional<URI> resolve(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return Optional.of(((IFileEditorInput) input).getFile().getLocationURI());
		} else if (input instanceof IURIEditorInput) {
			return Optional.of(((IURIEditorInput) input).getURI());
		} else {
			return Optional.empty();
		}
	}

}

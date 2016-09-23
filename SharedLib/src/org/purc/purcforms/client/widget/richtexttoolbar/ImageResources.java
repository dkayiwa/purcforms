package org.purc.purcforms.client.widget.richtexttoolbar;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * This {@link ClientBundle} is used for all the button icons. Using a bundle allows all of these images to be packed
 * into a single image, which saves a lot of HTTP requests, drastically improving startup time.
 */
public interface ImageResources extends ClientBundle {

	@Source("bold.gif")
	ImageResource bold();

	@Source("createLink.gif")
	ImageResource createLink();

	@Source("hr.gif")
	ImageResource hr();

	@Source("indent.gif")
	ImageResource indent();

	@Source("insertImage.gif")
	ImageResource insertImage();

	@Source("italic.gif")
	ImageResource italic();

	@Source("justifyCenter.gif")
	ImageResource justifyCenter();

	@Source("justifyLeft.gif")
	ImageResource justifyLeft();

	@Source("justifyRight.gif")
	ImageResource justifyRight();

	@Source("ol.gif")
	ImageResource ol();

	@Source("outdent.gif")
	ImageResource outdent();

	@Source("removeFormat.gif")
	ImageResource removeFormat();

	@Source("removeLink.gif")
	ImageResource removeLink();

	@Source("strikeThrough.gif")
	ImageResource strikeThrough();

	@Source("subscript.gif")
	ImageResource subscript();

	@Source("superscript.gif")
	ImageResource superscript();

	@Source("ul.gif")
	ImageResource ul();

	@Source("underline.gif")
	ImageResource underline();
}
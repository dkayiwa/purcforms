package org.purc.purcforms.client;

import org.purc.purcforms.client.locale.LocaleText;
import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.view.FormRunnerView.Images;
import org.purc.purcforms.client.widget.FormRunnerWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestException;

/**
 * This is the GWT entry point for the form runtime engine.
 */
public class FormRunnerEntryPoint implements EntryPoint{

	/** The form runtime widget. */
	private FormRunnerWidget formRunner;

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programatic access to all the images needed by widgets.
	 */
	public static final Images images = (Images) GWT.create(Images.class);

	private String xml = "";

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		FormUtil.dlg.setText(LocaleText.get("loading"));
		FormUtil.dlg.center();
		
		publishJS();

		DeferredCommand.addCommand(new Command() {
			public void execute() {
				onModuleLoadDeffered();
			}
		});		
	}

	public void onModuleLoadDeffered() {
		try{
			RootPanel rootPanel = RootPanel.get("purcformrunner");
			if(rootPanel == null){
				FormUtil.dlg.hide();
				return;
			}
			
			FormUtil.setupUncaughtExceptionHandler();	

			//FormUtil.retrieveUserDivParameters();

			formRunner = new FormRunnerWidget(images);
			
			rootPanel.add(formRunner);

			FormUtil.maximizeWidget(formRunner);

			String xmlUrl = FormUtil.getDivValue("xformurl");

			if(xmlUrl != null && xmlUrl.length() != 0){
//				Window.alert(xmlUrl);
				readXML(xmlUrl);
			} else {
				executeFormLoad();
			}
		}
		catch(Exception ex){
			FormUtil.displayException(ex);
		}
	}

	private void executeFormLoad() {
		//String formId = FormUtil.getFormId();
		//String entityId = FormUtil.getEntityId();
		String formId = "1";
		String entityId = "1";

		if (xml != null && xml.length() != 0) {
			formRunner.loadForm(Integer.parseInt(formId), Integer.parseInt(entityId), xml);
		} else if (formId != null && entityId != null) {
			formRunner.loadForm(Integer.parseInt(formId), Integer.parseInt(entityId));
		} else {
            FormUtil.dlg.hide();
            Window.alert(LocaleText.get("noFormId") + FormUtil.getEntityIdName() + LocaleText.get("divFound"));
        }

		DeferredCommand.addCommand(new Command() {
            public void execute() {
                //String formId = FormUtil.getFormId();
                //String entityId = FormUtil.getEntityId();

                String formId = "1";
                String entityId = "1";
                if (formId == null || entityId == null)
                    FormUtil.dlg.hide();
            }
        });
	}

//	private String readXML(String xmlUrl){
//		StringBuilder builder = new StringBuilder();
//
//		URL url = null;
//		try {
//			url = new URL(xmlUrl);
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//			String line;
//			while ((line = reader.readLine()) != null) {
//				builder.append(line);
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return builder.toString();
//	}

	private void readXML(String xmlUrl){
		try {
			new RequestBuilder(RequestBuilder.GET, xmlUrl).sendRequest("", new RequestCallback() {
				@Override
				public void onResponseReceived(Request req, Response resp) {
					xml = resp.getText();
					executeFormLoad();
				}
				@Override
				public void onError(Request res, Throwable throwable) {
					// handle errors
					Window.alert("Error loading the purcform XML");
				}
			});
		}
		catch (Exception e){
			Window.alert(e.toString());
		}
	}
	/**
	 * This is just a temporary hack for those who use both the form designer and runner as two
	 * separate GWT widgets and then the form designer registers the authentication callback
	 * instead of the form runner. So this method is just a away for them to override the
	 * form designer's call back with that of the form runner.
	 */
	public static void registerAuthenticationCallback(){
		publishJS();
	}

	// Set up the JS-callable signature as a global JS function.
	private static native void publishJS() /*-{
   		$wnd.authenticationCallback = @org.purc.purcforms.client.view.FormRunnerView::authenticationCallback(Z);
   		$wnd.submitForm = @org.purc.purcforms.client.view.FormRunnerView::submitForm();
	}-*/;
}
